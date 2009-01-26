package org.gbif.provider.model;

import org.gbif.provider.model.voc.PublicationStatus;
import org.gbif.provider.service.ResourceFactory;
import org.gbif.provider.util.ContextAwareTestBase;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


public class ResourceTest extends ContextAwareTestBase{
	@Autowired
	private ResourceFactory factory;
	
	@Test
	public void testIsPublished(){
		Resource resource = factory.newMetadataResourceInstance();
		Assert.assertFalse(resource.isPublished());
		resource = factory.newChecklistResourceInstance();
		resource.setStatus(PublicationStatus.draft);
		Assert.assertFalse(resource.isPublished());
		
		resource.setStatus(PublicationStatus.dirty);
		Assert.assertTrue(resource.isPublished());
		
		resource.setStatus(PublicationStatus.published);
		Assert.assertTrue(resource.isPublished());		
	}
}
