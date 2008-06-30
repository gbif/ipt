/**
 * 
 */
package org.gbif.scheduler.service.impl;

import java.util.List;

import org.appfuse.service.impl.GenericManagerImpl;

import com.ibiodiversity.harvest.dao.LogEventDao;
import com.ibiodiversity.harvest.model.LogEvent;
import com.ibiodiversity.harvest.service.LogEventManager;

/**
 * @author timrobertson
 */
public class LogEventManagerImpl extends GenericManagerImpl<LogEvent, Long> implements
		LogEventManager {
	private LogEventDao logEventDao;

	public LogEventManagerImpl(LogEventDao logEventDao) {
		super(logEventDao);
		this.logEventDao = logEventDao;
	}

	public List<LogEvent> findByIdGreaterThan(long id) {
		return logEventDao.findByIdGreaterThan(id);
	}
	
	public List<LogEvent> findByIdGreaterThan(long id, int minLevel) {
		return logEventDao.findByIdGreaterThan(id, minLevel);
	}
	
	public List<LogEvent> findByIdGreaterThan(long datasourceId, long id, int minLevel) {
		return logEventDao.findByIdGreaterThan(datasourceId, id, minLevel);
	}
}
