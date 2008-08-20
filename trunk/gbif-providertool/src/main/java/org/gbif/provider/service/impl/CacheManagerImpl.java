package org.gbif.provider.service.impl;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.lang.NotImplementedException;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.service.CacheManager;
import org.gbif.provider.service.UploadEventManager;
import org.gbif.provider.upload.CachingTask;
import org.gbif.provider.upload.OccUploadTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskRejectedException;


public class CacheManagerImpl implements CacheManager{
	private ExecutorService uploadExecutor;
	private ExecutorService processingExecutor;

	@Autowired
	private OccResourceManager occResourceManager;
	@Autowired
	private UploadEventManager uploadEventManager;

    private final Map<Long, Future> futures = new ConcurrentHashMap<Long, Future>();
    private final Map<Long, CachingTask> uploads = new ConcurrentHashMap<Long, CachingTask>();

	private void submitUpload(CachingTask task) throws TaskRejectedException{
		Long resourceId = task.getResourceId();
		if (futures.containsKey(resourceId)){
			Future f = futures.get(resourceId);
			if (!f.isDone()){
				// there is an old task still running or scheduled. 
				// Cant schedule another upload, throw exception instead
				throw new TaskRejectedException(String.format("Upload for resource %s already scheduled or running", resourceId));
			}
		}
		uploads.put(resourceId, task);
		Future f = uploadExecutor.submit(task);
		futures.put(resourceId, f);
	}

	protected OccUploadTask newOccUploadTask(){
		throw new NotImplementedException("Should have been overriden by Springs method injection");
	}
	protected CachingTask newOccProcessingTask(){
		throw new NotImplementedException("Should have been overriden by Springs method injection");
	}

	
	
	public void cleanupResources() {
		throw new NotImplementedException("TBD");
	}

	public void clearCache(Long resourceId) {
		throw new NotImplementedException("TBD");
	}

	public Set<Long> currentUploads() {
		throw new NotImplementedException("TBD");
	}
	public Set<Long> scheduledUploads() {
		throw new NotImplementedException("TBD");
	}

	public String getUploadStatus(Long resourceId) {
		throw new NotImplementedException("TBD");
	}

	public void runScheduledResources(Long userId) {
		throw new NotImplementedException("TBD");
	}

	public void runUpload(Long resourceId, Long userId, Integer maxRecords) {
		// create task
		OccUploadTask task = newOccUploadTask();
		task.setUserId(userId);
		task.setResourceId(resourceId);
		task.setMaxRecords(maxRecords);
		// submit
		submitUpload(task);
	}

	public void runPostProcess(Long resourceId, Long userId) {
		throw new NotImplementedException("TBD");
	}

	public void setUploadExecutor(ExecutorService uploadExecutor) {
		this.uploadExecutor = uploadExecutor;
	}

	public void setProcessingExecutor(ExecutorService processingExecutor) {
		this.processingExecutor = processingExecutor;
	}

	
	
	
}
