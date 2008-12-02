package org.gbif.provider.service.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.gbif.provider.service.CacheManager;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.ContextAwareTestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class CacheManagerTest extends ContextAwareTestBase{
	@Autowired
	private CacheManager cacheManager;

	@Test
	public void testRunUpload() throws ExecutionException, InterruptedException {
		Future f = cacheManager.runUpload(Constants.TEST_RESOURCE_ID, Constants.TEST_USER_ID);
		f.get();
	}
}
