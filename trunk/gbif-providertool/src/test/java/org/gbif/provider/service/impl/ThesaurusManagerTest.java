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

import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;

import org.appfuse.dao.BaseDaoTestCase;
import org.gbif.provider.model.ThesaurusConcept;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.Vocabulary;
import org.gbif.provider.service.ThesaurusManager;
import org.gbif.provider.service.UploadEventManager;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.ContextAwareTestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;


public class ThesaurusManagerTest extends ContextAwareTestBase{
	@Autowired
	protected ThesaurusManager thesaurusManager;


	@Test
	public void testVoc(){
		List<?> ts = thesaurusManager.getAllConcepts(Rank.URI);
		assertTrue(ts.size()>10);
		System.out.println(ts);
	}
	

	@Test	
	public void testGetConceptVocabularyString() {
		ThesaurusConcept c = thesaurusManager.getConcept(Rank.URI, "Species");
	}

	@Test
	public void testGetI18nCodeMap() {
		Map<Long, String> m = thesaurusManager.getI18nCodeMap(Rank.URI, "de");
		System.out.println(m);
	}

}
