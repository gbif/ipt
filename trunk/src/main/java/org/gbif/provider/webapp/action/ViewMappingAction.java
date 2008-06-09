package org.gbif.provider.webapp.action;

import java.sql.SQLException;
import java.util.List;

import org.appfuse.service.GenericManager;
import org.gbif.provider.datasource.DatasourceInterceptor;
import org.gbif.provider.model.DwcExtension;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.ViewMapping;
import org.gbif.provider.service.DatasourceInspectionManager;
import org.gbif.provider.service.ViewMappingManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Preparable;

public class ViewMappingAction extends BaseAction implements Preparable{
    private DatasourceInspectionManager datasourceInspectionManager;
    private GenericManager<OccurrenceResource, Long> occResourceManager;
    private GenericManager<DwcExtension, Long> dwcExtensionManager;
    private ViewMappingManager viewMappingManager;
    private ViewMapping mapping;
    private List tables;
	private Long mapping_id;
	private Long resource_id;
	private Long extension_id;
		
	public void setDatasourceInspectionManager(
			DatasourceInspectionManager datasourceInspectionManager) {
		this.datasourceInspectionManager = datasourceInspectionManager;
	}

	public void setOccResourceManager(
			GenericManager<OccurrenceResource, Long> occResourceManager) {
		this.occResourceManager = occResourceManager;
	}

	public void setDwcExtensionManager(
			GenericManager<DwcExtension, Long> dwcExtensionManager) {
		this.dwcExtensionManager = dwcExtensionManager;
	}

	public void setViewMappingManager(ViewMappingManager viewMappingManager) {
		this.viewMappingManager = viewMappingManager;
	}

	
	public ViewMapping getMapping() {
		return mapping;
	}

	public List getTables() {
		return tables;
	}
	
	public Long getMapping_id() {
		return mapping_id;
	}

	public void setMapping_id(Long mapping_id) {
		this.mapping_id = mapping_id;
	}

	public Long getResource_id() {
		return resource_id;
	}

	public void setResource_id(Long resource_id) {
		this.resource_id = resource_id;
	}

	public Long getExtension_id() {
		return extension_id;
	}

	public void setExtension_id(Long extension_id) {
		this.extension_id = extension_id;
	}

	public void prepare() throws Exception {
        if (mapping_id != null) {
        	mapping = viewMappingManager.get(mapping_id);
        	resource_id = mapping.getResource().getId();
        }else{
            if (resource_id != null && extension_id != null) {
            	mapping = new ViewMapping();
            	mapping.setResource(occResourceManager.get(resource_id));
            	mapping.setExtension(dwcExtensionManager.get(extension_id));
            }
        }
    }
	
	public String edit(){
        return SUCCESS;
	}
        
    public String save() throws Exception {
        if (cancel != null) {
            return "cancel";
        }
        if (delete != null) {
            return delete();
        }
        boolean isNew = (mapping.getId() == null);
        mapping = viewMappingManager.save(mapping);
        String key = (isNew) ? "viewMapping.added" : "viewMapping.updated";
        saveMessage(getText(key));
        return SUCCESS;
    }

    
    public String delete() {
    	viewMappingManager.remove(mapping.getId());
        saveMessage(getText("viewMapping.deleted"));
        return "cancel";
    }

    public String dbmeta() {
        try {
			tables = datasourceInspectionManager.getAllTables();
		} catch (SQLException e) {
			return ERROR;
		}
        return SUCCESS;
    }

 
}
