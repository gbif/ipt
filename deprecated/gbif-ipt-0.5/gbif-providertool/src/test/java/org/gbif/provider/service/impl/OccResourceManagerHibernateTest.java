package org.gbif.provider.service.impl;

import org.gbif.provider.model.voc.HostType;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.ContextAwareTestBase;
import org.junit.Test;

import com.googlecode.gchartjava.GeographicalArea;

public class OccResourceManagerHibernateTest extends ContextAwareTestBase{
	static int width=440;
	static int height=220;
	static boolean title=true;
	
	private OccResourceManager occResourceManager;

	public void setOccResourceManager(OccResourceManager occResourceManager) {
		this.occResourceManager = occResourceManager;
	}

	
	@Test
	public void testOccByBasisOfRecordPieUrl() {
		System.out.println(occResourceManager.occByBasisOfRecordPieUrl(Constants.TEST_RESOURCE_ID, width, height, title));
	}

	@Test
	public void testOccByHostPieUrl() {
		System.out.println(occResourceManager.occByHostPieUrl(Constants.TEST_RESOURCE_ID, HostType.Collection, width, height, title));
	}

	@Test
	public void testOccByRegionPieUrl() {
		System.out.println(occResourceManager.occByRegionPieUrl(Constants.TEST_RESOURCE_ID, RegionType.Continent, width, height, title));
		System.out.println(occResourceManager.occByRegionPieUrl(Constants.TEST_RESOURCE_ID, RegionType.Country, width, height, title));
		System.out.println(occResourceManager.occByRegionPieUrl(Constants.TEST_RESOURCE_ID, RegionType.Waterbody, width, height, title));
	}

	@Test
	public void testOccByTaxonPieUrl() {
		System.out.println(occResourceManager.occByTaxonPieUrl(Constants.TEST_RESOURCE_ID, Rank.Kingdom, width, height, title));
		System.out.println(occResourceManager.occByTaxonPieUrl(Constants.TEST_RESOURCE_ID, Rank.Family, width, height, title));
		System.out.println(occResourceManager.occByTaxonPieUrl(Constants.TEST_RESOURCE_ID, Rank.Genus, width, height, title));
		System.out.println(occResourceManager.occByTaxonPieUrl(Constants.TEST_RESOURCE_ID, Rank.TerminalTaxon, width, height, title));
	}

	@Test
	public void testByCountryMapUrl() {
		System.out.println(occResourceManager.occByCountryMapUrl(GeographicalArea.WORLD, Constants.TEST_RESOURCE_ID, width, height));
	}

	@Test
	public void testTaxaByCountryMapUrl() {
		System.out.println(occResourceManager.taxaByCountryMapUrl(GeographicalArea.WORLD, Constants.TEST_RESOURCE_ID, width, height));
		System.out.println(occResourceManager.taxaByRegion(Constants.TEST_RESOURCE_ID, RegionType.State));
	}

	@Test
	public void testOccByRegionWithTaxonFilter() {
		System.out.println(occResourceManager.occByRegion(Constants.TEST_RESOURCE_ID, RegionType.Country, 656l));
	}
	
}
