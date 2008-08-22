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

	/**
	 * Submit a new upload job (incl postprocessing) to the executor service. Throws an exception in case this resource has already a scheduled or running upload job
	 * @param maxRecords stop the upload after this maximum amount of record has been uploaded. Mainly for testing.
	 * @param userId the user id that submitted this task
	 * @param resource
	 * @return
	 */
	Future runUpload(Long resourceId, Long userId);

	void cancelUpload(Long resourceId);

	/**
	 * Submit only a new post-processing task to the executor service. Throws an exception in case this resource has already a scheduled or running task
	 * @param resource
	 * @return
	 */
	
	void runPostProcess(Long resourceId, Long userId);
	/**
	 * Clear all cached data, i.e. core+extension records and calculated stats data in resource itself
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
