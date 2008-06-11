package org.gbif.provider.dao.jdbc;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
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
	public ResultSet executeSql(String sql) throws SQLException {
		PreparedStatement ps = this.getConnection().prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs = ps.executeQuery();
		return rs;
	}
}
