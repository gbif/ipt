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

import org.appfuse.dao.BaseDaoTestCase;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.service.UploadEventManager;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.ContextAwareTestBase;
import org.junit.Test;
import org.springframework.orm.ObjectRetrievalFailureException;


public class UploadEventManagerTest extends ContextAwareTestBase{
	protected UploadEventManager uploadEventManager;

	public void setUploadEventManager(UploadEventManager uploadEventManager) {
		this.uploadEventManager = uploadEventManager;
	}


	@Test
	public void testGetUploadEventsByResource(){
		try {
			List<UploadEvent> events = uploadEventManager.getUploadEventsByResource(Constants.TEST_OCC_RESOURCE_ID);
			for (UploadEvent ev : events){
				logger.debug(ev);
			}
		}catch(ObjectRetrievalFailureException e){
			logger.debug(e.getMessage());
		}
	}
	
	@Test
	public void testGetGoogleChartData() {
		System.out.println(uploadEventManager.getGoogleChartData(1L, 450, 200));
		System.out.println(uploadEventManager.getGoogleChartData(321L, 450, 200));
	}	
}
