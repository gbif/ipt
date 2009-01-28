package org.gbif.provider.upload;

import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.task.Task;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.ContextAwareTestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class OccUploadTest extends ContextAwareTestBase{
	@Autowired
	@Qualifier("occUploadTask")
	private Task<UploadEvent> uploadTask;
	@Autowired
	private OccResourceManager occResourceManager;


	@Test
	@Transactional(readOnly=true, propagation=Propagation.REQUIRED)
	public void testUpload() throws Exception {
		uploadTask.init(Constants.TEST_OCC_RESOURCE_ID);		
		UploadEvent event = uploadTask.call();
		OccurrenceResource res = occResourceManager.get(Constants.TEST_OCC_RESOURCE_ID);
		System.out.println(res.getBbox());
		System.out.println(res.getNumTaxa());
		System.out.println(res.getNumFamilies());
		System.out.println(res.getNumCountries());
		System.out.println(event);
	}

}
