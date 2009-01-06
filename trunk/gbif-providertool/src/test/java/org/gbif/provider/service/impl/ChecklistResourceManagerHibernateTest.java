package org.gbif.provider.service.impl;

import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.voc.HostType;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.model.voc.RegionType;
import org.gbif.provider.service.ChecklistResourceManager;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.ContextAwareTestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.googlecode.gchartjava.GeographicalArea;

public class ChecklistResourceManagerHibernateTest extends ContextAwareTestBase{
	@Autowired
	private ChecklistResourceManager resourceManager;

	@Test
	public void testStats(){
		ChecklistResource res = resourceManager.get(Constants.TEST_CHECKLIST_RESOURCE_ID);
		resourceManager.setResourceStats(res);
		assertTrue(res.getNumTaxa()==42);
		assertTrue(res.getNumGenera()==2);
	}	
}
