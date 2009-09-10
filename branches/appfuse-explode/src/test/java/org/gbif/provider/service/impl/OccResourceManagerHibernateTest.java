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

import org.gbif.provider.model.voc.HostType;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.ContextAwareTestBase;

import com.googlecode.gchartjava.GeographicalArea;

import org.junit.Test;

/**
 * TODO: Documentation.
 * 
 */
public class OccResourceManagerHibernateTest extends ContextAwareTestBase {
  static int width = 440;
  static int height = 220;
  static boolean title = true;

  private OccResourceManager occResourceManager;

  public void setOccResourceManager(OccResourceManager occResourceManager) {
    this.occResourceManager = occResourceManager;
  }

  @Test
  public void testByCountryMapUrl() {
    System.out.println(occResourceManager.occByCountryMapUrl(
        GeographicalArea.WORLD, Constants.TEST_OCC_RESOURCE_ID, width, height));
  }

  @Test
  public void testOccByBasisOfRecordPieUrl() {
    System.out.println(occResourceManager.occByBasisOfRecordPieUrl(
        Constants.TEST_OCC_RESOURCE_ID, width, height, title));
  }

  @Test
  public void testOccByHostPieUrl() {
    System.out.println(occResourceManager.occByHostPieUrl(
        Constants.TEST_OCC_RESOURCE_ID, HostType.Collection, width, height,
        title));
  }

  @Test
  public void testOccByRegionPieUrl() {
    System.out.println(occResourceManager.occByRegionPieUrl(
        Constants.TEST_OCC_RESOURCE_ID, RegionType.Continent, width, height,
        title));
    System.out.println(occResourceManager.occByRegionPieUrl(
        Constants.TEST_OCC_RESOURCE_ID, RegionType.Country, width, height,
        title));
    System.out.println(occResourceManager.occByRegionPieUrl(
        Constants.TEST_OCC_RESOURCE_ID, RegionType.Waterbody, width, height,
        title));
  }

  @Test
  public void testOccByRegionWithTaxonFilter() {
    System.out.println(occResourceManager.occByRegion(
        Constants.TEST_OCC_RESOURCE_ID, RegionType.Country, 656L));
  }

  @Test
  public void testOccByTaxonPieUrl() {
    System.out.println(occResourceManager.occByTaxonPieUrl(
        Constants.TEST_OCC_RESOURCE_ID, Rank.Kingdom, width, height, title));
    System.out.println(occResourceManager.occByTaxonPieUrl(
        Constants.TEST_OCC_RESOURCE_ID, Rank.Family, width, height, title));
    System.out.println(occResourceManager.occByTaxonPieUrl(
        Constants.TEST_OCC_RESOURCE_ID, Rank.Genus, width, height, title));
    System.out.println(occResourceManager.occByTaxonPieUrl(
        Constants.TEST_OCC_RESOURCE_ID, Rank.TerminalTaxon, width, height,
        title));
  }

  @Test
  public void testTaxaByCountryMapUrl() {
    System.out.println(occResourceManager.taxaByCountryMapUrl(
        GeographicalArea.WORLD, Constants.TEST_OCC_RESOURCE_ID, width, height));
    System.out.println(occResourceManager.taxaByRegion(
        Constants.TEST_OCC_RESOURCE_ID, RegionType.State));
  }

}
