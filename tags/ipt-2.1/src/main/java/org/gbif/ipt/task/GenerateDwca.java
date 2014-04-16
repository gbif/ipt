package org.gbif.ipt.task;

import org.gbif.dwc.terms.Term;
import org.gbif.dwc.terms.TermFactory;
import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.ArchiveFactory;
import org.gbif.dwc.text.ArchiveField;
import org.gbif.dwc.text.ArchiveFile;
import org.gbif.dwc.text.MetaDescriptorWriter;
import org.gbif.file.CSVReader;
import org.gbif.file.CSVReaderFactory;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.RecordFilter;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.utils.file.ClosableReportingIterator;
import org.gbif.utils.file.CompressionUtil;
import org.gbif.utils.text.LineComparator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.base.Strings;
import com.google.common.collect.Ordering;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import freemarker.template.TemplateException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;

public class GenerateDwca extends ReportingTask implements Callable<Integer> {

  private enum STATE {
    WAITING, STARTED, DATAFILES, METADATA, BUNDLING, COMPLETED, ARCHIVING, VALIDATING, CANCELLED, FAILED
  }

  private static final Pattern escapeChars = Pattern.compile("[\t\n\r]");
  private final Resource resource;
  private int coreRecords = 0;
  private Archive archive;
  private File dwcaFolder;
  // status reporting
  private int currRecords = 0;
  private int currRecordsSkipped = 0;
  private String currExtension;
  private STATE state = STATE.WAITING;
  private final SourceManager sourceManager;
  private Exception exception;
  private AppConfig cfg;
  private static final int ID_COLUMN_INDEX = 0;
  private static final String CHARACTER_ENCODING = "UTF-8";
  private static final TermFactory TERM_FACTORY = TermFactory.instance();
  private static final String SORTED_FILE_PREFIX = "sorted_";
  private static final org.gbif.utils.file.FileUtils GBIF_FILE_UTILS = new org.gbif.utils.file.FileUtils();
  public static final String CANCELLED_STATE_MSG = "Archive generation cancelled";
  public static final String ID_COLUMN_NAME = "id";

  private static final Comparator<String> IGNORE_CASE_COMPARATOR = Ordering.from(new Comparator<String>() {

    public int compare(String o1, String o2) {
      return o1.compareToIgnoreCase(o2);
    }
  }).nullsFirst();

  @Inject
  public GenerateDwca(@Assisted Resource resource, @Assisted ReportHandler handler, DataDir dataDir,
    SourceManager sourceManager, AppConfig cfg) throws IOException {
    super(1000, resource.getShortname(), handler, dataDir);
    this.resource = resource;
    this.sourceManager = sourceManager;
    this.cfg = cfg;
  }

  /**
   * Adds a single data file for a list of extension mappings that must all be mapped to the same extension.
   * </br>
   * The ID column is always the 1st column (index 0) and is always equal to the core record identifier that has been
   * mapped (e.g. occurrenceID, taxonID, etc).
   * 
   * @param mappings list of ExtensionMapping
   * @throws IllegalArgumentException if not all mappings are mapped to the same extension
   * @throws InterruptedException if the thread was interrupted
   * @throws IOException if problems occurred while persisting new data files
   * @throws GeneratorException if any problem was encountered writing data file
   */
  private void addDataFile(List<ExtensionMapping> mappings) throws IOException,
    IllegalArgumentException, InterruptedException, GeneratorException {
    checkForInterruption();
    if (mappings == null || mappings.isEmpty()) {
      return;
    }

    // update reporting
    currRecords = 0;
    currRecordsSkipped = 0;
    Extension ext = mappings.get(0).getExtension();
    currExtension = ext.getTitle();

    // verify that all mappings share this extension
    for (ExtensionMapping m : mappings) {
      if (!ext.equals(m.getExtension())) {
        throw new IllegalArgumentException(
          "All mappings for a single data file need to be mapped to the same extension: " + ext.getRowType());
      }
    }

    // create new tab file with the help of the Archive class representing the core file or an extension
    ArchiveFile af = ArchiveFile.buildTabFile();
    af.setRowType(ext.getRowType());
    af.setEncoding(CHARACTER_ENCODING);
    af.setDateFormat("YYYY-MM-DD");
    // in the generated file column 0 will be the id row
    ArchiveField idField = new ArchiveField();
    idField.setIndex(ID_COLUMN_INDEX);
    af.setId(idField);

    // find the union of all terms mapped and make them a field in the final archive
    Set<Term> mappedConceptTerms = addFieldsToArchive(mappings, af);

    // retrieve the ordered list of mapped ExtensionProperty
    List<ExtensionProperty> propertyList = getOrderedMappedExtensionProperties(ext, mappedConceptTerms);

    // reassign indexes ordered by Extension
    assignIndexesOrderedByExtension(propertyList, af);

    // total column count is equal to id column + mapped columns
    int totalColumns = 1 + propertyList.size();

    // open new file writer for single data file
    String fn = ext.getName().toLowerCase().replaceAll("\\s", "_") + ".txt";
    File dataFile = new File(dwcaFolder, fn);
    Writer writer = org.gbif.utils.file.FileUtils.startNewUtf8File(dataFile);
    // add source file location
    af.addLocation(dataFile.getName());

    // ready to go though each mapping and dump the data
    addMessage(Level.INFO, "Start writing data file for " + currExtension);
    try {
      boolean headerWritten = false;
      for (ExtensionMapping m : mappings) {
        // prepare index ordered list of all output columns apart from id column
        PropertyMapping[] inCols = new PropertyMapping[totalColumns];
        for (ArchiveField f : af.getFields().values()) {
          if (f.getIndex() != null && f.getIndex() > ID_COLUMN_INDEX) {
            inCols[f.getIndex()] = m.getField(f.getTerm().qualifiedName());
          }
        }

        // write header line 1 time only to file
        if (!headerWritten) {
          writeHeaderLine(propertyList, totalColumns, af, writer);
          headerWritten = true;
        }

        // write data (records) to file
        dumpData(writer, inCols, m, totalColumns);
        // remember core record number
        if (ext.isCore()) {
          coreRecords = currRecords;
        }
      }
    } catch (IOException e) {
      // some error writing this file, report
      log.error("Fatal DwC-A Generator Error encountered while writing header line to data file", e);
      // set last error report!
      setState(e);
      throw new GeneratorException("Error writing header line to data file", e);
    } finally {
      writer.close();
    }

    // add archive file to archive
    if (ext.isCore()) {
      archive.setCore(af);
    } else {
      archive.addExtension(af);
    }

    // final reporting
    addMessage(Level.INFO, "Data file written for " + currExtension + " with " + currRecords + " records and "
      + totalColumns + " columns");
    // how many records were skipped?
    if (currRecordsSkipped > 0) {
      addMessage(Level.WARN, "!!! " + currRecordsSkipped + " records were skipped for " + currExtension
        + " due to errors interpreting line");
    }
  }

  /**
   * Write the header column line to file.
   * 
   * @param propertyList ordered list of all ExtensionProperty that have been mapped across all mappings for a single
   *        Extension
   * @param totalColumns total number of columns in header
   * @param af tab file with representing the core file or an extension
   * @param writer file writer
   * @throws IOException if writing the header line failed
   */
  private void writeHeaderLine(List<ExtensionProperty> propertyList, int totalColumns, ArchiveFile af, Writer writer)
    throws IOException {
    String[] headers = new String[totalColumns];
    // reserve 1st column for "id"
    headers[ID_COLUMN_INDEX] = ID_COLUMN_NAME;
    // add remaining mapped-column names
    int c = 1;
    for (ExtensionProperty property : propertyList) {
      headers[c] = property.simpleName();
      c++;
    }
    // write header line - once per mapping
    String headerLine = tabRow(headers);
    af.setIgnoreHeaderLines(1);
    writer.write(headerLine);
  }

  /**
   * Adds EML file to DwC-A folder.
   * 
   * @throws GeneratorException if EML file could not be copied to DwC-A folder
   * @throws InterruptedException if executing thread was interrupted
   */
  private void addEmlFile() throws GeneratorException, InterruptedException {
    checkForInterruption();
    setState(STATE.METADATA);
    try {
      FileUtils.copyFile(dataDir.resourceEmlFile(resource.getShortname(), null), new File(dwcaFolder,
        DataDir.EML_XML_FILENAME));
      archive.setMetadataLocation(DataDir.EML_XML_FILENAME);
    } catch (IOException e) {
      throw new GeneratorException("Problem occurred while adding EML file to DwC-A folder", e);
    }
    // final reporting
    addMessage(Level.INFO, "EML file added");
  }

  /**
   * Build a new ArchiveField having a ConceptTerm and defaultValue.
   * 
   * @param term ConceptTerm
   * @param defaultValue default value
   * @return ArchiveField created
   */
  private ArchiveField buildField(Term term, @Nullable String defaultValue) {
    ArchiveField f = new ArchiveField();
    f.setTerm(term);
    f.setDefaultValue(defaultValue);
    return f;
  }

  /**
   * Zips the DwC-A folder. A temp version is created first, and when successful, it it moved into the resource's
   * data directory.
   * 
   * @throws GeneratorException if DwC-A could not be zipped or moved
   * @throws InterruptedException if executing thread was interrupted
   */
  private void bundleArchive() throws GeneratorException, InterruptedException {
    checkForInterruption();
    setState(STATE.BUNDLING);
    File zip = null;
    try {
      // create zip
      zip = dataDir.tmpFile("dwca", ".zip");
      CompressionUtil.zipDir(dwcaFolder, zip);
      if (zip.exists()) {
        // move to data dir
        File target = dataDir.resourceDwcaFile(resource.getShortname());
        if (target.exists()) {
          FileUtils.forceDelete(target);
        }
        FileUtils.moveFile(zip, target);
      } else {
        throw new GeneratorException("Archive bundling failed: temp archive not created: " + zip.getAbsolutePath());
      }
    } catch (IOException e) {
      throw new GeneratorException("Problem occurred while bundling DwC-A", e);
    } finally {
      // cleanup zip directory, if compression was incomplete for example due to Exception
      // if moving zip to data dir was successful, it won't exist any more and cleanup will be skipped
      if (zip != null && zip.exists()) {
        FileUtils.deleteQuietly(zip);
      }
    }
    // final reporting
    addMessage(Level.INFO, "Archive compressed");
  }

  /**
   * Validate the DwC-A:
   * -ensure that if the core record identifier is mapped (e.g. occurrenceID, taxonID, etc) its present on all
   * rows, and is unique
   * 
   * @throws GeneratorException if DwC-A could not be validated
   * @throws InterruptedException if executing thread was interrupted
   */
  private void validate() throws GeneratorException, InterruptedException {
    checkForInterruption();
    setState(STATE.VALIDATING);

    try {
      // retrieve newly generated archive - decompressed
      Archive arch = ArchiveFactory.openArchive(dwcaFolder);
      // perform validation
      validateCoreDataFile(arch);
    } catch (IOException e) {
      throw new GeneratorException("Problem occurred while validating DwC-A", e);
    }
    // final reporting
    addMessage(Level.INFO, "Archive validated");
  }

  /**
   * Sort the core data file of a Darwin Core Archive by its ID column (always index 0 or 1st column). Sorting is case
   * sensitive.
   * 
   * @param arch Archive
   * @return the core data file of the Archive sorted by its ID column 0
   * @throws IOException if the sort fails for whatever reason
   */
  private File sortCoreDataFile(Archive arch) throws IOException {
    // retrieve the core file
    File unsorted = arch.getCore().getLocationFile();

    // create a new file that will store the records sorted by ID
    File sorted = new File(unsorted.getParentFile(), SORTED_FILE_PREFIX + unsorted.getName());
    // get the ignore column rows, delimiter, enclosed by, newline character
    int headerLines = arch.getCore().getIgnoreHeaderLines();
    String columnDelimiter = arch.getCore().getFieldsTerminatedBy();
    Character enclosedBy = arch.getCore().getFieldsEnclosedBy();
    String newlineDelimiter = arch.getCore().getLinesTerminatedBy();

    // keep track of how long the sort takes
    long time = System.currentTimeMillis();

    // sort by ID column: always index 0
    LineComparator lineComparator =
      new LineComparator(ID_COLUMN_INDEX, columnDelimiter, enclosedBy, IGNORE_CASE_COMPARATOR);
    GBIF_FILE_UTILS
      .sort(unsorted, sorted, CHARACTER_ENCODING, ID_COLUMN_INDEX, columnDelimiter, enclosedBy, newlineDelimiter,
        headerLines, lineComparator, true);
    log.debug(
      "Finished sorting core file in " + String.valueOf((System.currentTimeMillis() - time) / 1000) + " secs, check: "
        + sorted.getAbsoluteFile().toString());

    return sorted;
  }

  /**
   * Validate the Archive's core data file has an ID for each row, and that each ID is unique. Perform this check
   * only if the core record ID term (e.g. occurrenceID, taxonID, etc) has actually been mapped.
   * 
   * @param arch Archive
   * @throws GeneratorException if validation was interrupted due to an error
   * @throws InterruptedException if the thread was interrupted
   */
  private void validateCoreDataFile(Archive arch) throws GeneratorException, InterruptedException, IOException {
    // get the core record ID term
    String coreIdTerm = AppConfig.coreIdTerm(resource.getCoreRowType());

    // only validate the core data file, if the core record identifier (e.g. occurrenceID, taxonID) has been mapped
    if (arch.getCore().hasTerm(coreIdTerm)) {
      addMessage(Level.INFO, "Validating the core record ID " + coreIdTerm + " is always present and unique. "
                             + "Depending on the number of records, this can take a while.");

      // create a new core data file sorted by ID column 0
      File sortedCore = sortCoreDataFile(arch);

      // create an iterator on the new sorted core data file
      CSVReader reader = CSVReaderFactory.build(sortedCore, CHARACTER_ENCODING, arch.getCore().getFieldsTerminatedBy(),
        arch.getCore().getFieldsEnclosedBy(), arch.getCore().getIgnoreHeaderLines());

      int recordsWithNoId = 0;
      int recordsWithDuplicateId = 0;
      ClosableReportingIterator<String[]> iter = null;
      int line = 0;
      String id;
      String lastId = null;
      try {
        iter = reader.iterator();
        while (iter.hasNext()) {
          line++;
          if (line % 1000 == 0) {
            checkForInterruption(line);
            reportIfNeeded();
          }
          String[] record = iter.next();
          if (record == null || record.length == 0) {
            continue;
          }
          // Exception on reading row was encountered
          if (iter.hasRowError() && iter.getException() != null)  {
            throw new GeneratorException(
              "A fatal error was encountered while trying to validate sorted core data file: " + iter.getErrorMessage(),
              iter.getException());
          } else {
            id = record[ID_COLUMN_INDEX];

            // check id exists
            if (Strings.isNullOrEmpty(id)) {
              recordsWithNoId++;
            }

            // check id is unique, using case insensitive comparison. E.g. FISHES:1 and fishes:1 are equal
            if (!Strings.isNullOrEmpty(lastId) && !Strings.isNullOrEmpty(id)) {
              if (id.equalsIgnoreCase(lastId)) {
                writePublicationLogMessage("Duplicate id found: " + id);
                recordsWithDuplicateId++;
              }
            }
            // set so id gets compared on next iteration
            lastId = id;
          }
        }
      } catch (InterruptedException e) {
        // set last error report!
        setState(e);
        throw e;
      } catch (Exception e) {
        // some error validating this file, report
        log.error("Exception caught while validating archive", e);
        // set last error report!
        setState(e);
        throw new GeneratorException("Error while validating archive occurred on line " + line, e);
      } finally {
        if (iter != null) {
          // Exception on advancing cursor was encountered?
          if (!iter.hasRowError() && iter.getErrorMessage() != null) {
            writePublicationLogMessage("Error reading data: " + iter.getErrorMessage());
          }
          iter.close();
        }
        // always cleanup the sorted file, it must not be included in the dwca directory when compressed
        if (sortedCore != null) {
          FileUtils.deleteQuietly(sortedCore);
        }
      }

      // add empty ids user message
      if (recordsWithNoId > 0) {
        addMessage(Level.ERROR, String.valueOf(recordsWithNoId) + " line(s) missing an ID");
      } else {
        writePublicationLogMessage("No lines are missing an ID");
      }

      // add duplicate ids user message
      if (recordsWithDuplicateId > 0) {
        addMessage(Level.ERROR, String.valueOf(recordsWithDuplicateId)
          + " line(s) having a duplicate ID (please note comparisons are case insensitive)");
      } else {
        writePublicationLogMessage("No lines have duplicate IDs");
      }

      // if there was 1 or more records missing an ID, or having a duplicate ID, validation fails
      if (recordsWithNoId == 0 && recordsWithDuplicateId == 0) {
        addMessage(Level.INFO, "Validated: each line has an ID, and each ID is unique");
      } else {
        addMessage(Level.ERROR,
          "Archive validation failed, because not every row has a unique ID (please note comparisons are case insensitive)");
        throw new GeneratorException("Can't validate DwC-A for resource " + resource.getShortname()
          + ". Each row must have an ID, and each ID must be unique (please note comparisons are case insensitive)");
      }
    } else {
      writePublicationLogMessage("The core record ID " + coreIdTerm
        + " was not mapped, so there is no need to validate it");
    }
  }

  /**
   * This method copies a stable version of the latest bundled DwC-A file for archival purposes. This method should
   * only be called if the IPT has been configured in "archival mode".
   * 
   * @throws GeneratorException if the archival couldn't complete for any reason
   */
  private void archiveArchive() throws GeneratorException {
    setState(STATE.ARCHIVING);

    File target = dataDir.resourceDwcaFile(resource.getShortname());
    if (!target.exists()) {
      throw new GeneratorException("Can't archive DwC-A file for resource " + resource.getShortname()
        + "Published DwC-A file doesn't exist");
    }

    // copy stable version of the DwC-A file
    int version = resource.getEmlVersion();
    try {
      File versionedFile = dataDir.resourceDwcaFile(resource.getShortname(), version);
      FileUtils.copyFile(target, versionedFile);
    } catch (IOException e) {
      throw new GeneratorException("Can't archive DwC-A file for resource " + resource.getShortname(), e);
    }

    // final reporting
    addMessage(Level.INFO, "Archive version #" + String.valueOf(version) + " has been archived");
  }

  /**
   * Method responsible for all stages of DwC-A file generation.
   * 
   * @return number of records published in core file
   * @throws GeneratorException if DwC-A generation fails for any reason
   */
  public Integer call() throws Exception {
    try {
      checkForInterruption();
      setState(STATE.STARTED);

      // initial reporting
      addMessage(Level.INFO, "Archive generation started for version #" + String.valueOf(resource.getEmlVersion()));

      // create a temp dir to copy all dwca files to
      dwcaFolder = dataDir.tmpDir();
      archive = new Archive();

      // create data files
      createDataFiles();

      // copy eml file
      addEmlFile();

      // create meta.xml
      createMetaFile();

      // perform some validation, e.g. ensure all core record identifiers are present and unique
      validate();

      // zip archive and copy to resource folder
      bundleArchive();

      // reporting
      addMessage(Level.INFO, "Archive generated successfully!");

      // archive version of archive (if archival mode is turned on)
      if (cfg.isArchivalMode()) {
        archiveArchive();
      }

      // set final state
      setState(STATE.COMPLETED);

      return coreRecords;
    } catch (GeneratorException e) {
      // set last error report!
      setState(e);

      // write exception to publication log file when IPT is in debug mode, otherwise just log it
      if (cfg.debug()) {
        writeFailureToPublicationLog(e);
      } else {
        log.error(
          "Exception occurred trying to generate Darwin Core Archive for resource " + resource.getTitleAndShortname()
          + ": " + e.getMessage(), e);
      }

      // rethrow exception, which gets wrapped in an ExecutionException and re caught when calling Future.get
      throw e;
    } catch (InterruptedException e) {
      setState(e);
      writeFailureToPublicationLog(e);
      throw e;
    } catch (Exception e) {
      setState(e);
      writeFailureToPublicationLog(e);
      throw new GeneratorException(e);
    } finally {
      // cleanup temp dir that was used to store dwca files
      if (dwcaFolder != null && dwcaFolder.exists()) {
        FileUtils.deleteQuietly(dwcaFolder);
      }
      // ensure publication log writer is closed
      closePublicationLogWriter();
    }
  }

  /**
   * Checks if the executing thread has been interrupted, i.e. DwC-A generation was cancelled.
   * 
   * @throws InterruptedException if the thread was found to be interrupted
   */
  private void checkForInterruption() throws InterruptedException {
    if (Thread.interrupted()) {
      StatusReport report = report();
      String msg = "Interrupting dwca generator. Last status: " + report.getState();
      log.info(msg);
      throw new InterruptedException(msg);
    }
  }

  /**
   * Checks if the executing thread has been interrupted, i.e. DwC-A generation was cancelled.
   * 
   * @param line number of lines currently processed at the time of the check
   * @throws InterruptedException if the thread was found to be interrupted
   */
  private void checkForInterruption(int line) throws InterruptedException {
    if (Thread.interrupted()) {
      StatusReport report = report();
      String msg = "Interrupting dwca generator at line " + line + ". Last status: " + report.getState();
      log.info(msg);
      throw new InterruptedException(msg);
    }
  }

  @Override
  protected boolean completed() {
    return STATE.COMPLETED == this.state;
  }

  /**
   * Create data files.
   * 
   * @throws GeneratorException if the resource had no core file that was mapped
   * @throws InterruptedException if the thread was interrupted
   */
  private void createDataFiles() throws GeneratorException, InterruptedException {
    checkForInterruption();
    setState(STATE.DATAFILES);
    if (!resource.hasCore() || resource.getCoreMappings().get(0).getSource() == null) {
      throw new GeneratorException("Core is not mapped");
    }
    for (Extension ext : resource.getMappedExtensions()) {
      report();
      try {
        addDataFile(resource.getMappings(ext.getRowType()));
      } catch (IOException e) {
        throw new GeneratorException("Problem occurred while writing data file", e);
      } catch (IllegalArgumentException e) {
        throw new GeneratorException("Problem occurred while writing data file", e);
      }
    }
    // final reporting
    addMessage(Level.INFO, "All data files completed");
    report();
  }

  /**
   * Create meta.xml file.
   * 
   * @throws GeneratorException if meta.xml file creation failed
   * @throws InterruptedException if the thread was interrupted
   */
  private void createMetaFile() throws GeneratorException, InterruptedException {
    checkForInterruption();
    setState(STATE.METADATA);
    try {
      MetaDescriptorWriter.writeMetaFile(new File(dwcaFolder, "meta.xml"), archive);
    } catch (IOException e) {
      throw new GeneratorException("Meta.xml file could not be written", e);
    } catch (TemplateException e) {
      throw new GeneratorException("Meta.xml file could not be written", e);
    }
    // final reporting
    addMessage(Level.INFO, "meta.xml archive descriptor written");
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.task.ReportingTask#currentException()
   */
  @Override
  protected Exception currentException() {
    return exception;
  }

  @Override
  protected String currentState() {
    switch (state) {
      case WAITING:
        return "Not started yet";
      case STARTED:
        return "Starting archive generation";
      case DATAFILES:
        return "Processing record " + currRecords + " for data file <em>" + currExtension + "</em>";
      case METADATA:
        return "Creating metadata files";
      case BUNDLING:
        return "Compressing archive";
      case COMPLETED:
        return "Archive generated!";
      case VALIDATING:
        return "Validating archive";
      case ARCHIVING:
        return "Archiving version of archive";
      case CANCELLED:
        return CANCELLED_STATE_MSG;
      case FAILED:
        return "Failed. Fatal error!";
      default:
        return "You should never see this";
    }
  }

  /**
   * Write data file for mapping.
   *
   * @param writer file writer for single data file
   * @param inCols index ordered list of all output columns apart from id column
   * @param mapping mapping
   * @param dataFileRowSize number of columns in data file
   * @throws GeneratorException if there was an error writing data file for mapping.
   * @throws InterruptedException if the thread was interrupted
   */
  private void dumpData(Writer writer, PropertyMapping[] inCols, ExtensionMapping mapping, int dataFileRowSize)
    throws GeneratorException, InterruptedException {
    final String idSuffix = StringUtils.trimToEmpty(mapping.getIdSuffix());
    final RecordFilter filter = mapping.getFilter();
    // get maximum column index to check incoming rows for correctness
    int maxColumnIndex = mapping.getIdColumn() == null ? -1 : mapping.getIdColumn();
    for (PropertyMapping pm : mapping.getFields()) {
      if (pm.getIndex() != null && maxColumnIndex < pm.getIndex()) {
        maxColumnIndex = pm.getIndex();
      }
    }

    int recordsWithError = 0;
    int linesWithWrongColumnNumber = 0;
    int recordsFiltered = 0;
    ClosableReportingIterator<String[]> iter = null;
    int line = 0;
    try {
      // get the source iterator
      iter = sourceManager.rowIterator(mapping.getSource());

      while (iter.hasNext()) {
        line++;
        if (line % 1000 == 0) {
          checkForInterruption(line);
          reportIfNeeded();
        }
        String[] in = iter.next();
        if (in == null || in.length == 0) {
          continue;
        }

        // Exception on reading row was encountered, meaning record is incomplete and not written
        if (iter.hasRowError()) {
          writePublicationLogMessage("Error reading line #" + line + "\n" + iter.getErrorMessage());
          recordsWithError++;
          currRecordsSkipped++;
        } else {

          if (in.length <= maxColumnIndex) {
            writePublicationLogMessage("Line with fewer columns than mapped. SourceBase:"
              + mapping.getSource().getName()
              + " Line #" + line + " has " + in.length + " Columns: " + printLine(in));
            // input row is smaller than the highest mapped column. Resize array by adding nulls
            String[] in2 = new String[maxColumnIndex + 1];
            System.arraycopy(in, 0, in2, 0, in.length);
            in = in2;
            linesWithWrongColumnNumber++;
          }

          String[] record = new String[dataFileRowSize];

          // filter this record?
          boolean alreadyTranslated = false;
          if (filter != null && filter.getColumn() != null && filter.getComparator() != null
            && filter.getParam() != null) {
            boolean matchesFilter;
            if (filter.getFilterTime() == RecordFilter.FilterTime.AfterTranslation) {
              int newColumn = translatingRecord(mapping, inCols, in, record);
              matchesFilter = filter.matches(record, newColumn);
              alreadyTranslated = true;
            } else {
              matchesFilter = filter.matches(in, -1);
            }
            if (!matchesFilter) {
              writePublicationLogMessage("Line did not match the filter criteria and was skipped. SourceBase:"
                + mapping.getSource().getName() + " Line #" + line + ": " + printLine(in));
              recordsFiltered++;
              continue;
            }
          }

          // add id column - either an existing column or the line number
          // the id value is converted to lowercase - important for sorting the file by id (a step during validation)
          if (mapping.getIdColumn() == null) {
            record[ID_COLUMN_INDEX] = null;
          } else if (mapping.getIdColumn().equals(ExtensionMapping.IDGEN_LINE_NUMBER)) {
            record[ID_COLUMN_INDEX] = line + idSuffix;
          } else if (mapping.getIdColumn().equals(ExtensionMapping.IDGEN_UUID)) {
            record[ID_COLUMN_INDEX] = UUID.randomUUID().toString();
          } else if (mapping.getIdColumn() >= 0) {
            record[ID_COLUMN_INDEX] = (Strings.isNullOrEmpty(in[mapping.getIdColumn()])) ? idSuffix
              : in[mapping.getIdColumn()] + idSuffix;
          }

          // go through all archive fields
          if (!alreadyTranslated) {
            translatingRecord(mapping, inCols, in, record);
          }
          String newRow = tabRow(record);
          if (newRow != null) {
            writer.write(newRow);
            currRecords++;
          }
        }
      }
    } catch (InterruptedException e) {
      // set last error report!
      setState(e);
      throw e;
    } catch (Exception e) {
      // some error writing this file, report
      log.error("Fatal DwC-A Generator Error encountered", e);
      // set last error report!
      setState(e);
      throw new GeneratorException("Error writing data file for mapping " + mapping.getExtension().getTitle()
        + " in source " + mapping.getSource().getName() + ", line " + line, e);
    } finally {
      if (iter != null) {
        // Exception on advancing cursor encountered?
        if (!iter.hasRowError() && iter.getErrorMessage() != null) {
          writePublicationLogMessage("Error reading data: " + iter.getErrorMessage());
        }
        iter.close();
      }
    }

    // common message part used in constructing all reporting messages below
    String mp = " for mapping " + mapping.getExtension().getTitle() + " in source " + mapping.getSource().getName();

    // add lines incomplete message
    if (recordsWithError > 0) {
      addMessage(Level.WARN, String.valueOf(recordsWithError) + " records were skipped due to errors" + mp);
    } else {
      writePublicationLogMessage("No lines were skipped due to errors" + mp);
    }

    // add wrong lines user message
    if (linesWithWrongColumnNumber > 0) {
      addMessage(Level.WARN, String.valueOf(linesWithWrongColumnNumber) + " lines with fewer columns than mapped" + mp);
    } else {
      writePublicationLogMessage("No lines with fewer columns than mapped" + mp);
    }

    // add filter message
    if (recordsFiltered > 0) {
      addMessage(Level.INFO, String.valueOf(recordsFiltered)
        + " lines did not match the filter criteria and were skipped " + mp);
    } else {
      writePublicationLogMessage("All lines match the filter criteria" + mp);
    }
  }

  /**
   * Sets an exception and state of the worker to FAILED. The final StatusReport is generated at the end.
   * 
   * @param e exception
   */
  private void setState(Exception e) {
    exception = e;
    state = (exception instanceof InterruptedException) ? STATE.CANCELLED : STATE.FAILED;
    report();
  }

  /**
   * Sets only the state of the worker. The final StatusReport is generated at the end.
   * 
   * @param s STATE of worker
   */
  private void setState(STATE s) {
    state = s;
    report();
  }

  private String tabRow(String[] columns) {
    // escape \t \n \r chars !!!
    boolean empty = true;
    for (int i = 0; i < columns.length; i++) {
      if (columns[i] != null) {
        empty = false;
        columns[i] = StringUtils.trimToNull(escapeChars.matcher(columns[i]).replaceAll(" "));
      }
    }
    if (empty) {
      // dont create a row at all!
      return null;
    }
    return StringUtils.join(columns, '\t') + "\n";
  }

  private int translatingRecord(ExtensionMapping mapping, PropertyMapping[] inCols, String[] in, String[] record) {
    int newColumn = -1;
    for (int i = 1; i < inCols.length; i++) {
      PropertyMapping pm = inCols[i];
      String val = null;
      if (pm != null) {
        if (pm.getIndex() != null) {
          val = in[pm.getIndex()];
          if (mapping.getFilter() != null && pm.getIndex().equals(mapping.getFilter().getColumn())) {
            newColumn = i;
          }
          // translate value?
          if (pm.getTranslation() != null && pm.getTranslation().containsKey(val)) {
            val = pm.getTranslation().get(val);
          }
          /*
           * DataType type = mapping.getExtension().getProperty(pm.getTerm()).getType();
           * if (type != null) {
           * if (type == DataType.date) {
           * // TODO: parse date type with mapping datetime format
           * } else if (type == DataType.bool) {
           * // TODO: parse date type with mapping boolean format
           * } else if (type == DataType.decimal) {
           * // normalise punctuation
           * }
           * }
           */
        }
        // use default value for null values
        if (val == null) {
          val = pm.getDefaultValue();
        }
      }
      // add value to data file record
      record[i] = val;
    }
    return newColumn;
  }

  /**
   * Print a line representation of a string array used for logging.
   * 
   * @param in String array
   * @return line
   */
  private String printLine(String[] in) {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (int i = 0; i < in.length; i++) {
      sb.append(in[i]);
      if (i != in.length - 1) {
        sb.append("; ");
      }
    }
    sb.append("]");
    return sb.toString();
  }

  /**
   * Write message from exception to publication log file as a new line but suffocate any exception thrown.
   * 
   * @param e exception to write message from
   */
  private void writeFailureToPublicationLog(Throwable e) {
    StringBuilder sb = new StringBuilder();
    sb.append("Archive generation failed!\n");

    // write exception as nicely formatted string
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    sb.append(sw.toString());

    // write to publication log file
    writePublicationLogMessage(sb.toString());
  }

  /**
   * First we need to find the union of all terms mapped and make them a field in the final archive. We keep a static
   * mapping only if it applies to ALL mappings of the same term. While doing this, populate set of conceptTerms that
   * have been mapped (in all files) for a single Extension.
   * 
   * @param mappings list of ExtensionMapping
   * @param af ArchiveFile
   * @return set of conceptTerms that have been mapped (in all files) for a single Extension
   */
  private Set<Term> addFieldsToArchive(List<ExtensionMapping> mappings, ArchiveFile af) {

    Set<Term> mappedConceptTerms = new HashSet<Term>();
    for (ExtensionMapping m : mappings) {
      for (PropertyMapping pm : m.getFields()) {

        // ArchiveFile.ArchiveField must be dwc-api Term such as DcTerm, DwcTerm, etc.
        // Therefore, find Term corresponding to ExtensionProperty
        Term term = TERM_FACTORY.findTerm(pm.getTerm().qualifiedName());

        if (af.hasTerm(term)) {
          // different default value?
          ArchiveField field = af.getField(term);
          if (field.getDefaultValue() != null && !field.getDefaultValue().equals(pm.getDefaultValue())) {
            // different values, reset to null - we will have to explicitly write the values into the data file
            field.setDefaultValue(null);
            mappedConceptTerms.add(term);
          }
        } else {
          // check if we have a dynamic mapping
          if (pm.getIndex() != null) {

            log.debug("Handling property mapping for term: " + term.qualifiedName() + " (index "
              + pm.getIndex() + ")");

            if (pm.getIndex() >= 0) {
              // Since all default values ​​will be written in the data file, they won't be expressed in the
              // archive file (meta.xml). That's why we send a null value.
              af.addField(buildField(term, null));
              mappedConceptTerms.add(term);
            }
          } else {
            // Only with default value.
            af.addField(buildField(term, null));
            mappedConceptTerms.add(term);
          }
        }
      }
    }
    return mappedConceptTerms;
  }

  /**
   * Iterate through ordered list of those ExtensionProperty that have been mapped, and reassign the ArchiveFile
   * ArchiveField indexes, based on the order of their appearance in the ordered list be careful to reserve index 0 for
   * the ID column
   * 
   * @param propertyList ordered list of those ExtensionProperty that have been mapped
   * @param af ArchiveFile
   */
  private void assignIndexesOrderedByExtension(List<ExtensionProperty> propertyList, ArchiveFile af) {
    for (int propertyIndex = 0; propertyIndex < propertyList.size(); propertyIndex++) {
      ExtensionProperty extensionProperty = propertyList.get(propertyIndex);
      // retrieve the dwc-api Term corresponding to ExtensionProperty
      Term term = TERM_FACTORY.findTerm(extensionProperty.getQualname());
      // lookup ArchiveField using dwc-api Term
      ArchiveField f = af.getField(term);
      if (f != null && f.getIndex() == null) {
        // create new field index corresponding to its position in ordered list of columns indexed
        // +1 because index 0 is reserved for ID column
        int fieldIndex = propertyIndex + 1;
        // assign ArchiveField new index so that meta.xml file mirrors the ordered field order
        f.setIndex(fieldIndex);
      } else {
        log.warn("Skipping ExtensionProperty: " + extensionProperty.getQualname());
      }
    }
  }

  /**
   * Retrieve the ordered list of all Extension's mapped ExtensionProperty. Ordering is done according to Extension.
   * 
   * @param ext Extension
   * @param mappedConceptTerms set of all mapped ConceptTerm
   * @return ordered list of mapped ExtensionProperty
   */
  private List<ExtensionProperty>
    getOrderedMappedExtensionProperties(Extension ext, Set<Term> mappedConceptTerms) {
    List<ExtensionProperty> propertyList = new ArrayList<ExtensionProperty>();
    // start with all Extension's ExtensionProperty, in natural order
    propertyList.addAll(ext.getProperties());

    // matching (below) should be done on the qualified Normalised Name
    Set<String> names = new HashSet<String>();
    for (Term conceptTerm : mappedConceptTerms) {
      names.add(conceptTerm.qualifiedName());
    }

    // remove all ExtensionProperty that have not been mapped, leaving the ordered list of those that have been
    for (Iterator<ExtensionProperty> iterator = propertyList.iterator(); iterator.hasNext();) {
      ExtensionProperty extensionProperty = iterator.next();
      if (extensionProperty.qualifiedName() != null) {
        if (!names.contains(extensionProperty.qualifiedName())) {
          iterator.remove();
        }
      }
    }
    return propertyList;
  }
}
