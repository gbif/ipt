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
	public void testLocalIdUniqueConstraint() {
		//FIXME: somehow this ttest persists only the dwc, but not the dwc.tax and dwc.loc component. This causes other tests later own to fail!
		new AssertThrows(EntityExistsException.class) {
            public void test() {
            	final String LOCAL_ID = "xcf-x";
        		OccurrenceResource res = occResourceManager.get(Constants.TEST_RESOURCE_ID);

        		DarwinCore dwc = DarwinCore.newMock(res);
				dwc.setLocalId(LOCAL_ID);
				dwc = darwinCoreManager.save(dwc);
				
				// create new dwc record with different data, but the same localId!
        		DarwinCore dwcTwin = DarwinCore.newMock(res);
				dwcTwin.setLocalId(LOCAL_ID);		
				// should raise exception...
		        dwcTwin = darwinCoreManager.save(dwcTwin);
            }
        }.runTest();
	}
	
	@Test
	public void testConstraintSave(){		
		new AssertThrows(PropertyValueException.class) {
            public void test() {	
        		OccurrenceResource res = occResourceManager.get(Constants.TEST_RESOURCE_ID);
        		DarwinCore dwc = DarwinCore.newMock(res);
        		// remove resource to check if constraints work
        		dwc.setResource(null);
        		// should raise PropertyValueException
        		dwc = darwinCoreManager.save(dwc);		
            }
		}.runTest();
	}
	
	@Test
	public void testSimpleSave(){		
		OccurrenceResource res = occResourceManager.get(Constants.TEST_RESOURCE_ID);
		DarwinCore dwc = DarwinCore.newMock(res);
		dwc = darwinCoreManager.save(dwc);		
	}
}




