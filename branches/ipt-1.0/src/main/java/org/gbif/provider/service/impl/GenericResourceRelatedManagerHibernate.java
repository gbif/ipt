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

import org.gbif.provider.model.Resource;
import org.gbif.provider.model.ResourceRelatedObject;
import org.gbif.provider.service.GenericResourceRelatedManager;

import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * TODO: Documentation.
 * 
 * @param <T>
 */
@Transactional(readOnly = true)
public class GenericResourceRelatedManagerHibernate<T extends ResourceRelatedObject>
    extends GenericManagerHibernate<T> implements
    GenericResourceRelatedManager<T> {

  /**
   * Constructor that takes in a class to see which type of entity to persist
   * 
   * @param persistentClass the class type you'd like to persist
   */
  public GenericResourceRelatedManagerHibernate(final Class<T> persistentClass) {
    super(persistentClass);
  }

  public int count(Long resourceId) {
    return ((Long) query(
        String.format(
            "select count(e) from %s e WHERE e.resource.id = :resourceId",
            persistentClass.getSimpleName())).setLong("resourceId", resourceId).iterate().next()).intValue();
  }

  public List<T> getAll(final Long resourceId) {
    return query(
        String.format("from %s e WHERE e.resource.id = :resourceId",
            persistentClass.getSimpleName())).setLong("resourceId", resourceId).list();
  }

  @Transactional(readOnly = false)
  public int removeAll(Resource resource) {
    return removeAll(resource, persistentClass);
  }

  @Transactional(readOnly = false)
  protected int removeAll(Resource resource, Class resourceRelatedClass) {
    // use DML-style HQL batch updates
    // http://www.hibernate.org/hib_docs/reference/en/html/batch.html
    Session session = getSession();
    // now delete resource related entities
    String hqlUpdate = String.format(
        "delete %s e WHERE e.resource = :resource",
        resourceRelatedClass.getSimpleName());
    int count = session.createQuery(hqlUpdate).setEntity("resource", resource).executeUpdate();
    log.info(String.format("Removed %s %ss bound to resource %s", count,
        resourceRelatedClass.getSimpleName(), resource.getTitle()));
    return count;
  }

}
