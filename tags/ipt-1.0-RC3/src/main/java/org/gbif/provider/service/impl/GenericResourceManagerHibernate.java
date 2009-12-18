/*
 * Copyright 2009 GBIF.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gbif.provider.service.impl;

import org.gbif.provider.model.BBox;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.eml.Eml;
import org.gbif.provider.model.voc.PublicationStatus;
import org.gbif.provider.service.EmlManager;
import org.gbif.provider.service.FullTextSearchManager;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.service.RegistryManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.H2Utils;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Generic manager for all datasource based resources that need to be registered
 * with the routing datasource. Overriden methods keep the datasource
 * targetsource map of the active datasource registry in sync with the db.
 * 
 * @param <T>
 */
public class GenericResourceManagerHibernate<T extends Resource> extends
    GenericManagerHibernate<T> implements GenericResourceManager<T> {
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
    return (T) getSession().createQuery(
        String.format("select res FROM %s res WHERE guid = :guid",
            persistentClass.getSimpleName())).setParameter("guid", guid).uniqueResult();
  }

  public List<Long> getPublishedResourceIDs() {
    return query(
        String.format("select id from %s where status>=:status",
            persistentClass.getName())).setParameter("status",
        PublicationStatus.modified).list();
  }

  public List<T> getPublishedResources() {
    return query(
        String.format("select res from %s res where status>=:status",
            persistentClass.getName())).setParameter("status",
        PublicationStatus.modified).list();
  }

  public List<T> getResourcesByUser(final Long userId) {
    return getSession().createQuery(
        String.format("select res FROM %s res WHERE res.creator.id = :userId",
            persistentClass.getSimpleName())).setParameter("userId", userId).list();
  }

  public List<T> latest(int startPage, int pageSize) {
    return query(
        String.format(
            "from %s res where res.status>=:status ORDER BY res.modified, res.id",
            persistentClass.getSimpleName())).setParameter("status",
        PublicationStatus.modified).setFirstResult(
        H2Utils.offset(startPage, pageSize)).setMaxResults(pageSize).list();
  }

  @Transactional(readOnly = false)
  public T publish(Long resourceId) {
    T resource = get(resourceId);
    // make resource public. Otherwise it wont get registered with GBIF
    resource.setStatus(PublicationStatus.modified);
    updateRegistry(resource);
    // in case sth goes wrong
    Eml metadata;
    try {
      metadata = emlManager.publishNewEmlVersion(resource);
      resource.updateWithMetadata(metadata);
      // the resource is really published and the EML reflects the state of the
      // resource
      resource.setStatus(PublicationStatus.published);
      fullTextSearchManager.buildResourceIndex(resourceId);
    } catch (IOException e) {
      log.error(String.format("Can't publish resource %s. IOException",
          resourceId), e);
      resource.setStatus(PublicationStatus.unpublished);
      save(resource);
    }
    save(resource);
    flush();
    return resource;
  }

  @Override
  @Transactional(readOnly = false)
  public void remove(T obj) {
    // first remove all associated core records, taxa and regions
    if (obj != null) {
      Long resourceId = obj.getId();
      // unpublish resource first
      this.unPublish(resourceId);
      if (resourceId != null) {
        // remove data dir
        File dataDir = cfg.getResourceDataDir(resourceId);
        try {
          FileUtils.deleteDirectory(dataDir);
          log.info("Removed resource data dir " + dataDir.getAbsolutePath());
        } catch (IOException e) {
          log.error("Cant remove data dir for resource " + resourceId, e);
        }
      }
      // remove resource entity itself
      log.info("All aspects of a Resource should be cleaned and now the Resource itself may be deleted");
      super.remove(obj);
    }
  }

  public List<T> search(String q) {
    List<String> ids = fullTextSearchManager.search(q);
    List<T> results = new LinkedList<T>();
    for (String id : ids) {
      T res = get(id);
      log.debug("Adding record[" + id + "] to results. GUID[" + res.getGuid()
          + "]");
      results.add(res);
    }
    return results;
  }

  public List<T> searchByBBox(BBox box) {
    // res.geoCoverage.max.longitude
    String boxHqlLat = "(:boxMinY <= res.geoCoverage.min.latitude and :boxMaxY >= res.geoCoverage.max.latitude) or (:boxMinY between res.geoCoverage.min.latitude and res.geoCoverage.max.latitude) or (:boxMaxY between res.geoCoverage.min.latitude and res.geoCoverage.max.latitude)";
    String boxHqlLon = "(:boxMinX <= res.geoCoverage.min.longitude and :boxMaxX >= res.geoCoverage.max.longitude) or (:boxMinX between res.geoCoverage.min.longitude and res.geoCoverage.max.longitude) or (:boxMaxX between res.geoCoverage.min.longitude and res.geoCoverage.max.longitude)";
    String hql = String.format(
        "select res from %s res where res.status>=:status and (%s) and (%s)",
        persistentClass.getName(), boxHqlLat, boxHqlLon);
    log.debug(hql);
    log.debug("search box: " + box);
    return query(hql).setParameter("status", PublicationStatus.modified).setDouble(
        "boxMinX", box.getMin().getLongitude()).setDouble("boxMaxX",
        box.getMax().getLongitude()).setDouble("boxMinY",
        box.getMin().getLatitude()).setDouble("boxMaxY",
        box.getMax().getLatitude()).list();
  }

  public List<T> searchByKeyword(String keyword) {
    return query(
        String.format(
            "select res from %s res join res.keywords as k where res.status>=:status and k=:keyword",
            persistentClass.getName())).setParameter("status",
        PublicationStatus.modified).setParameter("keyword", keyword).list();
  }

  @Transactional(readOnly = false)
  public void unPublish(Long resourceId) {
    T resource = get(resourceId);
    resource.setStatus(PublicationStatus.unpublished);
    save(resource);
    fullTextSearchManager.buildResourceIndex(resourceId);
    try {
      registryManager.deleteResource(resource);
    } catch (Exception e) {
      log.warn("Failed to remove resource from registry", e);
    }
  }

  private void updateRegistry(Resource resource) {
    try {
      if (resource.isRegistered()) {
        registryManager.updateResource(resource);
      } else {
        registryManager.registerResource(resource);
      }
    } catch (Exception e) {
      resource.setDirty();
      log.warn("Failed to communicate with registry", e);
    }
  }
}
