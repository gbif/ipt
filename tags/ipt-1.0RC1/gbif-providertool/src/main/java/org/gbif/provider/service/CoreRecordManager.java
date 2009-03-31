package org.gbif.provider.service;

import java.util.List;

import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.dto.ValueListCount;
import org.gbif.provider.tapir.filter.Filter;
import org.hibernate.ScrollableResults;

public interface CoreRecordManager<T extends CoreRecord> extends GenericResourceRelatedManager<T>{

    List<T> getAll(final Long resourceId);
    ScrollableResults scrollResource(final Long resourceId);
    
	/**
	 * Flag all core records for a given resource as deleted by setting coreRecord.isDeleted=true
	 * @param resource that contains the core records to be flagged
	 */
	void flagAllAsDeleted(DataResource resource);
	
	/**
	 * Find a core record via its local ID within a given resource.
	 * This method assures to return a single record, as the combination is guaranteed to be unique in the database.
	 * @See save
	 * @param sourceId the local identifier used in the source
	 * @param resourceId the resource identifier for the source
	 * @return
	 */
	T findBySourceId(String sourceId, Long resourceId);
	
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
	
    /**
     * Generic method to save a core record - handles both update and insert.
     * Make sure the local_id + resource_fk combination is unique, otherwise you will get an EntityExistsException
     * @param object the object to save
     * @return the updated object
     */
    T save(T object);	

	/** get latest modified resources
	 * @param startPage starting page, first page = 1
	 * @param pageSize
	 * @return
	 */
	List<T> latest(Long resourceId, int startPage, int pageSize);

	public List<ValueListCount> inventory(Long resourceId, List<ExtensionProperty> properties, Filter filter, int start, int limit);
	public int inventoryCount(Long resourceId, List<ExtensionProperty> properties, Filter filter);
	public List<T> search(Long resourceId, Filter filter, int start, int limit);
	public int searchCount(Long resourceId, Filter filter);

}
