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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.struts2.interceptor.SessionAware;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.datasource.DatasourceInterceptor;
import org.gbif.provider.job.JobUtils;
import org.gbif.provider.job.UploadBaseJob;
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
	private JobManager jobManager;
	
	private List<Extension> extensions;
	private List occResources;
	private OccurrenceResource occResource;
	private String gChartData;
	private Job currentJob;
	private Job nextUpload;
	private final Map<String, String> jdbcDriverClasses = new HashMap<String, String>()   
	        {  
	            {  
	                put("com.mysql.jdbc.Driver", "MySQL");
	                put("org.postgresql.Driver", "Postrges");
	                put("org.h2.Driver", "H2");
	                put("net.sourceforge.jtds.jdbc.Driver", "MS SQL Server");  
	                put("oracle.jdbc.OracleDriver", "Oracle");  
	                put("org.hsqldb.jdbcDriver", "HSQL");  
	                put("org.apache.derby.jdbc.ClientDriver", "Derby");  
	            }  
	        };  
	  

	public Map<String, String> getJdbcDriverClasses() {
		return jdbcDriverClasses;
	}

	public void setResourceFactory(ResourceFactory resourceFactory) {
		this.resourceFactory = resourceFactory;
	}

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

	public Job getCurrentJob() {
		return currentJob;
	}

	public Job getNextUpload() {
		return nextUpload;
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
		if (resource_id != null) {
			occResource = occResourceManager.get(resource_id);
		} else {
			occResource = resourceFactory.newOccurrenceResourceInstance();
		}
	}

	public String execute() {
		// create GoogleChart string
		gChartData = uploadEventManager.getGoogleChartData(resource_id);
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
		List<Job> jobs = jobManager.getJobsInGroup(JobUtils.getJobGroup(occResource));
		for (Job j : jobs) {
			if (j.getStarted() != null) {
				// job is running
				if (currentJob != null){					
					log.warn("Multiple jobs running in parallel for resource "+resource_id);
				}
				currentJob = j;
			} else {
				try {
					Class jobClass = Class.forName(j.getJobClassName());
					if (jobClass.isAssignableFrom(UploadBaseJob.class) && (nextUpload == null || j.getNextFireTime().after(nextUpload.getNextFireTime()))) {
						nextUpload = j;
					}
				} catch (ClassNotFoundException e) {
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
		return SUCCESS;
	}

	public String delete() {
		occResourceManager.remove(occResource.getId());
		saveMessage(getText("occResource.deleted"));
		return "delete";
	}

	public String count() {
		return SUCCESS;
	}
}