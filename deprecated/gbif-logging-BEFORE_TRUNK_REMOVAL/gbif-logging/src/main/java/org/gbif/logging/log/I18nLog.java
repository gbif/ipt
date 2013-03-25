/**
 * 
 */
package org.gbif.logging.log;



/**
 * This is an I18N log class that is to allow developers to log using i18n file keys,
 * so that the logs can be read in multiple languages.  Note, that it is not internationised 
 * by implementations at log time, but allows for appenders to either resolve at log time
 * or store the i18n key for resolving later.  Typically a console or file appender would 
 * resolve to the locale of the appender, whereas a DB appender would store the i18n key, 
 * for display at a later time.
 * 
 * @author timrobertson
 */
public interface I18nLog {
	/**
	 * Used very rarely for very low level logging for messages that
	 * might lead to problem solving for a bug.  Use sparingly if at all.
	 * It might be used for example to log very verbose communications messages
	 */
	public void trace(String messageKey);
	public void trace(String messageKey, Throwable t);
	public void trace(String messageKey, String[] messageParams);
	public void trace(String messageKey, String[] messageParams, Throwable t);
	public void trace(String messageKey, String messageParam);
	public void trace(String messageKey, String messageParam, Throwable t);
	
	/**
	 * Used for low level logging for messages that
	 * might lead to problem solving
	 */
	public void debug(String messageKey);
	public void debug(String messageKey, Throwable t);
	public void debug(String messageKey, String[] messageParams);
	public void debug(String messageKey, String[] messageParams, Throwable t);
	public void debug(String messageKey, String messageParam);
	public void debug(String messageKey, String messageParam, Throwable t);
	
	/**
	 * Info level messages are the standard messages that a normal user would like to see.  E.g.:
	 * - Inventory starting
	 * - Inventory finished, 5,478 names found
	 * - Harvesting name range Aus bus - Cus dus
	 * - Name range Aus bus - Cus dus retrieved 876 records
	 * .
	 * .
	 * .  
	 * - Harvesting complete.  Total records return 143,872
	 */
	public void info(String messageKey);
	public void info(String messageKey, Throwable t);
	public void info(String messageKey, String[] messageParams);
	public void info(String messageKey, String[] messageParams, Throwable t);
	public void info(String messageKey, String messageParam);
	public void info(String messageKey, String messageParam, Throwable t);
	
	/**
	 * Warnings can normally be recovered from
	 */
	public void warn(String messageKey);
	public void warn(String messageKey, Throwable t);
	public void warn(String messageKey, String[] messageParams);
	public void warn(String messageKey, String[] messageParams, Throwable t);
	public void warn(String messageKey, String messageParam);
	public void warn(String messageKey, String messageParam, Throwable t);
	
	/**
	 * Used for error signaling - the application will continue but the error process is probably 
	 * not going to
	 */
	public void error(String messageKey);
	public void error(String messageKey, Throwable t);
	public void error(String messageKey, String[] messageParams);
	public void error(String messageKey, String[] messageParams, Throwable t);
	public void error(String messageKey, String messageParam);
	public void error(String messageKey, String messageParam, Throwable t);

	/**
	 * Used for when the application cannot recover
	 */
	public void fatal(String messageKey);
	public void fatal(String messageKey, Throwable t);
	public void fatal(String messageKey, String[] messageParams);
	public void fatal(String messageKey, String[] messageParams, Throwable t);
	public void fatal(String messageKey, String messageParam);
	public void fatal(String messageKey, String messageParam, Throwable t);
	
	/**
	 * Is debug enabled
	 */
	public boolean isDebugEnabled();

	/**
	 * Is Error enabled
	 */
	public boolean isErrorEnabled();

	/**
	 * Is fatal enabled
	 */
	public boolean isFatalEnabled();

	/**
	 * Is info enabled
	 */
	public boolean isInfoEnabled();

	/**
	 * Is trace enabled
	 */
	public boolean isTraceEnabled();

	/**
	 * Is warn enabled
	 */
	public boolean isWarnEnabled();
}
