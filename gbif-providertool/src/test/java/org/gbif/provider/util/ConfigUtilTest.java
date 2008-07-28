package org.gbif.provider.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ConfigUtilTest {
	public ConfigUtil cfg;

	@Before
	public void setUp() throws Exception {
		cfg = new ConfigUtil();
	}

	@Test
	public void testSetAppBaseUrl() {
		cfg.setAppBaseUrl("http://localhost:8080/");
		assertEquals("http://localhost:8080", cfg.getAppBaseUrl());
		
		cfg.setAppBaseUrl(" http://localhost:8080  ");
		assertEquals("http://localhost:8080", cfg.getAppBaseUrl());
	}

}
