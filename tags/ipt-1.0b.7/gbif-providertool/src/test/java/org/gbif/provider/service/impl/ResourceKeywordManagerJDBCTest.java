package org.gbif.provider.service.impl;

import java.util.Map;

import org.gbif.provider.service.ResourceKeywordManager;
import org.gbif.provider.util.ContextAwareTestBase;
import org.gbif.provider.util.ResourceTestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


public class ResourceKeywordManagerJDBCTest extends ContextAwareTestBase{
	@Autowired
	private ResourceKeywordManager  resourceKeywordManager;
	
	@Test
	public void testCloud(){
		Map m = resourceKeywordManager.getCloud();
//		assertTrue(m.size()>3);
	}
}
