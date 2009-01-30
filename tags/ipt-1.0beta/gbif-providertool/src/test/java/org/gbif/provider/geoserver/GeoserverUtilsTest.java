package org.gbif.provider.geoserver;


import org.gbif.provider.geo.GeoserverUtils;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.service.ResourceFactory;
import org.gbif.provider.util.ContextAwareTestBase;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class GeoserverUtilsTest extends ContextAwareTestBase{
	@Autowired
	private GeoserverUtils utils;
	@Autowired
	private ResourceFactory resourceFactory;
	
	
	@Test
	public void testFeatureInfoGen(){
		OccurrenceResource resource = resourceFactory.newOccurrenceResourceInstance();
		resource.setTitle("Walter Ulbrich");
		resource.setDescription("The one to rule them all.");
		System.out.println(utils.buildFeatureTypeDescriptor(resource));
	}

}
