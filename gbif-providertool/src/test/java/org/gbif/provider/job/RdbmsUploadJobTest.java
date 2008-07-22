package org.gbif.provider.job;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.appfuse.model.User;
import org.gbif.provider.datasource.RdbmsImportSourceTest;
import org.gbif.provider.job.RdbmsUploadJob;
import org.gbif.provider.job.UploadBaseJob;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.UploadEvent;
import org.gbif.scheduler.model.Job;
import org.junit.Before;
import org.junit.Test;

public class RdbmsUploadJobTest extends RdbmsImportSourceTest{
	private RdbmsUploadJob rdbmsUploadJob ;

	public void setRdbmsUploadJob(RdbmsUploadJob rdbmsUploadJob) {
		this.rdbmsUploadJob = rdbmsUploadJob;
	}

	
	@Test
	public void testUploadCore() throws InterruptedException {
		setUpSource();
		OccurrenceResource resource = (OccurrenceResource) getTestResource();
		UploadEvent event = new UploadEvent();
		Map seed = RdbmsUploadJob.getSeed(resource.getId(), 4L, 25);
		rdbmsUploadJob.launch(seed);
	}

	@Test
	public void testUploadExtension() {
	}

}
