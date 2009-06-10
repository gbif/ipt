package org.gbif.iptlite.service;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.mail.MethodNotSupportedException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.apache.bcel.verifier.exc.InvalidMethodException;
import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.service.AnnotationManager;
import org.gbif.provider.service.CacheManager;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.service.impl.BaseManagerJDBC;
import org.gbif.provider.task.Task;
import org.gbif.provider.util.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.transaction.annotation.Transactional;


public class CacheManagerJDBC extends BaseManagerJDBC implements CacheManager{
	protected final Log log = LogFactory.getLog(getClass());

	@Autowired
	@Qualifier("uploadExecutor")
	private ExecutorService uploadExecutor;
	@Autowired
	@Qualifier("processingExecutor")
	private ExecutorService processingExecutor;

	@Autowired
	private AppConfig cfg;
	@Autowired
	@Qualifier("dataResourceManager")
	private GenericResourceManager<DataResource> dataResourceManager;
	@Autowired
	private AnnotationManager annotationManager;


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

	protected Task<File> newArchiveTask(){
		throw new NotImplementedException("Should have been overriden by Springs method injection");
	}

	


	public Set<Long> currentUploads() {
		for (Long id : futures.keySet()){
			Future f = futures.get(id);
			if (f.isDone()){
				futures.remove(id);
				uploads.remove(id);
			}
		}
		return new HashSet<Long>(futures.keySet());
	}

	public boolean isBusy(Long resourceId){
		if (currentUploads().contains(resourceId)){
			return true;
		}		
		return false;
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
			status = "Archive generated: "+cfg.getArchiveUrl(resourceId);			
		}
		return status;
	}

	public Future runUpload(Long resourceId) {
		DataResource res = dataResourceManager.get(resourceId);
		// create task
		Task<File> task;
		task = newArchiveTask();
		task.init(resourceId);
		// submit
		return submitUpload(task);
	}

	
	
	
	public void analyze() {
		throw new InvalidMethodException("Cache method not supported due to missing cache db");
	}

	public void cancelUpload(Long resourceId) {
		throw new InvalidMethodException("Cache method not supported due to missing cache db");
	}

	public void prepareUpload(Long resourceId) {
		throw new InvalidMethodException("Cache method not supported due to missing cache db");
	}

	public void clear(Long resourceId) {
		throw new InvalidMethodException("Cache method not supported due to missing cache db");
	}
}
