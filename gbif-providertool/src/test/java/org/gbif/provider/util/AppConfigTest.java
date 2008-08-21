package org.gbif.provider.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class AppConfigTest extends ContextAwareTestBase{
	@Autowired
	public AppConfig cfg;

	@Test
	public void testPropertiesLoaded() {
		assertEquals("http://localhost:8080", cfg.getAppBaseUrl());
	}

	@Test
	public void testSetAppBaseUrl() {
		cfg.setAppBaseUrl("http://localhost:8080/");
		assertEquals("http://localhost:8080", cfg.getAppBaseUrl());
		
		cfg.setAppBaseUrl(" http://localhost:8080  ");
		assertEquals("http://localhost:8080", cfg.getAppBaseUrl());
	}

}
