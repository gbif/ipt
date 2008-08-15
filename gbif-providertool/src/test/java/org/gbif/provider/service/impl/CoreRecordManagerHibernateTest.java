package org.gbif.provider.service.impl;


import java.util.Random;
import java.util.UUID;

import javax.persistence.EntityExistsException;

import org.appfuse.dao.BaseDaoTestCase;
import org.appfuse.service.GenericManager;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.util.Constants;
import org.hibernate.PropertyValueException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.AssertThrows;

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
	public void te32stReindex() {
//		darwinCoreManager.reindex(Constants.TEST_RESOURCE_ID);		
//		darwinCoreManager.reindex(2l);		
	}

	
	@Test
	public void testSimpleSave(){		
		OccurrenceResource res = occResourceManager.get(Constants.TEST_RESOURCE_ID);
		DarwinCore dwc = DarwinCore.newMock(res);
    	try{
    		dwc = darwinCoreManager.save(dwc);		
    	}finally{
    		darwinCoreManager.flush();            		
    	}
	}
}




