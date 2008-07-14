package org.gbif.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;


public class JSONUtilsTest {
	Log logger = LogFactory.getLog(this.getClass());
	
	@SuppressWarnings("unchecked")
	@Test
	public void testAll() {
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("Long", 23L);
		map.put("Integer", new Integer(23));
		map.put("k1", "v1");
		map.put("k2:1", "v2{Tim}");
		Map<String, Object> map2 = new HashMap<String, Object>();
		map2.put("ik1", "iv1:1");
		map2.put("ik2", "iv2");
		map.put("k3", map2);
		
		logger.info("map: " + map);
		
		String asJSON = JSONUtils.jsonFromMap(map);
		logger.info("as JSON: " + asJSON);
		
		Map<String, Object> deserialized = JSONUtils.mapFromJSON(asJSON);
		logger.info("deserialized map: " + deserialized);
		assertTrue(deserialized.containsKey("Long"));
		assertTrue(deserialized.containsKey("Integer"));
		assertTrue(deserialized.containsKey("k1"));
		assertTrue(deserialized.containsKey("k2:1"));
		assertTrue(deserialized.containsKey("k3"));
		assertEquals("23", deserialized.get("Long"));
		assertEquals("23", deserialized.get("Integer"));
		assertEquals(deserialized.get("Long"), deserialized.get("Integer"));
		assertEquals("v1", deserialized.get("k1"));
		assertEquals("v2{Tim}", deserialized.get("k2:1"));
		assertTrue(deserialized.get("k3") instanceof Map);
		Map<String, Object> inner = (Map<String, Object>) deserialized.get("k3");
		assertTrue(inner.containsKey("ik1"));
		assertTrue(inner.containsKey("ik2"));
		assertEquals("iv1:1", inner.get("ik1"));
		assertEquals("iv2", inner.get("ik2"));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testWithList() {
		String asJSON = "{\"list\":[\"1\",\"2\",\"3\"],\"test\":\"test\"}";
		Map<String, Object> deserialized = JSONUtils.mapFromJSON(asJSON);
		logger.info("deserialized map: " + deserialized);
		assertTrue(deserialized.containsKey("list"));
		assertTrue(deserialized.containsKey("test"));
		assertEquals("test", deserialized.get("test"));
		assertTrue(deserialized.get("list") instanceof List);
		assertTrue(((List)deserialized.get("list")).size() == 3);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testWithListSerializeAndDeserialize() {
		List<String> list = new LinkedList<String>();
		list.add("tim");
		list.add("tom");
		list.add("tam");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("list", list);
		
		String asJSON = JSONUtils.jsonFromMap(map);
		logger.info("As JSON: " + asJSON);
		
		Map<String, Object> deserialized = JSONUtils.mapFromJSON(asJSON);
		assertTrue(deserialized.containsKey("list"));
		assertTrue(deserialized.get("list") instanceof List);
		assertTrue(((List)deserialized.get("list")).size() == 3);
		assertEquals("tom", ((List)deserialized.get("list")).get(1));
		
		
		String[] messageParams = {"1", "2"};
		Map<String, Object> data = new HashMap<String, Object>();
		list = new LinkedList<String>();
		for (String param : messageParams) {
			list.add(param);
		}
		data.put("list", list);
		logger.info("delme: " + JSONUtils.jsonFromMap(data));
		
	}	
	
	@SuppressWarnings("unchecked")
	@Test
	public void testWithMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("name", "datasource[1]");
		Map<String, String> params = new HashMap<String, String>();
		params.put("id", "1");
		params.put("operation", "inventory");
		map.put("params", params);
		
		String asJSON = JSONUtils.jsonFromMap(map);
		logger.info("As JSON: " + asJSON);
		
		Map<String, Object> deserialized = JSONUtils.mapFromJSON(asJSON);
		assertTrue(deserialized.containsKey("name"));
		assertEquals("datasource[1]", deserialized.get("name"));
		assertTrue(deserialized.containsKey("params"));
		assertTrue(deserialized.get("params") instanceof Map);
		assertTrue(((Map)deserialized.get("params")).containsKey("id"));
		assertEquals("1", ((Map)deserialized.get("params")).get("id"));
		assertTrue(((Map)deserialized.get("params")).containsKey("operation"));
		assertEquals("inventory", ((Map)deserialized.get("params")).get("operation"));
		
	}
	
	@Test
	public void testEmpty() {
		String asJSON = JSONUtils.jsonFromMap(new HashMap<String, Object>());
		logger.info("As JSON: " + asJSON);
		Map<String, Object> deserialized = JSONUtils.mapFromJSON(asJSON);
		assertTrue(deserialized.isEmpty());
		
		Map<String, Object> map = new HashMap<String,Object>();
		map.put("params", deserialized);
		
		asJSON = JSONUtils.jsonFromMap(map);
		logger.info("As JSON: " + asJSON);
		deserialized = JSONUtils.mapFromJSON(asJSON);
		assertTrue(deserialized.containsKey("params"));
		logger.info("Params: " + deserialized.get("params"));
		assertTrue(deserialized.get("params") instanceof Map);
		assertTrue(((Map)deserialized.get("params")).isEmpty());
		
	}

}
