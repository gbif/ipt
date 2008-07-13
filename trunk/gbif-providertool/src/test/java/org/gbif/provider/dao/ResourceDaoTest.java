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

package org.gbif.provider.dao;

import java.util.List;

import org.appfuse.dao.BaseDaoTestCase;
import org.gbif.provider.dao.ResourceDao;
import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.util.Constants;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;


public class ResourceDaoTest extends BaseDaoTestCase{
	protected ResourceDao<Resource> resourceDao;
	protected ResourceDao<OccurrenceResource> occResourceDao;
	protected ResourceDao<ChecklistResource> checklistResourceDao;

	public void setResourceDao(ResourceDao<Resource> resourceDao) {
		this.resourceDao = resourceDao;
	}

	public void setOccResourceDao(ResourceDao<OccurrenceResource> occResourceDao) {
		this.occResourceDao = occResourceDao;
	}

	public void setChecklistResourceDao(
			ResourceDao<ChecklistResource> checklistResourceDao) {
		this.checklistResourceDao = checklistResourceDao;
	}



	@Test
	public void testGetResourcesByUser(){
		try {
			List<Resource> resources = resourceDao.getResourcesByUser(Constants.TEST_USER_ID);
			assertTrue(resources.size() > 0);
			resources = resourceDao.getResourcesByUser(Constants.TEST_USER_ID+100L);
			assertTrue(resources.size() == 0);
			
			List<OccurrenceResource> occResources = occResourceDao.getResourcesByUser(Constants.TEST_USER_ID);
			assertTrue(occResources.size() > 0);
			occResources = occResourceDao.getResourcesByUser(Constants.TEST_USER_ID+100L);
			assertTrue(occResources.size() == 0);
			
			List<ChecklistResource> checkResources = checklistResourceDao.getResourcesByUser(Constants.TEST_USER_ID);
			assertTrue(checkResources.size() == 0);
			checkResources = checklistResourceDao.getResourcesByUser(Constants.TEST_USER_ID+100L);
			assertTrue(checkResources.size() == 0);
		}catch(ObjectRetrievalFailureException e){
			logger.debug(e.getMessage());
		}
	}
}
