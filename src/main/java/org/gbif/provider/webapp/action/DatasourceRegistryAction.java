package org.gbif.provider.webapp.action;

import java.util.List;

import javax.sql.DataSource;

import org.appfuse.service.GenericManager;
import org.gbif.provider.datasource.DatasourceRegistry;
import org.gbif.provider.model.DatasourceBasedResource;

public class DatasourceRegistryAction extends BaseAction {
    private DatasourceRegistry datasourceRegistry;
    private List<DatasourceBasedResource> datasources;

	public void setDatasourceRegistry(DatasourceRegistry datasourceRegistry) {
		this.datasourceRegistry = datasourceRegistry;
	}
	
	public List getDatasources() {
        return datasources;
    }
	
	
	public String list() {
        datasources = datasourceRegistry.getAll();
        return SUCCESS;
    }

}
