/**
 * 
 */
package org.gbif.logging.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Resource bundle that is built upon a Map and allows itself to grow.
 * Therefore, one can read in property files from varies places and collate them
 * into one bundle for example
 * @author timrobertson
 */
public class AppendablePropertyResourceBundle extends ResourceBundle {
	protected Log log = LogFactory.getLog(this.getClass());
	protected Map<String, String> properties = new HashMap<String,String>();

	public AppendablePropertyResourceBundle() {
	}

	public AppendablePropertyResourceBundle(ResourceBundle resourceBundle) {
		append(resourceBundle);
	}	
	

	public void append(ResourceBundle resourceBundle) {
		Enumeration<String> keys = resourceBundle.getKeys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			properties.put(key, resourceBundle.getString(key));
		}
	}
	
	@Override
	public Enumeration<String> getKeys() {
		return Collections.enumeration(properties.keySet());
	}

	@Override
	protected Object handleGetObject(String key) {
		return properties.get(key);
	}
}
