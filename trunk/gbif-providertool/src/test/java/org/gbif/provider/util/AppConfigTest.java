package org.gbif.provider.util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

public class AppConfigTest extends ContextAwareTestBase{
	@Autowired
	public AppConfig cfg;

	@Test
	public void testPropertiesLoaded() {
		assertEquals("http://localhost:8080", cfg.getBaseUrl());
	}

	@Test
	public void testSetAppBaseUrl() {
		cfg.setBaseUrl("http://localhost:8080/");
		assertEquals("http://localhost:8080", cfg.getBaseUrl());
		
		cfg.setBaseUrl(" http://localhost:8080  ");
		assertEquals("http://localhost:8080", cfg.getBaseUrl());
	}
	
	@Test
	public void testWebappResources() throws MalformedURLException {
		File newFile = cfg.getWebappFile("/KlausMariaBrandauerNackt.jpg");
		assertTrue(newFile!=null);
		assertFalse(newFile.exists());

		String img = "/images/favicon.ico";
		File oldFile = cfg.getWebappFile(img);
		assertTrue(oldFile.exists());
		URL oldURL = cfg.getWebappURL(img);
}


}
