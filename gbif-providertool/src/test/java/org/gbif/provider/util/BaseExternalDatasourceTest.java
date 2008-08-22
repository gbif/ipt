package org.gbif.provider.util;

import org.gbif.provider.datasource.DatasourceContextHolder;
import org.gbif.provider.datasource.DatasourceRegistry;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.service.DatasourceInspectionManager;
import org.gbif.provider.service.OccResourceManager;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseExternalDatasourceTest extends ContextAwareTestBase {
	@Autowired
	protected DatasourceInspectionManager datasourceInspectionManager;
	@Autowired
	private DatasourceRegistry datasourceRegistry;
	@Autowired
	private OccResourceManager occResourceManager;
	private DatasourceBasedResource resource;


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
	
	public DatasourceBasedResource getTestResource() {
		return resource;
	}	

}
