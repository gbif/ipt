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
	
	/**
	 * @param datasourceId to use
	 * @param id The minimum id (exclusive) to search on
	 * @param minLevel The min level inclusive to include
	 * @return The list of events or empty list
	 */
	public List<LogEvent> findByIdGreaterThan(long datasourceId, long id, int Minlevel);
}
