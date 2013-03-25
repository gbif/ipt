package org.gbif.provider.service.impl;

import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.service.OccStatManager;
import org.gbif.provider.util.ResourceTestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


public class OccStatManagerHibernateTest extends ResourceTestBase{
	@Autowired
	private OccStatManager occStatManager;
		
	@Test
	public void testUpdateStats(){
		this.setupOccResource();
		occStatManager.updateRegionAndTaxonStats((OccurrenceResource)resource);
	}
}
