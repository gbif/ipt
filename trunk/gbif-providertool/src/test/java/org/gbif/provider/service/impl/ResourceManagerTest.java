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
import org.gbif.provider.service.ResourceManager;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.ContextAwareTestBase;
import org.junit.Test;
import org.springframework.orm.ObjectRetrievalFailureException;


public class ResourceManagerTest extends ContextAwareTestBase{
	protected ResourceManager<Resource> resourceManager;
	protected ResourceManager<OccurrenceResource> occResourceManager;
	protected ResourceManager<ChecklistResource> checklistResourceManager;

	public void setResourceManager(ResourceManager<Resource> resourceManager) {
		this.resourceManager = resourceManager;
	}

	public void setOccResourceManager(ResourceManager<OccurrenceResource> occResourceManager) {
		this.occResourceManager = occResourceManager;
	}

	public void setChecklistResourceManager(
			ResourceManager<ChecklistResource> checklistResourceManager) {
		this.checklistResourceManager = checklistResourceManager;
	}



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
			assertTrue(checkResources.size() == 0);
			checkResources = checklistResourceManager.getResourcesByUser(Constants.TEST_USER_ID+100L);
			assertTrue(checkResources.size() == 0);
		}catch(ObjectRetrievalFailureException e){
			logger.debug(e.getMessage());
		}
	}
}
