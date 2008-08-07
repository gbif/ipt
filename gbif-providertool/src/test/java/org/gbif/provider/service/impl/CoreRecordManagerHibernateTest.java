package org.gbif.provider.service.impl;


import org.appfuse.dao.BaseDaoTestCase;
import org.appfuse.service.GenericManager;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.util.Constants;
import org.junit.Before;
import org.junit.Test;

public class CoreRecordManagerHibernateTest extends BaseDaoTestCase{
	private CoreRecordManagerHibernate<DarwinCore> darwinCoreManager;
	private GenericManager<OccurrenceResource, Long> occResourceManager;

	public void setDarwinCoreManager(
			CoreRecordManagerHibernate<DarwinCore> darwinCoreManager) {
		this.darwinCoreManager = darwinCoreManager;
	}
	public void setOccResourceManager(
			GenericManager<OccurrenceResource, Long> occResourceManager) {
		this.occResourceManager = occResourceManager;
	}

	@Test
	public void testFlagAllAsDeleted() {
		OccurrenceResource resource = (OccurrenceResource) occResourceManager.get(Constants.TEST_RESOURCE_ID);
		darwinCoreManager.flagAllAsDeleted(resource);
	}

	@Test
	public void testReindex() {
		darwinCoreManager.reindex(Constants.TEST_RESOURCE_ID);		
	}

}
