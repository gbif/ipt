package org.gbif.provider.webapp.action.portal;


import java.util.ArrayList;
import java.util.List;

import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.dto.ExtensionRecordsWrapper;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.ExtensionRecordManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.webapp.action.BaseOccurrenceResourceAction;
import org.springframework.beans.factory.annotation.Autowired;

public class DwcAction extends BaseOccurrenceResourceAction {
	@Autowired
	private DarwinCoreManager darwinCoreManager;
	@Autowired
	private ExtensionRecordManager extensionRecordManager;
    private String guid;
    private DarwinCore dwc;
    private ExtensionRecordsWrapper extWrapper;
    private List<Extension> extensions;
	@Autowired
	private AppConfig cfg;
	 
    public String execute(){
    	if (guid!=null){
    		dwc=darwinCoreManager.get(guid);
    		if (dwc !=null){
        		extWrapper = extensionRecordManager.getExtensionRecords(dwc.getResource(), dwc.getCoreId());
        		extensions = extWrapper.getExtensions();
    		}else{
    			extensions = new ArrayList<Extension>();
    		}
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

	public ExtensionRecordsWrapper getExtWrapper() {
		return extWrapper;
	}

	public List<Extension> getExtensions() {
		return extensions;
	}

}