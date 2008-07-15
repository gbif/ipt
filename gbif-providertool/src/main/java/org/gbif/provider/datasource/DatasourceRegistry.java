/***************************************************************************
* Copyright (C) 2008 Global Biodiversity Information Facility Secretariat.
* All Rights Reserved.
*
* The contents of this file are subject to the Mozilla Public
* License Version 1.1 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of
* the License at http://www.mozilla.org/MPL/
*
* Software distributed under the License is distributed on an "AS
* IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
* implied. See the License for the specific language governing
* rights and limitations under the License.

***************************************************************************/

package org.gbif.provider.datasource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.appfuse.service.GenericManager;
import org.gbif.provider.job.RdbmsUploadJob;
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
	protected static final Log log = LogFactory.getLog(DatasourceRegistry.class);
	private Map<Long, DataSource> datasources = new HashMap<Long, DataSource>();
    private GenericManager<OccurrenceResource, Long> occResourceManager;
    
	public Map<Long, DataSource> getDatasources() {
		return datasources;
	}
	public void setDatasources(Map<Long, DataSource> datasources) {
		this.datasources = datasources;
	}


	public void registerDatasource(DatasourceBasedResource resource){
		if (resource != null){
			DataSource dsa = resource.getDatasource();
			if (dsa == null){
				log.warn("Trying to register a resource without any datasource");
			}else{
				datasources.put(resource.getId(), dsa);
			}
		}
	}
	
	public DataSource getDataSource(Long id){
		if (id != null && !datasources.containsKey(id)){
			DatasourceBasedResource resource = occResourceManager.get(id);
			registerDatasource(resource);
		}
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
	public void setOccResourceManager(
			GenericManager<OccurrenceResource, Long> occResourceManager) {
		this.occResourceManager = occResourceManager;
	}
		
}
