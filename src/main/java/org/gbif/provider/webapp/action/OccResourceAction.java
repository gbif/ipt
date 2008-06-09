package org.gbif.provider.webapp.action;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.struts2.interceptor.SessionAware;
import org.appfuse.service.GenericManager;
import org.gbif.provider.datasource.DatasourceInterceptor;
import org.gbif.provider.model.DwcExtension;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.ViewMapping;
import org.gbif.provider.service.DatasourceInspectionManager;
import org.gbif.provider.service.ViewMappingManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Preparable;

public class OccResourceAction extends BaseAction implements Preparable, SessionAware {
    private GenericManager<OccurrenceResource, Long> occResourceManager;
    private GenericManager<DwcExtension, Long> dwcExtensionManager;
    private ViewMappingManager viewMappingManager;
    private List occResources;
    private List<DwcExtension> extensions;
    private OccurrenceResource occResource;
    private Map session;
    private Long resource_id;
    private Long id;

	
	public void setOccResourceManager(GenericManager<OccurrenceResource, Long> occResourceManager) {
		this.occResourceManager = occResourceManager;
	}

	public void setDwcExtensionManager(
			GenericManager<DwcExtension, Long> dwcExtensionManager) {
		this.dwcExtensionManager = dwcExtensionManager;
	}

	public void setViewMappingManager(ViewMappingManager viewMappingManager) {
		this.viewMappingManager = viewMappingManager;
	}

	public void setSession(Map arg0) {
		session = arg0;
	}
	
	public List getOccResources() {
        return occResources;
    }

	public List getExtensions() {
		return extensions;
	}

	public void setResource_id(Long resource_id) {
		this.resource_id = resource_id;
	}
	public void setId(Long  id) {
        this. id =  id;
    }
	private void setId2ResourceId(){
		// resource_id has higher priority cause its used for the resource interceptor
    	if (resource_id != null){
    		id=resource_id;
    	}
	}
	public OccurrenceResource getOccResource() {
        return occResource;
    }
    public void setOccResource(OccurrenceResource occResource) {
        this.occResource = occResource;
    }

	public void prepare() {
        if (getRequest().getMethod().equalsIgnoreCase("post")) {
            // prevent failures on new
            String occResourceId = getRequest().getParameter("occResource.id");
            if (occResourceId != null && !occResourceId.equals("")) {
                occResource = occResourceManager.get(new Long(occResourceId));
            }
        }
    }

    public String execute(){
    	setId2ResourceId();
        if (id != null) {
        	occResource = occResourceManager.get(id);
        	extensions = dwcExtensionManager.getAll();
        	// filter already mapped extensions
        	for (ViewMapping map : occResource.getMappings()){
    			extensions.remove(map.getExtension());
        	}
        }else{
        	return ERROR;
        }
    	return SUCCESS;
    }

    public String list() {
        occResources = occResourceManager.getAll();
        return SUCCESS;
    }

    public String edit() {
    	setId2ResourceId();
        if (id != null) {
        	occResource = occResourceManager.get(id);
        }else{
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
        String key = (isNew) ? "occResource.added" : "occResource.updated";
        saveMessage(getText(key));
        // set new current resource in session
    	session.put(DatasourceInterceptor.SESSION_ATTRIBUTE, occResource.getId());
        return SUCCESS;
    }
    
    public String delete() {
    	setId2ResourceId();
        occResourceManager.remove(occResource.getId());
        saveMessage(getText("occResource.deleted"));
        return "delete";
    }

    
}