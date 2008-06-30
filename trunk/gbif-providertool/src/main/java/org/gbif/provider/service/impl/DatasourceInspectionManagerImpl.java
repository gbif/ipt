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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.gbif.provider.dao.DatasourceInspectionDao;
import org.gbif.provider.service.DatasourceInspectionManager;
import org.springframework.beans.factory.annotation.Autowired;

public class DatasourceInspectionManagerImpl implements DatasourceInspectionManager {
	@Autowired
	private DatasourceInspectionDao dao;

	public List getAllTables() throws SQLException {
		DatabaseMetaData dbmd = dao.getDatabaseMetaData();
		List<String> tableNames = new ArrayList<String>();
	    ResultSet rs = dbmd.getTables(null, null, null, new String[]{"TABLE"});
    	while (rs.next()) {
    		tableNames.add((String) rs.getObject(3)); 
    	}
	    return tableNames;
	}
	
	public ResultSet executeViewSql(String viewSql) throws SQLException{
		return dao.executeSql(viewSql);
	}
}
