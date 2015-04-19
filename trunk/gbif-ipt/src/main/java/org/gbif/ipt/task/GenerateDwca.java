package org.gbif.ipt.task;

import org.gbif.api.model.common.DOI;
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
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.RecordFilter;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.utils.MapUtils;
import org.gbif.utils.file.ClosableReportingIterator;
import org.gbif.utils.file.CompressionUtil;
import org.gbif.utils.text.LineComparator;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.WildcardFileFilter;
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
  private final VocabulariesManager vocabManager;
  private Map<String, String> basisOfRecords;
  private Exception exception;
  private AppConfig cfg;
  private static final int ID_COLUMN_INDEX = 0;
  public static final String CHARACTER_ENCODING = "UTF-8";
  private static final TermFactory TERM_FACTORY = TermFactory.instance();
  private static final String SORTED_FILE_PREFIX = "sorted_";
  private static final org.gbif.utils.file.FileUtils GBIF_FILE_UTILS = new org.gbif.utils.file.FileUtils();
  public static final String CANCELLED_STATE_MSG = "Archive generation cancelled";
  public static final String ID_COLUMN_NAME = "id";
  public static final String TEXT_FILE_EXTENSION = ".txt";
  public static final String WILDCARD_CHARACTER = "*";

  private static final Comparator<String> IGNORE_CASE_COMPARATOR = Ordering.from(new Comparator<String>() {

    public int compare(String o1, String o2) {
      return o1.compareToIgnoreCase(o2);
    }
  }).nullsFirst();

  @Inject
  public GenerateDwca(@Assisted Resource resource, @Assisted ReportHandler handler, DataDir dataDir,
    SourceManager sourceManager, AppConfig cfg, VocabulariesManager vocabManager) throws IOException {
    super(1000, resource.getShortname(), handler, dataDir);
    this.resource = resource;
    this.sourceManager = sourceManager;
    this.cfg = cfg;
    this.vocabManager = vocabManager;
  }

  /**
   * Adds a single data file for a list of extension mappings that must all be mapped to the same extension.
   * </br>
   * The ID column is always the 1st column (index 0) and is always equal to the core record identifier that has been
   * mapped (e.g. occurrenceID, taxonID, etc).
   *
   * @param mappings list of ExtensionMapping
   * @param rowLimit maximum number of rows to write
   * @throws IllegalArgumentException if not all mappings are mapped to the same extension
   * @throws InterruptedException if the thread was interrupted
   * @throws IOException if problems occurred while persisting new data files
   * @throws GeneratorException if any problem was encountered writing data file
   */
  public void addDataFile(List<ExtensionMapping> mappings, @Nullable Integer rowLimit) throws IOException,
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
    // in the generated file column 0 will be the id column
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

    // create file name from extension name, with incremental suffix to resolve name conflicts (e.g. taxon.txt,
    // taxon2.txt, taxon3.txt)
    String extensionName = (ext.getName() == null) ? "f" : ext.getName().toLowerCase().replaceAll("\\s", "_");
    String fn = createFileName(dwcaFolder, extensionName);

    // open new file writer for single data file
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
        dumpData(writer, inCols, m, totalColumns, rowLimit, resource.getDoi());
        // remember core record number
        if (resource.getCoreRowType().equalsIgnoreCase(ext.getRowType())) {
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
    if (resource.getCoreRowType().equalsIgnoreCase(ext.getRowType())) {
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
      FileUtils.copyFile(dataDir.resourceEmlFile(resource.getShortname()), new File(dwcaFolder,
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
    BigDecimal version = resource.getEmlVersion();
    try {
      // create zip
      zip = dataDir.tmpFile("dwca", ".zip");
      CompressionUtil.zipDir(dwcaFolder, zip);
      if (zip.exists()) {
        // move to data dir with versioned name
        File versionedFile = dataDir.resourceDwcaFile(resource.getShortname(), version);
        if (versionedFile.exists()) {
          FileUtils.forceDelete(versionedFile);
        }
        FileUtils.moveFile(zip, versionedFile);
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
    addMessage(Level.INFO, "Archive has been compressed");
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
      // populate basisOfRecord lookup HashMap
      loadBasisOfRecordMapFromVocabulary();
      // perform validation on core file (includes core ID and basisOfRecord validation)
      validateCoreDataFile(arch);
      // perform validation on extension files (includes basisOfRecord validation)
      if (!arch.getExtensions().isEmpty()) {
        validateExtensionDataFiles(arch.getExtensions());
      }
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
   * Validate all extension files:
   * </br>
   * -validate basisOfRecords in extensions having occurrence rowtype.
   *
   * @param extensions Set of Archive extension data files (not core data files)
   *
   * @throws InterruptedException
   * @throws GeneratorException
   * @throws IOException
   */
  private void validateExtensionDataFiles(Set<ArchiveFile> extensions)
    throws InterruptedException, GeneratorException, IOException {
    for (ArchiveFile extension: extensions) {
      // validate extensions with occurrence rowType
      if (extension.getRowType().equalsIgnoreCase(Constants.DWC_ROWTYPE_OCCURRENCE)) {
        // populate basisOfRecord lookup HashMap
        loadBasisOfRecordMapFromVocabulary();
        // do BoR validation
        validateBasisOfRecord(extension);
      }
    }
  }

  /**
   * Populate basisOfRecords map from XML vocabulary, used to validate basisOfRecord values.
   */
  private void loadBasisOfRecordMapFromVocabulary() {
    if (basisOfRecords == null) {
      basisOfRecords = new HashMap<String, String>();
      basisOfRecords
        .putAll(vocabManager.getI18nVocab(Constants.VOCAB_URI_BASIS_OF_RECORDS, Locale.ENGLISH.getLanguage(), false));
      basisOfRecords = MapUtils.getMapWithLowercaseKeys(basisOfRecords);
    }
  }

  /**
   * Validation ensures that each occurrence record contains a basisOfRecord, and each basisOfRecord value matches
   * the Darwin Core Type vocabulary. Validation also alerts the user when they are using ambiguous basisOfRecord
   * 'occurrence', in case it has been used inappropriately.
   *
   * @param archiveFile file to validate
   *
   * @throws GeneratorException if validation was interrupted due to an error
   * @throws InterruptedException if the thread was interrupted
   * @throws java.io.IOException if a problem occurred or opening iterator on file for example
   */
  private void validateBasisOfRecord(ArchiveFile archiveFile)
    throws InterruptedException, IOException, GeneratorException {
    Term basisOfRecord = TERM_FACTORY.findTerm(Constants.DWC_BASIS_OF_RECORD);

    if (archiveFile.hasTerm(basisOfRecord)) {
      addMessage(Level.INFO, "Validating " + archiveFile.getTitle() + ": basisOfRecord must always be present and its "
                             + "value must match the Darwin Core Type Vocabulary."
                             + " Depending on the number of records, this can take a while.");

      // find index of basisOfRecord
      int index = archiveFile.getField(basisOfRecord).getIndex();

      // create an iterator on the data file
      CSVReader reader = archiveFile.getCSVReader();

      // create an iterator on the data file
      int recordsWithNoBasisOfRecord = 0;
      int recordsWithNonMatchingBasisOfRecord = 0;
      int recordsWithAmbiguousBasisOfRecord = 0;
      int line = 0;
      ClosableReportingIterator<String[]> iter = null;
      String bor;
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
          if (iter.hasRowError() && iter.getException() != null) {
            throw new GeneratorException(
              "A fatal error was encountered while trying to validate " + archiveFile.getTitle() + " : " + iter
                .getErrorMessage(), iter.getException());
          } else {
            bor = record[index];

            // check basisOfRecord exists
            if (Strings.isNullOrEmpty(bor)) {
              recordsWithNoBasisOfRecord++;
            } else {
              // check basisOfRecord matches vocabulary (lower case comparison). E.g. specimen matches Specimen are equal
              if (!basisOfRecords.containsKey(bor.toLowerCase())) {
                writePublicationLogMessage(
                  "Line #" + String.valueOf(line) + " has basisOfRecord [" + bor + "] that does not match the Darwin Core Type Vocabulary");
                recordsWithNonMatchingBasisOfRecord++;
              }
              // check basisOfRecord matches ambiguous "occurrence" (lower case comparison)
              else if (bor.equalsIgnoreCase("occurrence")) {
                recordsWithAmbiguousBasisOfRecord++;
              }
            }
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
      }

      // finish reporting
      summarizeBasisOfRecordValidation(recordsWithNoBasisOfRecord, recordsWithNonMatchingBasisOfRecord,
        recordsWithAmbiguousBasisOfRecord);

    } else {
      addMessage(Level.ERROR,
        "Archive validation failed, because required term basisOfRecord was not mapped in the occurrence file: "
        + archiveFile.getTitle());
      throw new GeneratorException("Can't validate DwC-A for resource " + resource.getShortname()
                                   + "Required term basisOfRecord was not mapped in the occurrence file: " + archiveFile
        .getTitle());
    }
  }

  /**
   * Validate the Archive's core data file has an ID for each row, and that each ID is unique. Perform this check
   * only if the core record ID term (e.g. occurrenceID, taxonID, etc) has actually been mapped.
   * </br>
   * If the core has rowType occurrence, validate the core data file has a basisOfRecord for each row, and that each
   * basisOfRecord matches the DwC Type Vocabulary.
   * 
   * @param arch Archive
   *
   * @throws GeneratorException if validation was interrupted due to an error
   * @throws InterruptedException if the thread was interrupted
   * @throws java.io.IOException if a problem occurred sorting core file, or opening iterator on it for example
   */
  private void validateCoreDataFile(Archive arch) throws GeneratorException, InterruptedException, IOException {
    // get the core record ID term
    String coreIdTerm = AppConfig.coreIdTerm(resource.getCoreRowType());
    // get the basisOfRecord term
    Term basisOfRecord = TERM_FACTORY.findTerm(Constants.DWC_BASIS_OF_RECORD);

    // fail immediately if occurrence core doesn't contain basisOfRecord mapping
    if (isOccurrenceCore(arch) && !arch.getCore().hasTerm(basisOfRecord)) {
      addMessage(Level.ERROR,
        "Archive validation failed, because required term basisOfRecord was not mapped in the occurrence core");
      throw new GeneratorException("Can't validate DwC-A for resource " + resource.getShortname()
                                   + ". Required term basisOfRecord was not mapped in the occurrence core");
    }

    // validate the core file if a) the record identifier (e.g. occurrenceID, taxonID) has been mapped
    // or b) the core file has rowType occurrence, in which case mandatory term basisOfRecord must be validated
    if (arch.getCore().hasTerm(coreIdTerm) || isOccurrenceCore(arch)) {

      if (arch.getCore().hasTerm(coreIdTerm)) {
        addMessage(Level.INFO, "Validating the core record ID " + coreIdTerm + " is always present and unique. "
                               + "Depending on the number of records, this can take a while.");
      }

      // find index of basisOfRecord
      int basisOfRecordIndex = -1;
      if (isOccurrenceCore(arch)) {
        addMessage(Level.INFO, "Validating that the occurrence core basisOfRecord is always present and its "
                               + "value matches the Darwin Core Type Vocabulary."
                               + " Depending on the number of records, this can take a while.");
        basisOfRecordIndex = arch.getCore().getField(basisOfRecord).getIndex();
      }

      // create a new core data file sorted by ID column 0
      File sortedCore = sortCoreDataFile(arch);

      // create an iterator on the new sorted core data file
      CSVReader reader = CSVReaderFactory.build(sortedCore, CHARACTER_ENCODING, arch.getCore().getFieldsTerminatedBy(),
        arch.getCore().getFieldsEnclosedBy(), arch.getCore().getIgnoreHeaderLines());

      // id related metrics
      int recordsWithNoId = 0;
      int recordsWithDuplicateId = 0;

      // basisOfRecord related metrics
      int recordsWithNoBasisOfRecord = 0;
      int recordsWithNonMatchingBasisOfRecord = 0;
      int recordsWithAmbiguousBasisOfRecord = 0;

      ClosableReportingIterator<String[]> iter = null;
      int line = 0;
      String id;
      String lastId = null;
      String bor;
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

            if (arch.getCore().hasTerm(coreIdTerm)) {
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

            if (isOccurrenceCore(arch)) {
              bor = record[basisOfRecordIndex];

              // check basisOfRecord exists
              if (Strings.isNullOrEmpty(bor)) {
                recordsWithNoBasisOfRecord++;
              } else {
                // check basisOfRecord matches vocabulary (lower case comparison). E.g. specimen matches Specimen are equal
                if (!basisOfRecords.containsKey(bor.toLowerCase())) {
                  writePublicationLogMessage(
                    "Line #" + String.valueOf(line) + " has basisOfRecord [" + bor + "] that does not match the Darwin Core Type Vocabulary");
                  recordsWithNonMatchingBasisOfRecord++;
                }
                // check basisOfRecord matches ambiguous "occurrence" (lower case comparison)
                else if (bor.equalsIgnoreCase("occurrence")) {
                  recordsWithAmbiguousBasisOfRecord++;
                }
              }
            }
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

      if (arch.getCore().hasTerm(coreIdTerm)) {
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
      }

      if (isOccurrenceCore(arch)) {
        // finish reporting
        summarizeBasisOfRecordValidation(recordsWithNoBasisOfRecord, recordsWithNonMatchingBasisOfRecord,
          recordsWithAmbiguousBasisOfRecord);
      }

    } else {
      writePublicationLogMessage("The core record ID " + coreIdTerm
        + " was not mapped, so there is no need to validate it");
    }
  }

  /**
   * Report basisOfRecord validation (shared by two methods 1. validateBasisOfRecord(ArchiveFile archiveFile)
   * 2. validateCoreDataFile(Archive arch).
   *
   * @param recordsWithNoBasisOfRecord number of records with no basisOfRecord
   * @param recordsWithNonMatchingBasisOfRecord number of records with basisOfRecord not matching DwC Type Vocabulary
   * @param recordsWithAmbiguousBasisOfRecord number of records with basisOfRecord equal to 'occurrence'
   *
   * @throws GeneratorException if validation threshold exceeded
   */
  private void summarizeBasisOfRecordValidation(int recordsWithNoBasisOfRecord, int recordsWithNonMatchingBasisOfRecord, int recordsWithAmbiguousBasisOfRecord)
    throws GeneratorException {
    // add empty BoR user message
    if (recordsWithNoBasisOfRecord > 0) {
      addMessage(Level.ERROR, String.valueOf(recordsWithNoBasisOfRecord) + " line(s) are missing a basisOfRecord");
    } else {
      writePublicationLogMessage("No lines are missing a basisOfRecord");
    }

    // add non matching BoR user message
    if (recordsWithNonMatchingBasisOfRecord > 0) {
      addMessage(Level.ERROR, String.valueOf(recordsWithNonMatchingBasisOfRecord)
                              + " line(s) have basisOfRecord that does not match the Darwin Core Type Vocabulary "
                              + "(please note comparisons are case insensitive)");
    } else {
      writePublicationLogMessage("All lines have basisOfRecord that matches the Darwin Core Type Vocabulary");
    }

    // add ambiguous BoR user message
    if (recordsWithAmbiguousBasisOfRecord > 0) {
      addMessage(Level.WARN, String.valueOf(recordsWithAmbiguousBasisOfRecord)
                             + " line(s) use ambiguous basisOfRecord 'occurrence'. It is advised that occurrence be "
                             + "reserved for cases when the basisOfRecord is unknown. Otherwise, a more specific "
                             + "basisOfRecord should be chosen.");
    } else {
      writePublicationLogMessage("No lines have ambiguous basisOfRecord 'occurrence'.");
    }

    // if there was 1 or more records missing a basisOfRecord, or having a non matching basisOfRecord, validation fails
    if (recordsWithNoBasisOfRecord == 0 && recordsWithNonMatchingBasisOfRecord == 0) {
      addMessage(Level.INFO,
        "Validated: each line has a basisOfRecord, and each basisOfRecord matches the Darwin Core Type Vocabulary");
    } else {
      addMessage(Level.ERROR,
        "Archive validation failed, because not every row in the occurrence file(s) has a valid basisOfRecord "
        + "(please note all basisOfRecord must match Darwin Core Type Vocabulary, and comparisons are case "
        + "insensitive)");
      throw new GeneratorException("Can't validate DwC-A for resource " + resource.getShortname()
                                   + ". Each row in the occurrence file(s) must have a basisOfRecord, and each "
                                   + "basisOfRecord must match the Darwin Core Type Vocabulary (please note "
                                   + "comparisons are case insensitive)");
    }
  }

  /**
   * @return true if the archive core file has occurrence rowType.
   */
  private boolean isOccurrenceCore(Archive arch) {
    return arch.getCore().getRowType().equalsIgnoreCase(Constants.DWC_ROWTYPE_OCCURRENCE);
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
      addMessage(Level.INFO, "Archive version #" + String.valueOf(resource.getEmlVersion()) + " generated successfully!");

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
    if (!resource.hasCore() || resource.getCoreRowType() == null
        || resource.getCoreMappings().get(0).getSource() == null) {
      throw new GeneratorException("Core is not mapped");
    }
    for (Extension ext : resource.getMappedExtensions()) {
      report();
      try {
        addDataFile(resource.getMappings(ext.getRowType()), null);
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
   * @param rowLimit maximum number of rows to write
   * @throws GeneratorException if there was an error writing data file for mapping.
   * @throws InterruptedException if the thread was interrupted
   */
  private void dumpData(Writer writer, PropertyMapping[] inCols, ExtensionMapping mapping, int dataFileRowSize,
    @Nullable Integer rowLimit, @Nullable DOI doi)
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
              applyTranslations(inCols, in, record, mapping.isDoiUsedForDatasetId(), doi);
              matchesFilter = filter.matches(in);
              alreadyTranslated = true;
            } else {
              matchesFilter = filter.matches(in);
            }
            if (!matchesFilter) {
              writePublicationLogMessage("Line did not match the filter criteria and was skipped. SourceBase:"
                + mapping.getSource().getName() + " Line #" + line + ": " + printLine(in));
              recordsFiltered++;
              continue;
            }
          }

          // add id column - either an existing column or the line number
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
            applyTranslations(inCols, in, record, mapping.isDoiUsedForDatasetId(), doi);
          }
          String newRow = tabRow(record);
          if (newRow != null) {
            writer.write(newRow);
            currRecords++;
            // don't exceed row limit (e.g. only want to write X number of rows used to preview first X rows of file)
            if (rowLimit != null && currRecords >= rowLimit) {
              break;
            }
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

  /**
   * Apply translations or default values to row, for all mapped properties.
   * </br>
   * The method starts by iterating through all mapped properties, checking each one if it has been translated or a
   * default value provided. The original value in the row is then replaced with the translated or default value.
   * A record array representing the values to be written to the data file is also updated.
   *
   * @param inCols values array, of columns in row that have been mapped
   * @param in values array, of all columns in row
   */
  private void applyTranslations(PropertyMapping[] inCols, String[] in, String[] record, boolean doiUsedForDatasetId,
    DOI doi) {
    for (int i = 1; i < inCols.length; i++) {
      PropertyMapping pm = inCols[i];
      String val = null;
      if (pm != null) {
        if (pm.getIndex() != null) {
          val = in[pm.getIndex()];
          // translate value?
          if (pm.getTranslation() != null && pm.getTranslation().containsKey(val)) {
            val = pm.getTranslation().get(val);
            // update value in original record
            in[pm.getIndex()] = val;
          }
        }
        // use default value for null values
        if (val == null) {
          val = pm.getDefaultValue();
        }
        // use DOI for datasetID property?
        if (pm.getTerm().qualifiedName().equalsIgnoreCase(Constants.DWC_DATASET_ID) && doiUsedForDatasetId
            && doi != null) {
          val = doi.toString();
        }
      }
      // add value to data file record
      record[i] = val;
    }
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

  /**
   * This method checks whether a competing file name exists in the folder where DwC-A files are written to.
   * If a competing file name exists, a numerical suffix is appended to the file name, to differentiate it from the
   * existing files' names. The numerical suffix is incrementing, and is equal to the number of existing files with
   * this name.
   * </br>
   * E.g. the initial name has no suffix (taxon.txt), but subsequent names look like (taxon2.txt, taxon3.txt, etc).
   *
   * Before IPT v2.2 the DwC-A file name has been determined from the extension name. When two extensions had the same
   * name, this caused one file to be overwritten - see Issue 1087.
   *
   * @param dwcaFolder folder where DwC-A files are written to
   * @param extensionName name of extension writing file for
   *
   * @return name of file for DwC-A file to be written
   */
  protected String createFileName(File dwcaFolder, String extensionName) {
    String wildcard = extensionName + WILDCARD_CHARACTER + TEXT_FILE_EXTENSION;
    FileFilter fileFilter = new WildcardFileFilter(wildcard, IOCase.INSENSITIVE);
    File[] files = dwcaFolder.listFiles(fileFilter);
    if (files.length > 0) {
      int max = 1;
      String fileName = null;
      for (File file: files) {
        try {
          fileName = file.getName();
          int suffixEndIndex = fileName.indexOf(TEXT_FILE_EXTENSION);
          String suffix = file.getName().substring(extensionName.length(), suffixEndIndex);
          int suffixInt = Integer.valueOf(suffix);
          if (suffixInt >= max) {
            max = suffixInt;
          }
        } catch (NumberFormatException e) {
          log.debug("No numerical suffix could be parsed from file name: " + Strings.nullToEmpty(fileName));
        }
      }
      return extensionName + String.valueOf(max + 1) + TEXT_FILE_EXTENSION;
    }
    return extensionName + TEXT_FILE_EXTENSION;
  }

  /**
   * Required for preview mapping feature, on manage resource page.
   *
   * @param dwcaFolder DwC-A directory
   */
  public void setDwcaFolder(File dwcaFolder) {
    this.dwcaFolder = dwcaFolder;
  }

  /**
   * Required for preview mapping feature, on manage resource page.
   *
   * @param archive DwC Archive
   */
  public void setArchive(Archive archive) {
    this.archive = archive;
  }
}
