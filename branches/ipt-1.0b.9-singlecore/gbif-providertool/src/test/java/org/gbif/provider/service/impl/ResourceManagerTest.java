/***************************************************************************
* Copyright (C) 2008 Global Biodiversity Information Facility Secretariat.
* All Rights Reserved.
*
* The contents of this file are subject to the Mozilla Public
* License Version 1.1 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of
* the License at http://www.mozilla.org/MPL/
*
* Software distributed under the License is distributed on an "AS
* IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
* implied. See the License for the specific language governing
* rights and limitations under the License.

***************************************************************************/

package org.gbif.provider.service.impl;

import java.util.List;

import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.voc.PublicationStatus;
import org.gbif.provider.service.ChecklistResourceManager;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.ContextAwareTestBase;
import org.gbif.provider.util.ResourceTestBase;
import org.junit.Test;
import static org.junit.Assert.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.ObjectRetrievalFailureException;


public class ResourceManagerTest extends ResourceTestBase{
	@Autowired
	@Qualifier("resourceManager")
	protected GenericResourceManager<Resource> resourceManager;
	@Autowired
	protected OccResourceManager occResourceManager;
	@Autowired
	protected ChecklistResourceManager checklistResourceManager;



	@Test
	public void testGetResourcesByUser(){
		try {
			List<Resource> resources = resourceManager.getResourcesByUser(Constants.TEST_USER_ID);
			assertTrue(resources.size() > 0);
			resources = resourceManager.getResourcesByUser(Constants.TEST_USER_ID+100L);
			assertTrue(resources.size() == 0);
			
			List<OccurrenceResource> occResources = occResourceManager.getResourcesByUser(Constants.TEST_USER_ID);
			assertTrue(occResources.size() > 0);
			occResources = occResourceManager.getResourcesByUser(Constants.TEST_USER_ID+100L);
			assertTrue(occResources.size() == 0);
			
			List<ChecklistResource> checkResources = checklistResourceManager.getResourcesByUser(Constants.TEST_USER_ID);
			//FIXME: set to 1 or so once checklist upload is done and we have a default test dataset
			//assertTrue(checkResources.size() == 0);
			checkResources = checklistResourceManager.getResourcesByUser(Constants.TEST_USER_ID+100L);
			assertTrue(checkResources.size() == 0);
		}catch(ObjectRetrievalFailureException e){
			logger.debug(e.getMessage());
		}
	}
	
	@Test
	public void testSaveResource(){
		setupOccResource();
		resource.setStatus(PublicationStatus.uptodate);
		Long id = resource.getId();
		resourceManager.save(resource);		
		Resource resu = resourceManager.get(id);
		// stays dirty and not uptodate because resource isnt registered
		assertEquals(resu.getStatus(), PublicationStatus.dirty);

		resource.setStatus(PublicationStatus.dirty);
		resourceManager.save(resource);		
		resu = resourceManager.get(id);
		assertEquals(resu.getStatus(), PublicationStatus.dirty);
}
}
