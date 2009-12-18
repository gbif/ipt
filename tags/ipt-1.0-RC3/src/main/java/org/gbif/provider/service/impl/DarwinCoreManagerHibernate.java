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

import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.Resource;
import org.gbif.provider.service.AnnotationManager;
import org.gbif.provider.service.DarwinCoreManager;

import org.hibernate.Query;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import javax.persistence.EntityExistsException;

/**
 * TODO: Documentation.
 * 
 */
@Transactional(readOnly = true)
public class DarwinCoreManagerHibernate extends
    CoreRecordManagerHibernate<DarwinCore> implements DarwinCoreManager {

  @Autowired
  private AnnotationManager annotationManager;

  public DarwinCoreManagerHibernate() {
    super(DarwinCore.class);
  }

  public List<DarwinCore> getByRegion(Long regionId, Long resourceId,
      boolean inclChildren) {
    String hql;
    if (inclChildren) {
      hql = "select dwc FROM DarwinCore dwc, Region r, Region r2 WHERE dwc.resource.id=:resourceId  and dwc.region=r2  and r.id=:regionId  and r2.lft>=r.lft and r2.rgt<=r.rgt";
    } else {
      hql = "select dwc FROM DarwinCore dwc WHERE dwc.resource.id=:resourceId  and dwc.region.id=:regionId";
    }
    Query query = getSession().createQuery(hql).setParameter("regionId",
        regionId).setParameter("resourceId", resourceId);
    return query.list();
  }

  public List<DarwinCore> getByTaxon(Long taxonId, Long resourceId,
      boolean inclChildren) {
    String hql;
    if (inclChildren) {
      hql = "select dwc FROM DarwinCore dwc, Taxon t, Taxon t2 WHERE dwc.resource.id=:resourceId  and dwc.taxon=t2  and t.id=:taxonId  and t2.lft>=t.lft and t2.rgt<=t.rgt";
    } else {
      hql = "select dwc FROM DarwinCore dwc WHERE dwc.resource.id=:resourceId  and dwc.taxon.id=:taxonId";
    }
    Query query = getSession().createQuery(hql).setParameter("taxonId", taxonId).setParameter(
        "resourceId", resourceId);
    return query.list();
  }

  @Override
  @Transactional(readOnly = false)
  public int removeAll(Resource resource) {
    return super.removeAll(resource);
  }

  @Override
  @Transactional(readOnly = false)
  public DarwinCore save(DarwinCore dwc) {
    // removed the unique checking here cause its too performance consuming
    // the database has a unique constraint on resource_fk + source_id
    // hibernate will raise a ""
    // if (dwc.getId() == null){
    // // only check sourceId constraint for transient objects
    // Long resourceId = dwc.getResourceId();
    // String sourceId = dwc.getSourceId();
    // DarwinCore twin = this.findBySourceId(sourceId, resourceId);
    // if (twin != null){
    // throw new
    // EntityExistsException(String.format("DarwinCoreRecord must have a unique sourceId within a resource. But sourceId %s exists already for resourceId %s",sourceId,
    // resourceId));
    // }
    // }
    try {
      dwc = super.save(dwc);
    } catch (ConstraintViolationException e) {
      // raised most likely when a source_id/resource_fk duplicate exists
      // therefore raise EntityExistsException...
      Long resourceId = null;
      if (dwc.getResource() != null) {
        resourceId = dwc.getResource().getId();
      }
      throw new EntityExistsException(
          String.format(
              "DarwinCoreRecord must have a unique sourceId within a resource. But sourceId %s seems to exist already for resourceId %s",
              dwc.getSourceId(), resourceId), e);
    }
    return dwc;
  }

}
