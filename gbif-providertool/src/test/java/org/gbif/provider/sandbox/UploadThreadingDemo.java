package org.gbif.provider.sandbox;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.appfuse.dao.BaseDaoTestCase;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.service.ResourceFactory;
import org.gbif.provider.util.ContextAwareTestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class UploadThreadingDemo extends ContextAwareTestBase{
	@Autowired
	@Qualifier("uploadExecutor")
	private ExecutorService uploadExecutor;
	@Autowired
	@Qualifier("processingExecutor")
	private ExecutorService processingExecutor;
	@Autowired
	private OccResourceManager occResourceManager;
	@Autowired
	private ResourceFactory resourceFactory;

	@Test
	public void testDummyUploads() throws InterruptedException, ExecutionException{
		Long i = 0l;
		List<Future<UploadEvent>> futures = new ArrayList<Future<UploadEvent>>(); 
		List<DummyUploadTask> uploads= new ArrayList<DummyUploadTask>(); 
		while(i<4){
			i++;
			DummyUploadTask task = (DummyUploadTask) this.applicationContext.getBean("dummyUploadTask");
			OccurrenceResource res = resourceFactory.newOccurrenceResourceInstance();
			res = occResourceManager.save(res);
			task.setResource(res);	
			uploads.add(task);
			System.out.println(task.status());
			Future<UploadEvent> f = uploadExecutor.submit(task);
			futures.add(f);
			System.out.println(task.status());
		}
		System.out.println("Started all 4 dummy uploads");
		while (futures.size() > 0){
			List<Future<UploadEvent>> f2 = new ArrayList<Future<UploadEvent>>(futures);
			for (Future<UploadEvent> f : f2){
				if(f.isDone()){
					System.out.println("Task done!");
					futures.remove(f);
				}
			}
			String stat = "status:";
			for (DummyUploadTask upl : uploads){
				stat = stat+" "+upl.status();
			}
			System.out.println(stat);
//			wait(2000);
//			System.out.println(futures.size() +" upload jobs still running");
		}
	}

	public void setUploadExecutor(ExecutorService uploadExecutor) {
		this.uploadExecutor = uploadExecutor;
	}

	public void setProcessingExecutor(ExecutorService processingExecutor) {
		this.processingExecutor = processingExecutor;
	}
	
}
