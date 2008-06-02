package org.gbif.provider.webapp.action;

import com.opensymphony.xwork2.Preparable;
import org.appfuse.service.GenericManager;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.appfuse.webapp.action.BaseAction;

import java.util.List;

public class OccResourceAction extends BaseAction implements Preparable {
    private GenericManager<OccurrenceResource, Long> occResourceManager;
    private List occResources;
    private OccurrenceResource occResource;
    private String serviceName;
    private Long  id;

    // Struts2 actions get Spring injection via bean name if the property is named the same
    public void setOccResourceManager(GenericManager<OccurrenceResource, Long> occResourceManager) {
        this.occResourceManager = occResourceManager;
    }

    
	public List getOccResources() {
        return occResources;
    }
    public void setId(Long  id) {
        this. id =  id;
    }
    public OccurrenceResource getOccResource() {
        return occResource;
    }
    public void setOccResource(OccurrenceResource occResource) {
        this.occResource = occResource;
    }
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

    /**
     * Grab the entity from the database before populating with request parameters
     */
    public void prepare() {
        if (getRequest().getMethod().equalsIgnoreCase("post")) {
            // prevent failures on new
            String occResourceId = getRequest().getParameter("occResource.id");
            if (occResourceId != null && !occResourceId.equals("")) {
                occResource = occResourceManager.get(new Long(occResourceId));
            }
        }
    }


    //    
    // below here are proper Action methods
    //
    public String execute(){
    	if (occResource == null){
            if (id != null) {
                occResource = occResourceManager.get(id);
            }else{
            	return ERROR;
            }
    	}
    	return SUCCESS;
    }

    public String list() {
        occResources = occResourceManager.getAll();
        return SUCCESS;
    }

    public String edit() {
        if (id != null) {
            occResource = occResourceManager.get(id);
        } else {
            occResource = new OccurrenceResource();
        }
        return SUCCESS;
    }

    public String save() throws Exception {
        if (cancel != null) {
            return "cancel";
        }
        if (delete != null) {
            return delete();
        }

        boolean isNew = (occResource.getId() == null);
        occResource = occResourceManager.save(occResource);
        id = occResource.getId();
        
        String key = (isNew) ? "occResource.added" : "occResource.updated";
        saveMessage(getText(key));
        
        return SUCCESS;
    }
    
    public String delete() {
        occResourceManager.remove(occResource.getId());
        saveMessage(getText("occResource.deleted"));

        return SUCCESS;
    }
    
    public String suggestServiceName(){
        serviceName = occResource.getTitle();
        return SUCCESS;
    }
    
}