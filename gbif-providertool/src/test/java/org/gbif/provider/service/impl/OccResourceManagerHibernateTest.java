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
	private OccResourceManager occResourceManager;

	public void setOccResourceManager(OccResourceManager occResourceManager) {
		this.occResourceManager = occResourceManager;
	}

	
	@Test
	public void testOccByBasisOfRecordPieUrl() {
		System.out.println(occResourceManager.occByBasisOfRecordPieUrl(Constants.TEST_RESOURCE_ID));
	}

	@Test
	public void testoccByCollectionPieUrl() {
		System.out.println(occResourceManager.occByCollectionPieUrl(Constants.TEST_RESOURCE_ID));
	}

	@Test
	public void testoccByInstitutionPieUrl() {
		System.out.println(occResourceManager.occByInstitutionPieUrl(Constants.TEST_RESOURCE_ID));
	}

	@Test
	public void testoccByRegionPieUrl() {
		System.out.println(occResourceManager.occByRegionPieUrl(Constants.TEST_RESOURCE_ID, RegionType.Continent));
		System.out.println(occResourceManager.occByRegionPieUrl(Constants.TEST_RESOURCE_ID, RegionType.Country));
		System.out.println(occResourceManager.occByRegionPieUrl(Constants.TEST_RESOURCE_ID, RegionType.Waterbody));
	}

	@Test
	public void testoccByTaxonPieUrl() {
		System.out.println(occResourceManager.occByTaxonPieUrl(Constants.TEST_RESOURCE_ID, Rank.Kingdom));
		System.out.println(occResourceManager.occByTaxonPieUrl(Constants.TEST_RESOURCE_ID, Rank.Family));
		System.out.println(occResourceManager.occByTaxonPieUrl(Constants.TEST_RESOURCE_ID, Rank.Genus));
	}

	@Test
	public void testtop10TaxaPieUrl() {
		System.out.println(occResourceManager.top10TaxaPieUrl(Constants.TEST_RESOURCE_ID));
	}

}
