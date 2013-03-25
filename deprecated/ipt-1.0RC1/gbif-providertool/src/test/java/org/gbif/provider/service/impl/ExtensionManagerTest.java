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
import java.util.Map;

import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ThesaurusConcept;
import org.gbif.provider.model.ThesaurusTerm;
import org.gbif.provider.model.ThesaurusVocabulary;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.service.ExtensionManager;
import org.gbif.provider.util.ContextAwareTestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Transactional(readOnly=true, propagation=Propagation.REQUIRED)
public class ExtensionManagerTest extends ContextAwareTestBase{
	@Autowired
	protected ExtensionManager extensionManager;


	@Test	
	public void testGetConceptVocabularyString() {
		Extension dwc = extensionManager.get(1l);
		assertTrue(dwc!=null);
//		System.out.println(dwc);
//		System.out.println(dwc.getProperties());
	}
}
