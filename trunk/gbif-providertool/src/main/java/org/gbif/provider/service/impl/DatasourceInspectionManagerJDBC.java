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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.gbif.provider.service.DatasourceInspectionManager;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

public class DatasourceInspectionManagerJDBC extends JdbcDaoSupport implements DatasourceInspectionManager {
	private static final int PREVIEW_SIZE = 5;
	/**
	 * @param sql
	 * @return a list of 5 rows plus a first header row of strings that contains the column names as TABLE.COLUMNNAME 
	 * @throws SQLException
	 */
	public List getPreview(String sql) throws SQLException {
		Statement stmt = this.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		stmt.setMaxRows(PREVIEW_SIZE);
		stmt.setFetchSize(PREVIEW_SIZE);
		ResultSet rs = stmt.executeQuery(sql);
		List preview = new ArrayList();
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
