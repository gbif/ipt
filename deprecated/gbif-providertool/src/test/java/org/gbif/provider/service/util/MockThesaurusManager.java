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
package org.gbif.provider.service.util;

import org.gbif.provider.model.ThesaurusConcept;
import org.gbif.provider.model.ThesaurusTerm;
import org.gbif.provider.model.ThesaurusVocabulary;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.service.ThesaurusManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * TODO: Documentation.
 * 
 */
public class MockThesaurusManager implements ThesaurusManager {

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.provider.service.GenericManager#debugSession()
   */
  public void debugSession() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.provider.service.GenericManager#exists(java.lang.Long)
   */
  public boolean exists(Long id) {
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.provider.service.GenericManager#flush()
   */
  public void flush() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.provider.service.GenericManager#get(java.lang.Long)
   */
  public ThesaurusTerm get(Long id) {
    return new ThesaurusTerm();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.provider.service.GenericManager#getAll()
   */
  public List<ThesaurusTerm> getAll() {
    return new LinkedList<ThesaurusTerm>();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.ThesaurusManager#getAllConcepts(java.lang.String)
   */
  public List<ThesaurusConcept> getAllConcepts(String vocabularyUri) {
    return new LinkedList<ThesaurusConcept>();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.provider.service.GenericManager#getAllDistinct()
   */
  public List<ThesaurusTerm> getAllDistinct() {
    return new LinkedList<ThesaurusTerm>();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.provider.service.GenericManager#getAllIds()
   */
  public List<Long> getAllIds() {
    return new LinkedList<Long>();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.ThesaurusManager#getAllTerms(java.lang.String,
   * java.lang.Boolean)
   */
  public List<ThesaurusTerm> getAllTerms(String conceptUri,
      Boolean preferredOnly) {
    return new LinkedList<ThesaurusTerm>();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.provider.service.ThesaurusManager#getConcept(java.lang.Long)
   */
  public ThesaurusConcept getConcept(Long id) {
    return new ThesaurusConcept();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.ThesaurusManager#getConcept(org.gbif.provider
   * .model.voc.Rank)
   */
  public ThesaurusConcept getConcept(Rank rank) {
    return new ThesaurusConcept();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.ThesaurusManager#getConcept(java.lang.String)
   */
  public ThesaurusConcept getConcept(String uri) {
    return new ThesaurusConcept();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.ThesaurusManager#getConcept(java.lang.String,
   * java.lang.String)
   */
  public ThesaurusConcept getConcept(String vocabularyUri, String term) {
    return new ThesaurusConcept();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.ThesaurusManager#getConceptCodeMap(java.lang.
   * String, java.lang.String, boolean)
   */
  public Map<String, String> getConceptCodeMap(String vocabularyUri,
      String language, boolean sortAlphabetically) {
    return new HashMap<String, String>();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.ThesaurusManager#getConceptIdMap(java.lang.String
   * , java.lang.String, boolean)
   */
  public Map<Long, String> getConceptIdMap(String vocabularyUri,
      String language, boolean sortAlphabetically) {
    return new HashMap<Long, String>();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.ThesaurusManager#getConcepts(java.lang.String,
   * java.lang.String)
   */
  public List<ThesaurusConcept> getConcepts(String vocabularyUri, String term) {
    return new LinkedList<ThesaurusConcept>();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.provider.service.ThesaurusManager#getTerm(java.lang.String,
   * java.lang.String)
   */
  public ThesaurusTerm getTerm(String conceptUri, String language) {
    return new ThesaurusTerm();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.provider.service.ThesaurusManager#getVocabularies()
   */
  public List<ThesaurusVocabulary> getVocabularies() {
    return new LinkedList<ThesaurusVocabulary>();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.ThesaurusManager#getVocabulary(java.lang.Long)
   */
  public ThesaurusVocabulary getVocabulary(Long id) {
    return new ThesaurusVocabulary();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.ThesaurusManager#getVocabulary(java.lang.String)
   */
  public ThesaurusVocabulary getVocabulary(String uri) {
    return new ThesaurusVocabulary();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.provider.service.GenericManager#remove(java.lang.Long)
   */
  public void remove(Long id) {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.provider.service.GenericManager#remove(java.lang.Object)
   */
  public void remove(ThesaurusTerm obj) {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.provider.service.GenericManager#save(java.lang.Object)
   */
  public ThesaurusTerm save(ThesaurusTerm object) {
    object.setId(1L);
    return object;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.provider.service.GenericManager#saveAll(java.util.Collection)
   */
  public void saveAll(Collection<ThesaurusTerm> objs) {
    long i = 0;
    for (ThesaurusTerm t : objs) {
      t.setId(i++);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.ThesaurusManager#synchronise(org.gbif.provider
   * .model.ThesaurusVocabulary)
   */
  public void synchronise(ThesaurusVocabulary tv) {
    tv.setId(1L);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.ThesaurusManager#synchroniseThesauriWithRepository
   * ()
   */
  public void synchroniseThesauriWithRepository() {
  }
}
