package org.gbif.provider.upload;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.ContextAwareTestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class OccUploadTaskTest extends ContextAwareTestBase{
	@Autowired
	@Qualifier("occUploadTask")
	private Task<UploadEvent> occUploadTask;
	@Autowired
	private AppConfig cfg;
	
	@Test
	public void testUploadTask() {
		occUploadTask.init(Constants.TEST_RESOURCE_ID, Constants.TEST_USER_ID);
		
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<UploadEvent> f = executor.submit(occUploadTask);
		try {
			UploadEvent event = f.get();
			System.out.println(event);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
