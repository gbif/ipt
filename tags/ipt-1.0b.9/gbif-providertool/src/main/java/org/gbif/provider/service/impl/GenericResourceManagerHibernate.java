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
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.gbif.provider.model.BBox;
import org.gbif.provider.model.Point;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.eml.Eml;
import org.gbif.provider.model.voc.PublicationStatus;
import org.gbif.provider.service.EmlManager;
import org.gbif.provider.service.FullTextSearchManager;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.service.RegistryException;
import org.gbif.provider.service.RegistryManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.H2Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

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
	@Autowired
	private RegistryManager registryManager;

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
        return query(String.format("select id from %s where status>=:status", persistentClass.getName()))
        .setParameter("status", PublicationStatus.dirty)
		.list();
	}
	
	@Override
	@Transactional(readOnly=false)
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
			log.info("All aspects of a Resource should be cleaned and now the Resource itself may be deleted");
			super.remove(obj);
		}
	}

	@Transactional(readOnly=false)
	public T publish(Long resourceId) {
		T resource = get(resourceId);
		// make resource public. Otherwise it wont get registered with GBIF
		resource.setStatus(PublicationStatus.dirty);
		updateRegistry(resource);
		// in case sth goes wrong
		Eml metadata;
		try {
			metadata = emlManager.publishNewEmlVersion(resource);
			resource.updateWithMetadata(metadata);
			// the resource is really published and the EML reflects the state of the resource
			resource.setStatus(PublicationStatus.uptodate);
			fullTextSearchManager.buildResourceIndex(resourceId);
		} catch (IOException e) {
			log.error(String.format("Can't publish resource %s. IOException", resourceId), e);
			resource.setStatus(PublicationStatus.draft);
			save(resource);
		}		
		save(resource);
		flush();
		return resource;
	}

	@Transactional(readOnly=false)
	public void unPublish(Long resourceId) {
		T resource = get(resourceId);
		resource.setStatus(PublicationStatus.draft);
		save(resource);
		fullTextSearchManager.buildResourceIndex(resourceId);
		try {
			registryManager.deleteResource(resource);
		} catch (RegistryException e) {
			log.warn("Failed to remove resource from registry", e);
		}
	}
	
	
	private void updateRegistry(Resource resource){
		try {
			if (resource.isRegistered()){
				registryManager.updateResource(resource);
			}else{
				registryManager.registerResource(resource);			
			}
		} catch (Exception e) {
			resource.setDirty();
			log.warn("Failed to communicate with registry", e);
		}
	}

	public List<T> latest(int startPage, int pageSize) {
        return query(String.format("from %s res where res.status>=:status ORDER BY res.modified, res.id", persistentClass.getSimpleName()))
        .setParameter("status", PublicationStatus.dirty)
        .setFirstResult(H2Utils.offset(startPage, pageSize))
        .setMaxResults(pageSize)
		.list();
	}

	public List<T> searchByKeyword(String keyword) {
        return query(String.format("select res from %s res join res.keywords as k where res.status>=:status and k=:keyword", persistentClass.getName()))
        .setParameter("status", PublicationStatus.dirty)
        .setParameter("keyword", keyword)
		.list();
	}

	public List<T> search(String q) {
		List<Long> ids = fullTextSearchManager.search(q);
		List<T> results = new LinkedList<T>();
	    for (Long id : ids) {
			T res = get(id);
			log.debug("Adding record[" + id+ "] to results. GUID[" + res.getGuid() + "]");
		    results.add(res);
	    }		    
	    return results;
	}

	public List<T> searchByBBox(BBox box) {
		// res.geoCoverage.max.longitude
		String boxHqlLat = "(:boxMinY <= res.geoCoverage.min.latitude and :boxMaxY >= res.geoCoverage.max.latitude) or (:boxMinY between res.geoCoverage.min.latitude and res.geoCoverage.max.latitude) or (:boxMaxY between res.geoCoverage.min.latitude and res.geoCoverage.max.latitude)";
		String boxHqlLon = "(:boxMinX <= res.geoCoverage.min.longitude and :boxMaxX >= res.geoCoverage.max.longitude) or (:boxMinX between res.geoCoverage.min.longitude and res.geoCoverage.max.longitude) or (:boxMaxX between res.geoCoverage.min.longitude and res.geoCoverage.max.longitude)";
		String hql = String.format("select res from %s res where res.status>=:status and (%s) and (%s)", persistentClass.getName(), boxHqlLat, boxHqlLon);
		log.debug(hql);
		log.debug("search box: "+box);
        return query(hql)
        .setParameter("status", PublicationStatus.dirty)
        .setDouble("boxMinX", box.getMin().getLongitude())
        .setDouble("boxMaxX", box.getMax().getLongitude())
        .setDouble("boxMinY", box.getMin().getLatitude())
        .setDouble("boxMaxY", box.getMax().getLatitude())
		.list();
	}
}
