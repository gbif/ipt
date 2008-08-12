package org.gbif.provider.job;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.appfuse.dao.BaseDaoTestCase;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.dto.DwcRegion;
import org.gbif.provider.model.dto.DwcTaxon;
import org.gbif.provider.util.Constants;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class GeographyBuilderTest extends BaseDaoTestCase{
	@Autowired
	private GeographyBuilder geographyBuilder;

	private DwcRegion getNewRegion(){
		DwcRegion dt = new DwcRegion();
		dt.setLocality("Görlitzer Park SO36");
		dt.setStateProvince("Berlin");
		dt.setCountry("DE");
		dt.setContinent("Europe");
		return dt;
	}

	@Test
	public void testExtractHierarchy() {
		SortedSet<Region> taxa = geographyBuilder.extractHierarchy(Constants.TEST_RESOURCE_ID, false);
		System.out.println(String.format("%s regions found in test resource", taxa.size()));
//		assertTrue(taxa.first().getLabel().equals("Apiaceae"));
//		assertTrue(taxa.last().getLabel().equals("noch unbestimmt !!!"));
//		assertTrue(taxa.size() == 857);
	}

	
	@Test
	public void testExplodeTaxa(){
		List<DwcRegion> regions = DwcRegion.explodeRegions(getNewRegion());
		System.out.println(regions);
		assertEquals(regions.size(), 4);
	}

//	@Test
//	public void testLaunch() {
//		Map<String, Object>seed = geographyBuilder.getSeed(Constants.TEST_RESOURCE_ID, Constants.TEST_USER_ID);
//		try {
//			geographyBuilder.launch(seed);
//		} catch (Exception e) {
//			fail();
//			e.printStackTrace();
//		}
//	}
}
