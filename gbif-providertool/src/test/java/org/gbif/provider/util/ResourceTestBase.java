package org.gbif.provider.util;

import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.service.OccResourceManager;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class ResourceTestBase extends ContextAwareTestBase {
	@Autowired
	protected OccResourceManager occResourceManager;
	protected OccurrenceResource resource;
	
	protected void setup(){
		resource = occResourceManager.get(Constants.TEST_RESOURCE_ID);
	}
}
