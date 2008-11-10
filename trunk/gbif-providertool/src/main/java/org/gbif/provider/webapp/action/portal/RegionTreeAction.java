package org.gbif.provider.webapp.action.portal;


import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.gbif.provider.model.Region;
import org.gbif.provider.service.RegionManager;
import org.gbif.provider.webapp.action.BaseOccurrenceResourceAction;
import org.springframework.beans.factory.annotation.Autowired;

public class RegionTreeAction extends BaseOccurrenceResourceAction {
	@Autowired
	private RegionManager regionManager;
    private Long id;
    private String parents="";
    private List<Region> nodes;
	private String treeType="region";
	 
    public String execute(){
    	if (id!=null && id>0l){
    		// open tree up to the id. 
    		// To do this first find all parent nodes
    		parents=StringUtils.join(regionManager.getParentIds(resource_id, id), ".");
    		// start always with the root node, i.e. 0
    		id=0l;
    		return rootNodes();
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

	public String getParents() {
		return parents;
	}

	public void setParents(String parents) {
		this.parents = parents;
	}
    
    public String getTreeType() {
		return treeType;
	}
    
    
}