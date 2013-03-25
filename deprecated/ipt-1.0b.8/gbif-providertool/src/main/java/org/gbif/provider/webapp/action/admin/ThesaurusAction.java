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

package org.gbif.provider.webapp.action.admin;

import java.util.List;

import org.gbif.provider.service.ExtensionManager;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.service.ThesaurusManager;
import org.gbif.provider.webapp.action.BaseAction;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ThesaurusConcept;
import org.gbif.provider.model.ThesaurusTerm;
import org.gbif.provider.model.ThesaurusVocabulary;
import org.gbif.provider.model.hibernate.IptNamingStrategy;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Preparable;

public class ThesaurusAction extends BaseAction {
	@Autowired
    private ThesaurusManager thesaurusManager;
	private List<ThesaurusVocabulary> vocabularies;
	private ThesaurusVocabulary vocabulary;
	private List<ThesaurusConcept> concepts;
	private List<ThesaurusTerm> terms;
    private ThesaurusConcept concept;
    private Long id;


	public String execute(){
		vocabularies = thesaurusManager.getVocabularies();
		return SUCCESS;
	}
	public String vocabulary(){
		vocabulary = thesaurusManager.getVocabulary(id);
		concepts = thesaurusManager.getAllConcepts(vocabulary.getUri());
		return SUCCESS;
	}
	public String concept(){
		concept = thesaurusManager.getConcept(new Long(id));
		terms = thesaurusManager.getAllTerms(concept.getUri(), false);
		return SUCCESS;
	}

	public String synchroniseAll() {
		thesaurusManager.synchroniseThesauriWithRepository();
		return SUCCESS;
	}	
	
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	public List<ThesaurusVocabulary> getVocabularies() {
		return vocabularies;
	}
	public List<ThesaurusConcept> getConcepts() {
		return concepts;
	}
	public List<ThesaurusTerm> getTerms() {
		return terms;
	}
	public ThesaurusVocabulary getVocabulary() {
		return vocabulary;
	}
	public ThesaurusConcept getConcept() {
		return concept;
	}


}
