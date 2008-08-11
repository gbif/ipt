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

import java.util.UUID;

import javax.persistence.EntityExistsException;

import org.appfuse.dao.BaseDaoTestCase;
import org.appfuse.service.GenericManager;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.util.Constants;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Test;
import org.springframework.test.AssertThrows;


public class DarwinCoreMangerTest extends BaseDaoTestCase{
	protected DarwinCoreManager darwinCoreManager;
	private GenericManager<OccurrenceResource, Long> occResourceManager;

	@Test
	public void testSave(){
		OccurrenceResource res = occResourceManager.get(Constants.TEST_RESOURCE_ID);
		DarwinCore dwc = DarwinCore.newMock(res);
		dwc = darwinCoreManager.save(dwc);		
	}

	
	public void setDarwinCoreManager(DarwinCoreManager darwinCoreManager) {
		this.darwinCoreManager = darwinCoreManager;
	}

	public void setOccResourceManager(
			GenericManager<OccurrenceResource, Long> occResourceManager) {
		this.occResourceManager = occResourceManager;
	}
	
}
