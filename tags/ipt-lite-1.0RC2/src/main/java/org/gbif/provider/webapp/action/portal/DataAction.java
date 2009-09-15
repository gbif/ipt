package org.gbif.provider.webapp.action.portal;


import static org.gbif.provider.util.Constants.DEFAULT_LOGO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.gbif.provider.util.AppConfig;
import org.gbif.provider.webapp.action.BaseMetadataResourceAction;
import org.springframework.beans.factory.annotation.Autowired;


public class DataAction extends BaseMetadataResourceAction {
	private InputStream inputStream;
	@Autowired
	private AppConfig cfg;
	private Integer version=0;
	
	private void setResourceId(){
    	if (resource_id==null){
    		prepare();
    		if (resource!=null){
        		resource_id=resource.getId();
    		}
    	}
	}
    public String execute() throws FileNotFoundException{
    	setResourceId();
    	if (resource_id != null){
    		File data = null;
    		data = cfg.getArchiveFile(resource_id);
    		inputStream = new FileInputStream(data);
    		return SUCCESS;
    	}
    	return RESOURCE404;
    }

    public String logo() throws FileNotFoundException{
    	setResourceId();
    	if (resource_id != null){
    		File logo = cfg.getResourceLogoFile(resource_id);
    		try {
				inputStream = new FileInputStream(logo);
			} catch (FileNotFoundException e) {
				logo = cfg.getWebappFile(DEFAULT_LOGO);
				inputStream = new FileInputStream(logo);
			}
    		return SUCCESS;
    	}
    	return RESOURCE404;
    }
    
    public String eml() throws FileNotFoundException{
    	setResourceId();
    	if (resource_id != null){
        	File eml = null;
    		if (version>0){
    			eml=cfg.getEmlFile(resource_id, version);
    		}else{
    			eml=cfg.getEmlFile(resource_id);
    		}
    		try {
    			inputStream = new FileInputStream(eml);
			} catch (FileNotFoundException e) {
		    	return RESOURCE404;
			}
    		return SUCCESS;
    	}
    	return RESOURCE404;
    }
    
    public InputStream getInputStream(){
		return inputStream;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}
	
}