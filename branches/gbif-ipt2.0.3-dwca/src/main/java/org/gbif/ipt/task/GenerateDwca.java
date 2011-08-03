package org.gbif.ipt.task;

import org.gbif.dwc.terms.ConceptTerm;
import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.ArchiveField;
import org.gbif.dwc.text.ArchiveFile;
import org.gbif.dwc.text.ArchiveWriter;
import org.gbif.dwc.text.ArchiveField.DataType;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.RecordFilter;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.RecordFilter.FilterTime;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.utils.file.ClosableIterator;
import org.gbif.utils.file.CompressionUtil;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.regex.Pattern;

import freemarker.template.TemplateException;

public class GenerateDwca extends ReportingTask implements Callable<Integer> {
  private enum STATE {
    WAITING, STARTED, DATAFILES, METADATA, BUNDLING, COMPLETED, STOPPING, FAILED
  };

  private static final Pattern escapeChars = Pattern.compile("[\t\n\r]");
  private final Resource resource;
  private final DataDir dataDir;
  private int coreRecords = 0;
  private Archive archive;
  private File dwcaFolder;
  // status reporting
  private int currRecords = 0;
  private String currExtension;
  private STATE state = STATE.WAITING;
  private final SourceManager sourceManager;
  private Exception exception;

  @Inject
  public GenerateDwca(@Assisted Resource resource, @Assisted ReportHandler handler, DataDir dataDir,
      SourceManager sourceManager) {
    super(1000, resource.getShortname(), handler);
    this.resource = resource;
    this.dataDir = dataDir;
    this.sourceManager = sourceManager;
  }

  /**
   * Adds a single data file for a list of extension mappings that must all be
   * mapped to the same extension
   * 
   * @param mappings
   * @throws IOException
   * @throws GeneratorException
   * @throws IllegalArgumentException if not all mappings are mapped to the same
   *         extension
   */
  private void addDataFile(List<ExtensionMapping> mappings) throws IOException, GeneratorException,
      IllegalArgumentException {
    checkForInterruption();
    if (mappings == null || mappings.isEmpty()) {
      return;
    }

    // update reporting
    currRecords = 0;
    Extension ext = mappings.get(0).getExtension();
    currExtension = ext.getTitle();

    // verify that all mappings share this extension
    for (ExtensionMapping m : mappings) {
      if (!ext.equals(m.getExtension())) {
        throw new IllegalArgumentException(
            "All mappings for a single data file need to be mapped to the same extension: " + ext.getRowType());
      }
    }

    // create new meta.xml with the help of the Archive class
    // create archive file representing this extensions
    ArchiveFile af = ArchiveFile.buildTabFile();
    af.setRowType(ext.getRowType());
    af.setEncoding("utf-8");
    af.setDateFormat("YYYY-MM-DD");
    // in the generated file column 0 will be the id row
    af.setId(buildField(null, 0, null));

    // first we need to find the union of all terms mapped and make them a field
    // in the final archive
    // we keep a static mapping only if it applies to ALL mappings of the same
    // term
    int dataFileRowSize = 1; // first column will become the id column
    for (ExtensionMapping m : mappings) {
      for (PropertyMapping pm : m.getFields()) {
        if (af.hasTerm(pm.getTerm())) {
          // different default value?
          ArchiveField field = af.getField(pm.getTerm());
          if (field.getDefaultValue() != null && !field.getDefaultValue().equals(pm.getDefaultValue())) {
            // different values, reset to null - we will have to explicitly
            // write the values into the data file
            field.setDefaultValue(null);
            field.setIndex(dataFileRowSize);
            dataFileRowSize++;
          }
        } else {
          // All dynamic mapping and all those fields with default values are going to be in data file.
          if (!pm.getTerm().qualifiedName().equalsIgnoreCase(Constants.DWC_OCCURRENCE_ID)
              && !pm.getTerm().qualifiedName().equalsIgnoreCase(Constants.DWC_TAXON_ID)) {
            // af.addField(buildField(pm.getTerm(), dataFileRowSize, pm.getDefaultValue()));

            // Since all default values ​​will be written in the data file, they won't be expressed in the
            // archive file (meta.xml).
            af.addField(buildField(pm.getTerm(), dataFileRowSize, null));
            dataFileRowSize++;
          } else {
            af.addField(buildField(pm.getTerm(), 0, null));
          }
        }
      }
    }

    // open new file writer for single data file
    String fn = ext.getName().toLowerCase().replaceAll("\\s", "_") + ".txt";
    File dataFile = new File(dwcaFolder, fn);
    Writer writer = org.gbif.utils.file.FileUtils.startNewUtf8File(dataFile);
    // add source file location
    af.addLocation(dataFile.getName());

    // ready to go though each mapping and dump the data
    addMessage(Level.INFO, "Start writing data file for " + currExtension);
    try {
      for (ExtensionMapping m : mappings) {
        // write to data file
        dumpData(writer, af, m, dataFileRowSize);
        // remember core record number
        if (ext.isCore()) {
          coreRecords = currRecords;
        }
      }
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
        + dataFileRowSize + " columns");
  }

  private void addEmlFile() throws IOException {
    setState(STATE.METADATA);
    FileUtils.copyFile(dataDir.resourceEmlFile(resource.getShortname(), null), new File(dwcaFolder, "eml.xml"));
    archive.setMetadataLocation("eml.xml");
    // final reporting
    addMessage(Level.INFO, "EML file added");
  }

  private ArchiveField buildField(ConceptTerm term, Integer column, String defaultValue) {
    ArchiveField f = new ArchiveField();
    f.setTerm(term);
    f.setIndex(column);
    f.setDefaultValue(defaultValue);
    return f;
  }

  private void bundleArchive() throws IOException {
    setState(STATE.BUNDLING);
    // create zip
    File zip = dataDir.tmpFile("dwca", ".zip");
    CompressionUtil.zipDir(dwcaFolder, zip);
    // move to data dir
    checkForInterruption();
    File target = dataDir.resourceDwcaFile(resource.getShortname());
    if (target.exists()) {
      target.delete();
    }
    FileUtils.moveFile(zip, target);
    // final reporting
    addMessage(Level.INFO, "Archive compressed");
  }

  public Integer call() throws Exception {
    try {
      checkForInterruption();
      setState(STATE.STARTED);
      addMessage(Level.INFO, "Archive generation started for resource " + resource.getShortname());
      // create a temp dir to copy all dwca files to
      dwcaFolder = dataDir.tmpDir();
      archive = new Archive();

      // create data files
      checkForInterruption();
      createDataFiles();

      // copy eml file
      checkForInterruption();
      addEmlFile();

      // create meta.xml
      checkForInterruption();
      createMetaFile();

      // zip archive and copy to resource folder
      checkForInterruption();
      bundleArchive();

      // final reporting
      addMessage(Level.INFO, "Archive generated successfully!");
      setState(STATE.COMPLETED);

      return coreRecords;

    } catch (Exception e) {
      // set last error report!
      setState(e);
      throw new GeneratorException(e);
    }
  }

  private void checkForInterruption() {
    if (Thread.interrupted()) {
      StatusReport report = report();
      log.info("Interrupting dwca generator. Last status: " + report.getState());
      throw new CancellationException("Canceled dwca generator");
    }
  }

  private void checkForInterruption(int line) throws GeneratorException {
    if (Thread.interrupted()) {
      StatusReport report = report();
      log.info("Interrupting dwca generator at line " + line + ". Last status: " + report.getState());
      throw new GeneratorException("Canceled");
    }
  }

  @Override
  protected boolean completed() {
    return STATE.COMPLETED == this.state;
  }

  private void createDataFiles() throws IOException, GeneratorException {
    setState(STATE.DATAFILES);
    if (!resource.hasCore() || resource.getCoreMappings().get(0).getSource() == null) {
      throw new GeneratorException("Core is not mapped");
    }
    for (Extension ext : resource.getMappedExtensions()) {
      report();
      addDataFile(resource.getMappings(ext.getRowType()));
    }
    // final reporting
    addMessage(Level.INFO, "All data files completed");
    report();
  }

  private void createMetaFile() throws IOException, TemplateException {
    setState(STATE.METADATA);
    ArchiveWriter writer = new ArchiveWriter();
    writer.writeMetaFile(new File(dwcaFolder, "meta.xml"), archive);
    // final reporting
    addMessage(Level.INFO, "meta.xml archive descriptor written");
  }

  /*
   * (non-Javadoc)
   * 
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
      case STOPPING:
        return "Stopping process";
      case FAILED:
        return "Failed. Fatal error!";
      default:
        return "You should never see this";
    }
  }

  /**
   * @param writer
   * @param dataFileRowSize
   * @param m
   * @throws GeneratorException
   */
  private void dumpData(Writer writer, ArchiveFile dataFile, ExtensionMapping mapping, int dataFileRowSize)
      throws GeneratorException {
    final String idSuffix = StringUtils.trimToEmpty(mapping.getIdSuffix());
    final RecordFilter filter = mapping.getFilter();
    // get maximum column index to check incoming rows for correctness
    int linesWithWrongColumnNumber = 0;
    int recordsFiltered = 0;
    int maxColumnIndex = mapping.getIdColumn() == null ? -1 : mapping.getIdColumn();
    for (PropertyMapping pm : mapping.getFields()) {
      if (pm.getIndex() != null && maxColumnIndex < pm.getIndex()) {
        maxColumnIndex = pm.getIndex();
      }
    }

    // prepare index ordered list of all output columns apart from id column, so
    // its fast to iterate
    PropertyMapping[] inCols = new PropertyMapping[dataFileRowSize];
    for (Iterator<ArchiveField> it = dataFile.getFields().values().iterator(); it.hasNext();) {
      ArchiveField f = it.next();
      if (f.getIndex() > 0) {
        inCols[f.getIndex()] = mapping.getField(f.getTerm().qualifiedName());
      } else {
        inCols[0] = mapping.getField(f.getTerm().qualifiedName());
        it.remove();
      }
    }
    /*
     * for (ArchiveField f : dataFile.getFields().values()) {
     * }
     */

    // get the source iterator
    ClosableIterator<String[]> iter = null;
    int line = 0;
    try {

      File logFile = dataDir.resourcePublicationLogFile(resource.getShortname());
      FileUtils.deleteQuietly(logFile);
      BufferedWriter logWriter = new BufferedWriter(new FileWriter(logFile));
      logWriter.write("Log Messages for publishing resource " + resource.getShortname() + " version "
          + resource.getEmlVersion());
      logWriter.write("\n\n");

      try {
        iter = sourceManager.rowIterator(mapping.getSource());

        // generating headers
        String[] headers = new String[inCols.length];
        if (inCols[0] != null) {
          headers[0] = inCols[0].getTerm().simpleName();
        } else {
          headers[0] = "id";
        }
        for (int c = 1; c < inCols.length; c++) {
          headers[c] = inCols[c].getTerm().simpleName();
        }
        String headerLine = tabRow(headers);
        dataFile.setIgnoreHeaderLines(1);
        writer.write(headerLine);

        while (iter.hasNext()) {
          line++;
          if (line % 1000 == 0) {
            checkForInterruption(line);
            reportIfNeeded();
          }
          String[] in = iter.next();
          String inLine = "[";
          for (int i = 0; i < in.length; i++) {
            if (i == 0) {
              inLine += in[i];
            } else {
              inLine += "; " + in[i];
            }
          }
          inLine += "]";
          if (in == null || in.length == 0) {
            continue;
          }

          if (in.length <= maxColumnIndex) {
            logWriter.write("Line with less columns than mapped\tSource:" + mapping.getSource().getName() + "\tLine:"
                + line + in.length + "\tColumns:" + in.length + "\t" + inLine);
            logWriter.write("\n");
            // input row is smaller than the highest mapped column. Resize array
            // by adding nulls
            String[] in2 = new String[maxColumnIndex + 1];
            System.arraycopy(in, 0, in2, 0, in.length);
            in = in2;
            linesWithWrongColumnNumber++;
          }

          String[] record = new String[dataFileRowSize];

          // filter this record?
          boolean matchesFilter, alreadyTranslated = false;
          if (filter != null && filter.getColumn() != null && filter.getComparator() != null
              && filter.getParam() != null) {
            if (filter.getFilterTime() == FilterTime.AfterTranslation) {
              int newColumn = translatingRecord(mapping, inCols, in, record);
              matchesFilter = filter.matches(record, newColumn);
              alreadyTranslated = true;
            } else {
              matchesFilter = filter.matches(in, -1);
            }
            if (!matchesFilter) {
              logWriter.write("Line did not match the filter criteria and were skipped\tSource:"
                  + mapping.getSource().getName() + "\tLine:" + line + in.length + "\t" + inLine);
              logWriter.write("\n");
              recordsFiltered++;
              continue;
            }
          }

          // add id column - either an existing column or the line number
          if (mapping.getIdColumn() == null) {
            record[0] = null;
          } else if (mapping.getIdColumn().equals(ExtensionMapping.IDGEN_LINE_NUMBER)) {
            record[0] = String.valueOf(line) + idSuffix;
          } else if (mapping.getIdColumn().equals(ExtensionMapping.IDGEN_UUID)) {
            record[0] = UUID.randomUUID().toString();
          } else if (mapping.getIdColumn() >= 0) {
            record[0] = in[mapping.getIdColumn()] + idSuffix;
          }

          // go thru all archive fields
          if (!alreadyTranslated) {
            translatingRecord(mapping, inCols, in, record);
          }
          String newRow = tabRow(record);
          if (newRow != null) {
            writer.write(newRow);
            currRecords++;
          }
        }

      } catch (Exception e) {
        // some error writing this file, report
        log.error("Fatal DwC-A Generator Error", e);
        throw new GeneratorException("Error writing data file for mapping " + mapping.getExtension().getName()
            + " in source " + mapping.getSource().getName() + ", line " + line, e);
      } finally {
        iter.close();
      }

      // add wrong lines user message
      if (linesWithWrongColumnNumber > 0) {
        String msg = linesWithWrongColumnNumber + " lines with less columns than mapped.";
        logWriter.write(msg + "\n");
        addMessage(Level.INFO, msg);
      }
      // add filter message
      if (recordsFiltered > 0) {
        String msg = recordsFiltered + " lines did not match the filter criteria and were skipped.";
        logWriter.write(msg + "\n");
        addMessage(Level.INFO, msg);
      }
      if (linesWithWrongColumnNumber == 0) {
        logWriter.write("No lines with less columns than mapped\n");
      }
      if (recordsFiltered == 0) {
        logWriter.write("All lines match the filter criteria\n");
      }
      logWriter.flush();
      logWriter.close();

    } catch (IOException e) {
      log.error("Log Generator Error", e);
      e.printStackTrace();
    }

  }

  private void setState(Exception e) {
    exception = e;
    state = STATE.FAILED;
    report();
  }

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
   * @param mapping
   * @param inCols
   * @param in
   * @param record
   */
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
          DataType type = mapping.getExtension().getProperty(pm.getTerm()).getType();
          if (type != null) {
            if (type == DataType.date) {
              // TODO: parse date type with mapping datetime format
            } else if (type == DataType.bool) {
              // TODO: parse date type with mapping boolean format
            } else if (type == DataType.decimal) {
              // normalise punctuation
            }
          }
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

}