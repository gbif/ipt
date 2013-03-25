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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.service.UploadEventManager;
import org.gbif.provider.util.UploadChartBuilder;
import org.hibernate.Session;

/**
 * Generic manager for all datasource based resources that need to be registered with the routing datasource.
 * Overriden methods keep the datasource targetsource map of the active datasource registry in sync with the db.
 * @author markus
 *
 * @param <T>
 */
public class UploadEventManagerHibernate extends GenericResourceRelatedManagerHibernate<UploadEvent> implements UploadEventManager {
	public UploadEventManagerHibernate() {
		super(UploadEvent.class);
	}

	@SuppressWarnings("unchecked")
	public List<UploadEvent> getUploadEventsByResource(final Long resourceId) {
		return getSession().createQuery("select event FROM UploadEvent event WHERE event.resource.id = :resourceId")
        	.setParameter("resourceId", resourceId).list();
	}

	public String getGoogleChartData(Long resourceId, int width, int height) {
		List<UploadEvent> events = this.getUploadEventsByResource(resourceId);
		UploadChartBuilder chartBuilder = new UploadChartBuilder();
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
