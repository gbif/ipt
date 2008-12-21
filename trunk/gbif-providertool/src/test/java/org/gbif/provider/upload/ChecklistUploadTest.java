package org.gbif.provider.upload;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.persistence.EntityExistsException;

import org.appfuse.dao.BaseDaoTestCase;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Region;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.model.dto.DwcRegion;
import org.gbif.provider.model.dto.DwcTaxon;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.task.RecordPostProcessor;
import org.gbif.provider.task.Task;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.ContextAwareTestBase;
import org.gbif.provider.util.ResourceTestBase;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.AssertThrows;
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
