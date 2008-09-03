package org.gbif.provider.webapp.action;


import java.util.List;

import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.TaxonManager;
import org.springframework.beans.factory.annotation.Autowired;

public class DataAction extends BaseOccurrenceResourceAction {
	 
    public String execute(){
    	if (resource_id!=null){
    		return SUCCESS;
    	}
    	return ERROR;
    }
    
}