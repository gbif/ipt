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
import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.dto.StatsCount;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.service.CacheManager;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.service.TaxonManager;
import org.gbif.provider.service.TransformationManager;
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
	@Autowired
	private TaxonManager taxonManager;
	@Autowired
	private TransformationManager transformationManager;

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
			// remove transformations
			transformationManager.removeAll(obj);
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
		Long resourceId = resource.getId();
		resource.setNumClasses(taxonManager.countByType(resourceId, Rank.Class));
		resource.setNumFamilies(taxonManager.countByType(resourceId, Rank.Family));
		resource.setNumGenera(taxonManager.countByType(resourceId, Rank.Genus));
		resource.setNumKingdoms(taxonManager.countByType(resourceId, Rank.Kingdom));
		resource.setNumOrders(taxonManager.countByType(resourceId, Rank.Order));
		resource.setNumPhyla(taxonManager.countByType(resourceId, Rank.Phylum));
		resource.setNumSpecies(taxonManager.countByType(resourceId, Rank.Species));
		resource.setNumTaxa(taxonManager.count(resourceId));
		resource.setNumTerminalTaxa(taxonManager.countTerminalNodes(resourceId));
		resource.setNumAccepted(taxonManager.countAccepted(resourceId));
		resource.setNumSynonyms(taxonManager.countSynonyms(resourceId));
	}
}
