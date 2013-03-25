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
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.model.dto.StatsCount;
import org.gbif.provider.service.AnnotationManager;
import org.gbif.provider.service.CacheManager;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.service.SourceManager;
import org.gbif.provider.service.TransformationManager;
import org.gbif.provider.service.UploadEventManager;
import org.gbif.provider.service.ViewMappingManager;
import org.gbif.provider.util.GChartBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

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
	@Autowired
	private TransformationManager transformationManager;
	@Autowired
	private ViewMappingManager mappingManager;
	@Autowired
    private SourceManager sourceManager;
	@Autowired
	private AnnotationManager annotationManager;
	@Autowired
	private UploadEventManager uploadEventManager;
	@Autowired
	@Qualifier("propertyMappingManager")
    private GenericManager<PropertyMapping> propertyMappingManager;


	public DataResourceManagerHibernate(Class<T> persistentClass) {
		super(persistentClass);
	}


	@Override
	@Transactional(readOnly=false)
	public void remove(T obj) {
		// first remove all associated core records, taxa and regions
		if (obj!=null){
			Long resourceId = obj.getId();
			log.debug("Trying to remove data resource "+resourceId);
			obj.resetStats();
			save(obj);
			flush();
			// remove upload events after we have set the resource.lastEvent to null
			uploadEventManager.removeAll(obj);
			// remove annotations
			annotationManager.removeAll(obj);
			// remove transformations
			transformationManager.removeAll(obj);
			// remove mappings
			mappingManager.removeAll(obj);
			// remove source file entities
			log.debug("Removing sourceManager");
			sourceManager.removeAll(obj);
			log.debug("SourceManager removed");
		}
		// remove resource entity
		super.remove(obj);
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

	protected void setResourceStats(DataResource resource) {
	}
}
