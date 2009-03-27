package org.gbif.provider.webapp.action.portal;


import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.gbif.provider.model.TreeNode;
import org.gbif.provider.service.TreeNodeManager;
import org.gbif.provider.webapp.action.BaseDataResourceAction;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseTreeAction<T extends TreeNode<T, E>, E extends Enum> extends BaseDataResourceAction {
	protected TreeNodeManager<T, E> treeNodeManager;
	protected Long id;
	protected Long focus;
	protected String parents="";
	protected List<T> nodes;
	protected String treeType;

	public BaseTreeAction(TreeNodeManager<T, E> treeNodeManager, String treeType) {
		super();
		this.treeNodeManager = treeNodeManager;
		this.treeType = treeType;
	}

	public String execute(){
    	if (id!=null){
    		if(focus==null){
	    		// initial tree request with selected tree node
	    		// return entire tree up to the id. 
	    		// To do this first find all parent nodes. 
	    		// Rendering of nodes will do a recursion depending on parent string
    			focus=id;
	    		parents=StringUtils.join(treeNodeManager.getParentIds(resource_id, id), ".");
	    	}else{
	    		// parents already set. this is a recursive call already
	    		return subNodes();
	    	}
    	}
		return rootNodes();
    }
    
	public String subNodes(){
		nodes = treeNodeManager.getChildren(resource_id, id);
    	return SUCCESS;
    }
	protected String rootNodes(){
		nodes = treeNodeManager.getRoots(resource_id);
    	return SUCCESS;
    }
    
    

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<T> getNodes() {
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