package org.gbif.provider.datasource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.Resource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

public class DatasourceRegistry {
	private Map<Long, DatasourceBasedResource> datasources = new HashMap<Long, DatasourceBasedResource>();
	
	public DataSource registerDataSource(DatasourceBasedResource resource){
		datasources.put(resource.getId(), resource);
		return resource.getDatasource();
	}
	
	public DatasourceBasedResource getDataSource(DatasourceBasedResource resource){
		return datasources.get(resource.getId());
	}
	
	public List<DatasourceBasedResource> getAll(){
		return new ArrayList<DatasourceBasedResource>(datasources.values());
	}
	
}
