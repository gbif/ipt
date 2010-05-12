/**
 * 
 */
package org.gbif.provider.service.impl;

import java.sql.Connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class is designed for inheritance and it can be used as a
 * {@link BaseManager} without {@link Transactional} support.
 * 
 */
public abstract class BaseManagerNoTransactions {

  @Autowired
  private SessionFactory sessionFactory;

  protected final static Log log = LogFactory.getLog(BaseManager.class);

  public void flush() {
    getSession().flush();
  }

  protected Connection getConnection() {
    Session s = getSession();
    Connection cn = s.connection();
    return cn;
  }

  protected Session getSession() {
    return SessionFactoryUtils.getSession(sessionFactory, false);
  }

  protected Query query(String hql) {
    return getSession().createQuery(hql);
  }
}
