package org.gbif.provider.service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import org.appfuse.model.User;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.UploadEvent;

public interface CacheManager {
	/**
	 * Retrieve the currently running upload jobs
	 * @return
	 */
	Set<Long> currentUploads();
	
	/**
	 * Get a simple human readable one liner that explains the current status of an upload job
	 * @param resource
	 * @return
	 */
	String getUploadStatus(Long resourceId);
	
	/**Returns true if a resource is busy uploading or postprocessing data
	 * @param resourceId
	 * @return
	 */
	boolean isBusy(Long resourceId);
	
	/**
	 * Submit a new upload job (incl postprocessing) to the executor service. Throws an exception in case this resource has already a scheduled or running upload job
	 * @param maxRecords stop the upload after this maximum amount of record has been uploaded. Mainly for testing.
	 * @param resource
	 * @return
	 */
	Future runUpload(Long resourceId);

	/**
	 * @param resourceId
	 */
	void cancelUpload(Long resourceId);


	/**
	 * Clear all cached upload artifacts but leaves data which is supposed to last between multiple uploads, i.e uploadEvents and darwin core records.
	 * Flags all darwin core records as deleted though. 
	 * When preparing a new upload this method should be used to clean up old upload artifacts. 
	 * @param resource
	 */	
	void prepareUpload(Long resourceId);

	/**
	 * Remove all data in cache related to this resource apart from the resource instance itself. 
	 * I.e. the resource will be in a state afterwards just as if it has just been newly created.
	 * The filesystem is not touched. This is handled by the ResourceManager.remove() method.
	 *  
	 * Be very careful when using this method as it removes all resource data! 
	 * When preparing a new upload this method should *not* be used as it also removes the upload history and all core records as opposed to just flag them for deletion.
	 * Use prepareUpload instead.
	 * @param resource
	 */	
	void clear(Long resourceId);

	/**Analyze cache database, updating statistics to speed up selects
	 * 
	 */
	void analyze();
}
