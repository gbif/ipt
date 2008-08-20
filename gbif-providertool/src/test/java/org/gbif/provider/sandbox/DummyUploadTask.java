package org.gbif.provider.sandbox;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.NotImplementedException;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.upload.CachingTask;

public class DummyUploadTask implements CachingTask{
	private ExecutorService processingExecutor;
	private DarwinCoreManager darwinCoreManager;
	private OccurrenceResource resource;
	private AtomicInteger counter = new AtomicInteger(0);
	private boolean uploadDone = false;
	private List<Future<Integer>> futures = new java.util.ArrayList<Future<Integer>>();

	public Long getResourceId() {
		return resource.getId();
	}

	public synchronized String status() {
		if (uploadDone){
			int activeTasks=0;
			for (Future  f : futures){
				if(!f.isDone()){
					activeTasks++;
				}
			}
			return "active tasks="+activeTasks;
		}else{
			return "c"+counter;
		}
	}

	public UploadEvent call() throws Exception {
		System.out.println("Start dummy upload for resource "+resource.getId());
		// do initial sequential upload
		UploadEvent event = new UploadEvent();
		while(counter.get() < 100){
			DarwinCore dwc = DarwinCore.newMock(resource);
			darwinCoreManager.save(dwc);
			counter.addAndGet(1);
		}
		event.setRecordsUploaded(counter.get());
		System.out.println("Upload of 100 mock darwin core records done for resource "+resource.getId());

		// execute 20 processing tasks
		int i = 0;
		while (i<20){
			i++;
			DummyProcessingTask task = createProcessingTask();
			futures.add(processingExecutor.submit(task));
			System.out.println(String.format("new processing task %i submitted for resource %s", i, getResourceId()));
		}		
		return event;
	}
	
	private DummyProcessingTask createProcessingTask(){
		DummyProcessingTask task = createProcessingTask();
		task.setResource(resource);
		return task;
	}
	private DummyProcessingTask newProcessingTask(){
		throw new NotImplementedException("This method should have been replaced by Spring IoC method injection");
	}

	public void setResource(OccurrenceResource resource) {
		this.resource = resource;
	}

	public void setDarwinCoreManager(DarwinCoreManager darwinCoreManager) {
		this.darwinCoreManager = darwinCoreManager;
	}

	public void setProcessingExecutor(ExecutorService processingExecutor) {
		this.processingExecutor = processingExecutor;
	}

}
