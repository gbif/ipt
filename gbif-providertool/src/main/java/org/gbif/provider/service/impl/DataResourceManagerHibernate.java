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

package org.gbif.provider.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.gbif.provider.datasource.DatasourceRegistry;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.dto.StatsCount;
import org.gbif.provider.service.CacheManager;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.GChartBuilder;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Generic manager for all datasource based resources that need to be registered with the routing datasource.
 * Overriden methods keep the datasource targetsource map of the active datasource registry in sync with the db.
 * @author markus
 *
 * @param <T>
 */
public class DataResourceManagerHibernate<T extends DataResource> extends GenericResourceManagerHibernate<T> implements GenericResourceManager<T> {
	private static final int MAX_CHART_DATA = 20;
	protected static GChartBuilder gpb = new GChartBuilder();

	public DataResourceManagerHibernate(Class<T> persistentClass) {
		super(persistentClass);
	}

	@Autowired
	private DatasourceRegistry registry;
	@Autowired
	private CacheManager cacheManager;


	@Override
	public void remove(T obj) {
		// first remove all associated core records, taxa and regions
		if (obj!=null){
			Long resourceId = obj.getId();
			cacheManager.clear(resourceId);
			// update registry
			if (registry.containsKey(resourceId)){
				registry.removeDatasource(resourceId);
			}		
		}
		// remove resource entity
		super.remove(obj);
	}

	@Override
	public T save(T resource) {
		// call the real thing first to get a resourceId assigned
		T persistentResource = super.save(resource);
		//datasource might have been updated, so re-register
		registry.registerDatasource(resource);
		return persistentResource;
	}
	
	@Override
	public T get(Long id) {
		T obj = super.get(id);
		// init some OnetoMany properties
		if (obj != null){
			obj.getAllMappings();
		}
		return obj;
	}

	

	protected List<StatsCount> getDataMap(List<Object[]> occBySth){
		List<StatsCount> data = new ArrayList<StatsCount>();
        for (Object[] row : occBySth){
        	Long id=null;
        	Object value;
        	Long count;
        	if (row.length==2){
            	value = row[0];
            	count = (Long) row[1];
        	}else{
            	id = (Long) row[0];
            	value = row[1];
            	try{
                	count = (Long) row[2];
            	} catch (ClassCastException e){
            		count = Long.valueOf(row[2].toString());
            	}
        	}
        	String label = null;
        	if (value!=null){
				label = value.toString();
        	}
        	if (StringUtils.trimToNull(label)==null){
        		label = "?";
        	}
        	data.add(new StatsCount(id, label, value, count));
        }
        // sort data
        Collections.sort(data);
        return data;
	}

	
	/**
	 * Select most frequent MAX_CHART_DATA data entries and group all other as a single #other# entry
	 * @param data
	 * @return
	 */
	protected List<StatsCount> limitDataForChart(List<StatsCount> data) {
		if (data.size()>MAX_CHART_DATA){
			List<StatsCount> exceedingData = data.subList(MAX_CHART_DATA, data.size()-1);
			Long cnt = 0l;
			for (StatsCount stat : exceedingData){
				cnt+=stat.getCount();
			}
			StatsCount other = new StatsCount(null, GChartBuilder.OTHER_LABEL, GChartBuilder.OTHER_LABEL, cnt);
			List<StatsCount> limitedData = new ArrayList<StatsCount>();
			limitedData.add(other);
			limitedData.addAll(data.subList(0, MAX_CHART_DATA-1));
			return limitedData;
		}
		return data;
	}
	
}
