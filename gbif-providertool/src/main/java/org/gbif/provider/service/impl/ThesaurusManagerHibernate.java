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

	public ThesaurusConcept getConcept(Long id){
		return (ThesaurusConcept) getSession().createQuery("from ThesaurusConcept   where id = :id")
    	.setParameter("id", id)
    	.uniqueResult();
	}
	public ThesaurusConcept getConcept(Vocabulary vocabulary, String term) {
		return (ThesaurusConcept) getSession().createQuery("select c from ThesaurusConcept c, ThesaurusTerm t   where t.concept = c and t.type = :type and t.term = :term")
    	.setParameter("type", vocabulary)
    	.setString("term", term)
    	.uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	public List<ThesaurusConcept> getAllConcepts(Vocabulary vocabulary) {
		return getSession().createQuery("from ThesaurusConcept   where type = :type  order by identifier")
    	.setParameter("type", vocabulary)
    	.list();
	}

	@SuppressWarnings("unchecked")
	public List<ThesaurusTerm> getAllTerms(ThesaurusConcept concept, Boolean acceptedOnly) {
		if (acceptedOnly){
			return getSession().createQuery("from ThesaurusTerm   where concept = :concept and accepted = true  order by lang, accepted desc")
	    	.setEntity("concept", concept)
	    	.list();
		}else{
			return getSession().createQuery("from ThesaurusTerm   where concept = :concept  order by lang, accepted desc")
	    	.setEntity("concept", concept)
	    	.list();
		}
	}

	public ThesaurusTerm getTerm(ThesaurusConcept concept, String language) {
		return (ThesaurusTerm) getSession().createQuery("from ThesaurusTerm   where concept = :concept and accepted=true and lang= :lang")
    	.setEntity("concept", concept)
    	.setString("lang", language)
    	.uniqueResult();
	}


}
