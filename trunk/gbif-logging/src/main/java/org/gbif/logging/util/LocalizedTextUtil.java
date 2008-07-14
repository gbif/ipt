/**
 * 
 */
package org.gbif.logging.util;

import java.io.IOException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * The standard localized text util is somewhat limited since it will only do a 
 * ResourceBundle.getBundle(...)
 * 
 * This will allow for localized text based on MergedResourceBundles
 * @author timrobertson
 */
public class LocalizedTextUtil {
	private String[] i18nBasenamesAsArray;
	// just give a default as it will normally be this
	private MergedResourceBundleFactory bundleFactory = new MergedResourceBundleFactory(new String[]{"ApplicationResources"});
	private ResourceBundle resourceBundle;
	
	public LocalizedTextUtil() {
		try {
			resourceBundle = bundleFactory.getBundle(Locale.getDefault());
		} catch (IOException e) {
			System.err.println("Logging is not configured properly");
			e.printStackTrace();
		}
	}
	
	public String findText(String key, String[] values, Locale locale, String defaultString) {
		if (resourceBundle!=null) {
			try {
				String message = resourceBundle.getString(key);
				if (values!=null) {
					for (int i=0; i<values.length; i++) {
						message = message.replaceAll("\\{" + i + "\\}", values[i]);
					}
				}
				return message;
				
			} catch (MissingResourceException e) { // not in bundle
			}
		}
		return defaultString;
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

	/**
	 * @return the resourceBundle
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}
	
	
	
}
