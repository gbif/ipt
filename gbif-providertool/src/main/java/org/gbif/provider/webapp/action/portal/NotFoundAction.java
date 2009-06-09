package org.gbif.provider.webapp.action.portal;

import org.gbif.provider.model.Resource;
import org.gbif.provider.webapp.action.BaseMetadataResourceAction;
import org.gbif.provider.webapp.action.BaseResourceAction;

import com.opensymphony.xwork2.Preparable;

public class NotFoundAction extends BaseMetadataResourceAction implements Preparable{
	
	public String execute(){
		if (resource==null){
			return this.RESOURCE404;
		}
		return RECORD404;
	}
	
}
