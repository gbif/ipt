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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.eml.Eml;
import org.gbif.provider.service.EmlManager;
import org.gbif.provider.service.FullTextSearchManager;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.util.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Generic manager for all datasource based resources that need to be registered with the routing datasource.
 * Overriden methods keep the datasource targetsource map of the active datasource registry in sync with the db.
 * @author markus
 *
 * @param <T>
 */
public class GenericResourceManagerHibernate<T extends Resource> extends GenericManagerHibernate<T> implements GenericResourceManager<T> {
	@Autowired
	protected AppConfig cfg;
	@Autowired
	private FullTextSearchManager fullTextSearchManager;
	@Autowired
	private EmlManager emlManager;

	public GenericResourceManagerHibernate(final Class<T> persistentClass) {
		super(persistentClass);
	}
	
	public T get(final String guid) {
		return (T) getSession().createQuery(String.format("select res FROM %s res WHERE guid = :guid", persistentClass.getSimpleName()))
		.setParameter("guid", guid).uniqueResult();
	}

	public List<T> getResourcesByUser(final Long userId) {
		return getSession().createQuery(String.format("select res FROM %s res WHERE res.creator.id = :userId", persistentClass.getSimpleName()))
    		.setParameter("userId", userId).list();
	}
	
	public List<Long> getPublishedResourceIDs() {
        return query(String.format("select id from %s where published=true", persistentClass.getName()))
		.list();
	}

	@Override
    public List<T> getTop(int maxResults) {
        return getSession().createQuery(String.format("from %s order by modified desc", persistentClass.getName()))
        		.setMaxResults(maxResults)
        		.list();
    }
	
	@Override
	public void remove(T obj) {
		// first remove all associated core records, taxa and regions
		if (obj!=null){
			Long resourceId = obj.getId();
			// unpublish resource first
			this.unPublish(resourceId);
			if (resourceId != null){
				// remove data dir
				File dataDir = cfg.getResourceDataDir(resourceId);
				try {
					FileUtils.deleteDirectory(dataDir);
					log.info("Removed resource data dir "+dataDir.getAbsolutePath());
				} catch (IOException e) {
					log.error("Cant remove data dir for resource "+resourceId, e);
				}
			}
			// remove resource entity itself
			super.remove(obj);
		}
	}

	public void publish(Long resourceId) {
		T resource = get(resourceId);
		resource.setPublished(true);
		Eml metadata;
		try {
			metadata = emlManager.publishNewEmlVersion(resource);
			resource.updateWithMetadata(metadata);
			register(resource);
			save(resource);
			fullTextSearchManager.buildResourceIndex(resourceId);
		} catch (IOException e) {
			log.error(String.format("Can't publish resource %s. IOException", resourceId), e);
			resource.setPublished(false);
			save(resource);
		}
	}

	public void unPublish(Long resourceId) {
		T resource = get(resourceId);
		resource.setPublished(false);
		unregister(resource);
		save(resource);
		fullTextSearchManager.buildResourceIndex(resourceId);
	}
	
	
	private void register(Resource resource){
		// FIXME: implement		
		log.warn("Automatic registration with GBIF hasn't been implemented yet");
	}
	private void unregister(Resource resource){
		// FIXME: implement		
		log.warn("Automatic (un)registration with GBIF hasn't been implemented yet");
	}
}
