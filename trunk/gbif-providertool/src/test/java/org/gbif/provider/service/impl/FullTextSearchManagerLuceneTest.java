package org.gbif.provider.service.impl;


import static org.junit.Assert.assertTrue;

import java.util.List;

import org.gbif.provider.service.FullTextSearchManager;
import org.gbif.provider.util.ResourceTestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class FullTextSearchManagerLuceneTest extends ResourceTestBase {
	@Autowired
	private FullTextSearchManager fullTextSearchManager;

	@Test
	public void setResourceMetadata() throws Exception {
		fullTextSearchManager.buildResourceIndex();
		List<String> resourceIDs = fullTextSearchManager.search("subalpi*");
		assertTrue(resourceIDs.size()>=1);
		resourceIDs = fullTextSearchManager.search("Subalpine*");
		assertTrue(resourceIDs.size()>=1);
		resourceIDs = fullTextSearchManager.search("belt*");
		assertTrue(resourceIDs.size()>=1);
		resourceIDs = fullTextSearchManager.search("belt");
		assertTrue(resourceIDs.size()>=1);
		resourceIDs = fullTextSearchManager.search("mountains*");
		assertTrue(resourceIDs.size()>=1);
		resourceIDs = fullTextSearchManager.search("Toros*");
		assertTrue(resourceIDs.size()>=1);
//		resourceIDs = fullTextSearchManager.search("Sites loc*");
//		assertTrue(resourceIDs.size()>=1);
		resourceIDs = fullTextSearchManager.search("Frei*");
		assertTrue(resourceIDs.size()>=1);
		resourceIDs = fullTextSearchManager.search("frei*");
		assertTrue(resourceIDs.size()>=1);
		resourceIDs = fullTextSearchManager.search("Sites*");
		assertTrue(resourceIDs.size()>=1);
	}
	
	@Test
	public void testFullTextMetadata() throws Exception {
		fullTextSearchManager.buildResourceIndex();
		List<String> resourceIDs = fullTextSearchManager.search("Pontaurus");
		assertTrue(resourceIDs.size()>=1);
		resourceIDs = fullTextSearchManager.search("Berlin");
		assertTrue(resourceIDs.size()>=1);
	}

}
