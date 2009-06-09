package org.gbif.iptlite.service;

import java.util.concurrent.Future;

public interface CacheManager {
	
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

}
