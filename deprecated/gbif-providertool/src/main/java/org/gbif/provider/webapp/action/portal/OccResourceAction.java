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
package org.gbif.provider.webapp.action.portal;

import org.gbif.provider.geo.MapUtil;
import org.gbif.provider.model.BBox;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.voc.HostType;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;
import org.gbif.provider.webapp.action.BaseOccurrenceResourceAction;

import com.opensymphony.xwork2.Preparable;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * TODO: Documentation.
 * 
 */
public class OccResourceAction extends BaseOccurrenceResourceAction implements
    Preparable {
  public static int width = OccResourceStatsAction.DEFAULT_WIDTH;

  public static int height = OccResourceStatsAction.DEFAULT_HEIGHT;

  public static int getHeight() {
    return height;
  }

  public static int getWidth() {
    return width;
  }

  public String geoserverMapUrl;
  public String geoserverMapBBox;

  @Autowired
  private MapUtil mapUtil;
  private List<OccurrenceResource> resources;
  private final Map<Integer, String> countryClasses = new TreeMap<Integer, String>();
  private final Map<Integer, String> regionClasses = new TreeMap<Integer, String>();
  private final Map<Integer, String> ranks = new TreeMap<Integer, String>();
  private final Map<Integer, String> hostTypes = new TreeMap<Integer, String>();

  @Override
  public String execute() {
    if (resource == null) {
      return RESOURCE404;
    }
    return SUCCESS;
  }

  public Map<Integer, String> getCountryClasses() {
    return countryClasses;
  }

  public String getGeoserverMapBBox() {
    return geoserverMapBBox;
  }

  public String getGeoserverMapUrl() {
    return geoserverMapUrl;
  }

  public Map<Integer, String> getHostTypes() {
    return hostTypes;
  }

  public Map<Integer, String> getRanks() {
    return ranks;
  }

  public Map<Integer, String> getRegionClasses() {
    return regionClasses;
  }

  public List<OccurrenceResource> getResources() {
    return resources;
  }

  public String list() {
    resources = occResourceManager.getAll();
    return SUCCESS;
  }

  @Override
  public void prepare() {
    super.prepare();
    if (resource != null) {
      // geoserver map link
      geoserverMapUrl = mapUtil.getWMSGoogleMapUrl(resourceId, null, null);
      if (resource.getBbox() != null && resource.getBbox().isValid()) {
        geoserverMapBBox = resource.getBbox().toStringWMS();
      } else {
        geoserverMapBBox = BBox.newWorldInstance().toStringWMS();
      }
    }
    // prepare select lists
    countryClasses.put(1, "occurrences");
    countryClasses.put(2, "distinct taxa");
    for (RegionType rt : RegionType.DARWIN_CORE_REGIONS) {
      regionClasses.put(rt.ordinal(), rt.name());
    }
    for (Rank rt : Rank.DARWIN_CORE_HIGHER_RANKS) {
      ranks.put(rt.ordinal(), rt.name());
    }
    ranks.put(Rank.TerminalTaxon.ordinal(), "All Taxa");

    // hosting bodies
    for (HostType ht : HostType.values()) {
      hostTypes.put(ht.ordinal(), ht.name());
    }
  }
}