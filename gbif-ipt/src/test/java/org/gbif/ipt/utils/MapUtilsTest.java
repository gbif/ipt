package org.gbif.ipt.utils;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class MapUtilsTest {

  private Map<String, String> map;

  @Before
  public void setup() {
    map = new HashMap<String, String>();
    map.put("OCCURRENCE", "OCCURRENCE");
    map.put("CHECKLIST", "CHECKLIST");
  }

  @Test
  public void testGetMapWithLowercaseKeys() {
    map = MapUtils.getMapWithLowercaseKeys(map);
    assertTrue(map.keySet().contains("occurrence"));
    assertTrue(map.keySet().contains("checklist"));
  }
}
