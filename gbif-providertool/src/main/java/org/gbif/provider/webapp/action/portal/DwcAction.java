package org.gbif.provider.webapp.action.portal;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gbif.provider.model.Annotation;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.dto.ExtendedRecord;
import org.gbif.provider.service.AnnotationManager;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.ExtensionRecordManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.NamespaceRegistry;
import org.gbif.provider.webapp.action.BaseDataResourceAction;
import org.gbif.provider.webapp.action.BaseOccurrenceResourceAction;
import org.springframework.beans.factory.annotation.Autowired;

public class DwcAction extends BaseDataResourceAction {
	@Autowired
	private DarwinCoreManager darwinCoreManager;
	@Autowired
	private ExtensionRecordManager extensionRecordManager;
	@Autowired
	private AnnotationManager annotationManager;
    private Long taxon_id;
    private Long region_id;
    private Long id;
    private DarwinCore dwc;
    private ExtendedRecord rec;
    private String format;
    private String q;
    private NamespaceRegistry nsr;
    private Map<Object, Object> json;
    private List<DarwinCore> occurrences;
    private List<Annotation> annotations;
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
    	if(resource==null){
    		resource=dwc.getResource();
    		updateResourceType();
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
        	// find annotations
        	annotations = annotationManager.getByRecord(dwc.getResourceId(), dwc.getGuid());
        	
    		return SUCCESS;
		}
		return RECORD404;
    }
    
	public String search() {
		super.prepare();
		occurrences = darwinCoreManager.search(resource_id, q);
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

	public String getQ() {
		return q;
	}

	public void setQ(String q) {
		this.q = q;
	}

	public List<DarwinCore> getOccurrences() {
		return occurrences;
	}

	public void setOccurrences(List<DarwinCore> occurrences) {
		this.occurrences = occurrences;
	}

	public List<Annotation> getAnnotations() {
		return annotations;
	}
	
}