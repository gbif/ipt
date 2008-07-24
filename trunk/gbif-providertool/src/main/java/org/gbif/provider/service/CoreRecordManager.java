package org.gbif.provider.service;

import java.util.List;

import org.gbif.provider.model.CoreRecord;

public interface CoreRecordManager<T extends CoreRecord> extends GenericManager<T>{

	/**
	 * Flag all core records for a given resource as deleted by setting coreRecord.isDeleted=true
	 * @param resourceId
	 */
	public void flagAsDeleted(Long resourceId);
	
	/**
	 * Update sideleted flag for a single record
	 * @param id
	 * @param resourceId
	 * @param isDeleted
	 */
	public void updateIsDeleted(Long id, Long resourceId, boolean isDeleted);
	
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
	
}
