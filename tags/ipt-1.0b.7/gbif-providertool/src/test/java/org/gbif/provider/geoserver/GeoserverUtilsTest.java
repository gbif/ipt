package org.gbif.provider.geoserver;


import static org.junit.Assert.*;

import java.io.IOException;

import org.gbif.provider.geo.GeoserverUtils;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.service.ResourceFactory;
import org.gbif.provider.util.ContextAwareTestBase;
import org.gbif.provider.util.ResourceTestBase;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class GeoserverUtilsTest extends ResourceTestBase{
	@Autowired
	private GeoserverUtils utils;
	@Autowired
	private ResourceFactory resourceFactory;
	
	
	@Test
	public void testFeatureInfoGen(){
		setupOccResource();
		resource.setTitle("Walter Ulbrich");
		String feature = utils.buildFeatureTypeDescriptor((OccurrenceResource)resource);
		assertTrue(feature!=null);
		assertTrue(feature.indexOf("Walter Ulbrich")>0);
	}

	@Test
	public void testReload() throws IOException{
		utils.reloadCatalog();
	}
}
