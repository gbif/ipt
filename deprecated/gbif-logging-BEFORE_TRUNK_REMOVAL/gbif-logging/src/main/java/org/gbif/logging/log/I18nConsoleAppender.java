/**
 * 
 */
package org.gbif.logging.log;

import java.io.IOException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.gbif.logging.util.MergedResourceBundleFactory;


/**
 * Resolves at log time to the console the i18n message
 * @author timrobertson
 */
public class I18nConsoleAppender extends ConsoleAppender {
	private String[] i18nBasenamesAsArray;
	// just give a default as it will normally be this
	private MergedResourceBundleFactory bundleFactory = new MergedResourceBundleFactory(new String[]{"ApplicationResources"});
	private ResourceBundle resourceBundle;
	@Override
	protected void subAppend(LoggingEvent event) {
		try {
			if (resourceBundle!=null && // well, screwy config but still let's do something acceptable
					event.getMessage() instanceof I18nMessage) {
				try {
					String message = resourceBundle.getString(((I18nMessage)event.getMessage()).getMessageKey());
					String[] values = ((I18nMessage)event.getMessage()).getMessageParameters();
					if (values!=null) {
						for (int i=0; i<values.length; i++) {
							message = message.replaceAll("\\{" + i + "\\}", values[i]);
						}
					}
					MessageLoggingEvent i18nEvent = new MessageLoggingEvent(event, message);
					super.subAppend(i18nEvent);
					
				} catch (MissingResourceException e) { // not in bundle
					String key = ((I18nMessage)event.getMessage()).getMessageKey();
					String[] params = ((I18nMessage)event.getMessage()).getMessageParameters();
					if (params!=null) {
						MessageLoggingEvent i18nEvent = new MessageLoggingEvent(event, key + params);
						super.subAppend(i18nEvent);
						
					} else {
						MessageLoggingEvent i18nEvent = new MessageLoggingEvent(event, key);
						super.subAppend(i18nEvent);
						
					}
				}
				
			} else { // just a normal log message (e.g. from underlying library) 
				MessageLoggingEvent i18nEvent = new MessageLoggingEvent(event, event.getMessage());
				super.subAppend(i18nEvent);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	public void setI18nBasenames(String i18nBasenames) {
		i18nBasenamesAsArray = i18nBasenames.split(",");
		System.out.println("I18nConsoleAppender configured with basenames: " + i18nBasenames);
		bundleFactory = new MergedResourceBundleFactory(i18nBasenamesAsArray);
		try {
			resourceBundle = bundleFactory.getBundle();
		} catch (IOException e) {
			System.err.println("Logging is not configured properly");
			e.printStackTrace();
		}
	}
}
