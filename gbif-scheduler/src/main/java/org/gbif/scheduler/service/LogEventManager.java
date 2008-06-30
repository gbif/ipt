/**
 * 
 */
package org.gbif.scheduler.service;

import java.util.List;

import org.appfuse.service.GenericManager;

import com.ibiodiversity.harvest.model.LogEvent;

/**
 * @author timrobertson
 */
public interface LogEventManager extends GenericManager<LogEvent, Long> {
	/**
	 * @param id Minimum id exclusive
	 * @return Events with an ID greater than that given or empty list
	 */
	public List<LogEvent> findByIdGreaterThan(long id);
	
	/**
	 * @param id Minimum id exclusive
	 * @param minLevel minimum logging level (1, trace, 2 debug, 3 info, 4 warn, 5 error, 6 fatal)
	 * @return Events with an ID greater than that given or empty list
	 */
	public List<LogEvent> findByIdGreaterThan(long id, int minLevel);
	
	/**
	 * @param datasourceId to use
	 * @param id Minimum id exclusive
	 * @param minLevel minimum logging level (1, trace, 2 debug, 3 info, 4 warn, 5 error, 6 fatal)
	 * @return Events with an ID greater than that given or empty list
	 */
	public List<LogEvent> findByIdGreaterThan(long datasourceId, long id, int minLevel);
}
