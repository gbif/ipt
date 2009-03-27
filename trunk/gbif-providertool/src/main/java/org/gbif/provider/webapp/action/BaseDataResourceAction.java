package org.gbif.provider.webapp.action;

import org.gbif.provider.model.DataResource;
import org.gbif.provider.service.GenericResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class BaseDataResourceAction extends BaseResourceAction<DataResource> {

	@Autowired
	public void setDataResourceManager(@Qualifier("dataResourceManager") GenericResourceManager<DataResource> dataResourceManager) {
		this.resourceManager = dataResourceManager;
	}
	
}
