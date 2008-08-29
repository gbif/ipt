package org.gbif.provider.webapp.action;


import java.util.List;

import org.gbif.provider.model.Region;
import org.gbif.provider.service.RegionManager;
import org.springframework.beans.factory.annotation.Autowired;

public class RegionTreeAction extends BaseOccurrenceResourceAction {
	@Autowired
	private RegionManager regionManager;
    private Long id;
    private List<Region> nodes;
	 
    public String execute(){
    	if (id!=null){
    		return subNodes();
    	}else{
    		return rootNodes();
    	}
    }
    
    public String subNodes(){
		nodes = regionManager.getChildren(resource_id, id);
    	return SUCCESS;
    }
    
    public String rootNodes(){
		nodes = regionManager.getRoots(resource_id);
    	return SUCCESS;
    }
    
    

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<Region> getNodes() {
		return nodes;
	}
    
    
    
}