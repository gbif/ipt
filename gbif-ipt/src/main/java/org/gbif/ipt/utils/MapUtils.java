package org.gbif.ipt.utils;

import java.util.LinkedHashMap;
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
   * @return modified map
   */
  public static Map<String, String> getMapWithLowercaseKeys(Map<String, String> m) {
    Map<String, String> copy = new LinkedHashMap<String, String>();
    for (Map.Entry<String, String> entry : m.entrySet()) {
      copy.put(entry.getKey().toLowerCase(), entry.getValue());
    }
    return copy;
  }
}
