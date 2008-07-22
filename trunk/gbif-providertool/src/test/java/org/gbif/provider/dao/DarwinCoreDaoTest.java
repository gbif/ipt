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
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.util.Constants;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;


public class DarwinCoreDaoTest extends BaseDaoTestCase{
	protected DarwinCoreDao darwinCoreDao;

	public void setDarwinCoreDao(DarwinCoreDao darwinCoreDao) {
		this.darwinCoreDao = darwinCoreDao;
	}



	@Test
	public void testSave(){
		DarwinCore dwc = DarwinCore.newInstance();
		dwc.setCatalogNumber("befhjsa6788-x");
		dwc.setBasisOfRecord("specimen");
		dwc.setInstitutionCode("RBGK");
		// location
		dwc.setCountry("Spain");
		// taxonomy
		dwc.setScientificName("Abies alba");
		dwc.setGenus("Abies");
		
		dwc = darwinCoreDao.save(dwc);
		System.out.println(dwc);
	}
}
