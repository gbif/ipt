/*
 * Copyright 2009 GBIF.
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
 */
package org.gbif.provider.service.impl;

import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.SourceBase;
import org.gbif.provider.model.SourceFile;
import org.gbif.provider.model.SourceSql;
import org.gbif.provider.service.SourceInspectionManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.MalformedTabFileException;
import org.gbif.provider.util.TabFileReader;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

/**
 * TODO: Documentation.
 * 
 */
public class SourceInspectionManagerImpl implements SourceInspectionManager {
  private class FileIterator implements Iterator<Object> {
    private final TabFileReader reader;
    private final int columnIdx;

    public FileIterator(File source, int columnIdx, boolean returnHeaderRow)
        throws IOException, MalformedTabFileException {
      reader = new TabFileReader(source, returnHeaderRow);
      this.columnIdx = columnIdx;
    }

    public boolean hasNext() {
      return reader.hasNext();
    }

    public Object next() {
      return reader.next()[columnIdx];
    }

    public void remove() {
      // unsupported
    }
  }
  private class SqlIterator implements Iterator<Object> {
    private final Statement stmt;
    private final ResultSet rs;
    private final String column;
    private boolean hasNext;

    public SqlIterator(SourceSql source, String column) throws SQLException {
      Connection conn = getResourceConnection(source.getResource());
      this.stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
          ResultSet.CONCUR_READ_ONLY);
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

  private static final int PREVIEW_SIZE = 5;

  @Autowired
  private AppConfig cfg;

  public List<String> getAllTables(DataResource resource) throws SQLException {
    Connection conn = getResourceConnection(resource);
    DatabaseMetaData dbmd = conn.getMetaData();
    List<String> tableNames = new ArrayList<String>();
    ResultSet rs = dbmd.getTables(null, null, null, new String[] {"TABLE"});
    while (rs.next()) {
      tableNames.add((String) rs.getObject(3));
    }
    return tableNames;
  }

  public Set<String> getDistinctValues(SourceBase source, String column)
      throws Exception {
    if (source == null || column == null) {
      throw new NullPointerException("source and column can't be null");
    }
    // first check if column exists
    List<String> header = getHeader(source);
    if (!header.contains(column)) {
      throw new IllegalArgumentException(String.format(
          "Source column %s does not exist in source %s", column,
          source.getName()));
    }
    // column exists. Now iterate through entire source and store distinct terms
    // in memory (uuuh)
    Set<String> terms = new HashSet<String>();
    Iterator iter = iterSourceColumn(source, column);
    while (iter.hasNext()) {
      String term = iter.next().toString();
      terms.add(term);
    }
    return terms;
  }

  public List<String> getHeader(SourceBase source) throws Exception {
    if (source == null) {
      throw new NullPointerException();
    }

    if (source instanceof SourceFile) {
      SourceFile src = (SourceFile) source;
      return getHeader(src);
    } else {
      SourceSql src = (SourceSql) source;
      return getHeader(src);
    }
  }

  public List<List<? extends Object>> getPreview(SourceBase source)
      throws Exception {
    if (source == null) {
      throw new NullPointerException();
    }

    if (source instanceof SourceFile) {
      SourceFile src = (SourceFile) source;
      return getPreview(src);
    } else {
      SourceSql src = (SourceSql) source;
      return getPreview(src);
    }
  }

  private List<String> getHeader(SourceFile source) throws IOException,
      MalformedTabFileException {
    TabFileReader reader = new TabFileReader(getSourceFile(source), true);
    List<String> headers;
    if (source.hasHeaders()) {
      headers = Arrays.asList(reader.getHeader());
    } else {
      // create numbered column names if no headers are present
      int numCols = reader.getHeader().length;
      headers = new ArrayList<String>();
      int i = 1;
      while (i <= numCols) {
        headers.add(String.format("col%03d", i));
        i++;
      }
    }
    reader.close();
    return headers;
  }

  private List<String> getHeader(SourceSql source) throws SQLException {
    List<List<? extends Object>> preview = getPreview(source);
    return (List<String>) preview.get(0);
  }

  private List<List<? extends Object>> getPreview(SourceFile source)
      throws IOException, MalformedTabFileException {
    List<List<? extends Object>> preview = new ArrayList<List<? extends Object>>();
    // read file
    preview.add(getHeader(source));
    TabFileReader reader = new TabFileReader(getSourceFile(source),
        !source.hasHeaders());
    while (reader.hasNext() && preview.size() <= PREVIEW_SIZE) {
      preview.add(Arrays.asList(reader.next()));
    }
    reader.close();
    return preview;
  }

  private List<List<? extends Object>> getPreview(SourceSql source)
      throws SQLException {
    Connection conn = getResourceConnection(source.getResource());
    Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
        ResultSet.CONCUR_READ_ONLY);
    stmt.setMaxRows(PREVIEW_SIZE);
    stmt.setFetchSize(PREVIEW_SIZE);
    ResultSet rs = stmt.executeQuery(source.getSql());
    List<List<? extends Object>> preview = new ArrayList<List<? extends Object>>();
    List<String> columnHeaders = new ArrayList<String>();

    // get metadata
    ResultSetMetaData meta = rs.getMetaData();
    int columnNum = meta.getColumnCount();
    for (int i = 1; i <= columnNum; i++) {
      String col = "";
      if (StringUtils.trimToNull(meta.getSchemaName(i)) != null) {
        col += StringUtils.trimToEmpty(meta.getSchemaName(i)) + ".";
      }
      if (StringUtils.trimToNull(meta.getTableName(i)) != null) {
        col += StringUtils.trimToEmpty(meta.getTableName(i)) + ".";
      }
      col += meta.getColumnName(i);
      columnHeaders.add(col);
    }
    preview.add(columnHeaders);

    // get first 5 rows into list of list for previewing data
    int row = 0;
    while (row < PREVIEW_SIZE && rs.next()) {
      row += 1;
      List rowList = new ArrayList(columnNum);
      for (int i = 1; i <= columnNum; i++) {
        rowList.add(rs.getObject(i));
      }
      preview.add(rowList);
    }
    rs.close();
    stmt.close();
    return preview;
  }

  private Connection getResourceConnection(DataResource resource)
      throws SQLException {
    // try to connect to db via JDBC
    DataSource ds = resource.getDatasource();
    Connection conn;
    if (ds != null) {
      conn = ds.getConnection();
    } else {
      throw new SQLException("Can't connect to database");
    }
    return conn;
  }

  private Iterator<Object> getSourceColumnIterator(SourceFile source,
      String column) throws IOException, MalformedTabFileException {
    int columnIdx = 0;
    List<String> h = getHeader(source);
    while (columnIdx < h.size()) {
      if (h.get(columnIdx).equals(column)) {
        break;
      }
      columnIdx++;
    }
    return new FileIterator(getSourceFile(source), columnIdx,
        !source.hasHeaders());
  }

  private Iterator<Object> getSourceColumnIterator(SourceSql source,
      String column) throws SQLException {
    return new SqlIterator(source, column);
  }

  private File getSourceFile(SourceFile source) {
    File sourceFile = cfg.getResourceSourceFile(source.getResource().getId(),
        source.getFilename());
    return sourceFile;
  }

  private Iterator<Object> iterSourceColumn(SourceBase source, String column)
      throws Exception {
    if (source == null || column == null) {
      throw new NullPointerException("source and column can't be null");
    }

    if (source instanceof SourceFile) {
      SourceFile src = (SourceFile) source;
      return getSourceColumnIterator(src, column);
    } else {
      SourceSql src = (SourceSql) source;
      return getSourceColumnIterator(src, column);
    }
  }

}
