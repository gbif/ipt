package org.gbif.iptlite.service;

import org.gbif.provider.model.BBox;
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

public class ArchiveTaskTest extends ContextAwareTestBase{
	@Autowired
	@Qualifier("archiveTask")
	private Task<UploadEvent> archiveTask;
	@Autowired
	private OccResourceManager occResourceManager;


	@Test
	@Transactional(readOnly=true, propagation=Propagation.REQUIRED)
	public void testUpload() throws Exception {
		archiveTask.init(Constants.TEST_OCC_RESOURCE_ID);		
		UploadEvent event = archiveTask.call();
		assertEquals(1534, event.getRecordsUploaded());
		assertEquals(0, event.getRecordsErroneous());
	}

}
