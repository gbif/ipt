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
package org.gbif.provider.service;

import org.gbif.provider.model.ThesaurusConcept;
import org.gbif.provider.model.ThesaurusTerm;
import org.gbif.provider.model.ThesaurusVocabulary;
import org.gbif.provider.model.voc.Rank;

import java.util.List;
import java.util.Map;

/**
 * TODO: Documentation.
 * 
 */
public interface ThesaurusManager extends GenericManager<ThesaurusTerm> {
  /**
   * list all concepts belonging to a certain vocabulary URI
   * 
   * @param vocabularyUri
   * @return
   */
  List<ThesaurusConcept> getAllConcepts(String vocabularyUri);

  /**
   * lists all known terms for a given concept. You can request only preferred
   * terms or get alternative ones too.
   * 
   * @param conceptUri
   * @param preferredOnly true if only preferred terms should be returned
   * @return
   */
  List<ThesaurusTerm> getAllTerms(String conceptUri, Boolean preferredOnly);

  /**
   * get concept by ID
   * 
   * @param id conceptid
   * @return
   */
  ThesaurusConcept getConcept(Long id);

  /**
   * try to get concept by a Rank enumeration.
   * 
   * @param rank
   * @return
   */
  ThesaurusConcept getConcept(Rank rank);

  /**
   * try to get concept by its globally unique URI.
   * 
   * @param uri
   * @return
   */
  ThesaurusConcept getConcept(String uri);

  /**
   * try to get concept by the vocabularyURI it belongs to and a term in any
   * language. If no term matches, returns null. If multiple concepts match, one
   * is selected arbitrarily
   * 
   * @param vocabularyUri
   * @param term
   * @return
   */
  ThesaurusConcept getConcept(String vocabularyUri, String term);

  /**
   * returns a map with key=concept.id, value=term.title in the language
   * requested. If no term for the given language is found the short
   * concept.identifier is used, e.g. DK for denmark. Useful to populate drop
   * downs and other user interface components
   * 
   * @param vocabularyUri
   * @param language
   * @return
   */
  Map<String, String> getConceptCodeMap(String vocabularyUri, String language,
      boolean sortAlphabetically);

  Map<Long, String> getConceptIdMap(String vocabularyUri, String language,
      boolean sortAlphabetically);

  /**
   * get all concepts of a certain vocabulary that has a certain term in any
   * language associated. If no term matches, returns null. If multiple concepts
   * match, all are returned.
   * 
   * @param vocabularyUri
   * @param term the term to match exactly, but case insensitive
   * @return
   */
  List<ThesaurusConcept> getConcepts(String vocabularyUri, String term);

  /**
   * get a preferred term for a given concept URI in a given language. if no
   * term for that language exists, null is returned.
   * 
   * @param conceptUri
   * @param language
   * @return
   */
  ThesaurusTerm getTerm(String conceptUri, String language);

  /**
   * get all known vocabularies
   * 
   * @return
   */
  List<ThesaurusVocabulary> getVocabularies();

  /**
   * Get vocabulary by its internal id
   * 
   * @param id primary key
   * @return
   */
  ThesaurusVocabulary getVocabulary(Long id);

  /**
   * Get vocabulary by its URI
   * 
   * @param uri vocabulary URI
   * @return
   */
  ThesaurusVocabulary getVocabulary(String uri);

  /**
   * Deletes any existing Vocab with the same URI and saves this one This might
   * not be the best strategy and a proper synchronise might be more appropriate
   * 
   * @param tv To save
   */
  void synchronise(ThesaurusVocabulary tv);

  /**
   * This will communicate with the central registry of thesauri and update the
   * database tables
   */
  void synchroniseThesauriWithRepository();
}
