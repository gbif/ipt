package org.gbif.provider.service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import org.appfuse.model.User;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.UploadEvent;

public interface CacheManager {
	/**
	 * Retrieve the currently running upload jobs
	 * @return
	 */
	Set<Long> currentUploads();
	
	/**
	 * Retrieve all upload jobs scheduled for execution, running or not yet started
	 * @return
	 */
	Set<Long> scheduledUploads();
	
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
	 * @param userId the user id that submitted this task
	 * @param resource
	 * @return
	 */
	Future runUpload(Long resourceId, Long userId);

	/**
	 * @param resourceId
	 */
	void cancelUpload(Long resourceId);

	/**
	 * Submit only a new post-processing task to the executor service. Throws an exception in case this resource has already a scheduled or running task
	 * @param resource
	 * @return
	 */	
	void runPostProcess(Long resourceId, Long userId);
	
	/**
	 * Clear all cached upload artifacts as if the resource has just been newly created, i.e all data apart from the resource metadata itself: 
	 * core+extension records, taxa, regions, uploadEvents, problemLogs, plus reset all cached stats in the resource entity itself
	 * Be very careful when using this method as it removes most resource data. 
	 * When preparing a new upload this method should *not* be used as it also removes the upload history and all core records as opposed to just flag them for deletion.
	 * Upload tasks need therefore to implement their own cache clearing methods.
	 * @param resource
	 */	
	void clearCache(Long resourceId);
	
	/**
	 * Go through all resources and run an upload job in case the resource schedule interval indicates so
	 */
	void runScheduledResources(Long userId);
	
	/**
	 * go through all resources and see if unfinished uploads exist. If so, clearCache and maybe submit new upload?
	 * Useful for restarting a service with an interrupted upload 
	 */
	void cleanupResources();
}
