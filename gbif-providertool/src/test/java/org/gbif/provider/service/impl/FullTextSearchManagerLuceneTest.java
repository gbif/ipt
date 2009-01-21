package org.gbif.provider.service.impl;


import java.util.List;

import org.gbif.provider.service.FullTextSearchManager;
import org.gbif.provider.util.ResourceTestBase;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import org.springframework.beans.factory.annotation.Autowired;

public class FullTextSearchManagerLuceneTest extends ResourceTestBase{
	@Autowired
	private FullTextSearchManager fullTextSearchManager;

	@Test
	public void setResourceMetadata() throws Exception {
		fullTextSearchManager.buildResourceIndexes();
		List<Long> resourceIDs = fullTextSearchManager.search("Toroslar");
		assertTrue(resourceIDs.size()==1);
	}

}
