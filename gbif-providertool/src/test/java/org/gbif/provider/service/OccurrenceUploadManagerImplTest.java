package org.gbif.provider.service;

import static org.junit.Assert.*;

import java.util.Map;

import org.gbif.provider.datasource.RdbmsImportSourceTest;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.UploadEvent;
import org.junit.Before;
import org.junit.Test;

public class OccurrenceUploadManagerImplTest extends RdbmsImportSourceTest{
	private OccurrenceUploadManager occurrenceUploadManager;

	public void setOccurrenceUploadManager(
			OccurrenceUploadManager occurrenceUploadManager) {
		this.occurrenceUploadManager = occurrenceUploadManager;
	}


	@Test
	public void testUploadCore() throws InterruptedException {
		setUpSource();
		OccurrenceResource resource = (OccurrenceResource) getTestResource();
		UploadEvent event = new UploadEvent();
		Map idMap = occurrenceUploadManager.uploadCore(source, resource, event);
	}

	@Test
	public void testUploadExtension() {
	}

}
