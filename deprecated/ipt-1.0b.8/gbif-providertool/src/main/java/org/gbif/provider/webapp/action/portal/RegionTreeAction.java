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
    private Long focus;
    private String parents="";
    private List<Region> nodes;
	private String treeType="region";
	 
    public String execute(){
    	if (id!=null){
    		if(focus==null){
	    		// initial tree request with selected tree node
	    		// return entire tree up to the id. 
	    		// To do this first find all parent nodes. 
	    		// Rendering of nodes will do a recursion depending on parent string
    			focus=id;
	    		parents=StringUtils.join(regionManager.getParentIds(resource_id, id), ".");
        	}else{
	    		// parents already set. this is a recursive call already
	    		return subNodes();
        	}
    	}
		return rootNodes();
    }
    
    public String subNodes(){
		nodes = regionManager.getChildren(resource_id, id);
    	return SUCCESS;
    }
	private String rootNodes(){
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

	public Long getFocus() {
		return focus;
	}

	public void setFocus(Long focus) {
		this.focus = focus;
	}
}