package org.gbif.provider.service.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
	protected final Log log = LogFactory.getLog(getClass());

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
		// FIXME: needs implementation
		log.error("NOT IMPLEMENTED YET");
	}

	public void clearCache(Long resourceId) {
		// FIXME: needs implementation
		log.error("NOT IMPLEMENTED YET");
	}

	public Set<Long> currentUploads() {
		for (Long id : futures.keySet()){
			Future f = futures.get(id);
			Task t = uploads.get(id);
			if (f.isDone()){
				futures.remove(f);
				uploads.remove(t);
			}
		}
		return new HashSet<Long>(futures.keySet());
	}
	public Set<Long> scheduledUploads() {
		// FIXME: needs implementation
		log.error("NOT IMPLEMENTED YET");
		return new HashSet<Long>();
	}

	public String getUploadStatus(Long resourceId) {
		Task t = uploads.get(resourceId);
		String status;
		if (t!=null){
			Future f = futures.get(resourceId);
			if (f.isDone()){
				status = "Finished: " + t.status();
				futures.remove(f);
				uploads.remove(t);
			}else{
				status = t.status();			
			}
		}else{
			status = "No activity";			
		}
		return status;
	}

	public void runScheduledResources(Long userId) {
		// FIXME: needs implementation
		log.error("NOT IMPLEMENTED YET");
	}

	public Future runUpload(Long resourceId, Long userId) {
		// create task
		Task<UploadEvent> task = newOccUploadTask();
		task.init(resourceId, userId);
		// submit
		return submitUpload(task);
	}

	public void cancelUpload(Long resourceId) {
		Future<?> f = futures.get(resourceId);
		if (f!=null){
			f.cancel(true);
		}else{
			log.warn("No task running for resource "+resourceId);
		}
		
	}

	public void runPostProcess(Long resourceId, Long userId) {
		log.error("NOT IMPLEMENTED YET");
	}

	public void setUploadExecutor(ExecutorService uploadExecutor) {
		this.uploadExecutor = uploadExecutor;
	}

	public void setProcessingExecutor(ExecutorService processingExecutor) {
		this.processingExecutor = processingExecutor;
	}

	
	
}
