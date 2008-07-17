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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.struts2.interceptor.SessionAware;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.datasource.DatasourceInterceptor;
import org.gbif.provider.job.JobUtils;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.model.ViewMapping;
import org.gbif.provider.service.DatasourceInspectionManager;
import org.gbif.provider.service.ResourceFactory;
import org.gbif.provider.service.UploadEventManager;
import org.gbif.provider.util.Constants;
import org.gbif.scheduler.model.Job;
import org.gbif.scheduler.service.JobManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Preparable;

public class OccResourceAction extends BaseOccurrenceResourceAction implements Preparable {
	private ResourceFactory resourceFactory;
	private GenericManager<Extension> extensionManager;
	private GenericManager<ViewMapping> viewMappingManager;
	private UploadEventManager uploadEventManager;
	private List occResources;
	private List<Extension> extensions;
	private OccurrenceResource occResource;
	private String gChartData;
	private JobManager jobManager;
	private List<Job> runningJobs;
	private Job nextJob;

	public void setJobManager(JobManager jobManager) {
		this.jobManager = jobManager;
	}

	public void setExtensionManager(
			GenericManager<Extension> extensionManager) {
		this.extensionManager = extensionManager;
	}

	public void setViewMappingManager(
			GenericManager<ViewMapping> viewMappingManager) {
		this.viewMappingManager = viewMappingManager;
	}

	public void setUploadEventManager(UploadEventManager uploadEventManager) {
		this.uploadEventManager = uploadEventManager;
	}

	public List getOccResources() {
		return occResources;
	}

	public List getExtensions() {
		return extensions;
	}

	public List<Job> getRunningJobs() {
		return runningJobs;
	}

	public Job getNextJob() {
		return nextJob;
	}

	public String getGChartData() {
		return gChartData;
	}

	public OccurrenceResource getOccResource() {
		return occResource;
	}

	public void setOccResource(OccurrenceResource occResource) {
		this.occResource = occResource;
	}

	public void prepare() {
		if (getResourceId() != null && !isNew()) {
			occResource = occResourceManager.get(getResourceId());
		} else {
			occResource = resourceFactory.newOccurrenceResourceInstance();
		}
	}

	public String execute() {
		// create GoogleChart string
		gChartData = uploadEventManager.getGoogleChartData(getResourceId());
		// get all availabel extensions for new mappings
		extensions = extensionManager.getAll();
		for (Extension ext : extensions) {
			if (ext.getId().equals(OccurrenceResource.EXTENSION_ID)) {
				// only show extensions sensu strictu. remove core "extension"
				extensions.remove(ext);
				break;
			}
		}
		// filter already mapped extensions
		for (ViewMapping map : occResource.getAllMappings()) {
			extensions.remove(map.getExtension());
		}
		// investigate upload jobs
		runningJobs = new ArrayList<Job>();
		List<Job> jobs = jobManager.getJobsInGroup(JobUtils
				.getJobGroup(occResource));
		for (Job j : jobs) {
			if (j.getStarted() != null) {
				// job is running
				runningJobs.add(j);
			} else {
				if (nextJob == null
						|| j.getNextFireTime().after(nextJob.getNextFireTime())) {
					nextJob = j;
				}
			}
		}
		return SUCCESS;
	}

	public String list() {
		occResources = occResourceManager.getResourcesByUser(getCurrentUser().getId());
		return SUCCESS;
	}

	public String edit() {
		return SUCCESS;
	}

	public String save() throws Exception {
		if (cancel != null) {
			return "cancel";
		}
		if (delete != null) {
			return delete();
		}

		boolean isNew = (occResource.getId() == null);
		occResource = occResourceManager.save(occResource);
		String key = (isNew) ? "occResource.added" : "occResource.updated";
		saveMessage(getText(key));
		// set new current resource in session
		session.put(DatasourceInterceptor.SESSION_ATTRIBUTE, occResource.getId());
		return SUCCESS;
	}

	public String delete() {
		occResourceManager.remove(occResource.getId());
		saveMessage(getText("occResource.deleted"));
		// remove resource from session
		session.put(DatasourceInterceptor.SESSION_ATTRIBUTE, null);
		return "delete";
	}

}