package org.gbif.provider.upload;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.appfuse.dao.BaseDaoTestCase;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.dto.DwcTaxon;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.ContextAwareTestBase;
import org.gbif.provider.util.ResourceTestBase;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.AssertThrows;

public class TaxonomyBuilderTest extends ResourceTestBase {
	@Autowired
	@Qualifier("taxonomyBuilder")
	private RecordPostProcessor<DarwinCore, Set<Taxon>> taxonomyBuilder;

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
	public void testCallable() throws Exception {
		setup();
		taxonomyBuilder.init(Constants.TEST_RESOURCE_ID, Constants.TEST_USER_ID);

		ExecutorService executor = Executors.newSingleThreadExecutor();
		log.debug("Submit geography builder task to single threaded executor");
		Future<Set<Taxon>> f = executor.submit(taxonomyBuilder);
		try {
			Set<Taxon> taxa = f.get();
			log.debug(String.format("%s taxa found in test resource. Taxonomically sorted list looks like this:", taxa.size()));
			List<Taxon> sortedTaxa = new ArrayList<Taxon>(taxa);
			Collections.sort(sortedTaxa);
			log.debug(sortedTaxa);
			// assertions based on PonTaurus dataset...
			assertTrue(taxa.size() > 850);
//			assertTrue(taxa.size() == 857);
		} catch (ExecutionException e) {
			e.printStackTrace();
		} finally {
			f.cancel(true);
		}
		
	}

}
