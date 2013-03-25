package org.gbif.logging.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * A factory for creating merged resource bundles
 * Once read the ResourceBundle is cached for eternity
 * @author trobertson
 */
public class MergedResourceBundleFactory {
	protected String[] accessibleBasenames;
	protected Map<Locale, ResourceBundle> cache = new HashMap<Locale, ResourceBundle>();
	
	public MergedResourceBundleFactory() {		
	}
	
	public MergedResourceBundleFactory(String[] accessibleBasenames) {
		this.accessibleBasenames = accessibleBasenames;
	}
	
	public ResourceBundle getBundle() throws IOException {
		return getBundle(Locale.getDefault());
		
	}
	
	public synchronized ResourceBundle getBundle(Locale locale) throws IOException {
		if (locale == null) {
			locale = Locale.getDefault();
		}
		
		if (cache.containsKey(locale)) {
			return cache.get(locale);
		}
		
		ResourceBundle base = null;
		ResourceBundle parent = null;
		
		String language = locale.getLanguage();
		String country = locale.getCountry();
		String variant = locale.getVariant();
		
		// build the base
		List<String> resourceFiles = new LinkedList<String>();
		for (String name : accessibleBasenames) {
			resourceFiles.add(name + ".properties");
		}
		MergedResourceBundle bundle = getBundle(resourceFiles);
		base = bundle;
		parent = bundle; 
		
		// build the language
		if (language != null && language.length()>0) {
			resourceFiles = new LinkedList<String>();
			for (String name : accessibleBasenames) {
				resourceFiles.add(name + "_" + language + ".properties");
			}
			bundle = getBundle(resourceFiles);
			if (bundle != null) {
				bundle.setParent(parent);
				parent = bundle;
				base = bundle;
			}
		}		
		
		// build the country
		if (country != null && country.length()>0 &&
				language != null && language.length()>0) {
			resourceFiles = new LinkedList<String>();
			for (String name : accessibleBasenames) {
				resourceFiles.add(name + "_" + language + "_" + country + ".properties");
			}
			bundle = getBundle(resourceFiles);
			if (bundle != null) {
				bundle.setParent(parent);
				parent = bundle;
				base = bundle;
			}
		}
		
		// build the variant
		if (variant != null && variant.length()>0 &&
				country != null && country.length()>0 &&
				language != null && language.length()>0) {
			resourceFiles = new LinkedList<String>();
			for (String name : accessibleBasenames) {
				resourceFiles.add(name + "_" + language + "_" + country + "_" + variant + ".properties");
			}
			bundle = getBundle(resourceFiles);
			if (bundle != null) {
				bundle.setParent(parent);
				base = bundle;
			}			
		}	
		
		if (base == null) {
			StringBuffer sb = new StringBuffer();
			for (int i=0; i<accessibleBasenames.length; i++) {
				sb.append(accessibleBasenames[i] + ".properties");
				if (i<accessibleBasenames.length-1)
					sb.append(",");
			}
			throw new MissingResourceException(sb.toString(), sb.toString(), sb.toString());
		}
		
		cache.put(locale, base);
		return base;
	}
	
	/**
	 * @param resourceFiles To look for on the CP
	 * @return The bundle which is a merged instance of all the properties found
	 * @throws IOException On error
	 */
	protected static MergedResourceBundle getBundle(List<String> resourceFiles) throws IOException {
		MergedResourceBundle bundle = null;
		for (String name : resourceFiles) {
			//System.out.println(name);
            Enumeration<URL> e = MergedResourceBundleFactory.class.getClassLoader().getResources(name);
            while (e.hasMoreElements()) {
            	URL url = e.nextElement();
            	System.out.println(url.toString());
                InputStream is = url.openStream();
                PropertyResourceBundle propertyResourceBundle = new PropertyResourceBundle(is);
                
                if (bundle==null) {
                	bundle = new MergedResourceBundle(propertyResourceBundle);
                } else {
                	bundle.merge(propertyResourceBundle);
                }
            }
		}
		return bundle;		
	}

	public String[] getAccessibleBasenames() {
		return accessibleBasenames;
	}

	public void setAccessibleBasenames(String[] accessibleBasenames) {
		this.accessibleBasenames = accessibleBasenames;
	}
}
