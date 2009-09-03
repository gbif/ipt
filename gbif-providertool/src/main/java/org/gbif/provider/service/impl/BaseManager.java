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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;

/**
 * TODO: Documentation.
 * 
 */
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public abstract class BaseManager {

  @Autowired
  private SessionFactory sessionFactory;

  /**
   * Log variable for all child classes. Uses LogFactory.getLog(getClass()) from
   * Commons Logging
   */
  protected final Log log = LogFactory.getLog(getClass());

  public void flush() {
    getSession().flush();
  }

  protected Connection getConnection() {
    Session s = getSession();
    Connection cn = s.connection();
    return cn;
  }

  // private Connection getConnection() throws SQLException {
  // Session s = SessionFactoryUtils.getSession(sessionFactory, false);
  // Connection cn = s.connection();
  // return cn;
  // }

  protected Session getSession() {
    return SessionFactoryUtils.getSession(sessionFactory, false);
  }

  protected Query query(String hql) {
    return getSession().createQuery(hql);
  }

}
