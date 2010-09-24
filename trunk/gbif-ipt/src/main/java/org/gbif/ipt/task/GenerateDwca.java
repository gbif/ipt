package org.gbif.ipt.task;

import org.gbif.dwc.terms.ConceptTerm;
import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.ArchiveField;
import org.gbif.dwc.text.ArchiveField.DataType;
import org.gbif.dwc.text.ArchiveFile;
import org.gbif.dwc.text.ArchiveWriter;
import org.gbif.file.ClosableIterator;
import org.gbif.file.CompressionUtil;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.manage.SourceManager;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import freemarker.template.TemplateException;

public class GenerateDwca extends ReportingTask implements Callable<Integer> {
  private enum STATE {
    WAITING, STARTED, DATAFILES, METADATA, BUNDLING, COMPLETED
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
  private SourceManager sourceManager;

  @Inject
  public GenerateDwca(@Assisted Resource resource, @Assisted ReportHandler handler, DataDir dataDir, SourceManager sourceManager) {
    super(1000, resource.getShortname(), handler);
    this.resource = resource;
    this.dataDir = dataDir;
    this.sourceManager=sourceManager;
  }

  private void addDataFile(ExtensionMapping mapping, boolean isCore) throws IOException, GeneratorException {
    // update reporting
    currRecords = 0;
    currExtension = mapping.getExtension().getTitle();
    // create new meta.xml with the help of the Archive class
    // create archive file representing this extensions
    ArchiveFile af = ArchiveFile.buildTabFile();
    af.setRowType(mapping.getExtension().getRowType());
    af.setEncoding("utf-8");
    af.setDateFormat("YYYY-MM-DD");
    // in the generated file column 0 will be the id row
    af.setId(buildField(null, 0, null));

    // build list of fields for the new data file and keep it in the archive file
    List<PropertyMapping> newColumns = new ArrayList<PropertyMapping>();
    // get maximum column index to check incoming rows for correctness
    int maxColumnIndex = mapping.getIdColumn() == null ? -1 : mapping.getIdColumn();
    int linesWithWrongColumnNumber = 0;
    for (PropertyMapping f : mapping.getFields()) {
      Integer idx = null;
      if (f.getIndex() != null) {
        newColumns.add(f);
        idx = newColumns.size();
        if (maxColumnIndex < f.getIndex()) {
          maxColumnIndex = f.getIndex();
        }
      }
      ArchiveField f2 = buildField(f.getTerm(), idx, f.getDefaultValue());
      af.addField(f2);
    }

    // create the new data file
    ClosableIterator<String[]> iter = sourceManager.rowIterator(mapping.getSource());
    int rowSize = newColumns.size();
    int line = 0;
    // open new file writer
    String fn = mapping.getExtension().getName().toLowerCase().replaceAll("\\s", "_") + ".txt";
    File dataFile = new File(dwcaFolder, fn);
    Writer writer = org.gbif.file.FileUtils.startNewUtf8File(dataFile);
    try {
      while (iter.hasNext()) {
        line++;
        if (line % 1000 == 0) {
          reportIfNeeded();
        }
        String[] in = iter.next();
        if (in == null || in.length == 0) {
          continue;
        }
        if (in.length <= maxColumnIndex) {
          linesWithWrongColumnNumber++;
          continue;
        }
        String[] row = new String[rowSize + 1];
        // add id column - either an existing column or the line number
        if (mapping.getIdColumn() == null) {
          row[0] = String.valueOf(line);
        } else {
          row[0] = in[mapping.getIdColumn()];
        }
        int idx = 1;
        for (PropertyMapping f : newColumns) {
          String val = in[f.getIndex()];
          // translate value?
          if (f.getTranslation() != null && f.getTranslation().containsKey(val)) {
            val = f.getTranslation().get(val);
          }
          if (f.getType() != null) {
            if (f.getType() == DataType.date) {
              // TODO: parse date type with mapping date format
            } else if (f.getType() == DataType.dateTime) {
              // TODO: parse date type with mapping date format
            } else if (f.getType() == DataType.decimal) {
              // normalise punctuation
            }
          }
          row[idx] = val;
          idx++;
        }
        writer.write(tabRow(row));
        currRecords++;
      }
    } catch (Exception e) {
      // some error writing this file, report
      throw new GeneratorException("Error writing data file for mapping " + mapping.getExtension().getName()
          + " in source " + mapping.getSource().getName() + ", line " + line, e);
    } finally {
      // remember core record number
      if (isCore) {
        coreRecords = currRecords;
      }
      iter.close();
      writer.close();
    }
    // add source file location
    af.addLocation(dataFile.getName());
    // add archive file to archive
    if (isCore) {
      archive.setCore(af);
    } else {
      archive.addExtension(af);
    }

    // add wrong lines user message
    if (linesWithWrongColumnNumber > 0) {
      addMessage(Level.INFO, linesWithWrongColumnNumber + " lines with less columns than mapped.");
    }
    // final reporting
    addMessage(Level.INFO, "Data file written for " + currExtension + " with " + currRecords + " records and "
        + newColumns.size() + " columns");
  }

  private void addEmlFile() throws IOException {
    setState(STATE.METADATA);
    FileUtils.copyFile(dataDir.resourceEmlFile(resource.getShortname(), null), new File(dwcaFolder, "eml.xml"));
    archive.setMetadataLocation("eml.xml");
    // final reporting
    addMessage(Level.INFO, "EML file written");
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
      setState(STATE.STARTED);
      addMessage(Level.INFO, "Archive generation started for resource " + resource.getShortname());
      // create a temp dir to copy all dwca files to
      dwcaFolder = dataDir.tmpDir();
      archive = new Archive();

      // create data files
      createDataFiles();

      // copy eml file
      addEmlFile();

      // create meta.xml
      createMetaFile();

      // zip archive and copy to resource folder
      bundleArchive();

      // final reporting
      addMessage(Level.INFO, "Archive generated successfully!");
      setState(STATE.COMPLETED);

      return coreRecords;

    } catch (Exception e) {
      throw new GeneratorException(e);
    }
  }

  @Override
  protected boolean completed() {
    return STATE.COMPLETED == this.state;
  }

  private void createDataFiles() throws IOException, GeneratorException {
    setState(STATE.DATAFILES);
    if (resource.getCore() == null || resource.getCore().getSource() == null) {
      throw new GeneratorException("Core is not mapped");
    }
    addDataFile(resource.getCore(), true);
    for (ExtensionMapping mapping : resource.getExtensions()) {
      report();
      addDataFile(mapping, false);
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
      default:
        return "You should never see this";
    }
  }

  private void setState(STATE s) {
    state = s;
    report();
  }

  private String tabRow(String[] columns) {
    // escape \t \n \r chars !!!
    for (int i = 0; i < columns.length; i++) {
      if (columns[i] != null) {
        columns[i] = StringUtils.trimToNull(escapeChars.matcher(columns[i]).replaceAll(" "));
      }
    }
    return StringUtils.join(columns, '\t') + "\n";
  }

}
