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
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.service.DatasourceInspectionManager;
import org.gbif.provider.util.MalformedTabFileException;
import org.gbif.provider.util.TabFileReader;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

public class DatasourceInspectionManagerImpl extends JdbcDaoSupport implements DatasourceInspectionManager {
	private static final int PREVIEW_SIZE = 5;
	
	public List<List<? extends Object>> getPreview(ViewMappingBase view) throws Exception {
		if (view == null){
			throw new NullPointerException();
		}
		
		if (view.getSourceSql()!=null){
			return getPreview(view.getSourceSql());		
		}else if(view.getSourceFile().exists()){
			return getPreview(view.getSourceFile());
		}else{
			throw new IllegalArgumentException("Neither file nor SQL source configured");
		}
	}
	public List<String> getHeader(ViewMappingBase view) throws Exception {
		if (view == null){
			throw new NullPointerException();
		}
		if (view.getSourceSql()!=null){
			return getHeader(view.getSourceSql());		
		}else if(view.getSourceFile().exists()){
			return getHeader(view.getSourceFile());
		}else{
			throw new IllegalArgumentException("Neither file nor SQL source configured");
		}
	}
	
	
	
	private List<List<? extends Object>> getPreview(String sql) throws SQLException {
		Statement stmt = this.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		stmt.setMaxRows(PREVIEW_SIZE);
		stmt.setFetchSize(PREVIEW_SIZE);
		ResultSet rs = stmt.executeQuery(sql);
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
	private List<String> getHeader(String sourceSql) throws SQLException {
		List<List<? extends Object>> preview = getPreview(sourceSql);
		return (List<String>) preview.get(0);
	}

	
	
	
	private List<List<? extends Object>> getPreview(File source) throws IOException, MalformedTabFileException {
		List<List<? extends Object>> preview = new ArrayList<List<? extends Object>>();
		TabFileReader reader = new TabFileReader(source);
		// read file
		 preview.add(Arrays.asList(reader.getHeader()));
	     while (reader.hasNext() && preview.size()<7) {
			 preview.add(Arrays.asList(reader.next()));
	     }		 
		 return preview;
	}
	private List<String> getHeader(File sourceFile) throws IOException, MalformedTabFileException {
		TabFileReader reader = new TabFileReader(sourceFile);
		return Arrays.asList(reader.getHeader());
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
	
}
