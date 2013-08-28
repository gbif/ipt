/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
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
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.ExcelFileSource;
import org.gbif.ipt.model.FileSource;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.SqlSource;
import org.gbif.ipt.model.TextFileSource;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.ImportException;
import org.gbif.ipt.service.SourceException;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.utils.file.ClosableIterator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.xwork.StringUtils;

public class SourceManagerImpl extends BaseManager implements SourceManager {

  private class ColumnIterator implements ClosableIterator<Object> {

    private final ClosableIterator<String[]> rows;
    private final int column;

    public ColumnIterator(FileSource source, int column) throws IOException {
      rows = source.rowIterator();
      this.column = column;
    }

    public void close() {
      rows.close();
    }

    public boolean hasNext() {
      return rows.hasNext();
    }

    public Object next() {
      String[] row = rows.next();
      if (row == null || row.length < column) {
        return null;
      }
      return row[column];
    }

    public void remove() {
      // unsupported
    }
  }

  private class SqlColumnIterator implements ClosableIterator<Object> {

    private final Connection conn;
    private final Statement stmt;
    private final ResultSet rs;
    private final int column;
    private boolean hasNext;
    private final String sourceName;

    public SqlColumnIterator(SqlSource source, int column) throws SQLException {
      this(source, column, source.getSql());
    }

    public SqlColumnIterator(SqlSource source, int column, int limit) throws SQLException {
      this(source, column, source.getSqlLimited(limit));
    }

    /**
     * SqlColumnIterator constructor
     *
     * @param source of the sql data
     * @param column to inspect, zero based numbering as used in the dwc archives
     * @param sql    statement to query in the sql source
     */
    private SqlColumnIterator(SqlSource source, int column, String sql) throws SQLException {
      this.conn = getDbConnection(source);
      this.stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
      source.getRdbms().enableLargeResultSet(this.stmt);
      this.column = column + 1;
      this.rs = stmt.executeQuery(sql);
      this.hasNext = rs.next();
      sourceName = source.getName();
    }

    public void close() {
      if (rs != null) {
        try {
          rs.close();
          stmt.close();
          conn.close();
        } catch (SQLException e) {
          log.error("Cant close iterator for sql source " + sourceName, e);
        }
      }

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

  private class SqlRowIterator implements ClosableIterator<String[]> {

    private final Connection conn;
    private final Statement stmt;
    private final ResultSet rs;
    private boolean hasNext;
    private final String sourceName;
    private final int rowSize;

    SqlRowIterator(SqlSource source) throws SQLException {
      this.conn = getDbConnection(source);
      this.stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
      source.getRdbms().enableLargeResultSet(this.stmt);
      this.rs = stmt.executeQuery(source.getSql());
      this.rowSize = rs.getMetaData().getColumnCount();
      this.hasNext = rs.next();
      sourceName = source.getName();
    }

    public void close() {
      if (rs != null) {
        try {
          rs.close();
          stmt.close();
          conn.close();
        } catch (SQLException e) {
          log.error("Cant close iterator for sql source " + sourceName, e);
        }
      }

    }

    public boolean hasNext() {
      return hasNext;
    }

    public String[] next() {
      String[] val = new String[rowSize];
      if (hasNext) {
        try {
          for (int i = 1; i <= rowSize; i++) {
            val[i - 1] = rs.getString(i);
          }
          // forward rs cursor
          hasNext = rs.next();
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

  // default fetch sized used in SQL statements
  private static final int FETCH_SIZE = 10;
  // the maximum time in seconds that a driver will wait while attempting to connect to a database
  private static final int CONNECTION_TIMEOUT_SECS = 5;

  @Inject
  public SourceManagerImpl(AppConfig cfg, DataDir dataDir) {
    super(cfg, dataDir);
  }

  public static void copyArchiveFileProperties(ArchiveFile from, TextFileSource to) {
    to.setEncoding(from.getEncoding());
    to.setFieldsEnclosedBy(from.getFieldsEnclosedBy() == null ? null : from.getFieldsEnclosedBy().toString());
    to.setFieldsTerminatedBy(from.getFieldsTerminatedBy());
    to.setIgnoreHeaderLines(from.getIgnoreHeaderLines());
    to.setDateFormat(from.getDateFormat());
  }

  private ExcelFileSource addExcelFile(File file, String fileName) throws ImportException {
    ExcelFileSource src = new ExcelFileSource();
    //TODO: encoding, header rows, date format?
    src.setSheetIdx(0);
    return src;
  }

  private TextFileSource addTextFile(File file, String fileName) throws ImportException {
    TextFileSource src = new TextFileSource();
    try {
      // anaylze individual files using the dwca reader
      Archive arch = ArchiveFactory.openArchive(file);
      copyArchiveFileProperties(arch.getCore(), src);
    } catch (IOException e) {
      log.warn(e.getMessage());
      throw new ImportException(e);
    } catch (UnsupportedArchiveException e) {
      // fine, cant read it with dwca library, but might still be a valid file for manual setup
      log.warn(e.getMessage());
    }
    return src;
  }

  public FileSource add(Resource resource, File file, String fileName) throws ImportException {
    log.debug("ADDING SOURCE " + fileName + " FROM " + file.getAbsolutePath());

    FileSource src;
    String suffix = FilenameUtils.getExtension(fileName);
    if (suffix != null && (suffix.equalsIgnoreCase("xls") || suffix.equalsIgnoreCase("xlsx"))) {
      src = addExcelFile(file, fileName);
    } else {
      src = addTextFile(file, fileName);
    }

    src.setName(fileName);
    src.setResource(resource);

    try {
      // copy file
      File ddFile = dataDir.sourceFile(resource, src);
      try {
        FileUtils.copyFile(file, ddFile);
      } catch (IOException e1) {
        throw new ImportException(e1);
      }
      src.setFile(ddFile);
      src.setLastModified(new Date());

      // add to resource, allow overwriting existing ones
      // if the file is uploaded not for the first time
      resource.addSource(src, true);
    } catch (AlreadyExistingException e) {
      throw new ImportException(e);
    }

    // analyze file
    analyze(src);
    return src;
  }

  public String analyze(Source source) {
    if (source instanceof SqlSource) {
      return analyze((SqlSource) source);

    } else {
      return analyze((FileSource) source);
    }
  }

  private String analyze(SqlSource ss) {
    String problem = null;
    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;
    try {
      con = getDbConnection(ss);
      // test sql
      if (StringUtils.trimToNull(ss.getSql()) != null) {
        stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        stmt.setFetchSize(FETCH_SIZE);
        rs = stmt.executeQuery(ss.getSqlLimited(FETCH_SIZE));
        // get number of columns
        ResultSetMetaData meta = rs.getMetaData();
        ss.setColumns(meta.getColumnCount());
        ss.setReadable(true);
      }
    } catch (SQLException e) {
      log.warn("Cant read sql source " + ss, e);
      problem = e.getMessage();
      ss.setReadable(false);
    } finally {
      // close result set, statement, and connection in that order
      if (rs != null) {
        try {
          rs.close();
        } catch (SQLException e) {
          log.error("ResultSet could not be closed: " + e.getMessage(), e);
        }
      }
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e) {
          log.error("Statement could not be closed: " + e.getMessage(), e);
        }
      }
      if (con != null) {
        try {
          con.close();
        } catch (SQLException e) {
          log.error("Connection could not be closed: " + e.getMessage(), e);
        }
      }
    }
    return problem;
  }

  private String analyze(FileSource src) {
    BufferedWriter logWriter = null;
    File logFile = dataDir.sourceLogFile(src.getResource().getShortname(), src.getName());
    try {
      FileUtils.deleteQuietly(logFile);

      Set<Integer> emptyLines;
      try{
        emptyLines = src.analyze();
      } catch (IOException e) {
        return e.getMessage();
      }

      logWriter = new BufferedWriter(new FileWriter(logFile));
      logWriter.write(
        "Log for source name:" + src.getName() + " from resource: " + src.getResource().getShortname() + "\n");
      if (!emptyLines.isEmpty()) {
        for (Integer i : Ordering.natural().sortedCopy(emptyLines)) {
          logWriter.write("Line: " + i + " [EMPTY LINE]\n");
        }
      } else {
        logWriter.write("No rows were skipped in this source");
      }


      logWriter.flush();

    } catch (IOException e) {
      log.warn("Cant write source log file " + logFile.getAbsolutePath(), e);
    } finally {
      if (logWriter != null) {
        IOUtils.closeQuietly(logWriter);
      }
    }

    return null;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.SourceManager#columns(org.gbif.ipt.model.SourceBase)
   */
  public List<String> columns(Source source) {
    if (source == null) {
      return Lists.newArrayList();
    }

    if (source instanceof SqlSource) {
      return columns((SqlSource) source);
    }
    return ((FileSource) source).columns();
  }

  private List<String> columns(SqlSource source) {
    List<String> columns = new ArrayList<String>();
    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;
    try {
      con = getDbConnection(source);
      if (con != null) {
        // test sql
        stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        stmt.setFetchSize(1);
        rs = stmt.executeQuery(source.getSqlLimited(1));
        // get column metadata
        ResultSetMetaData meta = rs.getMetaData();
        int idx = 1;
        int max = meta.getColumnCount();
        while (idx <= max) {
          columns.add(meta.getColumnLabel(idx));
          idx++;
        }
      } else {
        String msg = "Can't read sql source, the connection couldn't be created with the current parameters";
        columns.add(msg);
        log.warn(msg + " " + source);
      }
    } catch (SQLException e) {
      log.warn("Cant read sql source " + source, e);
    } finally {
      // close result set, statement, and connection in that order
      if (rs != null) {
        try {
          rs.close();
        } catch (SQLException e) {
          log.error("ResultSet could not be closed: " + e.getMessage(), e);
        }
      }
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e) {
          log.error("Statement could not be closed: " + e.getMessage(), e);
        }
      }
      if (con != null) {
        try {
          con.close();
        } catch (SQLException e) {
          log.error("Connection could not be closed: " + e.getMessage(), e);
        }
      }
    }
    return columns;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.MappingConfigManager#delete(org.gbif.ipt.model.SourceBase.TextFileSource)
   */
  public boolean delete(Resource resource, Source source) {
    if (source == null) {
      return false;
    }

    resource.deleteSource(source);
    if (source instanceof TextFileSource) {
      // also delete source data file
      TextFileSource fs = (TextFileSource) source;
      fs.getFile().delete();
    }
    if (source instanceof ExcelFileSource) {
      // also delete source data file if no further source uses it
      ExcelFileSource es = (ExcelFileSource) source;
      boolean del = true;
      for (Source src : resource.getSources()) {
        if (!src.equals(es) && src.isExcelSource() && ((ExcelFileSource) src).getFile().equals(es.getFile())) {
          // another excel source using the same file, dont delete
          del = false;
          break;
        }
      }
      if (del) {
        es.getFile().delete();
      }
    }
    return true;
  }

  private Connection getDbConnection(SqlSource source) throws SQLException {
    Connection conn = null;
    // try to connect to db via simple JDBC
    if (source.getHost() != null && source.getJdbcUrl() != null && source.getJdbcDriver() != null) {
      try {
        DriverManager.setLoginTimeout(CONNECTION_TIMEOUT_SECS);
        Class.forName(source.getJdbcDriver());
        conn = DriverManager.getConnection(source.getJdbcUrl(), source.getUsername(), source.getPassword());

        // If a SQLWarning object is available, log its
        // warning(s). There may be multiple warnings chained.

        SQLWarning warn = conn.getWarnings();
        while (warn != null) {
          log.warn("SQLWarning: state=" + warn.getSQLState() + ", message=" + warn.getMessage() + ", vendor=" + warn
            .getErrorCode());
          warn = warn.getNextWarning();
        }
      } catch (java.lang.ClassNotFoundException e) {
        String msg = String.format(
          "Couldnt load JDBC driver to create new external datasource connection with JDBC Class=%s and URL=%s. Error: %s",
          source.getJdbcDriver(), source.getJdbcUrl(), e.getMessage());
        log.warn(msg, e);
        throw new SQLException(msg);
      } catch (Exception e) {
        String msg = String
          .format("Couldnt create new external datasource connection with JDBC Class=%s, URL=%s, user=%s. Error: %s",
            source.getJdbcDriver(), source.getJdbcUrl(), source.getUsername(), e.getMessage());
        log.warn(msg, e);
        throw new SQLException(msg);
      }
    }
    return conn;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.SourceManager#inspectColumn(org.gbif.ipt.model.SourceBase, int, int)
   */
  public Set<String> inspectColumn(Source source, int column, int maxValues, int maxRows) throws SourceException {
    Set<String> values = new HashSet<String>();
    ClosableIterator<Object> iter = null;
    try {
      iter = iterSourceColumn(source, column, maxRows);
      // get distinct values
      while (iter.hasNext() && (maxValues < 1 || values.size() < maxValues)) {
        Object obj = iter.next();
        if (obj != null) {
          String val = obj.toString();
          values.add(val);
        }
      }
    } catch (Exception e) {
      log.error(e);
      throw new SourceException("Error reading source " + source.getName() + ": " + e.getMessage());
    } finally {
      if (iter != null) {
        iter.close();
      }
    }
    return values;
  }

  /**
   * @param limit limit for the recordset passed into the sql. If negative or zero no limit will be used
   */
  private ClosableIterator<Object> iterSourceColumn(Source source, int column, int limit) throws Exception {
    if (source instanceof SqlSource) {
      SqlSource src = (SqlSource) source;
      if (limit > 0) {
        return new SqlColumnIterator(src, column, limit);
      } else {
        return new SqlColumnIterator(src, column);
      }

    } else {
      return new ColumnIterator((FileSource) source, column);
    }
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.SourceManager#peek(org.gbif.ipt.model.SourceBase)
   */
  public List<String[]> peek(Source source, int rows) {
    if (source instanceof SqlSource) {
      return peek((SqlSource) source, rows);
    }
    // both excel and file implement FileSource
    return peek((FileSource) source, rows);
  }

  private List<String[]> peek(FileSource source, int rows) {
    List<String[]> preview = Lists.newArrayList();
    if (source != null) {
      try {
        Iterator<String[]> iter = source.rowIterator();
        while (rows > 0 && iter.hasNext()) {
          rows--;
          preview.add(iter.next());
        }
      } catch (Exception e) {
        log.warn("Cant peek into source " + source.getName(), e);
      }
    }

    return preview;
  }

  private List<String[]> peek(SqlSource source, int rows) {
    List<String[]> preview = new ArrayList<String[]>();
    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;
    try {
      con = getDbConnection(source);
      if (con != null) {
        // test sql
        stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        stmt.setFetchSize(rows);
        rs = stmt.executeQuery(source.getSqlLimited(rows + 1));
        // loop over result
        while (rows > 0 && rs.next()) {
          rows--;
          String[] row = new String[source.getColumns()];
          for (int idx = 0; idx < source.getColumns(); idx++) {
            row[idx] = rs.getString(idx + 1);
          }
          preview.add(row);
        }
      }
    } catch (SQLException e) {
      log.warn("Cant read sql source " + source, e);
    } finally {
      // close result set, statement, and connection in that order
      if (rs != null) {
        try {
          rs.close();
        } catch (SQLException e) {
          log.error("ResultSet could not be closed: " + e.getMessage(), e);
        }
      }
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e) {
          log.error("Statement could not be closed: " + e.getMessage(), e);
        }
      }
      if (con != null) {
        try {
          con.close();
        } catch (SQLException e) {
          log.error("Connection could not be closed: " + e.getMessage(), e);
        }
      }
    }
    return preview;
  }

  public ClosableIterator<String[]> rowIterator(Source source) throws SourceException {
    if (source == null) {
      return null;
    }
    try {
      if (source instanceof SqlSource) {
        return new SqlRowIterator((SqlSource) source);
      }
    // both excel and file implement FileSource
      return ((FileSource) source).rowIterator();

    } catch (Exception e) {
      log.error(e);
      throw new SourceException("Cant build iterator for source " + source.getName() + " :" + e.getMessage());
    }
  }
}
