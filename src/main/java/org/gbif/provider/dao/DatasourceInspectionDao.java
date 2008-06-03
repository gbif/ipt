package org.gbif.provider.dao;

import java.sql.SQLException;
import java.util.List;

public interface DatasourceInspectionDao {
	public List getAllTables() throws SQLException;
}
