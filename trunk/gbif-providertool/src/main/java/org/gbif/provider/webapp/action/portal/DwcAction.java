package org.gbif.provider.webapp.action.portal;


import java.util.HashMap;
import java.util.Map;

import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.dto.ExtendedRecord;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.ExtensionRecordManager;
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
    private Long id;
    private DarwinCore dwc;
    private ExtendedRecord rec;
    private String format;
    private NamespaceRegistry nsr;
    private Map<Object, Object> json;
	@Autowired
	private AppConfig cfg;
	 
	private void setRequestedRecord(){
    	if (id!=null){
    		dwc=darwinCoreManager.get(id);
    	}else if (guid!=null){
    		dwc=darwinCoreManager.get(guid);
    		if (dwc!=null){
        		id=dwc.getId();
    		}
    	}
	}
	
	public String execute(){
		setRequestedRecord();
		if (dwc !=null){
			region_id = dwc.getRegion().getId();
			taxon_id = dwc.getTaxon().getId();
    		rec = extensionRecordManager.extendCoreRecord(dwc.getResource(), dwc);
        	if (format!=null && format.equalsIgnoreCase("xml")){
        		nsr = new NamespaceRegistry(dwc.getResource());
        		return "xml";
        	}
        	else if (format!=null && format.equalsIgnoreCase("json")){
        		//TODO: create map to serialise into JSON
        		json = new HashMap<Object, Object>();
        		return "json";
        	}
    		return SUCCESS;
		}else{
			return RECORD404;
		}
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
	public DarwinCore getRecord(){
		return dwc;
	}

	public ExtendedRecord getRec() {
		return rec;
	}
	
}