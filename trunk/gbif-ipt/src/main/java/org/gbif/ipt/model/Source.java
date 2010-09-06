/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.ipt.model;

import org.gbif.file.CSVReader;
import org.gbif.ipt.config.JdbcSupport.JdbcInfo;
import org.gbif.ipt.utils.FileUtils;

import static com.google.common.base.Objects.equal;

import com.google.common.base.Objects;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

/**
 * @author markus
 * 
 */
public abstract class Source implements Iterable<String[]>, Comparable<Source> {
  public static class FileSource extends Source {
    private Character fieldsTerminatedBy = '\t';
    private Character fieldsEnclosedBy = CSVReader.NULL_CHAR;
    private int ignoreHeaderLines = 0;
    private File file;
    private long fileSize;
    private int rows;
    protected Date lastModified;

    public Character getFieldsEnclosedBy() {
      return fieldsEnclosedBy;
    }

    public Character getFieldsTerminatedBy() {
      return fieldsTerminatedBy;
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

    public CSVReader getReader() throws IOException {
      return CSVReader.buildReader(this.getFile(), this.getEncoding(), this.getFieldsTerminatedBy(),
          this.getFieldsEnclosedBy(), this.getIgnoreHeaderLines());
    }

    public int getRows() {
      return rows;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<String[]> iterator() {
      CSVReader reader;
      try {
        reader = CSVReader.buildReader(file, encoding, fieldsTerminatedBy, fieldsEnclosedBy, ignoreHeaderLines);
        return reader.iterator();
      } catch (IOException e) {
        e.printStackTrace();
      }
      return null;
    }

    public void setFieldsEnclosedBy(Character fieldsEnclosedBy) {
      this.fieldsEnclosedBy = fieldsEnclosedBy;
    }

    public void setFieldsTerminatedBy(Character fieldsTerminatedBy) {
      this.fieldsTerminatedBy = fieldsTerminatedBy;
    }

    public void setFile(File file) {
      this.file = file;
    }

    public void setFileSize(long fileSize) {
      this.fileSize = fileSize;
    }

    public void setIgnoreHeaderLines(int ignoreHeaderLines) {
      this.ignoreHeaderLines = ignoreHeaderLines;
    }

    public void setLastModified(Date lastModified) {
      this.lastModified = lastModified;
    }

    public void setRows(int rows) {
      this.rows = rows;
    }

  }

  public static class SqlSource extends Source {
    private String sql;
    private JdbcInfo rdbms;
    private String host;
    private String database;
    private String username;
    private Password password = new Password();

    public String getDatabase() {
      return database;
    }

    public String getHost() {
      return host;
    }

    public String getJdbcDriver() {
      return rdbms.getDriver();
    }

    public String getJdbcUrl() {
      return rdbms.getJdbcUrl(this);

    }

    public String getPassword() {
      return password.password;
    }

    public JdbcInfo getRdbms() {
      return rdbms;
    }

    public String getSql() {
      return sql;
    }

    public String getUsername() {
      return username;
    }

    public Iterator<String[]> iterator() {
      throw new NotImplementedException();
    }

    public void setDatabase(String database) {
      this.database = database;
    }

    public void setHost(String host) {
      this.host = host;
    }

    public void setPassword(String password) {
      this.password.password = password;
    }

    public void setRdbms(JdbcInfo rdbms) {
      this.rdbms = rdbms;
    }

    public void setSql(String sql) {
      this.sql = sql;
    }

    public void setUsername(String username) {
      this.username = username;
    }
  }

  protected Resource resource;
  protected String name;
  protected String encoding = "UTF-8";
  protected String dateFormat = "YYYY-MM-DD";
  protected int columns;
  protected boolean readable = false;

  public static String normaliseName(String name) {
    if (name == null) {
      return null;
    }
    return StringUtils.substringBeforeLast(name, ".").replaceAll("[\\s\\c\\W\\.\\:/]+", "").toLowerCase();
  }

  public int compareTo(Source o) {
    if (this == o) {
      return 0;
    }
    if (this.name == null) {
      return -1;
    }
    return name.compareTo(o.name);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!Source.class.isInstance(other)) {
      return false;
    }
    Source o = (Source) other;
    // return equal(resource, o.resource) && equal(name, o.name);
    return equal(name, o.name);
  }

  public int getColumns() {
    return columns;
  }

  public String getDateFormat() {
    return dateFormat;
  }

  public String getEncoding() {
    return encoding;
  }

  public String getName() {
    return name;
  }

  public Resource getResource() {
    return resource;
  }

  @Override
  public int hashCode() {
//    return Objects.hashCode(resource, name);
    return Objects.hashCode(name);
  }

  public boolean isReadable() {
    return readable;
  }

  public void setColumns(int columns) {
    this.columns = columns;
  }

  public void setDateFormat(String dateFormat) {
    this.dateFormat = dateFormat;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public void setName(String name) {
    this.name = normaliseName(name);
  }

  public void setReadable(boolean readable) {
    this.readable = readable;
  }

  public void setResource(Resource resource) {
    this.resource = resource;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "[" + name + ";" + resource + "]";
  }

}
