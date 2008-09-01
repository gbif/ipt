package org.gbif.provider.webapp.action.portal;


import java.util.List;

import org.gbif.provider.model.Taxon;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.webapp.action.BaseOccurrenceResourceAction;
import org.springframework.beans.factory.annotation.Autowired;

public class TaxonTreeAction extends BaseOccurrenceResourceAction {
	@Autowired
	private TaxonManager taxonManager;
    private Long id;
    private List<Taxon> nodes;
	 
    public String execute(){
    	if (id!=null){
    		return subNodes();
    	}else{
    		return rootNodes();
    	}
    }
    
    public String subNodes(){
		nodes = taxonManager.getChildren(resource_id, id);
    	return SUCCESS;
    }
    
    public String rootNodes(){
		nodes = taxonManager.getRoots(resource_id);
    	return SUCCESS;
    }
    
    

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<Taxon> getNodes() {
		return nodes;
	}
    
    
    
}