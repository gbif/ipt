/**
 * 
 */
package org.gbif.logging.log;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.appfuse.model.User;

import com.googlecode.jsonplugin.JSONException;
import com.googlecode.jsonplugin.JSONUtil;

import org.gbif.logging.dao.LogEventDao;
import org.gbif.logging.log.model.CauseDetail;
import org.gbif.logging.log.model.ExceptionDetail;
import org.gbif.logging.model.LogEvent;


/**
 * Resolves at log time to the console the i18n message
 * @author timrobertson
 */
public class I18nDatabaseAppender extends AppenderSkeleton {
	protected static LogEventDao logEventDao;	
	
	public static final String MDC_SOURCE_TYPE = "mdc.source_type";
	public static final String MDC_SOURCE_ID = "mdc.source_id";
	public static final String MDC_INSTANCE_ID = "mdc.instance_id";
	public static final String MDC_GROUP_ID = "mdc.group_id";
	public static final String MDC_USER = "mdc.user";
	

	@Override
	protected void append(LoggingEvent event) {
		if (event.getMessage() instanceof I18nMessage && logEventDao != null) {
			I18nMessage i18nMessage = (I18nMessage) event.getMessage();

			int level = I18nDatabaseAppender.getLevel(event.getLevel());			
			int sourceType = -1;
			int sourceId = -1;
			
			String instanceId = null;
			int groupId = -1;
			User user = null;

			// we are really typesafe... no exceptions here please!
			if (event.getMDC(MDC_SOURCE_TYPE) instanceof Integer) {
				sourceType = (Integer) event.getMDC(MDC_SOURCE_TYPE);
			}
			if (event.getMDC(MDC_SOURCE_ID) instanceof Integer) {
				sourceId = (Integer) event.getMDC(MDC_SOURCE_ID);
			}
			if (event.getMDC(MDC_INSTANCE_ID) instanceof String) {
				instanceId = (String) event.getMDC(MDC_INSTANCE_ID);
			}
			if (event.getMDC(MDC_GROUP_ID) instanceof Integer) {
				groupId = (Integer) event.getMDC(MDC_GROUP_ID);
			}
			if (event.getMDC(MDC_USER) instanceof User) {
				user = (User) event.getMDC(MDC_USER);
			}
			
			String infoAsJSON = null;
			if (event.getThrowableInformation() != null) {
				ThrowableInformation ti = event.getThrowableInformation();
				Throwable t = ti.getThrowable();
				if (t != null) {
					ExceptionDetail ed = new ExceptionDetail();
					ed.setMessage(t.getMessage());
					StackTraceElement[] stes = t.getStackTrace();
					for (StackTraceElement ste : stes) {
						CauseDetail cd = new CauseDetail();
						cd.setClassName(ste.getClassName());
						cd.setMethodName(ste.getMethodName());
						cd.setLineNumber(ste.getLineNumber());
						cd.setFileName(ste.getFileName());
						ed.getCauseDetails().add(cd);
					}
					try {
						infoAsJSON = JSONUtil.serialize(ed);
					} catch (JSONException e) {
						// given an appender just print to system.err
						e.printStackTrace();
					}
				}
			}
			
			if (infoAsJSON == null) {
				LogEvent logEvent = new LogEvent(sourceType, sourceId, instanceId, groupId,
						level,user,i18nMessage.getMessageKey(),i18nMessage.getMessageParameters());
				logEventDao.save(logEvent);
			} else {
				LogEvent logEvent = new LogEvent(sourceType, sourceId, instanceId, groupId,
						level,user,i18nMessage.getMessageKey(),i18nMessage.getMessageParameters(), infoAsJSON);
				logEventDao.save(logEvent);
			}
		}
	}
	
	/**
	 * @param level To convert
	 * @return The logging event as we understand it
	 */
	public static int getLevel(Level level) {
		if (Level.TRACE_INT == level.toInt()) {
			return LogEvent.LEVEL_TRACE;
		} else if (Level.DEBUG_INT == level.toInt()) {
			return LogEvent.LEVEL_DEBUG;
		} else if (Level.INFO_INT == level.toInt()) {
			return LogEvent.LEVEL_INFO;
		} else if (Level.WARN_INT == level.toInt()) {
			return LogEvent.LEVEL_WARN;
		} else if (Level.ERROR_INT == level.toInt()) {
			return LogEvent.LEVEL_ERROR;
		} else if (Level.FATAL_INT == level.toInt()) {
			return LogEvent.LEVEL_FATAL;
		} else {
			return LogEvent.LEVEL_UNKNOWN;
		}
	}
	
	@Override
	public void close() {
	}

	@Override
	public boolean requiresLayout() {
		return false;
	}

	/**
	 * @param logEventDao
	 */
	public void setLogEventDao(LogEventDao logEventDao) {
		if (I18nDatabaseAppender.logEventDao == null) {
			System.out.println("I18nDatabaseAppender configuring with new logEventDao");
			I18nDatabaseAppender.logEventDao = logEventDao;
		}		
	}

}
