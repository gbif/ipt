package org.gbif.provider.job;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.appfuse.model.User;
import org.gbif.provider.datasource.RdbmsImportSourceTest;
import org.gbif.provider.job.OccDbUploadJob;
import org.gbif.provider.job.OccUploadBaseJob;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.PathUtil;
import org.gbif.scheduler.model.Job;
import org.junit.Before;
import org.junit.Test;

public class RdbmsUploadJobTest extends RdbmsImportSourceTest{
	private OccDbUploadJob occDbUploadJob ;

	public void setOccDbUploadJob(OccDbUploadJob occDbUploadJob) {
		this.occDbUploadJob = occDbUploadJob;
	}

	@Test
	public void testUploadCore() throws InterruptedException {
		setUpSource();
		OccurrenceResource resource = (OccurrenceResource) getTestResource();
		UploadEvent event = new UploadEvent();
		Map seed = OccDbUploadJob.getSeed(resource.getId(), 4L, 25);
		occDbUploadJob.launch(seed);
	}

	@Test
	public void testUploadExtension() {
	}

}
