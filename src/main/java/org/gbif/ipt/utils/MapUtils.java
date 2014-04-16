package org.gbif.ipt.utils;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MapUtils {

  /**
   * Empty constructor.
   */
  private MapUtils() {
  }

  /**
   * Iterates over a map, and copies each entry to a new Map. The only difference, is that the
   * key is replaced with an all lowercase key instead.
   *
   * @param m map
   *
   * @return modified map
   */
  public static Map<String, String> getMapWithLowercaseKeys(Map<String, String> m) {
    Map<String, String> copy = new LinkedHashMap<String, String>();
    for (Map.Entry<String, String> entry : m.entrySet()) {
      copy.put(entry.getKey().toLowerCase(), entry.getValue());
    }
    return copy;
  }

  /**
   * Iterates over a map, and removes each entry whose key isn't present in a list. Comparison is done in lowercase.
   *
   * @param m map
   * @param ls list
   *
   * @return modified map
   */
  public static Map<String, String> removeNonMatchingKeys(Map<String, String> m, List<String> ls) {
    Iterator<Map.Entry<String, String>> iter = m.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry<String, String> entry = iter.next();
      if (!ls.contains(entry.getKey().toLowerCase())) {
        iter.remove();
      }
    }
    return m;
  }
}
