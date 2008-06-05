package org.gbif.provider.dao;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;

public interface DatasourceInspectionDao {
	public DatabaseMetaData getDatabaseMetaData() throws SQLException;
}
