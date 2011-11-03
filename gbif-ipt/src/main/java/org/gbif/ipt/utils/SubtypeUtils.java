/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.ipt.utils;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Temporary class until a vocabulary is created.
 */
public class SubtypeUtils {

  public static final Map<String, String> subtypeList = new LinkedHashMap<String, String>();

  static {
    // Checklist
    subtypeList.put("Select Checklist", "");
    subtypeList.put("Regional inventory", "Regional inventory");
    subtypeList.put("Thematic inventory", "Thematic inventory");
    subtypeList.put("Taxonomic authority", "Taxonomic authority");
    subtypeList.put("Nomenclator authority", "Nomenclator authority");
    subtypeList.put("Derived from occurrence data", "Derived from occurrence data");
    // Occurrence
    subtypeList.put("Select Occurrence", "");
    subtypeList.put("Specimen", "Specimen");
    subtypeList.put("Observation", "Observation");
  }

  public static Map<String, String> checklistSubtypeList() {
    Map<String, String> newSubtypeList = new LinkedHashMap<String, String>();
    for (Entry<String, String> entry : subtypeList.entrySet()) {
      if (entry.getKey().equals("Select Occurrence")) {
        break;
      } else {
        newSubtypeList.put(entry.getValue(), entry.getKey());
      }
    }
    return newSubtypeList;
  }

  public static Map<String, String> noSubtypeList() {
    Map<String, String> newSubtypeList = new LinkedHashMap<String, String>();
    newSubtypeList.put("", "No subtype");
    return newSubtypeList;
  }

  public static Map<String, String> occurrenceSubtypeList() {
    Map<String, String> newSubtypeList = new LinkedHashMap<String, String>();
    Iterator<Entry<String, String>> entries = subtypeList.entrySet().iterator();
    boolean var = false;
    while (entries.hasNext()) {
      Entry<String, String> entry = entries.next();
      if (entry.getKey().equals("Select Occurrence") || var) {
        newSubtypeList.put(entry.getValue(), entry.getKey());
        var = true;
      }
    }
    return newSubtypeList;
  }
}
