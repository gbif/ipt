package org.gbif.provider.service.impl;

import org.apache.commons.lang.StringUtils;
import org.gbif.provider.service.RegistryManager;
import org.gbif.provider.util.ContextAwareTestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


public class RegistryManagerImplTest extends ContextAwareTestBase{
	@Autowired
	private RegistryManager registryManager;
	
	@Test
	public void testLogin(){
		boolean result = registryManager.testLogin();
		assertTrue(result);
	}
}
