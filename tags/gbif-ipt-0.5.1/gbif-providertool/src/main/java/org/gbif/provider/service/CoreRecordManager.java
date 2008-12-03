package org.gbif.provider.service;

import java.util.List;

import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.OccurrenceResource;
import org.hibernate.ScrollableResults;

public interface CoreRecordManager<T extends CoreRecord> extends GenericResourceRelatedManager<T>{

    List<T> getAll(final Long resourceId);
    ScrollableResults scrollResource(final Long resourceId);
    
	/**
	 * Flag all core records for a given resource as deleted by setting coreRecord.isDeleted=true
	 * @param resource that contains the core records to be flagged
	 */
	void flagAllAsDeleted(OccurrenceResource resource);
	
	/**
	 * Find a core record via its local ID within a given resource.
	 * This method assures to return a single record, as the combination is guaranteed to be unique in the database.
	 * @See save
	 * @param localId the local identifier used in the source
	 * @param resourceId the resource identifier for the source
	 * @return
	 */
	T findByLocalId(String localId, Long resourceId);
	
	/**
	 * same as get by id, but allows underlying db to be partitioned by resource
	 * @param Id
	 * @param resourceId
	 * @return
	 */
	T get(Long Id, Long resourceId);
	
	/**
	 * get single record by its GUID
	 * @param Id
	 * @return
	 */
	T get(String guid);
	
	/**
	 * full text search in all core records of a given resource
	 * @param resourceId
	 * @param q
	 * @return
	 */
	List<T> search(Long resourceId, String q);
	void reindex(Long resourceId);
	
    /**
     * Generic method to save a core record - handles both update and insert.
     * Make sure the local_id + resource_fk combination is unique, otherwise you will get an EntityExistsException
     * @param object the object to save
     * @return the updated object
     */
    T save(T object);	
}
