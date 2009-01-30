package org.gbif.provider.webapp.action.portal;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.webapp.action.BaseOccurrenceResourceAction;
import org.springframework.beans.factory.annotation.Autowired;

public class TaxonTreeAction extends BaseOccurrenceResourceAction {
	@Autowired
	private TaxonManager taxonManager;
    private Long id;
    private String parents="";
    private List<Taxon> nodes;
	private String treeType = "taxon";
	

	public String execute(){
    	if (id!=null && id>0l){
    		// open tree up to the id. 
    		// To do this first find all parent nodes
    		parents=StringUtils.join(taxonManager.getParentIds(resource_id, id), ".");
    		// start always with the root node, i.e. 0
    		id=0l;
    		return rootNodes();
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