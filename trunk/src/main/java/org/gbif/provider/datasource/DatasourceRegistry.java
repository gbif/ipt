package org.gbif.provider.datasource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.appfuse.service.GenericManager;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

/**
 * Registry of active external datasources (=DatasourceBasedResource) used by ExternalResourceRoutingDatasource.
 * If a new resource is selected within a http session this registry must be updated. 
 * If a resource has been modified the responsible service/manager must update the corresponding datasource if it exists. 
 * @author markus
 * @See ExternalResourceRoutingDatasource
 */
public class DatasourceRegistry{
	private Map<Long, DataSource> datasources = new HashMap<Long, DataSource>();
	@Autowired
    private GenericManager<OccurrenceResource, Long> occResourceManager;
    
	public Map<Long, DataSource> getDatasources() {
		return datasources;
	}
	public void setDatasources(Map<Long, DataSource> datasources) {
		this.datasources = datasources;
	}


	public void registerDatasource(DatasourceBasedResource resource, boolean overwrite){
		if (datasources.containsKey(resource.getId()) && !overwrite ){
			// do nothing
		}else{
			DataSource dsa = resource.getDatasource();
			datasources.put(resource.getId(), dsa);
		}
	}
	
	public DataSource getDataSource(Long id){
		return datasources.get(id);
	}

	public void removeDatasource(Long id){
		DataSource dsa = getDataSource(id);
		datasources.remove(dsa);
	}
	
	public List<DatasourceBasedResource> getAll(){
		ArrayList<DatasourceBasedResource> activeResources = new ArrayList<DatasourceBasedResource>();
		for (Long id: datasources.keySet()){
			activeResources.add(occResourceManager.get(id));
		}
		return activeResources;
	}
	
	public boolean containsKey(Long id) {
		return datasources.containsKey(id);
	}
		
}
