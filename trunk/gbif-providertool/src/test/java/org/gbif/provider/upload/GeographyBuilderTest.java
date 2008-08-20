package org.gbif.provider.upload;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.appfuse.dao.BaseDaoTestCase;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.model.dto.DwcRegion;
import org.gbif.provider.model.dto.DwcTaxon;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.ContextAwareTestBase;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class GeographyBuilderTest extends ContextAwareTestBase{
	@Autowired
	@Qualifier("geographyBuilder")
	private RecordPostProcessor<DarwinCore, Set<Region>> geographyBuilder;


	private DwcRegion getNewRegion(){
		DwcRegion dt = new DwcRegion();
		dt.setLocality("Görlitzer Park SO36");
		dt.setStateProvince("Berlin");
		dt.setCountry("DE");
		dt.setContinent("Europe");
		return dt;
	}

	@Test
	public void testBuildHierarchy() throws Exception {
		geographyBuilder.setResourceId(Constants.TEST_RESOURCE_ID);
		geographyBuilder.setUserId(Constants.TEST_USER_ID);
		
		Set<Region> regions = geographyBuilder.call();
		System.out.println(String.format("%s regions found in test resource", regions.size()));
		assertTrue(regions.size() > 150);
//		assertTrue(regions.size() == 161);
	}

	
	@Test
	public void testBuildHierarchyCallable() throws Exception {
		System.out.println("START TEST");
		geographyBuilder.setResourceId(Constants.TEST_RESOURCE_ID);
		geographyBuilder.setUserId(Constants.TEST_USER_ID);
		
		ExecutorService executor = Executors.newSingleThreadExecutor();
		System.out.println("SUBMIT TASK");
		Future<Set<Region>> f = executor.submit(geographyBuilder);
		try {
			Set<Region> regions = f.get();
			System.out.println(String.format("%s regions found in test resource", regions.size()));
			System.out.println(regions);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testExplodeTaxa(){
		List<DwcRegion> regions = DwcRegion.explodeRegions(getNewRegion());
		System.out.println(regions);
		assertEquals(regions.size(), 4);
	}

}
