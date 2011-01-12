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
package org.gbif.provider.model.voc;

import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

public enum PublicationStatusForDisplay implements Serializable {
  PRIVATE("private"),
  UNPUBLISHED_CHANGES("unpublished changes"),
  PUBLISHED("published");

  public static final Map<String, String> htmlSelectMap;
  private final String name;

  static {
    Map<String, String> map = Maps.newHashMap();
    for (PublicationStatusForDisplay rt : PublicationStatusForDisplay.values()) {
      map.put(rt.name(), "publishedStatusType." + rt.name());
    }
    htmlSelectMap = Collections.unmodifiableMap(map);
  }

  /**
   * Returns a status created from a string description of the status. If the
   * description is null or if it's not a valid status name, null is returned.
   * 
   * @param status the status description
   * @return Role
   */
  public static PublicationStatusForDisplay fromString(String status) {
    if (status == null) {
      return null;
    }
    status = status.trim();
    for (PublicationStatusForDisplay r : PublicationStatusForDisplay.values()) {
      if (r.name.equalsIgnoreCase(status)) {
        return r;
      }
    }
    return null;
  }

  public static void main(String[] args) {
    for (PublicationStatusForDisplay rt : PublicationStatusForDisplay.values()) {
      System.out.printf("Name=%s, Status=%s\n", rt.getName(),
          PublicationStatusForDisplay.fromString(rt.getName()));
    }

  }

  private PublicationStatusForDisplay(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

}
