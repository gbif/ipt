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

import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.dto.StatsCount;
import org.gbif.provider.service.ChecklistResourceManager;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.ContextAwareTestBase;
import org.hibernate.PropertyValueException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.AssertThrows;


public class TaxonManagerTest extends ContextAwareTestBase{
	@Autowired
	protected TaxonManager taxonManager;
	@Autowired
	protected ChecklistResourceManager checklistResourceManager;

	@Test
	public void testRoots(){
		List<Taxon> rootTaxa = taxonManager.getRoots(Constants.TEST_OCC_RESOURCE_ID);
		// FIXME: add good assertion once the test resource default-data.xml includes taxa...
		assertTrue(rootTaxa.size()==1);
		System.out.println(rootTaxa);
		System.out.println(rootTaxa.size());
		
		rootTaxa = taxonManager.getRoots(Constants.TEST_CHECKLIST_RESOURCE_ID);
		System.out.println(rootTaxa);
		System.out.println(rootTaxa.size());
		if (!rootTaxa.isEmpty()){
			Taxon t = rootTaxa.get(0);
			System.out.println(String.format("%s  [%s-%s]",t.getScientificName(), t.getLft(), t.getRgt()));
			List<StatsCount> stats = taxonManager.getRankStats(t.getId());
			System.out.println(stats);
		}
	}	

	@Test
	public void testLookup(){
		taxonManager.lookupParentTaxa(Constants.TEST_CHECKLIST_RESOURCE_ID);
		taxonManager.lookupAcceptedTaxa(Constants.TEST_CHECKLIST_RESOURCE_ID);
		taxonManager.lookupBasionymTaxa(Constants.TEST_CHECKLIST_RESOURCE_ID);
	}	

}
