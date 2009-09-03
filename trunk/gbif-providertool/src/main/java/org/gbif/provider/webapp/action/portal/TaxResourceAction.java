/**
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

import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.StatusType;
import org.gbif.provider.webapp.action.BaseChecklistResourceAction;

import com.opensymphony.xwork2.Preparable;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * TODO: Documentation.
 * 
 */
public class TaxResourceAction extends BaseChecklistResourceAction implements
    Preparable {

  public static int width = OccResourceStatsAction.DEFAULT_WIDTH;
  public static int height = OccResourceStatsAction.DEFAULT_HEIGHT;

  public static int getHeight() {
    return height;
  }

  public static int getWidth() {
    return width;
  }

  private List<ChecklistResource> resources;
  private final Map<Integer, String> statusClasses = new TreeMap<Integer, String>();
  private final Map<Integer, String> ranks = new TreeMap<Integer, String>();

  @Override
  public String execute() {
    if (resource == null) {
      return RESOURCE404;
    }
    return SUCCESS;
  }

  public Map<Integer, String> getRanks() {
    return ranks;
  }

  public List<ChecklistResource> getResources() {
    return resources;
  }

  public Map<Integer, String> getStatusClasses() {
    return statusClasses;
  }

  public String list() {
    resources = resourceManager.getAll();
    return SUCCESS;
  }

  @Override
  public void prepare() {
    super.prepare();
    if (resource != null) {
      // geoserver map link
      // geoserverMapUrl = mapUtil.getGeoserverMapUrl(resourceId, width,
      // height, resource.getBbox(), null, null);
    }
    // prepare select lists
    for (StatusType rt : StatusType.values()) {
      statusClasses.put(rt.ordinal(), rt.name());
    }
    for (Rank rt : Rank.COMMON_RANKS) {
      ranks.put(rt.ordinal(), rt.name());
    }
  }

}