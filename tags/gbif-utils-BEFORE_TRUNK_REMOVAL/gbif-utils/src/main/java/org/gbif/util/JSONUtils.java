/**
 * 
 */
package org.gbif.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Utilities for dealing with JSON strings
 * 
 * This is built upon net.sf.json.JSONObject - therefore "class" is a reserved
 * word and cannot be used in the Map key
 * 
 * @author timrobertson
 */
public class JSONUtils {
	/**
	 * If the value is a map itself, then it will nest,
	 * otherwise toString on the value is called
	 * 
	 * This is built upon net.sf.json.JSONObject - therefore "class" is a reserved
	 * word and cannot be used in the Map key
	 */
	public static String jsonFromMap(Map<String, Object> map) {
		return JSONObject.fromObject(map).toString();
	}
	
	/**
	 * Builds the JSON back to the map
	 * @param JSON
	 * @return
	 */
	public static Map<String, Object> mapFromJSON(String JSON) {
		JSONObject jsonObject = JSONObject.fromObject(JSON);
		
		Map<String, Object> map = new HashMap<String, Object>();
		addToMap(map, jsonObject);
		return map;
	}
	
	// recursively add the maps
	protected static void addToMap(Map<String, Object> map, JSONObject jsonObject) {
		for (Object keyAsObject : jsonObject.keySet()) {
			Object o = jsonObject.get(keyAsObject);
			try {
				JSONObject nested = JSONObject.fromObject(o);
				
				if (nested.entrySet().size() == 0) { 
					if (o instanceof JSONObject && "{}".equals(nested.toString())) { // it is an empty nested collection - make it a map 
						map.put(keyAsObject.toString(), new HashMap<String, Object>());
					} else {
						// then it is not a nested object, but it is a non string type (e.g. Long)
						// we make everything a String...
						map.put(keyAsObject.toString(), o.toString());
					}
					
					
				} else {
					Map<String, Object> nestedMap = new HashMap<String, Object>();
					addToMap(nestedMap, nested);
					map.put(keyAsObject.toString(), nestedMap);
				}
				
			} catch (RuntimeException e) {
				// then it is not a nested object - but let's support list<String>... ONLY
				try {
					JSONArray dataAsArray = JSONArray.fromObject(o);
					List<String> dataAsList = new LinkedList<String>();
					for (Object data : dataAsArray) {
						dataAsList.add(data.toString());
					}
					map.put(keyAsObject.toString(), dataAsList);
					
				} catch (RuntimeException e1) {
					// it's just a String
					map.put(keyAsObject.toString(), o.toString());
				}
			}
		}
	}
}
