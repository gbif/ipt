package org.gbif.provider.datasource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.gbif.provider.model.ExternalDatasourceResourceBase;
import org.gbif.provider.model.Resource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

public class DatasourceRegistry {
	private Map<Long, ExternalDatasourceResourceBase> datasources = new HashMap<Long, ExternalDatasourceResourceBase>();
	
	public DataSource registerDataSource(ExternalDatasourceResourceBase resource){
		datasources.put(resource.getId(), resource);
		return resource.getDatasource();
	}
	
	public ExternalDatasourceResourceBase getDataSource(ExternalDatasourceResourceBase resource){
		return datasources.get(resource.getId());
	}
	
	public List<ExternalDatasourceResourceBase> getAll(){
		return new ArrayList<ExternalDatasourceResourceBase>(datasources.values());
	}
	
}
