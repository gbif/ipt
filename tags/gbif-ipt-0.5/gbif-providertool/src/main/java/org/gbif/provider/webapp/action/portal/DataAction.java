package org.gbif.provider.webapp.action.portal;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.struts2.util.ServletContextAware;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.webapp.action.BaseOccurrenceResourceAction;
import org.gbif.provider.webapp.action.BaseResourceAction;
import org.springframework.beans.factory.annotation.Autowired;
import static org.gbif.provider.util.Constants.DEFAULT_LOGO;


public class DataAction extends BaseResourceAction {
	private InputStream inputStream;
	@Autowired
	private AppConfig cfg;
	
    public String execute() throws FileNotFoundException{
    	if (resource_id != null){
    		File data = cfg.getDumpArchiveFile(resource_id);
    		inputStream = new FileInputStream(data);
    		return SUCCESS;
    	}
    	return ERROR;
    }

    public String logo() throws FileNotFoundException{
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
    	return ERROR;
    }
    
	public InputStream getInputStream(){
		return inputStream;
	}
   
}