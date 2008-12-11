package org.gbif.provider.service.impl;

import java.util.List;

import org.gbif.provider.model.ThesaurusConcept;
import org.gbif.provider.model.ThesaurusTerm;
import org.gbif.provider.model.voc.Vocabulary;
import org.gbif.provider.service.ThesaurusManager;

public class ThesaurusManagerHibernate extends GenericManagerHibernate<ThesaurusTerm> implements ThesaurusManager{

	public ThesaurusManagerHibernate() {
		super(ThesaurusTerm.class);
	}

	public List<ThesaurusConcept> getAllConcepts(Vocabulary vocabulary) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<ThesaurusTerm> getAllTerms(ThesaurusConcept concept,
			Boolean acceptedOnly) {
		// TODO Auto-generated method stub
		return null;
	}

	public ThesaurusTerm getTerm(ThesaurusConcept concept, String language) {
		// TODO Auto-generated method stub
		return null;
	}

	public ThesaurusTerm getTerm(Vocabulary vocabulary, String term) {
		// TODO Auto-generated method stub
		return null;
	}

}
