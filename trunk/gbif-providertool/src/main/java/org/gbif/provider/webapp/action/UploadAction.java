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

package org.gbif.provider.webapp.action;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.appfuse.service.GenericManager;
import org.gbif.logging.log.I18nLog;
import org.gbif.logging.log.I18nLogFactory;
import org.gbif.provider.datasource.DatasourceInterceptor;
import org.gbif.provider.datasource.impl.RdbmsImportSource;
import org.gbif.provider.job.JobUtils;
import org.gbif.provider.job.OccDbUploadJob;
import org.gbif.provider.job.OccDbUploadJob;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.service.DatasourceInspectionManager;
import org.gbif.provider.util.Constants;
import org.gbif.scheduler.model.Job;
import org.gbif.scheduler.scheduler.Launchable;
import org.gbif.scheduler.service.JobManager;
import org.gbif.util.JSONUtils;
import org.hibernate.type.SortedMapType;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysql.jdbc.DatabaseMetaData;
import com.opensymphony.xwork2.Preparable;

public class UploadAction extends BaseOccurrenceResourceAction implements Preparable{
	private static I18nLog logdb = I18nLogFactory.getLog(UploadAction.class);

	private DatasourceInspectionManager datasourceInspectionManager;
    private JobManager jobManager;
    private OccurrenceResource resource;
	private List<Job> scheduledJobs;
	private int repeatInDays;
	private Integer limit;

	private GenericManager<UploadEvent, Long> uploadEventManager;
	private List<UploadEvent> uploadEvents;
	
	public void setJobManager(JobManager jobManager) {
		this.jobManager = jobManager;
	}

	public void setDatasourceInspectionManager(
			DatasourceInspectionManager datasourceInspectionManager) {
		this.datasourceInspectionManager = datasourceInspectionManager;
	}

	public void setUploadEventManager(
			GenericManager<UploadEvent, Long> uploadEventManager) {
		this.uploadEventManager = uploadEventManager;
	}

	public void setRepeatInDays(int repeatInDays) {
		this.repeatInDays = repeatInDays;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public List<UploadEvent> getUploadEvents() {
		return uploadEvents;
	}

	public OccurrenceResource getResource() {
		return resource;
	}
	
	public List<Job> getScheduledJobs() {
		return scheduledJobs;
	}

	
	public void prepare() {
    	resource = occResourceManager.get(resource_id);
		scheduledJobs = jobManager.getJobsInGroup(JobUtils.getJobGroup(resource));
	}


	public String execute(){
        return SUCCESS;
	}
	
	public String addUploadJob() throws Exception{
		// create & store upload job based on resource alone
		//Job job = RdbmsUploadJob.newUploadJob(resource, getCurrentUser(), repeatInDays, limit);
		Job job = OccDbUploadJob.newUploadJob(resource, getCurrentUser(), repeatInDays, limit);
		jobManager.save(job);
		// add to scheduledJobs that was created previously in prepare() phase already
		scheduledJobs.add(job);
		
        saveMessage(getText("upload.addedJob"));
		return SUCCESS;
	}
	
	public String history(){
		//TODO: select only events belonging to this resource!
		uploadEvents=uploadEventManager.getAll();
		return SUCCESS;
	}

}
