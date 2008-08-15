package org.gbif.provider.job;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

import org.appfuse.dao.BaseDaoTestCase;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.dto.DwcTaxon;
import org.gbif.provider.util.Constants;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TaxonomyBuilderTest extends BaseDaoTestCase {
	@Autowired
	private TaxonomyBuilder taxonomyBuilder;

	private DwcTaxon getNewTaxon() {
		DwcTaxon dt = new DwcTaxon();
		dt.setFullname("Bellis perennis L.");
		dt.setAuthorship("L.");
		dt.setKingdom("Plantae");
		dt.setFamily("Asteraceae");
		dt.setGenus("Bellis");
		dt.setSpeciesEpi("perennis");
		return dt;
	}

	@Test
	public void testExplodeTaxa() {
		DwcTaxon dt = getNewTaxon();
		List<DwcTaxon> taxa = DwcTaxon.explodeTaxon(dt);
		assertEquals(taxa.size(), 4);
	}

	@Test
	public void testExtractTaxa() {
		SortedSet<DwcTaxon> taxa = taxonomyBuilder.extractTaxonomy(Constants.TEST_RESOURCE_ID, false);
		log.debug(String.format("%s taxa found in test resource", taxa.size()));
		// assertions based on PonTaurus dataset...
		assertTrue(taxa.first().getFullname().equals("Apiaceae"));
		assertTrue(taxa.last().getFullname().equals("noch unbestimmt !!!"));
		assertTrue(taxa.size() == 860);
	}

	@Test
	public void testLaunch() {
		Map<String, Object> seed = taxonomyBuilder.getSeed(
				Constants.TEST_RESOURCE_ID, Constants.TEST_USER_ID);
		try {
			taxonomyBuilder.launch(seed);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
