package org.gbif.ipt.action.manage;

import org.apache.commons.lang.StringUtils;
import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.manage.ResourceManager;

import com.google.inject.Inject;

public class ManagerBaseAction extends POSTAction{
	  @Inject
	  protected ResourceManager resourceManager;
	  protected Resource resource;
	  
	  
	@Override
	public void prepare() throws Exception {
		super.prepare();
		// look for resource parameter
	    String res = StringUtils.trimToNull(req.getParameter(Constants.REQ_PARAM_RESOURCE));
		resource = resourceManager.get(res);		
	    if (resource==null){
	    	notFound=true;
	    }
	}

	protected void saveResource(){
		resourceManager.save(resource);
	}


	public Resource getResource() {
		return resource;
	}


	public void setResource(Resource resource) {
		this.resource = resource;
	}
	
}
