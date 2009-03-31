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

import javax.persistence.EntityExistsException;

import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.ContextAwareTestBase;
import org.gbif.provider.util.ResourceTestBase;
import org.hibernate.PropertyValueException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.AssertThrows;


public class DarwinCoreMangerTest extends ResourceTestBase{
	@Autowired
	protected DarwinCoreManager darwinCoreManager;
	@Autowired
	private OccResourceManager occResourceManager;

	@Test
	public void testSave(){
		OccurrenceResource res = occResourceManager.get(Constants.TEST_OCC_RESOURCE_ID);
		DarwinCore dwc = DarwinCore.newMock(res);
		// this should fail if run twice... for manual testing only
		//dwc.setSourceId("12443990");
		dwc = darwinCoreManager.save(dwc);
		darwinCoreManager.flush();
	}

	@Test
	public void testSourceIdUniqueConstraint() {
		//FIXME: somehow this ttest persists only the dwc, but not the dwc.tax and dwc.loc component. This causes other tests later own to fail!
		new AssertThrows(EntityExistsException.class) {
            public void test() {
            	final String SOURCE_ID = "xcf-x";
        		OccurrenceResource res = occResourceManager.get(Constants.TEST_OCC_RESOURCE_ID);

        		DarwinCore dwc = DarwinCore.newMock(res);
				dwc.setSourceId(SOURCE_ID);
				dwc = darwinCoreManager.save(dwc);
				
				// create new dwc record with different data, but the same sourceId!
        		DarwinCore dwcTwin = DarwinCore.newMock(res);
				dwcTwin.setSourceId(SOURCE_ID);		
				// should raise exception...
		        dwcTwin = darwinCoreManager.save(dwcTwin);

        		darwinCoreManager.flush();            		
        	}
        }.runTest();
	}
	
	@Test
	public void testConstraintSave(){		
		new AssertThrows(PropertyValueException.class) {
            public void test() {
        		OccurrenceResource res = occResourceManager.get(Constants.TEST_OCC_RESOURCE_ID);
        		DarwinCore dwc = DarwinCore.newMock(res);
        		// remove resource to check if constraints work
        		dwc.setResource(null);
        		// should raise PropertyValueException
        		dwc = darwinCoreManager.save(dwc);		
        		darwinCoreManager.flush();            		
            }
		}.runTest();
	}
	
	
	@Test
	public void testFlagAllAsDeleted() {
		OccurrenceResource resource = (OccurrenceResource) occResourceManager.get(Constants.TEST_OCC_RESOURCE_ID);
		darwinCoreManager.flagAllAsDeleted(resource);
	}

	
	@Test
	public void testSimpleSave(){		
		OccurrenceResource res = occResourceManager.get(Constants.TEST_OCC_RESOURCE_ID);
		DarwinCore dwc = DarwinCore.newMock(res);
    	try{
    		dwc = darwinCoreManager.save(dwc);		
    	}finally{
    		darwinCoreManager.flush();            		
    	}
	}

	
	@Test
	public void testByTaxon() {
		List<DarwinCore> dwcs = darwinCoreManager.getByTaxon(Constants.TEST_TAXON_ID, Constants.TEST_OCC_RESOURCE_ID, false);
		System.out.println(dwcs.size());
		System.out.println(dwcs);
		dwcs = darwinCoreManager.getByTaxon(Constants.TEST_TAXON_ID, Constants.TEST_OCC_RESOURCE_ID, true);
		System.out.println(dwcs.size());
		System.out.println(dwcs);
		//FIXME: add proper taxonId once default data is stable
//		assertTrue(dwcs.size()>0);
	}
	
	
	@Test
	public void testByRegion() {
		Long rid = 274l;
		List<DarwinCore> dwcs = darwinCoreManager.getByRegion(rid, Constants.TEST_OCC_RESOURCE_ID, false);
		System.out.println(dwcs.size());
		System.out.println(dwcs);
		dwcs = darwinCoreManager.getByRegion(rid, Constants.TEST_OCC_RESOURCE_ID, true);
		System.out.println(dwcs.size());
		System.out.println(dwcs);
		//FIXME: add proper taxonId once default data is stable
//		assertTrue(dwcs.size()>0);
	}
	
	@Test
	public void testGetLatest(){
		List<DarwinCore> dwcs = darwinCoreManager.latest(Constants.TEST_OCC_RESOURCE_ID, 1, 25);
		dwcs = darwinCoreManager.latest(Constants.TEST_OCC_RESOURCE_ID, 2, 12);
	}
	
}
