package org.gbif.provider.upload;

import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.task.Task;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.ContextAwareTestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class ChecklistUploadTest extends ContextAwareTestBase{
	@Autowired
	@Qualifier("checklistUploadTask")
	private Task<UploadEvent> uploadTask;


	@Test
	@Transactional(readOnly=true, propagation=Propagation.REQUIRED)
	public void testUpload() throws Exception {
		uploadTask.init(Constants.TEST_CHECKLIST_RESOURCE_ID);		
		UploadEvent event = uploadTask.call();
		System.out.println(event);
	}

}
