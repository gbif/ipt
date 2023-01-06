/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.ipt.model;

import org.gbif.ipt.utils.FileUtils;
import org.gbif.utils.file.ClosableReportingIterator;
import org.gbif.utils.file.csv.CSVReader;
import org.gbif.utils.file.csv.CSVReaderFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UrlSource extends SourceBase implements RowIterable, SourceWithHeader {

  private static final Logger LOG = LogManager.getLogger(UrlSource.class);

  private URI url;

  private String fieldsTerminatedBy = "\t";
  private String fieldsEnclosedBy;
  private int ignoreHeaderLines = 0;
  private File file; // only for analyzing
  private long fileSize;
  private int rows;
  protected Date lastModified;

  public URI getUrl() {
    return url;
  }

  public void setUrl(URI url) {
    this.url = url;
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

  @Override
  public int getIgnoreHeaderLines() {
    return ignoreHeaderLines;
  }

  public File getFile() {
    return file;
  }

  public long getFileSize() {
    return fileSize;
  }

  public String getFileSizeFormatted() {
    return FileUtils.formatSize(fileSize, 1, true);
  }

  public int getRows() {
    return rows;
  }

  public Date getLastModified() {
    return lastModified;
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

  @Override
  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  public void setRows(int rows) {
    this.rows = rows;
  }

  @Override
  public SourceType getSourceType() {
    return SourceType.URL;
  }

  public Character getFieldQuoteChar() {
    if (fieldsEnclosedBy == null || fieldsEnclosedBy.length() == 0) {
      return null;
    }
    return fieldsEnclosedBy.charAt(0);
  }

  @Override
  public ClosableReportingIterator<String[]> rowIterator() {
    try {
      return getReader();
    } catch (IOException e) {
      LOG.warn("Exception caught", e);
    }
    return null;
  }

  private CSVReader getReader() throws IOException {
    InputStream input = url.toURL().openStream();
    return CSVReaderFactory.build(input, encoding, fieldsTerminatedBy, getFieldQuoteChar(), ignoreHeaderLines);
  }

  public List<String> columns() {
    List<String> columns = new ArrayList<>();

    try (CSVReader reader = getReader()) {
      if (ignoreHeaderLines > 0) {
        columns = Arrays.asList(reader.header);
      } else {
        // careful - the reader.header can be null. In this case set number of columns to 0
        int numColumns = (reader.header == null) ? 0 : reader.header.length;
        for (int x = 1; x <= numColumns; x++) {
          columns.add("Column #" + x);
        }
      }
    } catch (IOException e) {
      LOG.warn("Can't read source " + getName(), e);
    }

    return columns;
  }

  public Set<Integer> analyze() throws IOException {
    LOG.debug("Analyzing URL source {}", url);
    Set<Integer> emptyLines = new HashSet<>();

    try (InputStream in = url.toURL().openStream()) {
      Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
      setFile(file);
    } catch (IOException e) {
      LOG.error("URL not readable {}", url);
      setReadable(false);
      return emptyLines;
    }

    setFileSize(file.length());

    CSVReader reader = CSVReaderFactory.build(file, getEncoding(), getFieldsTerminatedBy(), getFieldQuoteChar(), getIgnoreHeaderLines());

    while (reader.hasNext()) {
      reader.next();
    }
    setColumns(reader.header == null ? 0 : reader.header.length);
    setRows(reader.getReadRows());
    setReadable(true);
    emptyLines = reader.getEmptyLines();
    reader.close();
    return emptyLines;
  }
}
