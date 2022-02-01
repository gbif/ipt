/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.ipt.utils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MapUtils {

  private MapUtils() {
  }

  /**
   * Iterates over a map, and copies each entry to a new Map. The only difference, is that the
   * key is replaced with an all lowercase key instead.
   *
   * All keys in Map are converted to lowercase in order to standardize keys across different versions of the IPT, as
   * well as to facilitate grouping of subtypes.
   *
   * @param m map
   *
   * @return modified map
   */
  public static Map<String, String> getMapWithLowercaseKeys(Map<String, String> m) {
    Map<String, String> copy = new LinkedHashMap<>();
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
    m.entrySet().removeIf(entry -> !ls.contains(entry.getKey().toLowerCase()));
    return m;
  }
}
