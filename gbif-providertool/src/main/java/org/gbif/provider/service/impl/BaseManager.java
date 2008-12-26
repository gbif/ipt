package org.gbif.provider.service.impl;

import java.sql.Connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly=true, propagation=Propagation.REQUIRED)
public abstract class BaseManager {

	@Autowired
	private SessionFactory sessionFactory;

	protected Session getSession() {
		return SessionFactoryUtils.getSession(sessionFactory, false);
	}

	protected Connection getConnection() {
				Session s = getSession();
				Connection cn = s.connection();
				return cn;
			}
	//		private Connection getConnection() throws SQLException {
	//			Session s = SessionFactoryUtils.getSession(sessionFactory, false);
	//			Connection cn = s.connection();
	//			return cn;
	//		}

	/**
	 * Log variable for all child classes. Uses LogFactory.getLog(getClass()) from Commons Logging
	 */
	protected final Log log = LogFactory.getLog(getClass());

	protected Query query(String hql) {
	    return getSession().createQuery(hql);
	}

	public void flush() {
		getSession().flush();
	}

}
