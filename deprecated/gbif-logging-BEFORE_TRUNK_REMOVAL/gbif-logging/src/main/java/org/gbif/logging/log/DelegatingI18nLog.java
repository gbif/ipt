/**
 * 
 */
package org.gbif.logging.log;

import org.apache.commons.logging.Log;

/**
 * Delegates log messages to supplied log 
 * @author timrobertson
 */
public class DelegatingI18nLog implements I18nLog {
	// delegates all possible messages to this
	protected Log log;
	
	/**
	 * @param log To delegate to
	 */
	public DelegatingI18nLog(Log log) {
		this.log = log;
	}

	public void debug(String messageKey, String[] messageParams) {
		log.debug(new I18nMessage(messageKey, messageParams));
	}

	public void debug(String messageKey, String[] messageParams, Throwable t) {
		log.debug(new I18nMessage(messageKey, messageParams), t);
	}

	public void error(String messageKey, String[] messageParams) {
		log.error(new I18nMessage(messageKey, messageParams));
	}

	public void error(String messageKey, String[] messageParams, Throwable t) {
		log.error(new I18nMessage(messageKey, messageParams), t);
	}

	public void fatal(String messageKey, String[] messageParams) {
		log.fatal(new I18nMessage(messageKey, messageParams));
	}

	public void fatal(String messageKey, String[] messageParams, Throwable t) {
		log.fatal(new I18nMessage(messageKey, messageParams), t);		
	}

	public void info(String messageKey, String[] messageParams) {
		log.info(new I18nMessage(messageKey, messageParams));
	}

	public void info(String messageKey, String[] messageParams, Throwable t) {
		log.info(new I18nMessage(messageKey, messageParams), t);	
	}

	public void trace(String messageKey, String[] messageParams) {
		log.trace(new I18nMessage(messageKey, messageParams));
	}

	public void trace(String messageKey, String[] messageParams, Throwable t) {
		log.trace(new I18nMessage(messageKey, messageParams), t);
		
	}

	public void warn(String messageKey, String[] messageParams) {
		log.warn(new I18nMessage(messageKey, messageParams));
		
	}

	public void warn(String messageKey, String[] messageParams, Throwable t) {
		log.warn(new I18nMessage(messageKey, messageParams), t);
		
	}
	
	public void debug(String message, Throwable t) {
		log.debug(new I18nMessage(message), t);
	}

	public void debug(String message) {
		log.debug(new I18nMessage(message));
	}

	public void error(String message, Throwable t) {
		log.error(new I18nMessage(message), t);
	}

	public void error(String message) {
		log.error(new I18nMessage(message));
	}

	public void fatal(String message, Throwable t) {
		log.fatal(new I18nMessage(message), t);
	}

	public void fatal(String message) {
		log.fatal(new I18nMessage(message));
	}

	public void info(String message, Throwable t) {
		log.info(new I18nMessage(message), t);
	}

	public void info(String message) {
		log.info(new I18nMessage(message));
	}
	
	public boolean isDebugEnabled() {
		return log.isDebugEnabled();
	}

	public boolean isErrorEnabled() {
		return log.isErrorEnabled();
	}

	public boolean isFatalEnabled() {
		return log.isFatalEnabled();
	}

	public boolean isInfoEnabled() {
		return log.isInfoEnabled();
	}

	public boolean isTraceEnabled() {
		return log.isTraceEnabled();
	}

	public boolean isWarnEnabled() {
		return log.isWarnEnabled();
	}

	public void trace(String message, Throwable t) {
		log.trace(new I18nMessage(message), t);
	}

	public void trace(String message) {
		log.trace(new I18nMessage(message));
	}

	public void warn(String message, Throwable t) {
		log.warn(new I18nMessage(message), t);
	}

	public void warn(String message) {
		log.warn(new I18nMessage(message));
	}

	public void debug(String messageKey, String messageParam) {
		debug(messageKey, new String[]{messageParam});
	}

	public void debug(String messageKey, String messageParam, Throwable t) {
		debug(messageKey, new String[]{messageParam}, t);
		
	}

	public void error(String messageKey, String messageParam) {
		error(messageKey, new String[]{messageParam});
	}

	public void error(String messageKey, String messageParam, Throwable t) {
		error(messageKey, new String[]{messageParam}, t);
	}

	public void fatal(String messageKey, String messageParam) {
		fatal(messageKey, new String[]{messageParam});
	}

	public void fatal(String messageKey, String messageParam, Throwable t) {
		fatal(messageKey, new String[]{messageParam}, t);
	}

	public void info(String messageKey, String messageParam) {
		info(messageKey, new String[]{messageParam});
	}

	public void info(String messageKey, String messageParam, Throwable t) {
		info(messageKey, new String[]{messageParam}, t);
	}

	public void trace(String messageKey, String messageParam) {
		trace(messageKey, new String[]{messageParam});
	}

	public void trace(String messageKey, String messageParam, Throwable t) {
		trace(messageKey, new String[]{messageParam}, t);
	}

	public void warn(String messageKey, String messageParam) {
		warn(messageKey, new String[]{messageParam});
	}

	public void warn(String messageKey, String messageParam, Throwable t) {
		warn(messageKey, new String[]{messageParam}, t);
	}
}
