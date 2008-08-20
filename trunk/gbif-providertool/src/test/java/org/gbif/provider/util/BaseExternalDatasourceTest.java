package org.gbif.provider.util;

import org.appfuse.dao.BaseDaoTestCase;
import org.appfuse.service.GenericManager;
import org.gbif.provider.datasource.DatasourceContextHolder;
import org.gbif.provider.datasource.DatasourceRegistry;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.service.DatasourceInspectionManager;

public abstract class BaseExternalDatasourceTest extends ContextAwareTestBase {
	protected DatasourceInspectionManager datasourceInspectionManager;
	private DatasourceRegistry datasourceRegistry;
	private GenericManager<OccurrenceResource, Long> occResourceManager;
	private DatasourceBasedResource resource;


	public void setDatasourceInspectionManager(
			DatasourceInspectionManager datasourceInspectionManager) {
		this.datasourceInspectionManager = datasourceInspectionManager;
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
		DatasourceContextHolder.setResourceId(Constants.TEST_RESOURCE_ID);
		resource = (DatasourceBasedResource) occResourceManager.get(Constants.TEST_RESOURCE_ID);
		datasourceRegistry.registerDatasource(resource);
	}
	

}
