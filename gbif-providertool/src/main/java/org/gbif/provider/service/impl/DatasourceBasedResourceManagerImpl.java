/***************************************************************************
* Copyright (C) 2008 Global Biodiversity Information Facility Secretariat.
* All Rights Reserved.
*
* The contents of this file are subject to the Mozilla Public
* License Version 1.1 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of
* the License at http://www.mozilla.org/MPL/
*
* Software distributed under the License is distributed on an "AS
* IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
* implied. See the License for the specific language governing
* rights and limitations under the License.

***************************************************************************/

package org.gbif.provider.service.impl;

import java.util.List;
import java.util.Map;

import org.appfuse.dao.GenericDao;
import org.appfuse.service.GenericManager;
import org.gbif.provider.dao.ResourceDao;
import org.gbif.provider.datasource.DatasourceRegistry;
import org.gbif.provider.datasource.ExternalResourceRoutingDatasource;
import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.CoreViewMapping;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.ViewMapping;
import org.gbif.provider.service.ResourceManager;
import org.gbif.provider.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Generic manager for all datasource based resources that need to be registered with the routing datasource.
 * Overriden methods keep the datasource targetsource map of the active datasource registry in sync with the db.
 * @author markus
 *
 * @param <T>
 */
public class DatasourceBasedResourceManagerImpl<T extends DatasourceBasedResource> extends ResourceManagerImpl<T> implements ResourceManager<T> {
	@Autowired
	private DatasourceRegistry registry;
	
	public DatasourceBasedResourceManagerImpl(ResourceDao<T> resourceDao) {
		super(resourceDao);
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
	
	@Override
	public T get(Long id) {
		T obj = super.get(id);
		// init some OnetoMany properties
		obj.getAllMappings();
		return obj;
	}

}
