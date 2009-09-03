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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Documentation.
 * 
 */
public enum ResourceType {
  Specimen("Sample", "http://rs.tdwg.org/dwc/terms/Sample"), PreservedSpecimen(
      "Sample", "http://rs.tdwg.org/dwc/terms/Sample"), LivingSpecimen(
      "Sample", "http://rs.tdwg.org/dwc/terms/Sample"), FossilSpecimen(
      "Sample", "http://rs.tdwg.org/dwc/terms/Sample"), Observation("Sample",
      "http://rs.tdwg.org/dwc/terms/Sample"), MachineObservation("Sample",
      "http://rs.tdwg.org/dwc/terms/Sample"), HumanObservation("Sample",
      "http://rs.tdwg.org/dwc/terms/Sample"), MultimediaImage("Sample",
      "http://rs.tdwg.org/dwc/terms/Sample"), MultimediaMovie("Sample",
      "http://rs.tdwg.org/dwc/terms/Sample"), MultimediaSound("Sample",
      "http://rs.tdwg.org/dwc/terms/Sample"), Checklist("Taxon",
      "http://rs.tdwg.org/dwc/terms/Taxon"), RegionalChecklist("Taxon",
      "http://rs.tdwg.org/dwc/terms/Taxon"), NomenclatureChecklist("Taxon",
      "http://rs.tdwg.org/dwc/terms/Taxon"), TaxonomicChecklist("Taxon",
      "http://rs.tdwg.org/dwc/terms/Taxon"), LegislativeChecklist("Taxon",
      "http://rs.tdwg.org/dwc/terms/Taxon"), DescriptionChecklist("Taxon",
      "http://rs.tdwg.org/dwc/terms/Taxon"), DistributionChecklist("Taxon",
      "http://rs.tdwg.org/dwc/terms/Taxon");

  public static final String TAXON_GROUP = "Taxon";
  public static final String SAMPLE_GROUP = "Sample";

  public static final Map<String, String> htmlSelectMap;

  static {
    Map<String, String> map = new HashMap<String, String>();
    for (ResourceType rt : ResourceType.values()) {
      map.put(rt.name(), "resourceType." + rt.name());
    }
    htmlSelectMap = Collections.unmodifiableMap(map);
  }

  public static ResourceType byName(String name) {
    for (ResourceType rt : ResourceType.values()) {
      if (rt.name().equalsIgnoreCase(name)) {
        return rt;
      }
    }
    return null;
  }

  /**
   * @param group To use (TAXON_GROUP, SAMPLE_GROUP)
   * @return The resource types for that group only
   */
  public static Map<String, String> htmlSelectMap(String group) {
    Map<String, String> map = new HashMap<String, String>();
    for (ResourceType rt : ResourceType.values()) {
      if (rt.group.equals(group)) {
        map.put(rt.name(), "resourceType." + rt.name());
      }
    }
    return Collections.unmodifiableMap(map);
  }

  public String group;
  public String rowType;

  private ResourceType(String group, String rowType) {
    this.group = group;
    this.rowType = rowType;
  }
}
