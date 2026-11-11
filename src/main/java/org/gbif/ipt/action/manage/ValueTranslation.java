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
package org.gbif.ipt.action.manage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

/**
 * Translation of a field's source values, as shown and submitted on the translation pages.
 * <p>
 * Each row has a key (e.g. "k1") that pairs its source value with its translated value. The keys are only meaningful
 * within one page: the form submits each row's source value together with its translation, so a save never depends on
 * the source values having been read in the same order as when the page was shown.
 */
class ValueTranslation {

  /**
   * Maximum number of distinct source values read from the source.
   */
  static final int MAX_DISTINCT_VALUES = 1000;

  /**
   * Maximum number of rows read from SQL sources when looking for distinct source values.
   */
  static final int MAX_SQL_ROWS = 10000;

  private static final Comparator<String> ORDER =
      String.CASE_INSENSITIVE_ORDER.thenComparing(Comparator.naturalOrder());

  // key -> source value, in display order
  private final Map<String, String> sourceValues = new LinkedHashMap<>();
  // key -> translated value
  private final Map<String, String> translatedValues = new LinkedHashMap<>();
  // keys of translated source values that were not found in the source
  private final Set<String> notInSource = new HashSet<>();
  private boolean truncated;

  /**
   * Populates the rows from the distinct values read from the source and the saved translation. Saved translations of
   * values not read from the source are kept, so that saving the page doesn't remove them.
   *
   * @param sourceSample distinct values read from the source, at most one more than {@link #MAX_DISTINCT_VALUES}
   * @param saved        saved translation, source value to translated value, can be null
   */
  void load(Collection<String> sourceSample, Map<String, String> saved) {
    sourceValues.clear();
    translatedValues.clear();
    notInSource.clear();

    List<String> sample = new ArrayList<>(sourceSample);
    truncated = sample.size() > MAX_DISTINCT_VALUES;
    if (truncated) {
      sample = sample.subList(0, MAX_DISTINCT_VALUES);
    }

    Set<String> sampleSet = new HashSet<>(sample);
    Set<String> values = new TreeSet<>(ORDER);
    values.addAll(sample);
    if (saved != null) {
      saved.keySet().stream().filter(Objects::nonNull).forEach(values::add);
    }

    int i = 1;
    for (String value : values) {
      String key = "k" + i++;
      sourceValues.put(key, value);
      if (saved != null && saved.containsKey(value)) {
        translatedValues.put(key, StringUtils.trimToEmpty(saved.get(value)));
        if (!sampleSet.contains(value)) {
          notInSource.add(key);
        }
      }
    }
  }

  /**
   * Translates each source value that isn't translated yet using the matcher.
   *
   * @param matcher returns the translated value for a source value, or null if there is no match
   * @return number of source values translated
   */
  int automap(Function<String, String> matcher) {
    int count = 0;
    for (Map.Entry<String, String> entry : sourceValues.entrySet()) {
      if (!translatedValues.containsKey(entry.getKey())) {
        String match = matcher.apply(entry.getValue());
        if (match != null) {
          translatedValues.put(entry.getKey(), match);
          count++;
        }
      }
    }
    return count;
  }

  /**
   * @return source value to translated value, for each row having both
   */
  Map<String, String> getPersistentMap() {
    Map<String, String> m = new TreeMap<>();
    for (Map.Entry<String, String> entry : translatedValues.entrySet()) {
      String sourceValue = sourceValues.get(entry.getKey());
      if (sourceValue != null) {
        m.put(sourceValue, StringUtils.trimToEmpty(entry.getValue()));
      }
    }
    return m;
  }

  /**
   * @return values compared ignoring case, whitespace, underscores and hyphens
   */
  static boolean looselyEquals(String a, String b) {
    return a != null && b != null && normalise(a).equals(normalise(b));
  }

  private static String normalise(String value) {
    return value.replaceAll("[\\s_-]", "").toLowerCase(Locale.ROOT);
  }

  /**
   * @return key to source value, e.g. {"k1", "obs"}
   */
  Map<String, String> getSourceValues() {
    return sourceValues;
  }

  /**
   * @return key to translated value, e.g. {"k1", "Observation"}
   */
  Map<String, String> getTranslatedValues() {
    return translatedValues;
  }

  /**
   * @return number of distinct values read from the source
   */
  int getSourceValueCount() {
    return sourceValues.size() - notInSource.size();
  }

  boolean isNotInSource(String key) {
    return notInSource.contains(key);
  }

  boolean isTruncated() {
    return truncated;
  }
}
