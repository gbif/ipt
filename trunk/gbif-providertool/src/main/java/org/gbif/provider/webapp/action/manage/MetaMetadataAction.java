package org.gbif.provider.webapp.action.manage;

import java.util.Map;

import org.apache.struts2.interceptor.SessionAware;
import org.gbif.provider.model.Resource;
import org.gbif.provider.service.GenericResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.gbif.provider.webapp.action.manage.BaseResourceMetadataAction;

import com.opensymphony.xwork2.Preparable;

public class MetaMetadataAction extends BaseResourceMetadataAction<Resource> implements Preparable, SessionAware{    
	@Autowired
	public void setResourceManager(@Qualifier("resourceManager") GenericResourceManager<Resource> resourceManager) {
		this.resourceManager = resourceManager;
	}

	@Override
	protected Resource newResource() {
		return resourceFactory.newResourceInstance();
	}
	
	public void setResourceTypes(Map<String, String> resourceTypes) {
		this.resourceTypes = resourceTypes;
	}

}
