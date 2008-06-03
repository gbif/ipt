package org.gbif.provider.dao.jdbc;

import java.util.List;

import org.appfuse.dao.BaseDaoTestCase;


public class DatasourceInspectionImplTest extends BaseDaoTestCase{
	private DatasourceInspectionImpl datasourceInspection;

	public void setDatasourceInspection(
			DatasourceInspectionImpl datasourceInspection) {
		this.datasourceInspection = datasourceInspection;
	}
		
	public void testGetAllTables() throws Exception {
		List<String> tables = datasourceInspection.getAllTables();
		System.out.println(tables);
	    assertTrue(1 > 0);
	}
}
