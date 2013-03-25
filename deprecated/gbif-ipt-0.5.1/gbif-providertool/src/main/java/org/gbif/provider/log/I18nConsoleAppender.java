/**
 * 
 */
package org.gbif.provider.log;

import java.io.IOException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.gbif.logging.log.I18nMessage;
import org.gbif.logging.log.MessageLoggingEvent;
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
			if (event.getMessage() instanceof I18nMessage) {
				if (resourceBundle!=null) {
					try {
						String message = resourceBundle.getString(((I18nMessage)event.getMessage()).getMessageKey());
						String[] values = ((I18nMessage)event.getMessage()).getMessageParameters();
						if (values!=null) {
							for (int i=0; i<values.length; i++) {
								message = message.replaceAll("\\{" + i + "\\}", values[i]);
							}
						}
						MessageLoggingEvent i18nEvent = new MessageLoggingEvent(event, message);
						appendWithoutStackTrace(i18nEvent);
						
					} catch (MissingResourceException e) { // not in bundle
						appendWithoutStackTrace(transformIntoPlainMessage(event));
					}
				}else{  // well, screwy config but still let's do something acceptable
					appendWithoutStackTrace(transformIntoPlainMessage(event));
				}

				
			} else { // just a normal log message (e.g. from underlying library) 
				super.subAppend(event);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	private MessageLoggingEvent transformIntoPlainMessage(LoggingEvent event){
		String key = ((I18nMessage)event.getMessage()).getMessageKey();
		String[] params = ((I18nMessage)event.getMessage()).getMessageParameters();
		MessageLoggingEvent i18nEvent;
		if (params!=null) {
			i18nEvent = new MessageLoggingEvent(event, key + params);
			
		} else {
			i18nEvent = new MessageLoggingEvent(event, key);
		}
		return i18nEvent;
	}
	
	private void appendWithoutStackTrace(LoggingEvent event){
		// dont call super.subAppend() to avoid stack traces in the console
		// if the message was an i18 message before
		this.qw.write(this.layout.format(event));
		if(this.immediateFlush) {
		   this.qw.flush();
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
