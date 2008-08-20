package org.gbif.provider.upload;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.gbif.provider.datasource.RdbmsImportSourceTest;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.service.CacheManager;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.ContextAwareTestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;

public class OccUploadTaskTest extends ContextAwareTestBase{
	@Autowired
	private OccUploadTask occUploadTask;

	@Test
	public void testUploadTask() {
		occUploadTask.setMaxRecords(100);
		occUploadTask.setResourceId(Constants.TEST_RESOURCE_ID);
		occUploadTask.setUserId(Constants.TEST_USER_ID);
		
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
