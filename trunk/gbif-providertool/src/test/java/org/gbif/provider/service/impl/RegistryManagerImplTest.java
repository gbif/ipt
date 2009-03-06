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
	public void testOrgAsJson(){
		String json = registryManager.findOrganisationsAsJSON("");
		assertTrue(json!=null && json.length()>100);

//		json = registryManager.findOrganisationsAsJSON("MarkusMaximus");
//		System.out.println(json);
//		assertTrue(StringUtils.trimToEmpty(json).replaceAll("\\s", "").equalsIgnoreCase("[]"));
	}
}
