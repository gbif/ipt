package org.gbif.provider.webapp.action;

import java.sql.SQLException;
import java.util.List;

import org.appfuse.service.GenericManager;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.ViewMapping;
import org.gbif.provider.service.DatasourceInspectionManager;
import org.springframework.beans.factory.annotation.Autowired;

public class ViewMappingAction extends BaseAction {
    private DatasourceInspectionManager datasourceInspectionManager;
    private GenericManager<ViewMapping, Long> viewMappingManager;
    private List tables;
	private Long resource_id;
		
	public void setDatasourceInspectionManager(
			DatasourceInspectionManager datasourceInspectionManager) {
		this.datasourceInspectionManager = datasourceInspectionManager;
	}

	public void setViewMappingManager(
			GenericManager<ViewMapping, Long> viewMappingManager) {
		this.viewMappingManager = viewMappingManager;
	}

	public List getTables() {
		return tables;
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
