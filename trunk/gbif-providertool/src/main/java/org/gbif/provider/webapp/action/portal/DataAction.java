package org.gbif.provider.webapp.action.portal;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.webapp.action.BaseOccurrenceResourceAction;
import org.springframework.beans.factory.annotation.Autowired;
import static org.gbif.provider.util.Constants.DEFAULT_LOGO;


public class DataAction extends BaseOccurrenceResourceAction {
	private InputStream inputStream;

	public InputStream getInputStream()
	{
		return inputStream;
	}
	
    public String execute() throws FileNotFoundException{
    	if (resource_id != null){
    		OccurrenceResource res = occResourceManager.get(resource_id);
    		File data = res.getDumpArchiveFile();
    		inputStream = new FileInputStream(data);
    		return SUCCESS;
    	}
    	return ERROR;
    }

    public String logo() throws FileNotFoundException{
    	if (resource_id != null){
    		OccurrenceResource res = occResourceManager.get(resource_id);
    		File logo = res.getLogoFile();
    		try {
				inputStream = new FileInputStream(logo);
			} catch (FileNotFoundException e) {
				inputStream = new FileInputStream(new File(DEFAULT_LOGO));
			}
    		return SUCCESS;
    	}
    	return ERROR;
    }
}