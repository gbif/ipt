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

	public List<LogEvent> findBySourceAndIdGreaterThan(int sourceId, int sourceType, long id, int level) {
		return (List<LogEvent>) getHibernateTemplate().find("from LogEvent where id>? and level>=? and sourceId=? and sourceType=?", new Object[]{id, level, sourceId, sourceType});
	}

	public List<LogEvent> findByGroupAndIdGreaterThan(int groupId, long id, int level) {
		return (List<LogEvent>) getHibernateTemplate().find("from LogEvent where id>? and level>=? and groupId=?", new Object[]{id, level, groupId});
	}

	public List<LogEvent> findByUserAndIdGreaterThan(long userId, long id, int level) {
		return (List<LogEvent>) getHibernateTemplate().find("from LogEvent where id>? and level>=? and user.id=?", new Object[]{id, level, userId});

	}

	public void removeByGroup(int groupId) {
		getHibernateTemplate().deleteAll(findByGroupAndIdGreaterThan(groupId, 0, 0));		
	}

	public void removeBySource(int sourceId, int sourceType) {
		getHibernateTemplate().deleteAll(findBySourceAndIdGreaterThan(sourceId, sourceType, 0, 0));		
	}
}
