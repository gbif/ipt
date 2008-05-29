package org.gbif.provider.webapp.action;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.struts2.interceptor.SessionAware;
import org.appfuse.service.GenericManager;
import org.appfuse.webapp.action.BaseAction;
import org.gbif.provider.model.OccurrenceResource;

import com.opensymphony.xwork2.Preparable;

/**
 * Homepage of the application giving initial statistics and listing imported resources
 * @author markus
 *
 */
public class HomeAction extends BaseAction implements Preparable {
    private GenericManager<OccurrenceResource, Long> occResourceManager;
    private List<OccurrenceResource> occResources;
    private Integer resourceCount;
    private Integer checklistCount;

    // Struts2 actions get Spring injection via bean name if the property is named the same
    public void setOccResourceManager(GenericManager<OccurrenceResource, Long> occResourceManager) {
        this.occResourceManager = occResourceManager;
    }
    
    
	public List<OccurrenceResource> getOccResources() {
		return occResources;
	}

	public Integer getResourceCount() {
		return resourceCount;
	}

	public Integer getChecklistCount() {
		return checklistCount;
	}
	
	
	
	public void prepare() throws Exception {
		resourceCount=97;
		checklistCount=3;
	}

	public String execute(){
		return SUCCESS;
	}

	

}
