package org.gbif.provider.job;

import static org.junit.Assert.*;

import org.gbif.provider.model.OccurrenceResource;
import org.junit.Before;
import org.junit.Test;

public class JobUtilsTest {
	private OccurrenceResource r;
	
	@Before
	public void setUp() throws Exception {
		r = OccurrenceResource.newInstance(null);
		r.setId(121L);
	}

	@Test
	public void testGetJobGroup() {		
		assertTrue(r.getId().equals(JobUtils.getResourceId(JobUtils.getJobGroup(r))));
	}

	@Test
	public void testGetResourceId() {
		Long rid = 37L;
		assertTrue(rid.equals(JobUtils.getResourceId("resource[37]")));		
	}

}
