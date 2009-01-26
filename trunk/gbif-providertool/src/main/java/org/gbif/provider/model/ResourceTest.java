package org.gbif.provider.model;

import org.gbif.provider.model.voc.PublicationStatus;
import org.gbif.provider.service.ResourceFactory;
import org.gbif.provider.service.impl.ResourceFactoryImpl;
import org.gbif.provider.util.ContextAwareTestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;


public class ResourceTest extends ContextAwareTestBase{
	@Autowired
	private ResourceFactory factory;
	
	@Test
	public void testIsPublished(){
		Resource resource = factory.newMetadataResourceInstance();
		assertFalse(resource.isPublished());
		resource = factory.newChecklistResourceInstance();
		resource.setStatus(PublicationStatus.draft);
		assertFalse(resource.isPublished());
		
		resource.setStatus(PublicationStatus.dirty);
		assertTrue(resource.isPublished());
		
		resource.setStatus(PublicationStatus.published);
		assertTrue(resource.isPublished());		
	}
}
