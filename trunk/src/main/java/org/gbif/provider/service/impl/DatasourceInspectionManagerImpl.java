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
