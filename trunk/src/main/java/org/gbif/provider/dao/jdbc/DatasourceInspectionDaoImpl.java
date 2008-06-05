package org.gbif.provider.dao.jdbc;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.gbif.provider.dao.DatasourceInspectionDao;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;
import org.springframework.stereotype.Repository;

public class DatasourceInspectionDaoImpl extends SimpleJdbcDaoSupport implements DatasourceInspectionDao {
	
	public DatabaseMetaData getDatabaseMetaData() throws SQLException {
		return this.getConnection().getMetaData();
	}
}
