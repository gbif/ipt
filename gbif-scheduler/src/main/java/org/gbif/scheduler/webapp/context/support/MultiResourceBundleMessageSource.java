/**
 * 
 */
package org.gbif.scheduler.webapp.context.support;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * @author timrobertson
 *
 */
public class MultiResourceBundleMessageSource extends
		ResourceBundleMessageSource {
	protected Log log = LogFactory.getLog(this.getClass());
	protected String[] accessibleBasenames = new String[0];
	
	/**
	 * Return the classloader of this class
	 */
	protected ClassLoader getBundleClassLoader() {
		return this.getClass().getClassLoader();
	}

	/**
	 * Ok,it is late and I wrote this very quickly and am not sure this is correct...
	 * I am trying to basically read in all the properties files named in the basename and then
	 * merge them.  A standard property RB will only use the first ApplicationResources.properties
	 * it finds
	 */
	@Override
	protected ResourceBundle doGetBundle(String basename, Locale locale)
			throws MissingResourceException {
		
		// build the parent resource bundle
		AppendablePropertyResourceBundle resourceBundle = new AppendablePropertyResourceBundle();
		for (String name : accessibleBasenames) {
			log.info("Working on " + name + ".properties");
			try {
				Enumeration<URL> e = this.getClass().getClassLoader().getResources(name + ".properties");
				while (e.hasMoreElements()) {
					InputStream is = e.nextElement().openStream();
					PropertyResourceBundle propertyResourceBundle = new PropertyResourceBundle(is);
					resourceBundle.append(propertyResourceBundle);
				}
			} catch (IOException e) {
				log.error("Unable to load resource: " + name, e);
			}
		}
		
		// and build in the locale specific one
		// TODO
		
		// and set it's parent
		// TODO
		
		return resourceBundle;
	}

	
	@Override
	public void setBasenames(String[] basenames) {
		super.setBasenames(basenames);
		accessibleBasenames = basenames;
	}
	
	
}
