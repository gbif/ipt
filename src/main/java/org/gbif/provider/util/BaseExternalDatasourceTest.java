package org.gbif.provider.util;

import org.appfuse.dao.BaseDaoTestCase;
import org.appfuse.service.GenericManager;
import org.gbif.provider.dao.DatasourceInspectionDao;
import org.gbif.provider.datasource.DatasourceContextHolder;
import org.gbif.provider.datasource.DatasourceRegistry;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.OccurrenceResource;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseExternalDatasourceTest extends BaseDaoTestCase {
	static Long RESOURCE_ID = 1L;

	public DatasourceInspectionDao datasourceInspectionDao;
	private DatasourceRegistry datasourceRegistry;
	private GenericManager<OccurrenceResource, Long> occResourceManager;
	private DatasourceBasedResource resource;

	public void setDatasourceInspectionDao(
			DatasourceInspectionDao datasourceInspectionDao) {
		this.datasourceInspectionDao = datasourceInspectionDao;
	}

	public void setDatasourceRegistry(DatasourceRegistry datasourceRegistry) {
		this.datasourceRegistry = datasourceRegistry;
	}

	public void setOccResourceManager(
			GenericManager<OccurrenceResource, Long> occResourceManager) {
		this.occResourceManager = occResourceManager;
	}

	public DatasourceBasedResource getTestResource() {
		return resource;
	}
	
	/**
	 * set resource_id = 1 for external datasource tests and register the
	 * datasource
	 * 
	 * @throws Exception
	 */
	public void setUpExternalDatasource() {
		DatasourceContextHolder.setResourceId(RESOURCE_ID);
		resource = occResourceManager.get(RESOURCE_ID);
		datasourceRegistry.registerDatasource(resource);
	}

}
