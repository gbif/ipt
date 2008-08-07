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

package org.gbif.provider.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.service.CoreRecordManager;
import org.hibernate.CacheMode;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;

/**
 * Generic manager for all datasource based resources that need to be registered with the routing datasource.
 * Overriden methods keep the datasource targetsource map of the active datasource registry in sync with the db.
 * @author markus
 *
 * @param <T>
 */
public class CoreRecordManagerHibernate<T extends CoreRecord> extends GenericManagerHibernate<T> implements CoreRecordManager<T> {
	protected String[] FIELD_NAMES;
	
	public CoreRecordManagerHibernate(Class<T> persistentClass, String[] searchableFieldNames) {
		super(persistentClass);
		this.FIELD_NAMES = searchableFieldNames;
	}

    public List<T> getAll(final Long resourceId) {
        Query query = getSession().createQuery(String.format("select core FROM %s core WHERE core.resource.id = :resourceId", persistentClass.getName()))
						.setParameter("resourceId", resourceId);
		return query.list();
    }
    
    public ScrollableResults scrollResource(final Long resourceId) {
        Query query = getSession().createQuery(String.format("select core FROM %s core WHERE core.resource.id = :resourceId", persistentClass.getName()))
						.setParameter("resourceId", resourceId);
        return query.scroll(ScrollMode.FORWARD_ONLY);
    }      

    
    public T findByLocalId(final String localId, final Long resourceId) {
        Query query = getSession().createQuery(String.format("select core FROM %s core WHERE core.resource.id = :resourceId and core.localId = :localId", persistentClass.getName()))
						.setParameter("resourceId", resourceId)
						.setParameter("localId", localId);
		return (T) query.uniqueResult();
	}

	public T get(final Long Id, final Long resourceId) {
		Query query = getSession().createQuery(String.format("select core FROM %s core WHERE core.resource.id = :resourceId AND core.id = :Id", persistentClass.getName()))
						.setParameter("resourceId", resourceId)
						.setParameter("Id", Id);
		return (T) query.uniqueResult();
	}

	public void flagAllAsDeleted(OccurrenceResource resource) {
		// use DML-style HQL batch updates
		// http://www.hibernate.org/hib_docs/reference/en/html/batch.html
		Session session = getSession();
		String hqlUpdate = String.format("update %s core set core.deleted = true WHERE core.resource = :resource", persistentClass.getName());
		int count = session.createQuery( hqlUpdate )
		        .setEntity("resource", resource)
		        .executeUpdate();
		
//		ScrollableResults coreRecords = session.createQuery(String.format("select core FROM %s core WHERE core.resource.id = :resourceId", persistentClass.getName()))
//							    		.setCacheMode(CacheMode.IGNORE)
//							    		.scroll(ScrollMode.FORWARD_ONLY);
//		int count=0;
//		while ( coreRecords.next() ) {
//		    T core = (T) coreRecords.get(0);
//		    core.setDeleted(true);
//		    // no explicit save call needed???
//		    if ( ++count % 100 == 0 ) {
//		        //flush a batch of updates and release memory:
//		        session.flush();
//		        session.clear();
//		    }
//		}
		log.info(String.format("%s %s records of resource were flagged as deleted.", count, persistentClass.getName(), resource.getId()));
	}

	public List<T> search(final Long resourceId, final String q) throws ParseException {
	     FullTextSession fullTextSession = Search.createFullTextSession(getSession());
	     QueryParser parser = new MultiFieldQueryParser(FIELD_NAMES, new StandardAnalyzer());
	     org.apache.lucene.search.Query query = parser.parse(q);
	     org.hibernate.Query hibernateQuery = fullTextSession.createFullTextQuery(query, persistentClass);
	     List results = hibernateQuery.list();
	     return results;
	}

	public void reindex(Long resourceId){
		Session hibernateSession = getSession();
		FullTextSession fullTextSession = Search.createFullTextSession(hibernateSession);
		ScrollableResults results = scrollResource(resourceId);
		Transaction tx = fullTextSession.beginTransaction();

		Long counter = 0l;
		while (results.next()){
            fullTextSession.index((T)results.get(0)); 
		    if (++counter % 1000 == 0){
                fullTextSession.flush(); //apply changes to indexes 
                fullTextSession.clear(); //clear since the queue is processed 
	            hibernateSession.clear(); 
                log.debug(String.format("Indexed %s records of resource %s", counter, resourceId));
		    }
		}
		tx.commit(); //index are written at commit time  		
	}
}
