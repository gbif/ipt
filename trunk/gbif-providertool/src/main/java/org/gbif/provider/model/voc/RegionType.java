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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TODO: Documentation.
 * 
 */
public enum RegionType {
  Continent("continent"), Waterbody("waterbody"), IslandGroup("islandGroup"), Island(
      "island"), Country("country"), State("stateProvince"), County("county"), Locality(
      "locality");

  public static final String URI = "http://rs.tdwg.org/ontology/voc/TaxonRank";

  public static final List<RegionType> DARWIN_CORE_REGIONS;
  static {
    List<RegionType> dwc = new ArrayList<RegionType>();
    dwc.add(Continent);
    dwc.add(Waterbody);
    dwc.add(IslandGroup);
    dwc.add(Island);
    dwc.add(Country);
    dwc.add(State);
    dwc.add(County);
    DARWIN_CORE_REGIONS = Collections.unmodifiableList(dwc);
  };

  public static RegionType getByInt(int i) {
    for (RegionType r : RegionType.values()) {
      if (r.ordinal() == i) {
        return r;
      }
    }
    return null;
  }

  public String columnName;

  private RegionType(String colName) {
    columnName = colName;
  }

}
