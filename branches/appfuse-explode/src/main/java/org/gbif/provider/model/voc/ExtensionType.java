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

import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.Taxon;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Documentation.
 * 
 */
public enum ExtensionType {
  Occurrence(1L, OccurrenceResource.class, DarwinCore.class, "Darwin_Core",
      "occ"), Checklist(7L, ChecklistResource.class, Taxon.class, "Taxon",
      "tax"), Metadata(null, Resource.class, Resource.class, "Resource", "meta");

  public static final Map<String, String> htmlSelectMap;
  static {
    Map<String, String> map = new HashMap<String, String>();
    for (ExtensionType et : ExtensionType.values()) {
      map.put(et.alias, "resourceType." + et.alias);
    }
    htmlSelectMap = Collections.unmodifiableMap(map);
  }

  public static ExtensionType byCoreClass(Class coreClass) {
    for (ExtensionType et : ExtensionType.values()) {
      if (et.coreClass.isAssignableFrom(coreClass)) {
        return et;
      }
    }
    return null;
  }

  public static ExtensionType byResourceClass(Class resourceClass) {
    for (ExtensionType et : ExtensionType.values()) {
      if (et.resourceClass.isAssignableFrom(resourceClass)) {
        return et;
      }
    }
    return null;
  }

  public Long id;
  public Class resourceClass;
  public Class coreClass;
  public String tableName;
  public String alias;

  private ExtensionType(Long id, Class resourceClass, Class coreClass,
      String tableName, String alias) {
    this.id = id;
    this.resourceClass = resourceClass;
    this.coreClass = coreClass;
    this.tableName = tableName;
    this.alias = alias;
  }
}
