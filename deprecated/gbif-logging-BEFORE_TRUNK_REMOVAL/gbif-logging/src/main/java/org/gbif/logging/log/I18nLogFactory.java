package org.gbif.logging.log;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * LogFactory that will return a new DelegatingI18nLog instance
 * @author timrobertson
 */
public class I18nLogFactory {
	
	public static I18nLog getLog(String name) {
		Log log = LogFactory.getLog(name);
		DelegatingI18nLog i18nLog = new DelegatingI18nLog(log);
		return i18nLog;
	}
	
	@SuppressWarnings("unchecked")
	public static I18nLog getLog(Class clazz) {
		Log log = LogFactory.getLog(clazz);
		DelegatingI18nLog i18nLog = new DelegatingI18nLog(log);
		return i18nLog;
	}
}