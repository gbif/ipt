package org.gbif.provider.service.impl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.xmlbeans.impl.xb.xsdschema.RestrictionDocument.Restriction;
import org.gbif.provider.model.ThesaurusConcept;
import org.gbif.provider.model.ThesaurusTerm;
import org.gbif.provider.model.ThesaurusVocabulary;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.service.ThesaurusManager;
import org.hibernate.criterion.Restrictions;
import org.hibernate.impl.CriteriaImpl;

public class ThesaurusManagerHibernate extends GenericManagerHibernate<ThesaurusTerm> implements ThesaurusManager{
	public ThesaurusManagerHibernate() {
		super(ThesaurusTerm.class);
	}

	public ThesaurusConcept getConcept(Long id){
		return (ThesaurusConcept) getSession().createQuery("from ThesaurusConcept   where id = :id")
    	.setParameter("id", id)
    	.uniqueResult();
	}
	public ThesaurusConcept getConcept(String uri) {
		return (ThesaurusConcept) getSession().createQuery("select c from ThesaurusConcept c  where c.uri=:uri")
    	.setString("term", uri)
    	.uniqueResult();
	}
	public ThesaurusConcept getConcept(Rank rank) {
		return getConcept(rank.uri);
	}
	public ThesaurusConcept getConcept(String vocabularyUri, String term) {
		List<ThesaurusConcept> concepts = getConcepts(vocabularyUri, term);
		if (concepts.isEmpty()){
			return null;
		}else{
			return concepts.get(0);
		}
	}
	
	public List<ThesaurusConcept> getConcepts(String vocabularyUri, String term) {
		return getSession().createQuery("select c from ThesaurusConcept c join c.terms t  where c.vocabulary.uri=:vocabularyUri and t.title=:term   order by c.conceptOrder, c.identifier")
    	.setString("vocabularyUri", vocabularyUri)
    	.setString("term", term)
    	.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<ThesaurusConcept> getAllConcepts(String vocabularyUri) {
		// select c from  ThesaurusConcept c join ThesaurusVocabulary v  where v.uri=:vocabularyUri 
		return getSession().createQuery("select con from  ThesaurusConcept con where con.vocabulary.uri=:vocabularyUri   order by con.conceptOrder, con.identifier")
    	.setString("vocabularyUri", vocabularyUri)
    	.list();
	}
//order by ThesaurusConcept.conceptOrder
	@SuppressWarnings("unchecked")
	public List<ThesaurusTerm> getAllTerms(String conceptUri, Boolean preferredOnly) {
		if (preferredOnly){
			return getSession().createQuery("select t from ThesaurusTerm t   where t.concept.uri=:conceptUri and t.preferred=true  order by lang, preferred desc")
	    	.setString("conceptUri", conceptUri)
	    	.list();
		}else{
			return getSession().createQuery("select t from ThesaurusTerm t   where t.concept.uri=:conceptUri  order by lang, preferred desc")
	    	.setString("conceptUri", conceptUri)
	    	.list();
		}
	}

	public ThesaurusTerm getTerm(String conceptUri, String language) {
		return (ThesaurusTerm) getSession().createQuery("from ThesaurusTerm t   where t.concept.uri=:conceptUri and preferred=true and t.lang=:lang")
    	.setString("conceptUri", conceptUri)
    	.setString("lang", language)
    	.uniqueResult();
	}


	public Map<Long, String> getConceptIdMap(String vocabularyUri, String language, boolean sortAlphabetically) {
		Map<Long, String> map = new LinkedHashMap<Long, String>();
		String hql;
		if (sortAlphabetically){
			hql = "select c.id, c.identifier, t.title, t2.title, concat(t.title, t2.title, c.identifier)  from ThesaurusConcept c left join c.terms t  with t.lang=:lang and t.preferred=true  left join c.terms t2  with t2.lang=:english and t2.preferred=true    where c.vocabulary.uri=:vocUri   order by concat(t.title, t2.title, c.identifier)";
		}else{
			hql = "select c.id, c.identifier, t.title, t2.title  from ThesaurusConcept c left join c.terms t  with t.lang=:lang and t.preferred=true  left join c.terms t2  with t2.lang=:english and t2.preferred=true    where c.vocabulary.uri=:vocUri   order by c.conceptOrder, c.identifier";
		}
		List<Object[]> rows = getSession().createQuery(hql)
			.setString("vocUri", vocabularyUri)
			.setString("lang", language)
			.setString("english", "en")
			.list();  
		for (Object[] row : rows){
			// does language specific term exist?
			if (row[2]!=null){
				map.put((Long) row[0], (String) row[2]);
			}else if (row[3]!=null){
				// does english version exist?
				map.put((Long) row[0], (String) row[3]);
			}else{
				// otherwise use the code/identifier itself
				map.put((Long) row[0], (String) row[1]);
			}
		}
		return map;
	}

	public Map<String, String> getConceptCodeMap(String vocabularyUri, String language, boolean sortAlphabetically) {
		Map<String, String> map = new LinkedHashMap<String, String>();
		String hql;
		if (sortAlphabetically){
			hql = "select c.identifier, t.title, t2.title, concat(t.title, t2.title, c.identifier)  from ThesaurusConcept c left join c.terms t  with t.lang=:lang and t.preferred=true  left join c.terms t2  with t2.lang=:english and t2.preferred=true    where c.vocabulary.uri=:vocUri   order by concat(t.title, t2.title, c.identifier)";
		}else{
			hql = "select c.identifier, t.title, t2.title  from ThesaurusConcept c left join c.terms t  with t.lang=:lang and t.preferred=true  left join c.terms t2  with t2.lang=:english and t2.preferred=true    where c.vocabulary.uri=:vocUri   order by c.conceptOrder, c.identifier";
		}
		List<Object[]> rows = getSession().createQuery(hql)
			.setString("vocUri", vocabularyUri)
			.setString("lang", language)
			.setString("english", "en")
			.list();  
		for (Object[] row : rows){
			// does language specific term exist?
			String ident = ((String)row[0]).toLowerCase();
			if (row[1]!=null){
				map.put(ident, (String) row[1]);
			}else if (row[2]!=null){
				// does english version exist?
				map.put(ident, (String) row[2]);
			}else{
				// otherwise use the code/identifier itself
				map.put(ident, (String) row[0]);
			}
		}
		return map;
	}

	public List<ThesaurusVocabulary> getVocabularies() {
		return getSession().createQuery("from ThesaurusVocabulary order by uri").list();
	}

	public ThesaurusVocabulary getVocabulary(String uri) {
		return (ThesaurusVocabulary) getSession().createQuery("from ThesaurusVocabulary where uri = :uri")
				.setString("uri", uri)
				.uniqueResult();
	}

	public ThesaurusVocabulary getVocabulary(Long id) {
		return (ThesaurusVocabulary) getSession().get(ThesaurusVocabulary.class, id);
	}


}
