package org.gbif.provider.service.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.gbif.provider.model.ThesaurusConcept;
import org.gbif.provider.model.ThesaurusTerm;
import org.gbif.provider.model.ThesaurusVocabulary;
import org.gbif.provider.service.ThesaurusManager;

public class ThesaurusManagerHibernate extends GenericManagerHibernate<ThesaurusTerm> implements ThesaurusManager{
	public ThesaurusManagerHibernate() {
		super(ThesaurusTerm.class);
	}

	public ThesaurusConcept getConcept(Long id){
		return (ThesaurusConcept) getSession().createQuery("from ThesaurusConcept   where id = :id")
    	.setParameter("id", id)
    	.uniqueResult();
	}
	public ThesaurusConcept getConcept(String vocabularyUri, String term) {
		return (ThesaurusConcept) getSession().createQuery("from ThesaurusConcept c, ThesaurusTerm t  where c.vocabulary.uri=:vocabularyUri and t.concept=c and t.title=:term   order by c.conceptOrder")
    	.setString("vocabularyUri", vocabularyUri)
    	.setString("term", term)
    	.uniqueResult();
	}
	
	public List<ThesaurusConcept> getConcepts(String vocabularyUri, String term) {
		return getSession().createQuery("from ThesaurusConcept c, ThesaurusTerm t  where c.vocabulary.uri=:vocabularyUri and t.concept=c and t.title=:term   order by c.conceptOrder")
    	.setString("vocabularyUri", vocabularyUri)
    	.setString("term", term)
    	.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<ThesaurusConcept> getAllConcepts(String vocabularyUri) {
		// select c from  ThesaurusConcept c join ThesaurusVocabulary v  where v.uri=:vocabularyUri 
		return getSession().createQuery("from  ThesaurusConcept con where con.vocabulary.uri=:vocabularyUri ")
    	.setString("vocabularyUri", vocabularyUri)
    	.list();
	}
//order by ThesaurusConcept.conceptOrder
	@SuppressWarnings("unchecked")
	public List<ThesaurusTerm> getAllTerms(String conceptUri, Boolean acceptedOnly) {
		if (acceptedOnly){
			return getSession().createQuery("from ThesaurusTerm t   where t.concept.uri=:conceptUri and t.concept.accepted=true  order by lang, accepted desc")
	    	.setString("conceptUri", conceptUri)
	    	.list();
		}else{
			return getSession().createQuery("from ThesaurusTerm t   where t.concept.uri=:conceptUri  order by lang, accepted desc")
	    	.setString("conceptUri", conceptUri)
	    	.list();
		}
	}

	public ThesaurusTerm getTerm(String conceptUri, String language) {
		return (ThesaurusTerm) getSession().createQuery("from ThesaurusTerm t   where t.concept.uri=:conceptUri and accepted=true and t.lang=:lang")
    	.setString("conceptUri", conceptUri)
    	.setString("lang", language)
    	.uniqueResult();
	}


	public Map<Long, String> getI18nCodeMap(String vocabularyUri, String language) {
		Map<Long, String> map = new HashMap<Long, String>();
		List<Object[]> rows = getSession().createQuery("select c.id, c.identifier, t.title  from ThesaurusConcept c left join ThesaurusTerm t with t.lang=:lang  where c.vocabulary.uri=:vocUri")
			.setString("vocUri", vocabularyUri)
			.setString("lang", language)
			.list();  
		for (Object[] row : rows){
			// does language specific term exist?
			if (row[2]!=null){
				map.put((Long) row[0], (String) row[2]);
			}else{
				// otherwise use the code/identifier itself
				map.put((Long) row[0], (String) row[1]);
			}
		}
		return map;
	}

	public List<ThesaurusVocabulary> getVocabularies() {
		return getSession().createQuery("from ThesaurusVocabulary order by uri").list();
	}

	public ThesaurusVocabulary getVocabulary(String uri) {
		return (ThesaurusVocabulary) getSession().createQuery("from ThesaurusVocabulary where uri = :uri")
				.setString("uir", uri)
				.uniqueResult();
	}

	public ThesaurusVocabulary getVocabulary(Long id) {
		return (ThesaurusVocabulary) getSession().get(ThesaurusVocabulary.class, id);
	}


}
