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
 * TODO: Documentation.
 * 
 */
public enum Role implements Serializable {
  ORIGINATOR("Originator"),
  AUTHOR("Author"),
  CONTENT_PROVIDER("ContentProvider"),
  CUSTODIAN_STEWARD("CustodianSteward"),
  DISTRIBUTOR("Distributor"),
  EDITOR("Editor"),
  METADATA_PROVIDER("MetadataProvider"),
  OWNER("Owner"),
  POINT_OF_CONTACT("PointOfContact"),
  PRINCIPAL_INVESTIGATOR("PrincipleInvestigator"),
  PROCESSOR("Processor"),
  PUBLISHER("Publisher"),
  USER("User"),
  FIELD_STATION_MANAGER("FieldStationManager"),
  INFORMATION_MANAGER("InformationManager"),
  RESPONSIBLE_PARTY("ResponsibleParty"),
  ASSOCIATED_PARTY("AssociatedParty");

  public static final Map<String, String> htmlSelectMap;
  private final String name;

  static {
    Map<String, String> map = Maps.newHashMap();
    for (Role rt : Role.values()) {
      map.put(rt.name(), "roleType." + rt.name());
    }
    htmlSelectMap = Collections.unmodifiableMap(map);
  }

  /**
   * Returns a role created from a string description of the role. If the
   * description is null or if it's not a valid role name, null is returned.
   * 
   * @param role the role description
   * @return Role
   */
  public static Role fromString(String role) {
    if (role == null) {
      return null;
    }
    role = role.trim();
    for (Role r : Role.values()) {
      if (r.name.equalsIgnoreCase(role)) {
        return r;
      }
    }
    return null;
  }

  public static void main(String[] args) {
    for (Role rt : Role.values()) {
      System.out.printf("Name=%s, Role=%s\n", rt.getName(),
          Role.fromString(rt.getName()));
    }

  }

  private Role(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

}
