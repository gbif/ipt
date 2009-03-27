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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.dto.ValueListCount;
import org.gbif.provider.service.CoreRecordManager;
import org.gbif.provider.service.FullTextSearchManager;
import org.gbif.provider.tapir.filter.Filter;
import org.gbif.provider.util.H2Utils;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Generic manager for all datasource based resources that need to be registered with the routing datasource.
 * Overriden methods keep the datasource targetsource map of the active datasource registry in sync with the db.
 * @author markus
 *
 * @param <T>
 */
@Transactional(readOnly=true)
public class CoreRecordManagerHibernate<T extends CoreRecord> extends GenericResourceRelatedManagerHibernate<T> implements CoreRecordManager<T> {
	@Autowired
	protected FullTextSearchManager fullTextSearchManager;	

	public CoreRecordManagerHibernate(Class<T> persistentClass) {
		super(persistentClass);
	}

    public T findBySourceId(final String sourceId, final Long resourceId) {
    	T result = null;
    	try{
    		Query query = getSession().createQuery(String.format("select core FROM %s core WHERE core.resource.id = :resourceId and core.sourceId = :sourceId", persistentClass.getName()))
						.setLong("resourceId", resourceId)
						.setString("sourceId", sourceId);
        	result = (T) query.uniqueResult();
    	} catch (NonUniqueResultException e){
    		log.debug("source ID is not unique within the resource. Corrupted cache!");
    	}
		return result;
	}

	public T get(final String guid) {
    	T result = null;
    	try{
    		Query query = getSession().createQuery(String.format("select core FROM %s core WHERE core.guid = :guid", persistentClass.getName()))
						.setParameter("guid", guid);
        	result = (T) query.uniqueResult();
    	} catch (NonUniqueResultException e){
    		log.debug("GUID is not unique. Corrupted cache!");
    	}
		return result;
	}

	@Transactional(readOnly=false)
	public void flagAllAsDeleted(DataResource resource) {
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


	@Override
	public List<T> getAll(final Long resourceId) {
        return query(String.format("from %s e WHERE deleted=false and e.resource.id = :resourceId", persistentClass.getSimpleName()))
		        .setLong("resourceId", resourceId)
        		.list();
	}

	@Override
    public int count(Long resourceId) {
        return ( (Long) query(String.format("select count(e) from %s e WHERE deleted=false and e.resource.id = :resourceId", persistentClass.getSimpleName()))
        .setLong("resourceId", resourceId)
        .iterate().next() ).intValue();
	}


	public List<T> search(final Long resourceId, final String q) {
		List<Long> ids = fullTextSearchManager.search(resourceId, q);
		List<T> results = new LinkedList<T>();
	    for (Long id : ids) {
			T coreObj = get(id);
			log.debug("Adding record[" + id+ "] to results. GUID[" + coreObj.getGuid() + "]");
		    results.add(coreObj);
	    }		    
	    return results;
	}

	public List<T> latest(Long resourceId, int startPage, int pageSize) {
        return query(String.format("from %s e WHERE deleted=false and e.resource.id = :resourceId ORDER BY e.modified, e.id", persistentClass.getSimpleName()))
        .setLong("resourceId", resourceId)
        .setFirstResult(H2Utils.offset(startPage, pageSize))
        .setMaxResults(pageSize)
		.list();
	}	
	
    public ScrollableResults scrollResource(final Long resourceId) {
        Query query = getSession().createQuery(String.format("select core FROM %s core WHERE deleted=false and core.resource.id = :resourceId", persistentClass.getName()))
						.setParameter("resourceId", resourceId);
        return query.scroll(ScrollMode.FORWARD_ONLY);
    }      

    

    // TAPIR related inventory
    public List<ValueListCount> inventory(Long resourceId, List<ExtensionProperty> properties, Filter filter, int start, int limit) {
    	List<ValueListCount> values = new ArrayList<ValueListCount>();
    	String selectHQL = buildSelect(properties);
    	String filterHQL = buildHqlFilter(filter);
    	String hql = String.format("select new List(count(*), %s) from %s WHERE deleted=false and resource.id = :resourceId %s group by %s  ORDER BY %s", selectHQL, persistentClass.getSimpleName(), filterHQL, selectHQL, selectHQL);
        List<List<Object>> rows = query(hql)
	        .setLong("resourceId", resourceId)
	        .setFirstResult(start)
	        .setMaxResults(limit)
			.list();
        for (List<Object> row : rows){
        	values.add(new ValueListCount((Long)row.get(0), row.subList(1, row.size())));
        }
        return values;
	}

	public int inventoryCount(Long resourceId, List<ExtensionProperty> properties, Filter filter) {
    	String selectHQL = buildSelect(properties);
    	String filterHQL = buildHqlFilter(filter);
    	//FIXME: no idea how to count this through hibernate...	
    	List<List<Object>> rows = query(String.format("select new List(%s) from %s WHERE deleted=false and resource.id = :resourceId %s group by %s", selectHQL, persistentClass.getSimpleName(), filterHQL, selectHQL))
        	.setLong("resourceId", resourceId)
	        .list();
        return rows.size();
	}
    
    private String buildSelect(List<ExtensionProperty> properties){
    	List<String> props = new ArrayList<String>();
    	for (ExtensionProperty prop : properties){
    		if (!prop.getExtension().isCore()){
    			throw new IllegalArgumentException("Only core properties are accepted");
    		}
    		props.add(prop.getHQLName());
    	}
    	return StringUtils.join(props, ",");
    }
    
    // TAPIR related search
	public List<T> search(Long resourceId, Filter filter, int start, int limit) {
    	String filterHQL = buildHqlFilter(filter);
        return query(String.format("from %s WHERE deleted=false and resource.id = :resourceId %s ORDER BY id", persistentClass.getSimpleName(), filterHQL))
        .setLong("resourceId", resourceId)
        .setFirstResult(start)
        .setMaxResults(limit)
		.list();
	}

	public int searchCount(Long resourceId, Filter filter) {
    	String filterHQL = buildHqlFilter(filter);
        return ((Long) query(String.format("select count(*) from %s WHERE deleted=false and resource.id = :resourceId %s", persistentClass.getSimpleName(), filterHQL))
        	.setLong("resourceId", resourceId)
	        .iterate().next() ).intValue();
	}

	private String buildHqlFilter(Filter f){
    	String filterHQL = "";
    	if (f !=null){
    		filterHQL = f.toHQL();
    		if (StringUtils.trimToNull(filterHQL)!=null){
    			filterHQL = " and "+filterHQL;
    			log.debug("Using HQL filter: "+filterHQL);
    		}
    	}
    	return filterHQL;
	}
}
