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
import org.gbif.utils.file.ClosableIterator;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.Source.FileSource;
import org.gbif.ipt.model.Source.SqlSource;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.ImportException;
import org.gbif.ipt.service.SourceException;
import org.gbif.ipt.service.manage.SourceManager;

import com.google.inject.Inject;
import com.google.inject.internal.Nullable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.xwork.StringUtils;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author markus
 * 
 */
public class SourceManagerImpl extends BaseManager implements SourceManager {
  private class FileColumnIterator implements ClosableIterator<Object> {
    private final CSVReader reader;
    private final int column;

    public FileColumnIterator(FileSource source, int column) throws IOException {
      reader = source.getReader();
      this.column = column;
    }

    public void close() {
      if (reader != null) {
        reader.close();
      }
    }

    public boolean hasNext() {
      return reader.hasNext();
    }

    public Object next() {
      String[] row = reader.next();
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
     * @param sql statement to query in the sql source
     * @throws SQLException
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

  @Inject
  public SourceManagerImpl(AppConfig cfg, DataDir dataDir) {
    super(cfg, dataDir);
  }

  public static void copyArchiveFileProperties(ArchiveFile from, FileSource to) {
    to.setEncoding(from.getEncoding());
    to.setFieldsEnclosedBy(from.getFieldsEnclosedBy() == null ? null : from.getFieldsEnclosedBy().toString());
    to.setFieldsTerminatedBy(from.getFieldsTerminatedBy());
    to.setIgnoreHeaderLines(from.getIgnoreHeaderLines());
    to.setDateFormat(from.getDateFormat());
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.ResourceConfigManager#add(org.gbif.ipt.model.ResourceConfiguration, java.io.File)
   */
  public FileSource add(Resource resource, File file, @Nullable String sourceName) throws ImportException {
    return addOneFile(resource, file, sourceName);
  }

  private FileSource addOneFile(Resource resource, File file, @Nullable String sourceName) throws ImportException {
    FileSource src = new FileSource();
    if (sourceName == null) {
      sourceName = file.getName();
    }
    src.setName(sourceName);
    src.setResource(resource);
    log.debug("ADDING SOURCE " + sourceName + " FROM " + file.getAbsolutePath());
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

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.SourceManager#analyze(org.gbif.ipt.model.Source)
   */
  public String analyze(Source source) {
    String problem = null;
    if (source instanceof FileSource) {
      FileSource fs = (FileSource) source;
      try {
    	 CSVReader reader = fs.getReader();
        fs.setFileSize(fs.getFile().length());
        fs.setColumns(reader.header.length);
        while (reader.hasNext()) {
          reader.next();
        }
        fs.setRows(reader.getReadRows());
        fs.setReadable(true);
        
        File logFile= dataDir.sourceLogFile(source.getResource().getShortname(), source.getName()) ;
        FileUtils.deleteQuietly(logFile);
        try {
        	BufferedWriter logWriter = new BufferedWriter(new FileWriter(logFile));
        	logWriter.write("Log for source name:"+ source.getName()+" from resource: "+source.getResource().getShortname()+"\n");
	        if(reader.getEmptyLines().size()>0){
	        	List<Integer> emptyLines=new ArrayList<Integer>(reader.getEmptyLines());
	        	Collections.sort(emptyLines);
	        	for(Integer i: emptyLines){
	        		logWriter.write("Line: "+i+" [EMPTY LINE]\n");
	        	}
        	}else{
        		logWriter.write("No rows were skipped in this source");
        	}
	        logWriter.flush();
	        logWriter.close();
        } catch (IOException e) {
        	log.warn("Cant write source log file " + logFile.getAbsolutePath(), e);
        }
      } catch (IOException e) {
        problem = e.getMessage();
        log.warn("Cant read source file " + fs.getFile().getAbsolutePath(), e);
        fs.setReadable(false);
        fs.setRows(-1);
      }
      
      
    } else {
      SqlSource ss = (SqlSource) source;
      try {
        Connection con = getDbConnection(ss);
        // test sql
        if (StringUtils.trimToNull(ss.getSql()) != null) {
          Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
          stmt.setFetchSize(10);
          ResultSet rs = stmt.executeQuery(ss.getSqlLimited(10));
          // get number of columns
          ResultSetMetaData meta = rs.getMetaData();
          ss.setColumns(meta.getColumnCount());
          ss.setReadable(true);
          rs.close();
          stmt.close();
        } else {

        }
        con.close();
      } catch (SQLException e) {
        log.warn("Cant read sql source " + ss, e);
        problem = e.getMessage();
        ss.setReadable(false);
      }
    }
    return problem;
  }

  private List<String> columns(FileSource source) {
    if (source != null) {
      try {
        CSVReader reader = source.getReader();
        if (source.getIgnoreHeaderLines() > 0) {
          return Arrays.asList(reader.header);
        } else {
          List<String> columns = new ArrayList<String>();
          int x = 1;
          for (String col : reader.header) {
            columns.add("Column #" + x);
            x++;
          }
          return columns;
        }
      } catch (IOException e) {
        log.warn("Cant read source " + source.getName(), e);
      }
    }

    return new ArrayList<String>();
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.SourceManager#columns(org.gbif.ipt.model.Source)
   */
  public List<String> columns(Source source) {
    if (source instanceof FileSource) {
      return columns((FileSource) source);
    }
    return columns((SqlSource) source);
  }

  private List<String> columns(SqlSource source) {
    List<String> columns = new ArrayList<String>();
    try {
      Connection con = getDbConnection(source);
      if (con != null) {
        // test sql
        Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        stmt.setFetchSize(1);
        ResultSet rs = stmt.executeQuery(source.getSqlLimited(1));
        // get column metadata
        ResultSetMetaData meta = rs.getMetaData();
        int idx = 1;
        int max = meta.getColumnCount();
        while (idx <= max) {
          columns.add(meta.getColumnLabel(idx));
          idx++;
        }
        rs.close();
        stmt.close();
        con.close();
      } else {
        String msg = "Can't read sql source, the connection couldn't be created with the current parameters";
        columns.add(msg);
        log.warn(msg + " " + source);
      }
    } catch (SQLException e) {
      log.warn("Cant read sql source " + source, e);
    }
    return columns;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.MappingConfigManager#delete(org.gbif.ipt.model.Source.FileSource)
   */
  public boolean delete(Resource resource, Source source) {
    boolean deleted = false;
    if (source != null) {
      resource.deleteSource(source);
      if (source instanceof FileSource) {
        // also delete source data file
        FileSource fs = (FileSource) source;
        fs.getFile().delete();
      }
      deleted = true;
    }
    return deleted;
  }

  private Connection getDbConnection(SqlSource source) throws SQLException {
    Connection conn = null;
    // try to connect to db via simple JDBC
    if (source.getHost() != null && source.getJdbcUrl() != null && source.getJdbcDriver() != null) {
      try {
        DriverManager.setLoginTimeout(5);
        Class.forName(source.getJdbcDriver());
        conn = DriverManager.getConnection(source.getJdbcUrl(), source.getUsername(), source.getPassword());

        // If a SQLWarning object is available, log its
        // warning(s). There may be multiple warnings chained.

        SQLWarning warn = conn.getWarnings();
        while (warn != null) {
          log.warn("SQLWarning: state=" + warn.getSQLState() + ", message=" + warn.getMessage() + ", vendor=" + warn.getErrorCode());
          warn = warn.getNextWarning();
        }
      } catch (java.lang.ClassNotFoundException e) {
        String msg = String.format("Couldnt load JDBC driver to create new external datasource connection with JDBC Class=%s and URL=%s. Error: %s",
            source.getJdbcDriver(), source.getJdbcUrl(), e.getMessage());
        log.warn(msg, e);
        throw new SQLException(msg);
      } catch (Exception e) {
        String msg = String.format("Couldnt create new external datasource connection with JDBC Class=%s, URL=%s, user=%s. Error: %s", source.getJdbcDriver(),
            source.getJdbcUrl(), source.getUsername(), e.getMessage());
        log.warn(msg, e);
        throw new SQLException(msg);
      }
    }
    return conn;
  }

  public int importArchive(Resource resource, File file, boolean overwriteEml) throws ImportException {
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

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.SourceManager#inspectColumn(org.gbif.ipt.model.Source, int, int)
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
   * @param source
   * @param column
   * @param limit limit for the recordset passed into the sql. If negative or zero no limit will be used
   * @return
   * @throws Exception
   */
  private ClosableIterator<Object> iterSourceColumn(Source source, int column, int limit) throws Exception {
    if (source instanceof FileSource) {
      FileSource src = (FileSource) source;
      return new FileColumnIterator(src, column);
    } else {
      SqlSource src = (SqlSource) source;
      if (limit > 0) {
        return new SqlColumnIterator(src, column, limit);
      } else {
        return new SqlColumnIterator(src, column);
      }
    }
  }

  private List<String[]> peek(FileSource source, int rows) {
    List<String[]> preview = new ArrayList<String[]>();
    if (source != null) {
      try {
        CSVReader reader = source.getReader();
        while (rows > 0 && reader.hasNext()) {
          rows--;
          preview.add(reader.next());
        }
      } catch (IOException e) {
        log.warn("Cant read source " + source.getName(), e);
      }
    }

    return preview;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.SourceManager#peek(org.gbif.ipt.model.Source)
   */
  public List<String[]> peek(Source source, int rows) {
    if (source instanceof FileSource) {
      return peek((FileSource) source, rows);
    }
    return peek((SqlSource) source, rows);
  }

  private List<String[]> peek(SqlSource source, int rows) {
    List<String[]> preview = new ArrayList<String[]>();
    try {
      Connection con = getDbConnection(source);
      if (con != null) {
        // test sql
        Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        stmt.setFetchSize(rows);
        ResultSet rs = stmt.executeQuery(source.getSqlLimited(rows + 1));
        // loop over result
        while (rows > 0 && rs.next()) {
          rows--;
          String[] row = new String[source.getColumns()];
          for (int idx = 0; idx < source.getColumns(); idx++) {
            row[idx] = rs.getString(idx + 1);
          }
          preview.add(row);
        }
        rs.close();
        stmt.close();
        con.close();
      }
    } catch (SQLException e) {
      log.warn("Cant read sql source " + source, e);
    }
    return preview;
  }

  public ClosableIterator<String[]> rowIterator(Source source) throws SourceException {
    if (source == null) {
      return null;
    }
    try {
      if (source instanceof FileSource) {
        return ((FileSource) source).getReader().iterator();
      }
      return new SqlRowIterator((SqlSource) source);
    } catch (Exception e) {
      log.error(e);
      throw new SourceException("Cant build iterator for source " + source.getName() + " :" + e.getMessage());
    }
  }
}
