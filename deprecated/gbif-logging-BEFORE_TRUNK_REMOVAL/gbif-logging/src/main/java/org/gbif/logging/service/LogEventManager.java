/**
 * 
 */
package org.gbif.logging.service;

import java.util.List;

import org.appfuse.service.GenericManager;

import org.gbif.logging.model.LogEvent;

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
	 * @param id of the source entity (eg job/process) that issued the event
	 * @param the type of source entity that issued the id, usually linked to the class of a job/process
	 * @param id Minimum id exclusive
	 * @param minLevel minimum logging level (1, trace, 2 debug, 3 info, 4 warn, 5 error, 6 fatal)
	 * @return Events with an ID greater than that given or empty list
	 */
	public List<LogEvent> findBySourceAndIdGreaterThan(int sourceId, int sourceType, long id, int minLevel);


	/**
	 * @param userId
	 * @param id
	 * @param minLevel
	 * @return
	 */
	public List<LogEvent> findByUserAndIdGreaterThan(long userId, long id, int minLevel);

	/**
	 * @param groupId
	 * @param id
	 * @param minLevel
	 * @return
	 */
	public List<LogEvent> findByGroupAndIdGreaterThan(int groupId, long id, int minLevel);
	

	public void removeByGroup(int groupId);

	public void removeBySource(int sourceId, int sourceType);
	
}
