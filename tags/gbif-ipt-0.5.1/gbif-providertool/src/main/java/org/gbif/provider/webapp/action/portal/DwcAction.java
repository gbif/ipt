package org.gbif.provider.webapp.action.portal;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.dto.ExtensionRecordsWrapper;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.ExtensionRecordManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.NamespaceRegistry;
import org.gbif.provider.webapp.action.BaseOccurrenceResourceAction;
import org.springframework.beans.factory.annotation.Autowired;

public class DwcAction extends BaseOccurrenceResourceAction {
	@Autowired
	private DarwinCoreManager darwinCoreManager;
	@Autowired
	private ExtensionRecordManager extensionRecordManager;
    private Long taxon_id;
    private Long region_id;
    private String guid;
    private DarwinCore dwc;
    private ExtensionRecordsWrapper extWrapper;
    private List<Extension> extensions;
    private String format;
    private NamespaceRegistry nsr;
    private Map<Object, Object> json;
	@Autowired
	private AppConfig cfg;
	 
    public String execute(){
    	if (guid!=null){
    		dwc=darwinCoreManager.get(guid);
    		if (dwc !=null){
    			region_id = dwc.getRegion().getId();
    			taxon_id = dwc.getTaxon().getId();
        		extWrapper = extensionRecordManager.getExtensionRecords(dwc.getResource(), dwc.getCoreId());
        		extensions = extWrapper.getExtensions();
            	if (format!=null && format.equalsIgnoreCase("xml")){
            		nsr = new NamespaceRegistry(dwc.getResource());
            		return "xml";
            	}
            	else if (format!=null && format.equalsIgnoreCase("json")){
            		//TODO: create map to serialise into JSON
            		json = new HashMap<Object, Object>();
            		return "json";
            	}
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

	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}




	public Long getTaxon_id() {
		return taxon_id;
	}




	public Long getRegion_id() {
		return region_id;
	}




	public NamespaceRegistry getNsr() {
		return nsr;
	}

}