package org.gbif.provider.service;

import java.util.List;

import org.gbif.provider.model.ThesaurusConcept;
import org.gbif.provider.model.ThesaurusTerm;
import org.gbif.provider.model.voc.Vocabulary;

public interface ThesaurusManager extends GenericManager<ThesaurusTerm>{
	public List<ThesaurusConcept> getAllConcepts(Vocabulary vocabulary);
	public List<ThesaurusTerm> getAllTerms(ThesaurusConcept concept, Boolean acceptedOnly);
	public ThesaurusTerm getTerm(ThesaurusConcept concept, String language);
	public ThesaurusTerm getTerm(Vocabulary vocabulary, String term);
}
