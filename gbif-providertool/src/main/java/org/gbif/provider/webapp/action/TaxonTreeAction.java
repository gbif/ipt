package org.gbif.provider.webapp.action;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.TreeNode;
import org.gbif.provider.service.TaxonManager;
import org.springframework.beans.factory.annotation.Autowired;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import com.opensymphony.xwork2.ActionSupport;

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