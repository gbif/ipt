package org.gbif.provider.webapp.action.tapir;


import java.util.List;

import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.service.DarwinCoreManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.webapp.action.BaseOccurrenceResourceAction;
import org.springframework.beans.factory.annotation.Autowired;

public class ServiceAction extends BaseOccurrenceResourceAction {
	@Autowired
	private DarwinCoreManager darwinCoreManager;
    private String op;
    private OccurrenceResource resource;
    private List<DarwinCore> records;
	 
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

	private String capabilities() {
		// TODO Auto-generated method stub
		return "capabilities";
	}

	private String search() {
		// TODO Auto-generated method stub
		return "search";
	}

	private String ping() {
		// TODO Auto-generated method stub
		return "ping";
	}

	private String metadata() {
		// TODO Auto-generated method stub
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
    
}