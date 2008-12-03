package org.gbif.provider.webapp.action.tapir;


import java.util.Date;
import java.util.List;

import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.webapp.action.BaseOccurrenceResourceAction;
import org.springframework.beans.factory.annotation.Autowired;

public class TapirAction extends BaseOccurrenceResourceAction {
	@Autowired
	private DarwinCoreManager darwinCoreManager;
    private String op;
    private OccurrenceResource resource;
    private List<DarwinCore> records;
    private Date now = new Date();
	 
    public String execute(){
    	if (op==null){
    		return metadata();
    	}else if (op.startsWith("c")){
    		return capabilities();
    	}else if (op.startsWith("m")){
    		return metadata();
    	}else if (op.startsWith("s")){
    		return search();
    	}else if (op.startsWith("p")){
    		return ping();
    	}else{
    		return metadata();
    	}
    }

	private String ping() {
		return "ping";
	}

	private String capabilities() {
		if (resource_id != null) {
			resource = occResourceManager.get(resource_id);
		}
		return "capabilities";
	}

	private String search() {
		if (resource_id != null) {
			resource = occResourceManager.get(resource_id);
		}
		return "search";
	}

	private String metadata() {
		if (resource_id != null) {
			resource = occResourceManager.get(resource_id);
		}
		return "metadata";
	}

	
	
	public String getOp() {
		return op;
	}

	public void setOp(String op) {
		this.op = op;
	}

	public List<DarwinCore> getRecords() {
		return records;
	}

	public OccurrenceResource getResource() {
		return resource;
	}

	public Date getNow() {
		return now;
	}
    
}