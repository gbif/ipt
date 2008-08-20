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
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.ContextAwareTestBase;
import org.hibernate.PropertyValueException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.AssertThrows;


public class CopyOfDarwinCoreMangerTest extends ContextAwareTestBase{
	@Autowired
	protected DarwinCoreManager darwinCoreManager;
	@Autowired
	private OccResourceManager occResourceManager;

	@Test
	public void testSave(){
		OccurrenceResource res = occResourceManager.get(Constants.TEST_RESOURCE_ID);
		DarwinCore dwc = DarwinCore.newMock(res);
		// this should fail if run twice... for manual testing only
		//dwc.setLocalId("12443990");
		dwc = darwinCoreManager.save(dwc);
		darwinCoreManager.flush();
	}

	@Test
	public void testLocalIdUniqueConstraint() {
		//FIXME: somehow this ttest persists only the dwc, but not the dwc.tax and dwc.loc component. This causes other tests later own to fail!
		new AssertThrows(EntityExistsException.class) {
            public void test() {
            	final String LOCAL_ID = "xcf-x";
        		OccurrenceResource res = occResourceManager.get(Constants.TEST_RESOURCE_ID);

        		DarwinCore dwc = DarwinCore.newMock(res);
				dwc.setLocalId(LOCAL_ID);
				dwc = darwinCoreManager.save(dwc);
				
				// create new dwc record with different data, but the same localId!
        		DarwinCore dwcTwin = DarwinCore.newMock(res);
				dwcTwin.setLocalId(LOCAL_ID);		
				// should raise exception...
		        dwcTwin = darwinCoreManager.save(dwcTwin);

        		darwinCoreManager.flush();            		
        	}
        }.runTest();
	}
	
	
}
