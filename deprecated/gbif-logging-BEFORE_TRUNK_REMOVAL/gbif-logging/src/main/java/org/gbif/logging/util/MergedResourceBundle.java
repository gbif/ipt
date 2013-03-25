package org.gbif.logging.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * A resource bundle that is allowed to grow and is based on a Map
 * This class is not threadsafe for writing
 * @author trobertson
 */
public class MergedResourceBundle extends ResourceBundle {
	// The properties
	protected Map<String, Object> properties = new HashMap<String, Object>();

	/**
	 * Empty bundle
	 */
	public MergedResourceBundle() {
	}

	/**
	 * @param resourceBundle To copy into this new bundle
	 */
	public MergedResourceBundle(ResourceBundle resourceBundle) {
		merge(resourceBundle);
	}

	/**
	 * @param resourceBundle To copy into this bundle
	 */
	public void merge(ResourceBundle resourceBundle) {
		Enumeration<String> keys = resourceBundle.getKeys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			properties.put(key, resourceBundle.getString(key));
		}
	}

	/**
	 * Let's open this up
	 */
	@Override
	public void setParent(ResourceBundle parent) {
		super.setParent(parent);
	}

	/**
	 * Gets the keys in the map
	 */
	@Override
	public Enumeration<String> getKeys() {
		return Collections.enumeration(properties.keySet());
	}

	/**
	 * Gets the object from the map
	 */
	@Override
	protected Object handleGetObject(String key) {
		return properties.get(key);
	}
}
