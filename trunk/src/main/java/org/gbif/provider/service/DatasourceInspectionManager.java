package org.gbif.provider.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface DatasourceInspectionManager {
	public List getAllTables() throws SQLException;
	public ResultSet executeViewSql(String viewSql) throws SQLException;
}
