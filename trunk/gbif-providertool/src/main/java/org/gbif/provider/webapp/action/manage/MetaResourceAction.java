package org.gbif.provider.webapp.action.manage;

import org.apache.struts2.interceptor.SessionAware;
import org.gbif.provider.model.Resource;
import org.gbif.provider.service.GenericResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.opensymphony.xwork2.Preparable;

public class MetaResourceAction extends org.gbif.provider.webapp.action.manage.BaseResourceManagerAction<Resource> implements Preparable, SessionAware{    
	@Autowired()
	public void setResourceManager(@Qualifier("resourceManager") GenericResourceManager<Resource> resourceManager) {
		this.resourceManager = resourceManager;
	}

	@Override
	protected Resource newResource() {
		return resourceFactory.newResourceInstance();
	}

}
