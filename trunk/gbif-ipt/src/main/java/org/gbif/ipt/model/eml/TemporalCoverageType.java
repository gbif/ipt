/*
 * Copyright 2009 GBIF.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gbif.ipt.model.eml;

import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * Enumeration of temporal coverage types.
 * 
 */
public enum TemporalCoverageType implements Serializable {
    SINGLE_DATE("singleDateTime"),
    DATE_RANGE("dateRange"),
    FORMATION_PERIOD("formationPeriod"),
    LIVING_TIME_PERIOD("livingTimePeriod");
  
  public static final Map<String, String> htmlSelectMap;
  private final String name;

  static {
    Map<String, String> map = Maps.newHashMap();
    for (TemporalCoverageType rt : TemporalCoverageType.values()) {
      map.put(rt.name(), "temporalCoverageType." + rt.name());
    }
    htmlSelectMap = Collections.unmodifiableMap(map);
  }

  /**
   * Returns a TemporalCoverageType created from a string description of the type. If the
   * description is null or if it's not a valid TemporalCoverageType name, null is returned.
   * 
   * @param temporalCoverageType the temporal coverage type as a string
   * @return TemporalCoverageType
   */
  public static TemporalCoverageType fromString(String type) {
    if (type == null) {
      return null;
    }
    type = type.trim();
    for (TemporalCoverageType r : TemporalCoverageType.values()) {
      if (r.name.equalsIgnoreCase(type)) {
        return r;
      }
    }
    return null;
  }

  public static void main(String[] args) {
    for (TemporalCoverageType rt : TemporalCoverageType.values()) {
      System.out.printf("Name=%s, Type=%s\n", rt.getName(),
          TemporalCoverageType.fromString(rt.getName()));
    }
  }

  private TemporalCoverageType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}