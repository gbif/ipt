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
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Getter;
import lombok.Setter;

public class UrlSource extends SourceBase implements RowIterable, SourceWithHeader {

  private static final long serialVersionUID = 5509301114753152587L;
  private static final Logger LOG = LogManager.getLogger(UrlSource.class);

  @Setter
  @Getter
  private URI url;
  @Setter
  @Getter
  private String fieldsTerminatedBy = "\t";
  @Setter
  @Getter
  private String fieldsEnclosedBy;
  private int ignoreHeaderLines = 0;
  @Setter
  @Getter
  private File file; // only for analyzing
  @Setter
  @Getter
  private long fileSize;
  @Setter
  @Getter
  private int rows;
  @Getter
  protected Date lastModified;

  public String getFieldsEnclosedByEscaped() {
    return escape(fieldsEnclosedBy);
  }

  public String getFieldsTerminatedByEscaped() {
    return escape(fieldsTerminatedBy);
  }

  @Override
  public int getIgnoreHeaderLines() {
    return ignoreHeaderLines;
  }

  public String formattedFileSize(String locale) {
    return FileUtils.formatSize(fileSize, 1, locale, false);
  }

  public void setFieldsEnclosedByEscaped(String fieldsEnclosedBy) {
    this.fieldsEnclosedBy = unescape(fieldsEnclosedBy);
  }

  public void setFieldsTerminatedByEscaped(String fieldsTerminatedBy) {
    this.fieldsTerminatedBy = unescape(fieldsTerminatedBy);
  }

  public void setIgnoreHeaderLines(Integer ignoreHeaderLines) {
    this.ignoreHeaderLines = ignoreHeaderLines == null ? 0 : ignoreHeaderLines;
  }

  @Override
  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  @Override
  public SourceType getSourceType() {
    return SourceType.URL;
  }

  public Character getFieldQuoteChar() {
    if (fieldsEnclosedBy == null || fieldsEnclosedBy.isEmpty()) {
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

  public static InputStream decompressInputStream(InputStream inputStream) throws IOException {
    // Increase buffer size to reduce blocking between threads.
    PipedInputStream pipedInputStream = new PipedInputStream(8192);
    PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);

    Thread producerThread = new Thread(() -> {
      try (ZipInputStream zipStream = new ZipInputStream(inputStream);
           PipedOutputStream out = pipedOutputStream) {

        ZipEntry firstEntry = zipStream.getNextEntry();
        if (firstEntry == null) {
          LOG.error("Exception while reading zipped URL source: no entries in the archive");
          throw new IOException("Exception while reading zipped URL source: no entries in the archive");
        }

        LOG.debug("Reading file {} from archive", firstEntry.getName());

        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = zipStream.read(buffer)) != -1) {
          out.write(buffer, 0, bytesRead);
        }

      } catch (IOException e) {
        LOG.error("Exception during decompression", e);
      } finally {
        // Always close the output stream to signal the consumer that writing is complete.
        try {
          pipedOutputStream.close();
          inputStream.close();
        } catch (IOException e) {
          LOG.error("Error closing PipedOutputStream", e);
        }
      }
    });

    producerThread.start();
    return pipedInputStream; // The caller reads from this InputStream
  }

  private CSVReader getReader() throws IOException {
    InputStream input;
    if (url.toString().endsWith("zip")) {
      input = decompressInputStream(url.toURL().openStream());
    } else {
      input = url.toURL().openStream();
    }
    return CSVReaderFactory.build(input, encoding, fieldsTerminatedBy, getFieldQuoteChar(), ignoreHeaderLines);
  }

  public List<String> columns() {
    List<String> columns = new ArrayList<>();

    try (CSVReader reader = getReader()) {
      if (ignoreHeaderLines > 0 && reader.header != null) {
        columns = Arrays.asList(reader.header);
      } else {
        // careful - the reader.header can be null. In this case, set the number of columns to 0
        int numColumns = (reader.header == null) ? 0 : reader.header.length;
        for (int x = 1; x <= numColumns; x++) {
          columns.add("Column #" + x);
        }
      }
    } catch (IOException e) {
      LOG.error("Can't read source {}", getName(), e);
    }

    return columns;
  }

  public Set<Integer> analyze() throws IOException {
    LOG.debug("Analyzing URL source {}", url);
    Set<Integer> emptyLines = new HashSet<>();

    if (!file.exists()) {
      boolean newFileCreated = file.createNewFile();

      if (!newFileCreated) {
        LOG.error("Failed to create new file {}", file);
        setReadable(false);
        return emptyLines;
      }
    }

    try {
      InputStream in = url.toURL().openStream();
      if (url.toString().endsWith("zip")) {
        Files.copy(decompressInputStream(in), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
      } else {
        Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
      }
      setFile(file);
    } catch (IOException e) {
      LOG.error("URL not readable {}. Error: {}", url, e.getMessage());
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
