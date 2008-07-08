/**
 * 
 */
package org.gbif.logging.dao.hibernate;

import java.util.List;

import org.appfuse.dao.hibernate.GenericDaoHibernate;

import org.gbif.logging.dao.LogEventDao;
import org.gbif.logging.model.LogEvent;

/**
 * @author timrobertson
 */
public class LogEventDaoHibernate extends GenericDaoHibernate<LogEvent, Long> implements LogEventDao {
	public LogEventDaoHibernate() {
		super(LogEvent.class);
	}

	/**
	 * @see org.gbif.logging.dao.LogEventDao#findByIdGreaterThan(long)
	 */
	@SuppressWarnings("unchecked")
	public List<LogEvent> findByIdGreaterThan(long id) {
		return (List<LogEvent>) getHibernateTemplate().find("from LogEvent where id>?", id);
	}

	/**
	 * @see org.gbif.logging.dao.LogEventDao#findByIdGreaterThan(long, int)
	 */
	@SuppressWarnings("unchecked")
	public List<LogEvent> findByIdGreaterThan(long id, int level) {
		return (List<LogEvent>) getHibernateTemplate().find("from LogEvent where id>? and level>=?", new Object[]{id, level});
	}
	
	/**
	 * @see org.gbif.logging.dao.LogEventDao#findByIdGreaterThan(long, long, int)
	 */
	@SuppressWarnings("unchecked")
	public List<LogEvent> findByIdGreaterThan(long datasourceId, long id, int level) {
		return (List<LogEvent>) getHibernateTemplate().find("from LogEvent where bioDatasource.id=? and id>? and level>=?", new Object[]{datasourceId, id, level});
	}
}
