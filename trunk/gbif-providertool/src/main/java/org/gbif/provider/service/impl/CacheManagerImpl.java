package org.gbif.provider.service.impl;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.lang.NotImplementedException;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.service.CacheManager;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.service.UploadEventManager;
import org.gbif.provider.upload.OccUploadTask;
import org.gbif.provider.upload.Task;
import org.gbif.provider.util.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskRejectedException;


public class CacheManagerImpl implements CacheManager{
	@Autowired
	private AppConfig cfg;
	@Autowired
	@Qualifier("uploadExecutor")
	private ExecutorService uploadExecutor;
	@Autowired
	@Qualifier("processingExecutor")
	private ExecutorService processingExecutor;

	@Autowired
	private OccResourceManager occResourceManager;
	@Autowired
	private UploadEventManager uploadEventManager;

    private final Map<Long, Future> futures = new ConcurrentHashMap<Long, Future>();
    private final Map<Long, Task> uploads = new ConcurrentHashMap<Long, Task>();

	private Future submitUpload(Task task) throws TaskRejectedException{
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
		return f;
	}

	protected Task<UploadEvent> newOccUploadTask(){
		throw new NotImplementedException("Should have been overriden by Springs method injection");
	}
	protected Task newOccProcessingTask(){
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

	public Future runUpload(Long resourceId, Long userId) {
		// create task
		Task<UploadEvent> task = newOccUploadTask();
		task.init(resourceId, userId);
		// submit
		return submitUpload(task);
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
