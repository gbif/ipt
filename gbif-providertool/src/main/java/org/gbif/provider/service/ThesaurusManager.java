package org.gbif.provider.service;

import java.util.List;

import org.gbif.provider.model.ThesaurusConcept;
import org.gbif.provider.model.ThesaurusTerm;
import org.gbif.provider.model.voc.Vocabulary;

public interface ThesaurusManager extends GenericManager<ThesaurusTerm>{
	public List<ThesaurusConcept> getAllConcepts(Vocabulary vocabulary);
	public ThesaurusConcept getConcept(Long id);
	public ThesaurusConcept getConcept(Vocabulary vocabulary, String term);
	public List<ThesaurusTerm> getAllTerms(ThesaurusConcept concept, Boolean acceptedOnly);
	public ThesaurusTerm getTerm(ThesaurusConcept concept, String language);
}
