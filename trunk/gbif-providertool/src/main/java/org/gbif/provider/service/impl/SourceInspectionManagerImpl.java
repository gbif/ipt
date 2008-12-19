/***************************************************************************
* Copyright (C) 2008 Global Biodiversity Information Facility Secretariat.
* All Rights Reserved.
*
* The contents of this file are subject to the Mozilla Public
* License Version 1.1 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of
* the License at http://www.mozilla.org/MPL/
*
* Software distributed under the License is distributed on an "AS
* IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
* implied. See the License for the specific language governing
* rights and limitations under the License.

***************************************************************************/

package org.gbif.provider.service.impl;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.gbif.provider.model.SourceBase;
import org.gbif.provider.model.SourceColumn;
import org.gbif.provider.model.SourceFile;
import org.gbif.provider.model.SourceSql;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.service.SourceInspectionManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.MalformedTabFileException;
import org.gbif.provider.util.TabFileReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

public class SourceInspectionManagerImpl extends JdbcDaoSupport implements SourceInspectionManager {
	private static final int PREVIEW_SIZE = 5;
	@Autowired
	private AppConfig cfg;
	
	public List<List<? extends Object>> getPreview(SourceBase source) throws Exception {
		if (source == null){
			throw new NullPointerException();
		}
		
		if(source instanceof SourceFile){
			SourceFile src = (SourceFile) source; 
			return getPreview(src);		
		}else{
			SourceSql src = (SourceSql) source; 
			return getPreview(src);		
		}
	}
	
	public List<String> getHeader(SourceBase source) throws Exception {
		if (source == null){
			throw new NullPointerException();
		}

		if(source instanceof SourceFile){
			SourceFile src = (SourceFile) source; 
			return getHeader(src);		
		}else{
			SourceSql src = (SourceSql) source; 
			return getHeader(src);		
		}
	}
	
	
	
	private List<List<? extends Object>> getPreview(SourceSql source) throws SQLException {
		Statement stmt = this.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		stmt.setMaxRows(PREVIEW_SIZE);
		stmt.setFetchSize(PREVIEW_SIZE);
		ResultSet rs = stmt.executeQuery(source.getSql());
		List<List<? extends Object>> preview = new ArrayList<List<? extends Object>>();
		List<String> columnHeaders = new ArrayList<String>();
		
		// get metadata
        ResultSetMetaData meta = rs.getMetaData();
        int columnNum = meta.getColumnCount();
        for (int i=1; i<=columnNum; i++){
        	columnHeaders.add(meta.getTableName(i)+"."+meta.getColumnName(i));
        }
        preview.add(columnHeaders);
        
        // get first 5 rows into list of list for previewing data
        int row=0;
        while (row < PREVIEW_SIZE && rs.next()){
        	row += 1;
        	List rowList=new ArrayList(columnNum);
            for (int i=1; i<=columnNum; i++){
            	rowList.add(rs.getObject(i));
            }
            preview.add(rowList);
        }
		rs.close();
		stmt.close();
		return preview;
	}
	private List<String> getHeader(SourceSql source) throws SQLException {
		List<List<? extends Object>> preview = getPreview(source);
		return (List<String>) preview.get(0);
	}

	
	
	
	private List<List<? extends Object>> getPreview(SourceFile source) throws IOException, MalformedTabFileException {
		List<List<? extends Object>> preview = new ArrayList<List<? extends Object>>();
		TabFileReader reader = new TabFileReader(getSourceFile(source));
		// read file
		 preview.add(Arrays.asList(reader.getHeader()));
	     while (reader.hasNext() && preview.size()<7) {
			 preview.add(Arrays.asList(reader.next()));
	     }		 
		 return preview;
	}
	private List<String> getHeader(SourceFile source) throws IOException, MalformedTabFileException {
		TabFileReader reader = new TabFileReader(getSourceFile(source));
		return Arrays.asList(reader.getHeader());
	}
	private File getSourceFile(SourceFile source){
		File sourceFile = cfg.getResourceSourceFile(source.getResource().getId(), source.getFilename());
		return sourceFile;
	}
	
	
	public List getAllTables() throws SQLException {
		DatabaseMetaData dbmd = this.getConnection().getMetaData();
		List<String> tableNames = new ArrayList<String>();
	    ResultSet rs = dbmd.getTables(null, null, null, new String[]{"TABLE"});
    	while (rs.next()) {
    		tableNames.add((String) rs.getObject(3)); 
    	}
	    return tableNames;
	}

	public Set<String> getDistinctValues(SourceBase source, SourceColumn column) throws Exception {
		if (source==null || column == null){
			throw new NullPointerException("source and column can't be null");
		}
		// first check if column exists
		List<String> header = getHeader(source);
		if (!header.contains(column.getColumnName())){
			throw new IllegalArgumentException(String.format("Source column %s does not exist in source %s", column.getColumnName(), source.getName()));
		}
		// column exists. Now iterate through entire source and store distinct terms in memory (uuuh)
		Set<String> terms = new HashSet<String>();
		Iterator iter = iterSourceColumn(source, column);
		while(iter.hasNext()){
			String term = iter.next().toString();
			terms.add(term);
		}
		return terms;
	}
	
	private Iterator<Object> iterSourceColumn(SourceBase source, SourceColumn column) throws Exception{
		if (source == null || column == null){
			throw new NullPointerException("source and column can't be null");
		}

		if(source instanceof SourceFile){
			SourceFile src = (SourceFile) source; 
			return getSourceColumnIterator(src, column);		
		}else{
			SourceSql src = (SourceSql) source; 
			return getSourceColumnIterator(src, column);		
		}
	}
	
	private Iterator<Object> getSourceColumnIterator(SourceFile source, SourceColumn column) throws IOException, MalformedTabFileException{
		return new FileIterator(getSourceFile(source), column.getColumnName());
	}
	private class FileIterator implements Iterator<Object>{
		private TabFileReader reader;
		private int columnIdx;
		public FileIterator(File source, String column) throws IOException, MalformedTabFileException{
			reader = new TabFileReader(source);
			String[] h = reader.getHeader();
			while (columnIdx<h.length){
				columnIdx++;
				if (h[columnIdx].equals(column)){
					break;
				}
			}
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
	
	private Iterator<Object> getSourceColumnIterator(SourceSql source, SourceColumn column) throws SQLException{
		return new SqlIterator(source, column);		
	}
	private class SqlIterator implements Iterator<Object>{
		private Statement stmt;
		private ResultSet rs;
		private String column;
		private boolean hasNext;
		public SqlIterator(SourceSql source, SourceColumn column) throws SQLException{
			this.stmt = getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			this.stmt.setFetchSize(100);
			this.rs = stmt.executeQuery(source.getSql());
			this.column = column.getColumnName();
    		this.hasNext = rs.next();
		}
		public boolean hasNext() {
			return hasNext;
		}
		public Object next() {
			String val = null;
			if (hasNext){
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
	
}
