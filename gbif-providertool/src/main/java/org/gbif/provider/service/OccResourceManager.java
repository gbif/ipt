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
package org.gbif.provider.service;

import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.dto.StatsCount;
import org.gbif.provider.model.voc.HostType;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;

import com.googlecode.gchartjava.GeographicalArea;

import java.util.List;

/**
 * TODO: Documentation.
 * 
 */
public interface OccResourceManager extends
    GenericResourceManager<OccurrenceResource> {
  // helper
  GeographicalArea getMapArea(String area);

  List<StatsCount> occByBasisOfRecord(Long resourceId);

  String occByBasisOfRecordPieUrl(List<StatsCount> data, int width, int height,
      boolean title);

  String occByBasisOfRecordPieUrl(Long resourceId, int width, int height,
      boolean title);

  String occByCountryMapUrl(GeographicalArea area, List<StatsCount> data,
      int width, int height);

  // maps
  String occByCountryMapUrl(GeographicalArea area, Long resourceId, int width,
      int height);

  List<StatsCount> occByDateColected(Long resourceId);

  String occByDateColectedUrl(List<StatsCount> data, int width, int height,
      boolean title);

  // line
  String occByDateColectedUrl(Long resourceId, int width, int height,
      boolean title);

  List<StatsCount> occByHost(Long resourceId, HostType ht);

  String occByHostPieUrl(List<StatsCount> data, HostType ht, int width,
      int height, boolean title);

  String occByHostPieUrl(Long resourceId, HostType ht, int width, int height,
      boolean title);

  List<StatsCount> occByRegion(Long resourceId, RegionType region,
      Long taxonIdFilter);

  String occByRegionPieUrl(List<StatsCount> data, RegionType region, int width,
      int height, boolean title);

  // pie charts
  String occByRegionPieUrl(Long resourceId, RegionType region, int width,
      int height, boolean title);

  List<StatsCount> occByTaxon(Long resourceId, Rank rank);

  String occByTaxonPieUrl(List<StatsCount> data, Rank rank, int width,
      int height, boolean title);

  String occByTaxonPieUrl(Long resourceId, Rank rank, int width, int height,
      boolean title);

  OccurrenceResource setResourceStats(OccurrenceResource resource);

  String taxaByCountryMapUrl(GeographicalArea area, List<StatsCount> data,
      int width, int height);

  String taxaByCountryMapUrl(GeographicalArea area, Long resourceId, int width,
      int height);

  // DATA
  List<StatsCount> taxaByRegion(Long resourceId, RegionType region);

}
