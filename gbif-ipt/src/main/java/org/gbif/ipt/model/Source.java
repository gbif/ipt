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

import org.apache.commons.lang.NotImplementedException;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

/**
 * @author markus
 * 
 */
public abstract class Source implements Iterable<String[]> {
  public static class FileSource extends Source {
    private char fieldsTerminatedBy = '\t';
    private char fieldsEnclosedBy;
    private char linesTerminatedBy = '\n';
    private int ignoreHeaderLines = 0;
    private File file;

    public char getFieldsEnclosedBy() {
      return fieldsEnclosedBy;
    }

    public char getFieldsTerminatedBy() {
      return fieldsTerminatedBy;
    }

    public File getFile() {
      return file;
    }

    public int getIgnoreHeaderLines() {
      return ignoreHeaderLines;
    }

    public char getLinesTerminatedBy() {
      return linesTerminatedBy;
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

    public void setFieldsEnclosedBy(char fieldsEnclosedBy) {
      this.fieldsEnclosedBy = fieldsEnclosedBy;
    }

    public void setFieldsTerminatedBy(char fieldsTerminatedBy) {
      this.fieldsTerminatedBy = fieldsTerminatedBy;
    }

    public void setFile(File file) {
      this.file = file;
    }

    public void setIgnoreHeaderLines(int ignoreHeaderLines) {
      this.ignoreHeaderLines = ignoreHeaderLines;
    }

    public void setLinesTerminatedBy(char linesTerminatedBy) {
      this.linesTerminatedBy = linesTerminatedBy;
    }
  }

  public static class SqlSource extends Source {
    private String sql;
    private String host;
    private String database;
    private String username;
    private String password;

    public String getDatabase() {
      return database;
    }

    public String getHost() {
      return host;
    }

    public String getPassword() {
      return password;
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
      this.password = password;
    }

    public void setSql(String sql) {
      this.sql = sql;
    }

    public void setUsername(String username) {
      this.username = username;
    }
  }

  protected Resource resource;
  protected String title;
  protected String encoding = "UTF-8";
  protected String dateFormat = "YYYY-MM-DD";
  protected Date lastModified;

  public String getDateFormat() {
    return dateFormat;
  }

  public String getEncoding() {
    return encoding;
  }

  public Date getLastModified() {
    return lastModified;
  }

  public Resource getResource() {
    return resource;
  }

  public String getTitle() {
    return title;
  }

  public void setDateFormat(String dateFormat) {
    this.dateFormat = dateFormat;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  public void setResource(Resource resource) {
    this.resource = resource;
  }

  public void setTitle(String title) {
    this.title = title;
  }

}
