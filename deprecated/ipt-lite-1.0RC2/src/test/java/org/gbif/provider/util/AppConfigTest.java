package org.gbif.provider.util;

import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.factory.DarwinCoreFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class AppConfigTest extends ContextAwareTestBase{
	@Autowired
	public AppConfig cfg;

	@Test
	public void testPropertiesLoaded() {
		assertEquals("http://localhost:8080/ipt", cfg.getBaseUrl());
	}

	@Test
	public void testSetAppBaseUrl() {
		cfg.setBaseUrl("http://localhost:8080/ipt/");
		assertEquals("http://localhost:8080/ipt", cfg.getBaseUrl());
		
		cfg.setBaseUrl(" http://localhost:8080/ipt  ");
		assertEquals("http://localhost:8080/ipt", cfg.getBaseUrl());
	}
	
}
