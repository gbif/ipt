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
package org.gbif.provider.webapp.action.admin;

import org.gbif.provider.model.ThesaurusConcept;
import org.gbif.provider.model.ThesaurusTerm;
import org.gbif.provider.model.ThesaurusVocabulary;
import org.gbif.provider.service.ThesaurusManager;
import org.gbif.provider.webapp.action.BaseAction;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * TODO: Documentation.
 * 
 */
public class ThesaurusAction extends BaseAction {
  @Autowired
  private ThesaurusManager thesaurusManager;
  private List<ThesaurusVocabulary> vocabularies;
  private ThesaurusVocabulary vocabulary;
  private List<ThesaurusConcept> concepts;
  private List<ThesaurusTerm> terms;
  private ThesaurusConcept concept;
  private Long id;

  public String concept() {
    concept = thesaurusManager.getConcept(new Long(id));
    terms = thesaurusManager.getAllTerms(concept.getUri(), false);
    return SUCCESS;
  }

  @Override
  public String execute() {
    vocabulary = thesaurusManager.getVocabulary(id);
    concepts = thesaurusManager.getAllConcepts(vocabulary.getUri());
    return SUCCESS;
  }

  public ThesaurusConcept getConcept() {
    return concept;
  }

  public List<ThesaurusConcept> getConcepts() {
    return concepts;
  }

  public Long getId() {
    return id;
  }

  public List<ThesaurusTerm> getTerms() {
    return terms;
  }

  public List<ThesaurusVocabulary> getVocabularies() {
    return vocabularies;
  }

  public ThesaurusVocabulary getVocabulary() {
    return vocabulary;
  }

  public String list() {
    vocabularies = thesaurusManager.getVocabularies();
    return SUCCESS;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String synchroniseAll() {
    thesaurusManager.synchroniseThesauriWithRepository();
    return SUCCESS;
  }

}
