package org.gbif.provider.service.impl;

import java.util.Map;

import org.appfuse.dao.GenericDao;
import org.appfuse.service.GenericManager;
import org.appfuse.service.impl.GenericManagerImpl;
import org.gbif.provider.datasource.DatasourceRegistry;
import org.gbif.provider.datasource.ExternalResourceRoutingDatasource;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.service.DatasourceBasedResourceManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Generic manager for all datasource based resources that need to be registered with the routing datasource.
 * Overriden methods keep the datasource targetsource map of the active datasource registry in sync with the db.
 * @author markus
 *
 * @param <T>
 */
public class DatasourceBasedResourceManagerImpl<T extends DatasourceBasedResource> extends GenericManagerImpl<T, Long> implements DatasourceBasedResourceManager<T> {
	@Autowired
	private DatasourceRegistry registry;

	public DatasourceBasedResourceManagerImpl(GenericDao<T, Long> datasourceBasedResourceDao) {
		super(datasourceBasedResourceDao);
	}
	
	@Override
	public void remove(Long id) {
		// update registry
		if (registry.containsKey(id)){
			registry.removeDatasource(id);
		}
		// call the real thing
		super.remove(id);
	}

	@Override
	public T save(T resource) {
		// call the real thing first to get a resourceId assigned
		T persistentResource = super.save(resource);
		//datasource might have been updated, so re-register
		registry.registerDatasource(resource);
		return persistentResource;
	}

}
