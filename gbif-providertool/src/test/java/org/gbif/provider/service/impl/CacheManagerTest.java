package org.gbif.provider.service.impl;

import org.gbif.provider.datasource.RdbmsImportSourceTest;
import org.gbif.provider.service.CacheManager;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.util.Constants;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class CacheManagerTest extends RdbmsImportSourceTest{
	@Autowired
	private CacheManager cacheManager;

	@Test
	public void testRunUpload() {
		setUpSource();
		cacheManager.runUpload(Constants.TEST_RESOURCE_ID, Constants.TEST_USER_ID, 100);
	}
}
