/**
 * 
 */
package org.gbif.logging.dao;

import java.util.List;

import org.appfuse.dao.GenericDao;

import org.gbif.logging.model.LogEvent;

/**
 * @author timrobertson
 */
public interface LogEventDao extends GenericDao<LogEvent, Long> {
	/**
	 * @param id The minimum id (exclusive) to search on
	 * @return The list of events or empty list
	 */
	public List<LogEvent> findByIdGreaterThan(long id);
	
	/**
	 * @param id The minimum id (exclusive) to search on
	 * @param minLevel The min level inclusive to include
	 * @return The list of events or empty list
	 */
	public List<LogEvent> findByIdGreaterThan(long id, int Minlevel);

	public List<LogEvent> findByGroupAndIdGreaterThan(int groupId, long id, int minLevel);

	public List<LogEvent> findBySourceAndIdGreaterThan(int sourceId, int sourceType, long id, int minLevel);

	public List<LogEvent> findByUserAndIdGreaterThan(long userId, long id, int minLevel);

	public void removeByGroup(int groupId);

	public void removeBySource(int sourceId, int sourceType);
}
