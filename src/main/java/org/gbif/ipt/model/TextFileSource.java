package org.gbif.ipt.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gbif.ipt.utils.FileUtils;
import org.gbif.utils.file.ClosableReportingIterator;
import org.gbif.utils.file.csv.CSVReader;
import org.gbif.utils.file.csv.CSVReaderFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * A delimited text file based source such as CSV or tab files.
 */
public class TextFileSource extends SourceBase implements FileSource {

  private static final Logger LOG = LogManager.getLogger(TextFileSource.class);
  private static final String SUFFIX = ".txt";

  private String fieldsTerminatedBy = "\t";
  private String fieldsEnclosedBy;
  private int ignoreHeaderLines = 0;
  private File file;
  private long fileSize;
  private int rows;
  protected Date lastModified;

  public Character getFieldQuoteChar() {
    if (fieldsEnclosedBy == null || fieldsEnclosedBy.length() == 0) {
      return null;
    }
    return fieldsEnclosedBy.charAt(0);
  }

  public String getFieldsEnclosedBy() {
    return fieldsEnclosedBy;
  }

  public String getFieldsEnclosedByEscaped() {
    return escape(fieldsEnclosedBy);
  }

  public String getFieldsTerminatedBy() {
    return fieldsTerminatedBy;
  }

  public String getFieldsTerminatedByEscaped() {
    return escape(fieldsTerminatedBy);
  }

  public File getFile() {
    return file;
  }

  public long getFileSize() {
    return fileSize;
  }

  public String getFileSizeFormatted() {
    return FileUtils.formatSize(fileSize, 1);
  }

  public int getIgnoreHeaderLines() {
    return ignoreHeaderLines;
  }

  public Date getLastModified() {
    return lastModified;
  }

  private CSVReader getReader() throws IOException {
    return CSVReaderFactory.build(file, encoding, fieldsTerminatedBy, getFieldQuoteChar(), ignoreHeaderLines);
  }

  public int getRows() {
    return rows;
  }

  public ClosableReportingIterator<String[]> rowIterator() {
    try {
      return getReader();
    } catch (IOException e) {
      LOG.warn("Exception caught", e);
    }
    return null;
  }

  public List<String> columns() {
    try {
      CSVReader reader = getReader();
      if (ignoreHeaderLines > 0) {
        List<String> columns = Arrays.asList(reader.header);
        reader.close();
        return columns;
      } else {
        List<String> columns = new ArrayList<>();
        // careful - the reader.header can be null. In this case set number of columns to 0
        int numColumns = (reader.header == null) ? 0 : reader.header.length;
        for (int x = 1; x <= numColumns; x++) {
          columns.add("Column #" + x);
        }
        reader.close();
        return columns;
      }
    } catch (IOException e) {
      LOG.warn("Cant read source " + getName(), e);
    }

    return new ArrayList<>();
  }

  public void setFieldsEnclosedBy(String fieldsEnclosedBy) {
    this.fieldsEnclosedBy = fieldsEnclosedBy;
  }

  public void setFieldsEnclosedByEscaped(String fieldsEnclosedBy) {
    this.fieldsEnclosedBy = unescape(fieldsEnclosedBy);
  }

  public void setFieldsTerminatedBy(String fieldsTerminatedBy) {
    this.fieldsTerminatedBy = fieldsTerminatedBy;
  }

  public void setFieldsTerminatedByEscaped(String fieldsTerminatedBy) {
    this.fieldsTerminatedBy = unescape(fieldsTerminatedBy);
  }

  public void setFile(File file) {
    this.file = file;
  }

  public void setFileSize(long fileSize) {
    this.fileSize = fileSize;
  }

  public void setIgnoreHeaderLines(Integer ignoreHeaderLines) {
    this.ignoreHeaderLines = ignoreHeaderLines == null ? 0 : ignoreHeaderLines;
  }

  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  public void setRows(int rows) {
    this.rows = rows;
  }

  public String getPreferredFileSuffix() {
    return SUFFIX;
  }

  public Set<Integer> analyze() throws IOException {
    setFileSize(getFile().length());

    CSVReader reader = getReader();
    while (reader.hasNext()) {
      reader.next();
    }
    setColumns(reader.header == null ? 0 : reader.header.length);
    setRows(reader.getReadRows());
    setReadable(true);
    Set<Integer> emptyLines = reader.getEmptyLines();
    reader.close();
    return emptyLines;
  }

  public SourceType getSourceType() {
    return SourceType.TEXT_FILE;
  }
}
