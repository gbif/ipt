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

import org.gbif.provider.model.BaseObject;
import org.gbif.provider.service.GenericManager;

import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * This class serves as the Base class for all other Managers - namely to hold
 * common CRUD methods that they might all use. You should only need to extend
 * this class when your require custom CRUD logic.
 * 
 * <p>
 * To register this class in your Spring context file, use the following XML.
 * 
 * <pre>
 *     &lt;bean id="userManager" class="com.yasasu.service.impl.GenericManagerImpl"&gt;
 *         &lt;constructor-arg&gt;
 *             &lt;constructor-arg value="com.yasasu.model.User"/&gt;
 *         &lt;/constructor-arg&gt;
 *     &lt;/bean&gt;
 * </pre>
 * 
 * @param <T> a type variable
 */
public class GenericManagerHibernate<T extends BaseObject> extends BaseManager
    implements GenericManager<T> { // extends HibernateDaoSupport
  public static int MAX_SEARCH_RESULTS = 50;
  protected Class<T> persistentClass;

  /**
   * Constructor that takes in a class to see which type of entity to persist
   * 
   * @param persistentClass the class type you'd like to persist
   */
  public GenericManagerHibernate(final Class<T> persistentClass) {
    this.persistentClass = persistentClass;
  }

  public void debugSession() {
    log.debug(getSession());
  }

  /**
   * {@inheritDoc}
   */
  public boolean exists(Long id) {
    T entity = get(id);
    return entity != null;
  }

  /**
   * {@inheritDoc}
   */
  public T get(Long id) {
    return (T) getSession().get(persistentClass, id);
  }

  /**
   * {@inheritDoc}
   */
  public List<T> getAll() {
    return query(String.format("from %s", persistentClass.getName())).list();
  }

  /**
   * {@inheritDoc}
   */
  public List<T> getAllDistinct() {
    Collection result = new LinkedHashSet(getAll());
    return new ArrayList(result);
  }

  public List<Long> getAllIds() {
    return query(String.format("select id from %s", persistentClass.getName())).list();
  }

  /**
   * {@inheritDoc}
   */
  @Transactional(readOnly = false)
  public void remove(Long id) {
    T obj = get(id);
    if (obj != null) {
      remove(obj);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Transactional(readOnly = false)
  public void remove(T obj) {
    getSession().delete(obj);
  }

  /**
   * {@inheritDoc}
   */
  @Transactional(readOnly = false)
  public T save(T object) {
    getSession().saveOrUpdate(object);
    return object;
  }

  public void saveAll(Collection<T> objs) {
    for (T obj : objs) {
      save(obj);
    }
  }

  @Transactional(readOnly = false)
  protected void universalRemove(BaseObject obj) {
    getSession().delete(obj);
  }

  @Transactional(readOnly = false)
  protected BaseObject universalSave(BaseObject obj) {
    getSession().saveOrUpdate(obj);
    return obj;
  }

}
