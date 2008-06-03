package org.gbif.provider.service;

import org.appfuse.service.GenericManager;
import org.gbif.provider.model.DatasourceBasedResource;

public interface DatasourceBasedResourceManager<T extends DatasourceBasedResource> 
		extends GenericManager<T, Long> {
	
}
