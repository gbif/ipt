package org.gbif.provider.dao;

import java.sql.SQLException;
import java.util.List;

public interface DatasourceInspection {
	public List getAllTables() throws SQLException;
}
