package org.gbif.provider.dao.jdbc;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.gbif.provider.dao.DatasourceInspection;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;

public class DatasourceInspectionImpl extends SimpleJdbcDaoSupport implements DatasourceInspection {

	public List getAllTables() throws SQLException {
		List<String> tableNames = new ArrayList<String>();
		DatabaseMetaData dbmd = this.getConnection().getMetaData();
	    ResultSet rs = dbmd.getTables(null, null, null, new String[]{"TABLE"});
    	while (rs.next()) {
    		tableNames.add((String) rs.getObject(3)); 
    	}
	    return tableNames;
	}
	
	public DatabaseMetaData getDatabaseMetaData() throws SQLException {
		return this.getConnection().getMetaData();
	}
}
