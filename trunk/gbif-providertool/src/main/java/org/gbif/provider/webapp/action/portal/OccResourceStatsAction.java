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

import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.voc.HostType;
import org.gbif.provider.model.voc.ImageType;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;
import org.gbif.provider.service.OccResourceManager;

import com.opensymphony.xwork2.Preparable;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * ActionClass to generate the data for a single occurrence resource statistic
 * with chart image and data Can be parameterized with: "zoom" : return the
 * largest image possible if true. Defaults to false "title" : set title in
 * image? Defaults to false
 * 
 */
public class OccResourceStatsAction extends
    ResourceStatsBaseAction<OccurrenceResource> implements Preparable {
  private final OccResourceManager occResourceManager;

  @Autowired
  public OccResourceStatsAction(OccResourceManager occResourceManager) {
    this.resourceManager = occResourceManager;
    this.occResourceManager = occResourceManager;
  }

  public String statsByBasisOfRecord() {
    if (!useCachedImage(ImageType.ChartByBasisOfRecord)) {
      data = occResourceManager.occByBasisOfRecord(resourceId);
      String url = occResourceManager.occByBasisOfRecordPieUrl(data, width,
          height, title);
      cacheImage(ImageType.ChartByBasisOfRecord, url);
    }
    return PIE_RESULT;
  }

  // MAPS
  public String statsByCountry() {
    setMapSize();
    if (type == 2) {
      // TODO: link to list of taxon found in that country
      recordAction = "";
    } else {
      recordAction = "occRegion";
    }
    if (!useCachedImage(ImageType.CountryMapOfOccurrence)) {
      String url;
      if (type == 2) {
        data = occResourceManager.taxaByRegion(resourceId, RegionType.Country);
        url = occResourceManager.taxaByCountryMapUrl(
            occResourceManager.getMapArea(area), data, width, height);
      } else {
        data = occResourceManager.occByRegion(resourceId, RegionType.Country,
            filter);
        url = occResourceManager.occByCountryMapUrl(
            occResourceManager.getMapArea(area), data, width, height);
      }
      cacheImage(ImageType.CountryMapOfOccurrence, url);
    }
    return MAP_RESULT;
  }

  public String statsByDateColected() {
    if (!useCachedImage(ImageType.ChartByDateCollected)) {
      data = occResourceManager.occByDateColected(resourceId);
      String url = occResourceManager.occByDateColectedUrl(data, width, height,
          title);
      cacheImage(ImageType.ChartByDateCollected, url);
    }
    return CHART_RESULT;
  }

  public String statsByHost() {
    types = HostType.values();
    if (!useCachedImage(ImageType.ChartByHost)) {
      HostType ht = HostType.getByInt(type);
      data = occResourceManager.occByHost(resourceId, ht);
      String url = occResourceManager.occByHostPieUrl(data, ht, width, height,
          title);
      cacheImage(ImageType.ChartByHost, url);
    }
    return PIE_RESULT;
  }

  public String statsByRegion() {
    recordAction = "occRegion";
    types = RegionType.DARWIN_CORE_REGIONS.toArray();
    if (!useCachedImage(ImageType.ChartByRegion)) {
      RegionType reg = RegionType.getByInt(type);
      data = occResourceManager.occByRegion(resourceId, reg, filter);
      String url = occResourceManager.occByRegionPieUrl(data, reg, width,
          height, title);
      cacheImage(ImageType.ChartByRegion, url);
    }
    return PIE_RESULT;
  }

  public String statsByTaxon() {
    recordAction = "occTaxon";
    List<Rank> ranks = new ArrayList<Rank>(Rank.DARWIN_CORE_HIGHER_RANKS);
    ranks.add(Rank.TerminalTaxon);
    types = ranks.toArray();
    if (!useCachedImage(ImageType.ChartByTaxon)) {
      Rank rnk = Rank.getByInt(type);
      data = occResourceManager.occByTaxon(resourceId, rnk);
      String url = occResourceManager.occByTaxonPieUrl(data, rnk, width,
          height, title);
      cacheImage(ImageType.ChartByTaxon, url);
    }
    return PIE_RESULT;
  }

}