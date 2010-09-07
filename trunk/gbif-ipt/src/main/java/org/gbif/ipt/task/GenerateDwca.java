package org.gbif.ipt.task;

import org.gbif.dwc.terms.ConceptTerm;
import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.ArchiveField;
import org.gbif.dwc.text.ArchiveField.DataType;
import org.gbif.dwc.text.ArchiveFile;
import org.gbif.dwc.text.ArchiveWriter;
import org.gbif.file.CompressionUtil;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.ExtensionMapping;
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
import java.util.regex.Pattern;

import freemarker.template.TemplateException;

public class GenerateDwca implements ReportingTask<Integer> {
  private static final Pattern escapeChars = Pattern.compile("[\t\n\r]");
  private final Resource resource;
  private final DataDir dataDir;
  private final SourceManager sourceManager;
  private int records = 0;
  private Archive archive;
  private File dwcaFolder;
  private List<TaskMessage> messages = new ArrayList<TaskMessage>();

  @Inject
  public GenerateDwca(@Assisted Resource resource, DataDir dataDir, SourceManager sourceManager) {
    super();
    this.resource = resource;
    this.dataDir = dataDir;
    this.sourceManager = sourceManager;
  }

  private void addDataFile(ExtensionMapping mapping, boolean isCore) throws IOException, GeneratorException {
    // create new meta.xml with the help of the Archive class
    // create archive file representing this extensions
    ArchiveFile af = ArchiveFile.buildTabFile();
    af.setRowType(mapping.getExtension().getRowType());
    af.setEncoding("utf-8");
    af.setDateFormat("YYYY-MM-DD");
    // in the generated file column 0 will be the id row
    af.setId(buildField(null, 0, null));

    // build list of fields for the new data file and keep it in the archive file
    List<ArchiveField> newColumns = new ArrayList<ArchiveField>();
    // get maximum column index to check incoming rows for correctness
    int maxColumnIndex = mapping.getIdColumn() == null ? -1 : mapping.getIdColumn();
    int linesWithWrongColumnNumber = 0;
    for (ArchiveField f : mapping.getFields()) {
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
    Iterator<String[]> iter = mapping.getSource().iterator();
    int rowSize = newColumns.size();
    int line = 0;
    // open new file writer
    File dataFile = new File(dwcaFolder, mapping.getExtension().getName().toLowerCase().replaceAll("\\s", "_") + ".txt");
    Writer writer = org.gbif.file.FileUtils.startNewUtf8File(dataFile);
    try {
      while (iter.hasNext()) {
        line++;
        String[] in = iter.next();
        if (in == null || in.length == 0) {
          continue;
        }
        if (in.length < maxColumnIndex) {
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
        for (ArchiveField f : newColumns) {
          String val = in[f.getIndex()];
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
        if (isCore) {
          records++;
        }
      }
    } catch (Exception e) {
      // some error writing this file, report
      throw new GeneratorException("Error writing data file for mapping " + mapping.getExtension().getName()
          + " in source " + mapping.getSource().getName() + ", line " + line, e);
    } finally {
      writer.close();
    }
    // add wrong lines user message
    if (linesWithWrongColumnNumber > 0) {
      messages.add(new TaskMessage(Level.INFO, +linesWithWrongColumnNumber + " lines with less columns than mapped."));
    }
    // add source file location
    af.addLocation(dataFile.getName());
    // add archive file to archive
    if (isCore) {
      archive.setCore(af);
    } else {
      archive.addExtension(af);
    }
  }

  private void addEmlFile() throws IOException {
    FileUtils.copyFile(dataDir.resourceEmlFile(resource.getShortname(), null), new File(dwcaFolder, "eml.xml"));
    archive.setMetadataLocation("eml.xml");
  }

  private ArchiveField buildField(ConceptTerm term, Integer column, String defaultValue) {
    ArchiveField f = new ArchiveField();
    f.setTerm(term);
    f.setIndex(column);
    f.setDefaultValue(defaultValue);
    return f;
  }

  private void bundleArchive() throws IOException {
    // create zip
    File zip = dataDir.tmpFile("dwca", ".zip");
    CompressionUtil.zipDir(dwcaFolder, zip);
    // move to data dir
    File target = dataDir.resourceDwcaFile(resource.getShortname());
    if (target.exists()) {
      target.delete();
    }
    FileUtils.moveFile(zip, target);
  }

  public Integer call() throws Exception {
    try {
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
      // return messages for UI
      return records;
    } catch (Exception e) {
      throw new GeneratorException(e);
    }
  }

  private void createDataFiles() throws IOException, GeneratorException {
    if (resource.getCore() == null || resource.getCore().getSource() == null) {
      throw new GeneratorException("Core is not mapped");
    }
    addDataFile(resource.getCore(), true);
    for (ExtensionMapping mapping : resource.getExtensions()) {
      addDataFile(mapping, false);
    }
  }

  private void createMetaFile() throws IOException, TemplateException {
    ArchiveWriter writer = new ArchiveWriter();
    writer.writeMetaFile(new File(dwcaFolder, "meta.xml"), archive);
  }

  public int getRecords() {
    return records;
  }

  public List<TaskMessage> messages() {
    return messages;
  }

  public String state() {
    return "Processing record " + records;
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
