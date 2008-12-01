package org.gbif.provider.util;

import org.gbif.provider.datasource.DatasourceContextHolder;
import org.gbif.provider.datasource.DatasourceRegistry;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.service.SourceInspectionManager;
import org.gbif.provider.service.OccResourceManager;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseExternalDatasourceTest extends ContextAwareTestBase {
	@Autowired
	protected SourceInspectionManager datasourceInspectionManager;
	@Autowired
	private DatasourceRegistry datasourceRegistry;
	@Autowired
	private OccResourceManager occResourceManager;
	private DataResource resource;


	/**
	 * set resource_id = 1 for external datasource tests and register the
	 * datasource
	 * 
	 * @throws Exception
	 */
	public void setUpExternalDatasource() {
		DatasourceContextHolder.setResourceId(Constants.TEST_DB_RESOURCE_ID);
		resource = (DataResource) occResourceManager.get(Constants.TEST_DB_RESOURCE_ID);
		datasourceRegistry.registerDatasource(resource);
	}
	
	public DataResource getTestRdbmsResource() {
		return resource;
	}	

}
