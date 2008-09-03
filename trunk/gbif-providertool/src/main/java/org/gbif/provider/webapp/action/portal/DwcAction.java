package org.gbif.provider.webapp.action.portal;


import java.util.List;

import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.webapp.action.BaseOccurrenceResourceAction;
import org.springframework.beans.factory.annotation.Autowired;

public class DwcAction extends BaseOccurrenceResourceAction {
	@Autowired
	private DarwinCoreManager darwinCoreManager;
    private String guid;
    private DarwinCore dwc;
	 
    public String execute(){
    	if (guid!=null){
    		dwc=darwinCoreManager.get(guid);
    	}
		return SUCCESS;
    }
    



	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public DarwinCore getDwc() {
		return dwc;
	}

}