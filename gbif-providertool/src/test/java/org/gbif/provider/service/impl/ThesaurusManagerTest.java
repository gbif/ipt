/*
 * Copyright 2009 GBIF.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gbif.provider.service.impl;

import org.gbif.provider.model.ThesaurusConcept;
import org.gbif.provider.model.ThesaurusTerm;
import org.gbif.provider.model.ThesaurusVocabulary;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.Vocabulary;
import org.gbif.provider.service.ThesaurusManager;
import org.gbif.provider.util.ContextAwareTestBase;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * TODO: Documentation.
 * 
 */
public class ThesaurusManagerTest extends ContextAwareTestBase {
  public static final String TEST_CONCEPT_URI = "http://rs.tdwg.org/ontology/voc/TaxonRank#Species";
  @Autowired
  protected ThesaurusManager thesaurusManager;

  @Test
  public void testGetAllConcepts() {
    List<?> ts = thesaurusManager.getAllConcepts(Rank.URI);
    // System.out.println(ts);
    assertTrue(ts.size() > 10);
  }

  @Test
  public void testGetAllTerms() {
    List<?> ts = thesaurusManager.getAllTerms(TEST_CONCEPT_URI, true);
    // System.out.println(ts);
    assertTrue(ts.size() > 0);

    ts = thesaurusManager.getAllTerms(TEST_CONCEPT_URI, false);
    // System.out.println(ts);
    assertTrue(ts.size() > 0);
  }

  @Test
  public void testGetConcept() {
    ThesaurusConcept c = thesaurusManager.getConcept(Rank.URI, "species");
    // System.out.println(c);
    assertTrue(c != null);
  }

  @Test
  public void testGetConcepts() {
    List<?> ts = thesaurusManager.getConcepts(Rank.URI, "species");
    // System.out.println(ts);
    assertTrue(ts.size() > 1);
  }

  @Test
  public void testGetConceptVocabularyString() {
    ThesaurusConcept c = thesaurusManager.getConcept(Rank.URI, "Species");
  }

  @Test
  public void testGetI18nCodeMap() {
    Map<String, String> m = thesaurusManager.getConceptCodeMap(Rank.URI, "de",
        false);
    m = thesaurusManager.getConceptCodeMap(Vocabulary.Language.uri, "de", true);
    System.out.println(m);
    assertTrue(m.size() > 10);
  }

  @Test
  public void testGetTerm() {
    ThesaurusTerm m = thesaurusManager.getTerm(
        "http://rs.tdwg.org/ontology/voc/TaxonRank#Species", "en");
    assertTrue(m != null);
    // System.out.println(m);
  }

  @Test
  public void testGetVocabularies() {
    List<ThesaurusVocabulary> vocs = thesaurusManager.getVocabularies();
    // System.out.println(vocs);
    assertTrue(vocs.size() > 1);
  }

  @Test
  public void testGetVocabulary() {
    ThesaurusVocabulary voc = thesaurusManager.getVocabulary("http://rs.tdwg.org/ontology/voc/TaxonRank");
    // System.out.println(voc);
    assertTrue(voc != null);
  }
}
