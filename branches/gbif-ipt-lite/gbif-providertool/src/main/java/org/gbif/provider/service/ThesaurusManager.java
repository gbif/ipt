package org.gbif.provider.service;

import java.util.List;
import java.util.Map;

import org.gbif.provider.model.ThesaurusConcept;
import org.gbif.provider.model.ThesaurusTerm;
import org.gbif.provider.model.ThesaurusVocabulary;
import org.gbif.provider.model.voc.Rank;

public interface ThesaurusManager extends GenericManager<ThesaurusTerm>{
	/** Get vocabulary by its URI
	 * @param uri vocabulary URI
	 * @return
	 */
	public ThesaurusVocabulary getVocabulary(String uri);
	/** Get vocabulary by its internal id
	 * @param id primary key
	 * @return
	 */
	public ThesaurusVocabulary getVocabulary(Long id);
	/**get all known vocabularies
	 * @return
	 */
	public List<ThesaurusVocabulary> getVocabularies();

	
	
	/**get concept by ID
	 * @param id conceptid
	 * @return
	 */
	public ThesaurusConcept getConcept(Long id);
	/** try to get concept by its globally unique URI.
	 * @param uri
	 * @return
	 */
	public ThesaurusConcept getConcept(String uri);
	/** try to get concept by a Rank enumeration.
	 * @param rank
	 * @return
	 */
	public ThesaurusConcept getConcept(Rank rank);
	/** try to get concept by the vocabularyURI it belongs to and a term in any language.
	 * If no term matches, returns null. If multiple concepts match, one is selected arbitrarily
	 * @param vocabularyUri
	 * @param term
	 * @return
	 */
	public ThesaurusConcept getConcept(String vocabularyUri, String term);
	/** get all concepts of a certain vocabulary that has a certain term in any language associated.
	 * If no term matches, returns null. If multiple concepts match, all are returned.
	 * @param vocabularyUri
	 * @param term the term to match exactly, but case insensitive
	 * @return
	 */
	public List<ThesaurusConcept> getConcepts(String vocabularyUri, String term);
	/**list all concepts belonging to a certain vocabulary URI
	 * @param vocabularyUri
	 * @return
	 */
	public List<ThesaurusConcept> getAllConcepts(String vocabularyUri);
	/** returns a map with key=concept.id, value=term.title in the language requested.
	 * If no term for the given language is found the short concept.identifier is used, e.g. DK for denmark. 
	 * Useful to populate drop downs and other user interface components
	 * @param vocabularyUri
	 * @param language
	 * @return
	 */
	public Map<String, String> getConceptCodeMap(String vocabularyUri, String language, boolean sortAlphabetically);
	public Map<Long, String> getConceptIdMap(String vocabularyUri, String language, boolean sortAlphabetically);

	/** lists all known terms for a given concept.
	 * You can request only preferred terms or get alternative ones too.
	 * @param conceptUri
	 * @param preferredOnly true if only preferred terms should be returned
	 * @return
	 */
	public List<ThesaurusTerm> getAllTerms(String conceptUri, Boolean preferredOnly);
	/** get a preferred term for a given concept URI in a given language.
	 * if no term for that language exists, null is returned.
	 * @param conceptUri
	 * @param language
	 * @return
	 */
	public ThesaurusTerm getTerm(String conceptUri, String language);
	
	/**
	 * This will communicate with the central registry of thesauri and update the database tables 
	 */
	public void synchroniseThesauriWithRepository();	
	
	/**
	 * Deletes any existing Vocab with the same URI and saves this one
	 * This might not be the best strategy and a proper synchronise might be more appropriate
	 * @param tv To save
	 */
	public void synchronise(ThesaurusVocabulary tv);
}
