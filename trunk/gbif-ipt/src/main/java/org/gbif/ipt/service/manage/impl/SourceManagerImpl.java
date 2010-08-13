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

package org.gbif.ipt.service.manage.impl;

import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.ArchiveFactory;
import org.gbif.dwc.text.ArchiveFile;
import org.gbif.dwc.text.UnsupportedArchiveException;
import org.gbif.file.CSVReader;
import org.gbif.ipt.model.ResourceConfiguration;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.Source.FileSource;
import org.gbif.ipt.model.Source.SqlSource;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.ImportException;
import org.gbif.ipt.service.manage.SourceManager;

import com.google.inject.Inject;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author markus
 * 
 */
public class SourceManagerImpl extends BaseManager implements SourceManager {

  private class FileIterator implements Iterator<Object> {
    private final CSVReader reader;
    private final int column;

    public FileIterator(FileSource source, int column) throws IOException {
      reader = source.getReader();
      this.column = column;
    }

    public boolean hasNext() {
      return reader.hasNext();
    }

    public Object next() {
      return reader.next()[column];
    }

    public void remove() {
      // unsupported
    }
  }

  private class SqlIterator implements Iterator<Object> {
    private final Statement stmt;
    private final ResultSet rs;
    private final int column;
    private boolean hasNext;

    public SqlIterator(SqlSource source, int column) throws SQLException {
      Connection conn = getDbConnection(source);
      this.stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
      this.stmt.setFetchSize(100);
      this.rs = stmt.executeQuery(source.getSql());
      this.column = column;
      this.hasNext = rs.next();
    }

    public boolean hasNext() {
      return hasNext;
    }

    public Object next() {
      String val = null;
      if (hasNext) {
        try {
          // forward rs cursor
          hasNext = rs.next();
          val = rs.getString(column);
        } catch (SQLException e2) {
          hasNext = false;
        }
      }
      return val;
    }

    public void remove() {
      // unsupported
    }
  }

  @Inject
  public SourceManagerImpl() {
    super();
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.ResourceConfigManager#add(org.gbif.ipt.model.ResourceConfiguration, java.io.File)
   */
  public FileSource add(ResourceConfiguration config, File file) throws ImportException {
    return addOneFile(config, file);
  }

  private FileSource addOneFile(ResourceConfiguration config, File file) throws ImportException {
    FileSource src = new FileSource();
    src.setResource(config.getResource());
    // copy file
    File ddFile = dataDir.sourceFile(config.getResource(), src);
    try {
      FileUtils.copyFile(file, ddFile);
    } catch (IOException e1) {
      throw new ImportException(e1);
    }
    src.setFile(ddFile);
    src.setLastModified(new Date());
    // add to config
    config.getSources().add(src);
    try {
      // anaylze individual files using the dwca reader
      Archive arch = ArchiveFactory.openArchive(file);
      copyArchiveFileProperties(arch.getCore(), src);
    } catch (UnsupportedArchiveException e) {
      log.warn(e.getMessage(), e);
      // throw new ImportException(e);
    } catch (IOException e) {
      log.warn(e.getMessage(), e);
    }
    // analyze file
    analyze(src);
    return src;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.SourceManager#analyze(org.gbif.ipt.model.Source)
   */
  public void analyze(Source source) {
    if (source instanceof FileSource) {
      FileSource fs = (FileSource) source;
      try {
        fs.setFileSize(fs.getFile().length());
        CSVReader reader = fs.getReader();
        fs.setColumns(reader.getHeader().length);
        int rows = 0;
        while (reader.hasNext()) {
          reader.next();
          rows++;
        }
        fs.setRows(rows);
        fs.setReadable(true);
      } catch (IOException e) {
        log.warn("Cant read source file " + fs.getFile().getAbsolutePath(), e);
        fs.setReadable(false);
        fs.setRows(-1);
      }
    } else {
      SqlSource ss = (SqlSource) source;
    }

  }

  private void copyArchiveFileProperties(ArchiveFile from, FileSource to) {
    to.setEncoding(from.getEncoding());
    to.setFieldsEnclosedBy(from.getFieldsEnclosedBy());
    to.setFieldsTerminatedBy(from.getFieldsTerminatedBy());
    to.setIgnoreHeaderLines(from.getIgnoreHeaderLines());
    to.setLinesTerminatedBy(from.getLinesTerminatedBy());
    to.setName(from.getTitle());
    to.setDateFormat(from.getDateFormat());
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.MappingConfigManager#delete(org.gbif.ipt.model.Source.FileSource)
   */
  public void delete(ResourceConfiguration config, Source source) {
    // TODO Auto-generated method stub

  }

  public List<String> getAllTables(SqlSource source) throws SQLException {
    Connection conn = getDbConnection(source);
    DatabaseMetaData dbmd = conn.getMetaData();
    List<String> tableNames = new ArrayList<String>();
    ResultSet rs = dbmd.getTables(null, null, null, new String[]{"TABLE"});
    while (rs.next()) {
      tableNames.add((String) rs.getObject(3));
    }
    conn.close();
    return tableNames;
  }

  private Connection getDbConnection(SqlSource source) throws SQLException {
    Connection conn = null;
    // try to connect to db via simple JDBC
    if (source.getJdbcUrl() != null && source.getJdbcDriver() != null) {
      try {
        Class.forName(source.getJdbcDriver());
        conn = DriverManager.getConnection(source.getJdbcUrl(), source.getUsername(), source.getPassword());

        // If a SQLWarning object is available, print its
        // warning(s). There may be multiple warnings chained.

        SQLWarning warn = conn.getWarnings();
        while (warn != null) {
          System.out.println("SQLState: " + warn.getSQLState());
          System.out.println("Message:  " + warn.getMessage());
          System.out.println("Vendor:   " + warn.getErrorCode());
          System.out.println("");
          warn = warn.getNextWarning();
        }
      } catch (java.lang.ClassNotFoundException e) {
        String msg = String.format(
            "Couldnt load JDBC driver to create new external datasource connection with JDBC Class=%s and URL=%s. Error: %s",
            source.getJdbcDriver(), source.getJdbcUrl(), e.getMessage());
        log.warn(msg, e);
        throw new SQLException(msg);
      } catch (Exception e) {
        String msg = String.format(
            "Couldnt create new external datasource connection with JDBC Class=%s, URL=%s, user=%s. Error: %s",
            source.getJdbcDriver(), source.getJdbcUrl(), source.getUsername(), e.getMessage());
        log.warn(msg, e);
        throw new SQLException(msg);
      }
    }
    return conn;
  }

  public Set<String> getDistinctValues(Source source, int column) throws Exception {
    // iterate through entire source and store distinct terms in memory (scary)
    Set<String> terms = new HashSet<String>();
    Iterator iter = iterSourceColumn(source, column);
    while (iter.hasNext()) {
      String term = iter.next().toString();
      terms.add(term);
    }
    return terms;
  }

  public int importArchive(ResourceConfiguration config, File file, boolean overwriteEml) throws ImportException {
    // anaylze using the dwca reader
    try {
      Archive arch = ArchiveFactory.openArchive(file);
      return 0;
    } catch (UnsupportedArchiveException e) {
      throw new ImportException(e);
    } catch (IOException e) {
      throw new ImportException(e);
    }
  }

  private Iterator<Object> iterSourceColumn(Source source, int column) throws Exception {
    if (source instanceof FileSource) {
      FileSource src = (FileSource) source;
      return new FileIterator(src, column);
    } else {
      SqlSource src = (SqlSource) source;
      return new SqlIterator(src, column);
    }
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.SourceManager#peek(org.gbif.ipt.model.Source)
   */
  public List<String[]> peek(Source source) {
    // TODO Auto-generated method stub
    return new ArrayList<String[]>();
  }
}
