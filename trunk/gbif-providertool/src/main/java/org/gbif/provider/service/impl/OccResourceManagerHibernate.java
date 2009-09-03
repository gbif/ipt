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
package org.gbif.provider.service.impl;

import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.dto.StatsCount;
import org.gbif.provider.model.voc.HostType;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.service.RegionManager;
import org.gbif.provider.util.StatsUtils;

import com.googlecode.gchartjava.GeographicalArea;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

/**
 * TODO: Documentation.
 * 
 */
public class OccResourceManagerHibernate extends
    DataResourceManagerHibernate<OccurrenceResource> implements
    OccResourceManager {

  @Autowired
  private GeoserverManagerImpl geoTools;
  @Autowired
  private RegionManager regionManager;

  public OccResourceManagerHibernate() {
    super(OccurrenceResource.class);
  }

  public GeographicalArea getMapArea(String area) {
    GeographicalArea a;
    try {
      a = GeographicalArea.valueOf(area.toUpperCase());
    } catch (IllegalArgumentException e) {
      a = GeographicalArea.WORLD;
    }
    return a;
  }

  public List<StatsCount> occByBasisOfRecord(Long resourceId) {
    // get data from db
    List<Object[]> occBySth = getSession().createQuery(
        "select dwc.basisOfRecord, count(dwc)   from DarwinCore dwc   where dwc.resource.id = :resourceId   group by dwc.basisOfRecord").setParameter(
        "resourceId", resourceId).list();
    return StatsUtils.getDataMap(occBySth);
  }

  public String occByBasisOfRecordPieUrl(List<StatsCount> data, int width,
      int height, boolean title) {
    String titleText = null;
    if (title) {
      titleText = "Occurrences By BasisOfRecord";
    }
    // get chart string
    data = limitDataForChart(data);
    return gpb.generatePieChartUrl(width, height, titleText, data);
  }

  public String occByBasisOfRecordPieUrl(Long resourceId, int width,
      int height, boolean title) {
    List<StatsCount> data = occByBasisOfRecord(resourceId);
    return occByBasisOfRecordPieUrl(data, width, height, title);
  }

  public String occByCountryMapUrl(GeographicalArea area,
      List<StatsCount> data, int width, int height) {
    // get chartmap string
    return gpb.generateMapChartUrl(width, height, data, area);
  }

  public String occByCountryMapUrl(GeographicalArea area, Long resourceId,
      int width, int height) {
    List<StatsCount> data = occByRegion(resourceId, RegionType.Country, null);
    return occByCountryMapUrl(area, data, width, height);
  }

  public List<StatsCount> occByDateColected(Long resourceId) {
    // get data from db
    List<Object[]> occBySth = getSession().createQuery(
        "select year(dwc.collected), count(dwc)   from DarwinCore dwc   where dwc.resource.id = :resourceId   group by year(dwc.collected)").setParameter(
        "resourceId", resourceId).list();
    return StatsUtils.getDataMap(occBySth);
  }

  public String occByDateColectedUrl(List<StatsCount> data, int width,
      int height, boolean title) {
    String titleText = null;
    if (title) {
      titleText = "Occurrences By DateCollected";
    }
    // get chart string
    String chartUrl = gpb.generateChronoChartUrl(width, height, titleText, data);
    return chartUrl;
  }

  public String occByDateColectedUrl(Long resourceId, int width, int height,
      boolean title) {
    List<StatsCount> data = occByDateColected(resourceId);
    return occByDateColectedUrl(data, width, height, title);
  }

  public List<StatsCount> occByHost(Long resourceId, HostType ht) {
    String hql = String.format(
        "select dwc.%s, count(dwc)   from DarwinCore dwc   where dwc.resource.id = :resourceId   group by dwc.%s",
        ht.columnName, ht.columnName);
    List<Object[]> occBySth = getSession().createQuery(hql).setParameter(
        "resourceId", resourceId).list();
    return StatsUtils.getDataMap(occBySth);
  }

  public String occByHostPieUrl(List<StatsCount> data, HostType ht, int width,
      int height, boolean title) {
    assert (ht != null);
    String titleText = null;
    if (title) {
      titleText = "Occurrences By " + ht.toString();
    }
    // get chart string
    data = limitDataForChart(data);
    return gpb.generatePieChartUrl(width, height, titleText, data);
  }

  public String occByHostPieUrl(Long resourceId, HostType ht, int width,
      int height, boolean title) {
    List<StatsCount> data = occByHost(resourceId, ht);
    return occByHostPieUrl(data, ht, width, height, title);
  }

  public List<StatsCount> occByRegion(Long resourceId, RegionType region,
      Long taxonIdFilter) {
    String hql;
    List<Object[]> occBySth;
    if (taxonIdFilter != null) {
      // only select regions of certain type that have taxon taxonFilter
      hql = String.format("select r.id, r.label, sum(s.numOcc)   from Region r, Region r2, OccStatByRegionAndTaxon s   where r.resource.id=:resourceId and r2.resource.id=:resourceId  and r.type=:type  and r2.lft>=r.lft and r2.rgt<=r.rgt  and s.taxon.id=:taxonId  and s.region=r2    group by r");
      occBySth = getSession().createQuery(hql).setParameter("resourceId",
          resourceId).setParameter("taxonId", taxonIdFilter).setParameter(
          "type", region).list();
    } else {
      // select all regions of certain type
      hql = String.format("select r.id, r.label, sum(r2.occTotal)   from Region r, Region r2   where r.resource.id=:resourceId and r2.resource.id=:resourceId  and r.type=:type  and r2.lft>=r.lft and r2.rgt<=r.rgt  group by r");
      occBySth = getSession().createQuery(hql).setParameter("resourceId",
          resourceId).setParameter("type", region).list();
    }
    return StatsUtils.getDataMap(occBySth);
  }

  public String occByRegionPieUrl(List<StatsCount> data, RegionType region,
      int width, int height, boolean title) {
    assert (region != null);
    String titleText = null;
    if (title) {
      titleText = "Occurrences By " + region.toString();
    }
    // get chart string
    data = limitDataForChart(data);
    return gpb.generatePieChartUrl(width, height, titleText, data);
  }

  public String occByRegionPieUrl(Long resourceId, RegionType region,
      int width, int height, boolean title) {
    List<StatsCount> data = occByRegion(resourceId, region, null);
    return occByRegionPieUrl(data, region, width, height, title);
  }

  public List<StatsCount> occByTaxon(Long resourceId, Rank rank) {
    String hql = "";
    List<Object[]> occBySth;
    if (rank == null || rank.equals(Rank.TerminalTaxon)) {
      // count all terminal taxa. No matter what rank. Higher, non terminal taxa
      // have occ_count=0, so we can include them without problem
      hql = String.format("select t.id, t.label, t.occTotal   from Taxon t   where t.resource.id=:resourceId");
      occBySth = getSession().createQuery(hql).setParameter("resourceId",
          resourceId).list();
    } else {
      // only select certain rank
      hql = String.format("select t.id, t.label, sum(t2.occTotal)   from Taxon t, Taxon t2   where t.resource.id=:resourceId and t2.resource.id=:resourceId  and t.type=:rank  and t2.lft>=t.lft and t2.rgt<=t.rgt  group by t");
      occBySth = getSession().createQuery(hql).setParameter("resourceId",
          resourceId).setParameter("rank", rank).list();
    }
    return StatsUtils.getDataMap(occBySth);
  }

  public String occByTaxonPieUrl(List<StatsCount> data, Rank rank, int width,
      int height, boolean title) {
    assert (rank != null);
    String titleText = null;
    if (title) {
      titleText = "Occurrences By " + rank.toString();
    }
    // get chart string
    data = limitDataForChart(data);
    return gpb.generatePieChartUrl(width, height, titleText, data);
  }

  public String occByTaxonPieUrl(Long resourceId, Rank rank, int width,
      int height, boolean title) {
    List<StatsCount> data = occByTaxon(resourceId, rank);
    return occByTaxonPieUrl(data, rank, width, height, title);
  }

  /*
   * (non-Javadoc) Also writes/updates the geoserver featuretype
   * 
   * @see
   * org.gbif.provider.service.impl.GenericResourceManagerHibernate#publish(
   * java.lang.Long)
   */
  @Override
  public OccurrenceResource publish(Long resourceId) {
    OccurrenceResource resource = super.publish(resourceId);
    try {
      geoTools.updateFeatureType(resource);
    } catch (IOException e) {
      log.error("Can't write new Geoserver FeatureTypeInfo for resource "
          + resource.getId());
    }
    return resource;
  }

  public OccurrenceResource setResourceStats(OccurrenceResource resource) {
    log.debug("Building occurrence resource stats");
    Long resourceId = resource.getId();
    super.setResourceStats(resource);
    // count occurrence specific stats
    resource.setNumCountries(regionManager.countByType(resourceId,
        RegionType.Country));
    resource.setNumRegions(regionManager.count(resourceId));
    resource.setNumTerminalRegions(regionManager.countTerminalNodes(resourceId));
    resource = this.save(resource);
    this.flush();
    // save stats
    return resource;
  }

  public String taxaByCountryMapUrl(GeographicalArea area,
      List<StatsCount> data, int width, int height) {
    // get chartmap string
    return gpb.generateMapChartUrl(width, height, data, area);
  }

  public String taxaByCountryMapUrl(GeographicalArea area, Long resourceId,
      int width, int height) {
    List<StatsCount> data = taxaByRegion(resourceId, RegionType.Country);
    return taxaByCountryMapUrl(area, data, width, height);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.OccResourceManager#speciesByCountry(java.lang
   * .Long)
   */
  public List<StatsCount> taxaByRegion(Long resourceId, RegionType region) {
    // only select regions of certain type that have taxon taxonFilter
    String hql = "select r.id, r.label, count(distinct s.taxon)   from Region r, Region r2, OccStatByRegionAndTaxon s   WHERE r.resource.id=:resourceId  and r2.resource.id=:resourceId  and r.type=:type  and r2.lft>=r.lft and r2.rgt<=r.rgt  and s.region=r2    group by r";
    List<Object[]> occBySth = getSession().createQuery(hql).setParameter(
        "type", region).setParameter("resourceId", resourceId).list();
    return StatsUtils.getDataMap(occBySth);
  }

  /*
   * (non-Javadoc) Also removes the geoserver featuretype in case of occurrence
   * resources
   * 
   * @see
   * org.gbif.provider.service.impl.GenericResourceManagerHibernate#unPublish
   * (java.lang.Long)
   */
  @Override
  public void unPublish(Long resourceId) {
    OccurrenceResource resource = get(resourceId);
    try {
      geoTools.removeFeatureType(resource);
    } catch (IOException e) {
      log.error("Can't remove Geoserver FeatureTypeInfo for resource "
          + resource.getId());
    }
    super.unPublish(resourceId);
  }

}
