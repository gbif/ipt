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

import org.gbif.provider.datasource.DatasourceRegistry;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.service.ResourceManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Generic manager for all datasource based resources that need to be registered with the routing datasource.
 * Overriden methods keep the datasource targetsource map of the active datasource registry in sync with the db.
 * @author markus
 *
 * @param <T>
 */
public class DatasourceBasedResourceManagerHibernate<T extends DatasourceBasedResource> extends ResourceManagerHibernate<T> implements ResourceManager<T> {
	
	public DatasourceBasedResourceManagerHibernate(Class<T> persistentClass) {
		super(persistentClass);
	}

	@Autowired
	private DatasourceRegistry registry;
	

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
