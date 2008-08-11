package org.gbif.provider.service;

import java.util.List;

import org.apache.lucene.queryParser.ParseException;
import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.OccurrenceResource;
import org.hibernate.ScrollableResults;

public interface CoreRecordManager<T extends CoreRecord> extends GenericManager<T>{

    public List<T> getAll(final Long resourceId);
    public ScrollableResults scrollResource(final Long resourceId);
    
	/**
	 * Flag all core records for a given resource as deleted by setting coreRecord.isDeleted=true
	 * @param resource that contains the core records to be flagged
	 */
	public void flagAllAsDeleted(OccurrenceResource resource);
	
	/**
	 * Find a core record via its local ID within a given resource
	 * @param localId the local identifier used in the source
	 * @param resourceId the resource identifier for the source
	 * @return
	 */
	public T findByLocalId(String localId, Long resourceId);
	
	/**
	 * same as get by id, but allows underlying db to be partitioned by resource
	 * @param Id
	 * @param resourceId
	 * @return
	 */
	public T get(Long Id, Long resourceId);
	
	
	/**
	 * full text search in all core records of a given resource
	 * @param resourceId
	 * @param q
	 * @return
	 * @throws ParseException 
	 */
	public List<T> search(Long resourceId, String q) throws ParseException;
	public void reindex(Long resourceId);
	public void flush();
}
