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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.appfuse.dao.GenericDao;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.service.impl.GenericManagerImpl;
import org.gbif.logging.log.I18nLog;
import org.gbif.logging.log.I18nLogFactory;
import org.gbif.provider.dao.UploadEventDao;
import org.gbif.provider.datasource.DatasourceRegistry;
import org.gbif.provider.datasource.ExternalResourceRoutingDatasource;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.service.ResourceManager;
import org.gbif.provider.service.UploadEventManager;
import org.gbif.provider.util.GChartBuilder;
import org.gbif.provider.webapp.action.UploadAction;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.TextProvider;
import com.opensymphony.xwork2.TextProviderFactory;

/**
 * Generic manager for all datasource based resources that need to be registered with the routing datasource.
 * Overriden methods keep the datasource targetsource map of the active datasource registry in sync with the db.
 * @author markus
 *
 * @param <T>
 */
public class UploadEventManagerImpl extends GenericManagerImpl<UploadEvent> implements UploadEventManager {
	private UploadEventDao dao;
    //private final transient TextProvider textProvider = TextProviderFactory.getInstance();

	public UploadEventManagerImpl(UploadEventDao dao) {
		super(dao);
		this.dao=dao;
	}

	public List<UploadEvent> getUploadEventsByResource(Long resourceId) {
		//DatasourceBasedResource resource = occResourceManager.get(resourceId);
		List<UploadEvent> events = dao.getUploadEventsByResource(resourceId);
		return events;
	}

	public String getGoogleChartData(Long resourceId, int width, int height) {
		List<UploadEvent> events = this.getUploadEventsByResource(resourceId);
		GChartBuilder chartBuilder = new GChartBuilder();
		Map<Date, Long> uploadedDS = new HashMap<Date, Long>();
		Map<Date, Long> changedDS = new HashMap<Date, Long>();
		Map<Date, Long> addedDS = new HashMap<Date, Long>();
		Map<Date, Long> deletedDS = new HashMap<Date, Long>();
		// if no events exist do an empty default one to get some image at all
		if (events.isEmpty()){
			UploadEvent e = new UploadEvent();
			e.setRecordsAdded(0);
			e.setRecordsChanged(0);
			e.setRecordsDeleted(0);
			e.setRecordsUploaded(0);
			e.setExecutionDate(new Date());
			events.add(e);
		}
		for (UploadEvent e : events){
			uploadedDS.put(e.getExecutionDate(), Long.valueOf(e.getRecordsUploaded()));
			changedDS.put(e.getExecutionDate(), Long.valueOf(e.getRecordsChanged()));
			addedDS.put(e.getExecutionDate(), Long.valueOf(e.getRecordsAdded()));
			deletedDS.put(e.getExecutionDate(), Long.valueOf(e.getRecordsDeleted()));
		}
		chartBuilder.addDataset(uploadedDS, "Uploaded");
		chartBuilder.addDataset(changedDS, "Changed");
		chartBuilder.addDataset(addedDS, "Added");
		chartBuilder.addDataset(deletedDS, "Deleted");
		
		return chartBuilder.generateChartDataString(width, height);
	}

	@Override
	public UploadEvent save(UploadEvent object) {
		return super.save(object);
	}

}
