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
package org.gbif.provider.model.eml;

import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * Enumeration of Method types.
 * 
 */
public enum MethodType implements Serializable {
    METHOD_STEP("methodStep"),
    SAMPLING("sampling"),
    QUALITY_CONTROL("qualityControl");
  
  public static final Map<String, String> htmlSelectMap;
  private final String name;

  static {
    Map<String, String> map = Maps.newHashMap();
    for (MethodType rt : MethodType.values()) {
      map.put(rt.name(), "methodType." + rt.name());
    }
    htmlSelectMap = Collections.unmodifiableMap(map);
  }

  /**
   * Returns a methodType created from a string description of the type. If the
   * description is null or if it's not a valid MethodType name, null is returned.
   * 
   * @param methodType the Methjod type as a string
   * @return MethodType
   */
  public static MethodType fromString(String type) {
    if (type == null) {
      return null;
    }
    type = type.trim();
    for (MethodType r : MethodType.values()) {
      if (r.name.equalsIgnoreCase(type)) {
        return r;
      }
    }
    return null;
  }

  public static void main(String[] args) {
    for (MethodType rt : MethodType.values()) {
      System.out.printf("Name=%s, Type=%s\n", rt.getName(),
          MethodType.fromString(rt.getName()));
    }
  }

  private MethodType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}