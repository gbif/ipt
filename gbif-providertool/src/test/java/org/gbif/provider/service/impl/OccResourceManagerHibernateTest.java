package org.gbif.provider.service.impl;

import static org.junit.Assert.*;

import org.appfuse.dao.BaseDaoTestCase;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.util.Constants;
import org.junit.Before;
import org.junit.Test;

public class OccResourceManagerHibernateTest extends BaseDaoTestCase{
	static int width=200;
	static int height=100;
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
	public void testOccByCollectionPieUrl() {
		System.out.println(occResourceManager.occByCollectionPieUrl(Constants.TEST_RESOURCE_ID, width, height, title));
	}

	@Test
	public void testOccByInstitutionPieUrl() {
		System.out.println(occResourceManager.occByInstitutionPieUrl(Constants.TEST_RESOURCE_ID, width, height, title));
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
	}

	@Test
	public void testTop10TaxaPieUrl() {
		System.out.println(occResourceManager.top10TaxaPieUrl(Constants.TEST_RESOURCE_ID, width, height, title));
	}

	@Test
	public void testSpeciesByCountryMapUrl() {
		System.out.println(occResourceManager.speciesByCountryMapUrl(Constants.TEST_RESOURCE_ID, width, height, title));
	}

}
