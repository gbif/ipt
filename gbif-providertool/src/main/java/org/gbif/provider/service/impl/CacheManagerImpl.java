package org.gbif.provider.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.logging.service.LogEventManager;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.service.CacheManager;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.ExtensionRecordManager;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.service.OccStatManager;
import org.gbif.provider.service.RegionManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.service.UploadEventManager;
import org.gbif.provider.upload.Task;
import org.gbif.provider.util.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.transaction.annotation.Transactional;


public class CacheManagerImpl implements CacheManager{
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
	private OccResourceManager occResourceManager;
	@Autowired
	private UploadEventManager uploadEventManager;
	@Autowired
	private TaxonManager taxonManager;
	@Autowired
	private RegionManager regionManager;
	@Autowired
	private DarwinCoreManager darwinCoreManager;
	@Autowired
	private ExtensionRecordManager extensionRecordManager;
	@Autowired
	private OccStatManager occStatManager;
	@Autowired
	private LogEventManager logEventManager;


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

	@Transactional(readOnly=false)
	public void clearCache(Long resourceId) {
		OccurrenceResource res = occResourceManager.get(resourceId);
		// flag all old records as deleted, but dont remove them from the cache
		darwinCoreManager.flagAllAsDeleted(res);
		// remove extension data
		for (ViewMappingBase vm : res.getExtensionMappings()){
			vm.setRecTotal(0);
			extensionRecordManager.removeAll(vm.getExtension(), resourceId);
		}
		// update resource stats
		res.resetStats();
		occResourceManager.save(res);
		
		// remove core record related upload artifacts like taxa & regions
		occStatManager.removeAll(res);
		taxonManager.removeAll(res);
		regionManager.removeAll(res);
		logEventManager.removeByGroup(resourceId.intValue());

		// remove generated files
		File dump = cfg.getDumpArchiveFile(resourceId);
		dump.delete();
		
		// remove cached static files
		File cacheDir = cfg.getResourceCacheDir(resourceId);
		try {
			FileUtils.deleteDirectory(cacheDir);
		} catch (IOException e) {
			log.error("Couldn't remove existing resource cache at "+cacheDir.getAbsolutePath(), e);
			e.printStackTrace();
		}
	}

	@Transactional(readOnly=false)
	public void resetResource(Long resourceId) {
		clearCache(resourceId);
		OccurrenceResource res = occResourceManager.get(resourceId);
		uploadEventManager.removeAll(res);
		darwinCoreManager.removeAll(res);
		// remove data dir
		File dataDir = cfg.getResourceDataDir(resourceId);
		try {
			FileUtils.deleteDirectory(dataDir);
			log.info("Removed resource data dir "+dataDir.getAbsolutePath());
		} catch (IOException e) {
			log.error("Cant remove data dir for resource "+resourceId, e);
		}
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
	public Set<Long> scheduledUploads() {
		// FIXME: needs implementation
		log.error("NOT IMPLEMENTED YET");
		return new HashSet<Long>();
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
			status = "Source ready to be used.";			
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
