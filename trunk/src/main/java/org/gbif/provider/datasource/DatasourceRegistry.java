package org.gbif.provider.datasource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

public class DatasourceRegistry {
	private Map<Long, OccurrenceResource> datasources = new HashMap<Long, OccurrenceResource>();
	
	public DataSource registerDataSource(OccurrenceResource resource){
		datasources.put(resource.getId(), resource);
		return resource.getDatasource();
	}
	
	public OccurrenceResource getDataSource(OccurrenceResource resource){
		return datasources.get(resource.getId());
	}
	
	public List<OccurrenceResource> getAll(){
		return new ArrayList<OccurrenceResource>(datasources.values());
	}
	
}
