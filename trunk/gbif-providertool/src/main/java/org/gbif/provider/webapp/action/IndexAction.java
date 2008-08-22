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

import java.util.List;

import javax.servlet.ServletContext;

import org.apache.struts2.util.ServletContextAware;
import org.appfuse.webapp.action.BaseAction;
import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.service.ResourceManager;

import com.opensymphony.xwork2.Preparable;

/**
 * Homepage of the application giving initial statistics and listing imported resources
 * @author markus
 *
 */
public class IndexAction extends BaseAction implements Preparable, ServletContextAware {
    private ResourceManager<OccurrenceResource> occResourceManager;
    private ResourceManager<ChecklistResource> checklistResourceManager;
    private ResourceManager<Resource> resourceManager;
    private List<OccurrenceResource> occResources;
    private List<ChecklistResource> checklistResources;
    private List<Resource> resources;
	private ServletContext context;

	public void setOccResourceManager(ResourceManager<OccurrenceResource> occResourceManager) {
		this.occResourceManager = occResourceManager;
	}
	public void setChecklistResourceManager(ResourceManager<ChecklistResource> checklistResourceManager) {
		this.checklistResourceManager = checklistResourceManager;
	}
	public void setResourceManager(ResourceManager<Resource> resourceManager) {
		this.resourceManager = resourceManager;
	}
	
	public List<ChecklistResource> getChecklistResources() {
		return checklistResources;
	}
	public List<Resource> getResources() {
		return resources;
	}
	public List<OccurrenceResource> getOccResources() {
		return occResources;
	}
	
	public Integer getOccResourceCount() {
		return occResources.size();
	}
	public Integer getChecklistCount() {
		return checklistResources.size();
	}
	public Integer getResourceCount() {
		return resources.size();
	}
		
	
	
	public void prepare() throws Exception {
		occResources = occResourceManager.getAllDistinct();
		checklistResources = checklistResourceManager.getAllDistinct();
		resources = resourceManager.getAllDistinct();
	}

	public String execute(){
		log.debug(context.getAttributeNames());
		log.debug(context.getServerInfo());
		return SUCCESS;
	}

	public void setServletContext(ServletContext context) {
		this.context=context;
	}

	

}
