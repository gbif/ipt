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
    private Long id;
    private DarwinCore dwc;
	 
    public String execute(){
    	if (id!=null){
    		dwc=darwinCoreManager.get(id);
    	}
		return SUCCESS;
    }
    


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public DarwinCore getDwc() {
		return dwc;
	}

}