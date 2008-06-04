package org.gbif.provider.webapp.action;

import com.opensymphony.xwork2.Preparable;

import org.apache.struts2.interceptor.SessionAware;
import org.appfuse.service.GenericManager;
import org.gbif.provider.dao.DatasourceInspectionDao;
import org.gbif.provider.datasource.DatasourceContextHolder;
import org.gbif.provider.datasource.DatasourceRegistry;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.gbif.provider.datasource.ExternalResourceRoutingDatasource;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class OccResourceAction extends BaseAction implements Preparable, SessionAware {
    private GenericManager<OccurrenceResource, Long> occResourceManager;
    private DatasourceInspectionDao datasourceInspection;
    private Map session;
    private List occResources;
    private OccurrenceResource occResource;
    private List tables;
    private Long  id;

    // Struts2 actions get Spring injection via bean name if the property is named the same
    public void setOccResourceManager(GenericManager<OccurrenceResource, Long> occResourceManager) {
        this.occResourceManager = occResourceManager;
    }
    
	public void setDatasourceInspection(DatasourceInspectionDao datasourceInspection) {
		this.datasourceInspection = datasourceInspection;
	}
	
	public List getOccResources() {
        return occResources;
    }
	
    public List getTables() {
		return tables;
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
	
	// Interceptor interfaces
	public void setSession(Map session) {
		this.session=session;		
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

	private void selectCurrentDatasource(OccurrenceResource resource){
		DatasourceContextHolder.setResourceId(resource.getId());
	}


    //    
    // below here are proper Action methods
    //
    public String execute(){
        if (id != null) {
        	// update current resource in session
        	occResource = occResourceManager.get(id);
        	selectCurrentDatasource(occResource);
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
        if (id != null) {
        	// update current resource in session
        	occResource = occResourceManager.get(id);
        	selectCurrentDatasource(occResource);
        }else{
        	occResource = new OccurrenceResource();
        }
        return SUCCESS;
    }

    public String dbmeta() {
        if (id != null) {
        	// update current resource in session
        	occResource = occResourceManager.get(id);
        	selectCurrentDatasource(occResource);
        }else{
        	return ERROR;
        }

        try {
			tables = datasourceInspection.getAllTables();
		} catch (SQLException e) {
			return ERROR;
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
        return SUCCESS;
    }
    
    public String delete() {
        log.debug("Removing datasource from active datasources for resource "+occResource.getId());
        occResourceManager.remove(occResource.getId());
        saveMessage(getText("occResource.deleted"));
        return SUCCESS;
    }

    
}