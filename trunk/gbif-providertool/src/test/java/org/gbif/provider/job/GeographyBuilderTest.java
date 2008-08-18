package org.gbif.provider.job;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.appfuse.dao.BaseDaoTestCase;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.dto.DwcRegion;
import org.gbif.provider.model.dto.DwcTaxon;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.util.Constants;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class GeographyBuilderTest extends BaseDaoTestCase{
	@Autowired
	private GeographyBuilder geographyBuilder;
	@Autowired
	private OccResourceManager occResourceManager;

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
		OccurrenceResource resource = occResourceManager.get(Constants.TEST_RESOURCE_ID);
		SortedSet<Region> regions = geographyBuilder.extractHierarchy(resource, false);
		System.out.println(String.format("%s regions found in test resource", regions.size()));
		assertTrue(regions.first().getLabel().equals("PL"));
		assertTrue(regions.last().getLabel().equals("Doganbey - Seferihisar. N 38°06´25´´ O 26°51´03´´ near the sea., 0m"));
		assertTrue(regions.size() == 161);
	}

	
	@Test
	public void testExplodeTaxa(){
		List<DwcRegion> regions = DwcRegion.explodeRegions(getNewRegion());
		System.out.println(regions);
		assertEquals(regions.size(), 4);
	}

}
