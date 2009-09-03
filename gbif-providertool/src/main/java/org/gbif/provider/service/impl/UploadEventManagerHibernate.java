/*
 * Copyright 2009 GBIF.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gbif.provider.service.impl;

import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.service.UploadEventManager;
import org.gbif.provider.util.UploadChartBuilder;

import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic manager for all datasource based resources that need to be registered
 * with the routing datasource. Overriden methods keep the datasource
 * targetsource map of the active datasource registry in sync with the db.
 * 
 */
public class UploadEventManagerHibernate extends
    GenericResourceRelatedManagerHibernate<UploadEvent> implements
    UploadEventManager {
  public UploadEventManagerHibernate() {
    super(UploadEvent.class);
  }

  public String getGoogleChartData(Long resourceId, int width, int height) {
    List<UploadEvent> events = this.getUploadEventsByResource(resourceId);
    UploadChartBuilder chartBuilder = new UploadChartBuilder();
    Map<Date, Long> uploadedDS = new HashMap<Date, Long>();
    Map<Date, Long> changedDS = new HashMap<Date, Long>();
    Map<Date, Long> addedDS = new HashMap<Date, Long>();
    Map<Date, Long> deletedDS = new HashMap<Date, Long>();
    // if no events exist do an empty default one to get some image at all
    if (events.isEmpty()) {
      UploadEvent e = new UploadEvent();
      e.setRecordsAdded(0);
      e.setRecordsChanged(0);
      e.setRecordsDeleted(0);
      e.setRecordsUploaded(0);
      e.setExecutionDate(new Date());
      events.add(e);
    }
    for (UploadEvent e : events) {
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

  @SuppressWarnings("unchecked")
  public List<UploadEvent> getUploadEventsByResource(final Long resourceId) {
    return getSession().createQuery(
        "select event FROM UploadEvent event WHERE event.resource.id = :resourceId").setParameter(
        "resourceId", resourceId).list();
  }

  @Override
  @Transactional(readOnly = false)
  public UploadEvent save(UploadEvent object) {
    return super.save(object);
  }

}
