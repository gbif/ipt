package org.gbif.ipt.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
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

  @Test
  public void testRemoveNonMatchingKeys() {
    List<String> ls = new ArrayList<String>();
    ls.add("occurrence");
    // only 1 key, only 1 match, so only 1 entry should be left over
    map = MapUtils.removeNonMatchingKeys(map, ls);
    assertEquals(1, map.size());
    assertTrue(map.keySet().contains("OCCURRENCE"));
  }
}
