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
import org.appfuse.service.GenericManager;
import org.appfuse.service.impl.GenericManagerImpl;
import org.gbif.provider.dao.UploadEventDao;
import org.gbif.provider.datasource.DatasourceRegistry;
import org.gbif.provider.datasource.ExternalResourceRoutingDatasource;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.service.DatasourceBasedResourceManager;
import org.gbif.provider.service.UploadEventManager;
import org.gbif.provider.util.GChartBuilder;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Generic manager for all datasource based resources that need to be registered with the routing datasource.
 * Overriden methods keep the datasource targetsource map of the active datasource registry in sync with the db.
 * @author markus
 *
 * @param <T>
 */
public class UploadEventManagerImpl extends GenericManagerImpl<UploadEvent, Long> implements UploadEventManager {
    private GenericManager<OccurrenceResource, Long> occResourceManager;
    private UploadEventDao dao;
    
	public void setOccResourceManager(
			GenericManager<OccurrenceResource, Long> occResourceManager) {
		this.occResourceManager = occResourceManager;
	}

	public UploadEventManagerImpl(UploadEventDao dao) {
		super(dao);
		this.dao=dao;
	}

	public List<UploadEvent> getUploadEventsByResource(Long resourceId) {
		//DatasourceBasedResource resource = occResourceManager.get(resourceId);
		List<UploadEvent> events = dao.getUploadEventsByResource(resourceId);
		return events;
	}

	public String getGoogleChartData(Long resourceId) {
		List<UploadEvent> events = this.getUploadEventsByResource(resourceId);
		GChartBuilder chartBuilder = new GChartBuilder();
		Map<Date, Long> uploadedDS = new HashMap<Date, Long>();
		Map<Date, Long> addedDS = new HashMap<Date, Long>();
		Map<Date, Long> changedDS = new HashMap<Date, Long>();
		Map<Date, Long> deletedDS = new HashMap<Date, Long>();
		for (UploadEvent e : events){
			uploadedDS.put(e.getExecutionDate(), e.getRecordsUploaded());
			addedDS.put(e.getExecutionDate(), e.getRecordsAdded());
			changedDS.put(e.getExecutionDate(), e.getRecordsChanged());
			deletedDS.put(e.getExecutionDate(), e.getRecordsDeleted());
		}
		chartBuilder.addDataset(uploadedDS, "Uploaded");
		chartBuilder.addDataset(addedDS, "Added");
		chartBuilder.addDataset(changedDS, "Changed");
		chartBuilder.addDataset(deletedDS, "Deleted");
		return chartBuilder.generateChartDataString(450,200);
	}

}
