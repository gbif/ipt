package org.gbif.provider.util;

import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.service.ResourceFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class ResourceTestBase extends TransactionalTestBase {
	@Autowired
	protected ResourceFactory resourceFactory;
	@Autowired
	protected OccResourceManager occResourceManager;
	protected DataResource resource;
	
	protected void setupOccResource(){
		resource = occResourceManager.get(Constants.TEST_OCC_RESOURCE_ID);
	}
	protected void setupTaxResource(){
		resource = occResourceManager.get(Constants.TEST_CHECKLIST_RESOURCE_ID);
	}
	protected OccurrenceResource getResourceMock(){
		OccurrenceResource res = resourceFactory.newOccurrenceResourceInstance();
		res.setTitle("FooBar");
		res.setId(1973l);
		return res;
	}
}
