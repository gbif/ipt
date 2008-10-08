/**
 * 
 */
package org.gbif.logging.log;

import org.apache.log4j.LogManager;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

/**
 * This HACK class was necessary since most of the LoggingEvent is not overridable, and the message
 * needs to be replaced with an i18n version 
 * 
 * @see I18nConsoleAppender
 * @author timrobertson
 */
public class MessageLoggingEvent extends LoggingEvent {
	private static final long serialVersionUID = -5246941229042854168L;
	protected String categoryName;
	protected ThrowableInformation throwableInfo;
	
	/**
	 * A copy constructor setting the local throwableInfo and the local categoryName
	 */
	public MessageLoggingEvent(LoggingEvent event, Object message) {
		// hmmmm... LogManager.getLogger(event.getLoggerName()) 
		// if you are looking for problems and end up here then it is likely to be the problem ;o) 
		super(event.fqnOfCategoryClass, LogManager.getLogger(event.getLoggerName()), event.timeStamp, event.getLevel(), message, null);
		this.categoryName = event.getLoggerName();
		this.throwableInfo = event.getThrowableInformation();
	}

	/**
	 * Overriden to return local categoryName  
	 */
	@Override
	public String getLoggerName() {
		return categoryName;
	}

	/**
	 * Overriden to retrun local throwableInfo
	 */
	@Override
	public ThrowableInformation getThrowableInformation() {
		return throwableInfo;
	}

	/**
	 * Overriden to retrun local throwableInfo
	 */
	@Override
	public String[] getThrowableStrRep() {
		if (throwableInfo == null)
			return null;
		else
			return throwableInfo.getThrowableStrRep();
	}
}