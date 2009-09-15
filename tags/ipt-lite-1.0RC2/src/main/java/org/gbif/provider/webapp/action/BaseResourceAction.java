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

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.struts2.interceptor.SessionAware;
import org.appfuse.model.LabelValue;
import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.voc.ExtensionType;
import org.gbif.provider.service.ChecklistResourceManager;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class BaseResourceAction<T extends Resource> extends BaseAction implements SessionAware{
	private static final long serialVersionUID = 1643640896L;
	@Autowired
	protected ChecklistResourceManager checklistResourceManager;
	@Autowired
	protected OccResourceManager occResourceManager;
    @Autowired
    @Qualifier("resourceManager")
    protected GenericResourceManager<Resource> metaResourceManager;
	
    protected GenericResourceManager<T> resourceManager;
	protected Long resource_id;
	protected String guid;
	protected T resource;
	protected Map session;
	protected String resourceType;
	private Map<String, String> resourceTypes = translateI18nMap(new HashMap<String, String>(ExtensionType.htmlSelectMap));
	
	public void prepare() {
		if (resource_id != null) {
			resource = resourceManager.get(resource_id);
		}else if (guid != null){
			resource = resourceManager.get(guid);
		}
		if (resource != null) {
			// update recently viewed resources in session
			updateRecentResouces();
			// if resource instance exists this defines the resourceType we are dealing with
			updateResourceType();
		}
	}

	protected void updateResourceType(){
		if (resource!=null){
			resource_id=resource.getId();
			if (resource instanceof OccurrenceResource){
				resourceType = OCCURRENCE;
			}else if (resource instanceof ChecklistResource){
				resourceType =CHECKLIST;
			}else if (resource instanceof Resource){
				resourceType = METADATA;
			}
		}
	}
	
	protected GenericResourceManager<? extends Resource> getResourceTypeMatchingManager(){
		if (resourceType!=null && resourceType.equalsIgnoreCase(ExtensionType.Occurrence.alias)){
			return occResourceManager;
		}else if (resourceType!=null && resourceType.equalsIgnoreCase(ExtensionType.Checklist.alias)){
			return checklistResourceManager;
		}else{
			return metaResourceManager;
		}		
	}
		
	protected void updateRecentResouces(){
		LabelValue res = new LabelValue(resource.getTitle(), resource.getId().toString());
		Queue<LabelValue> queue; 
		Object rr = session.get(Constants.RECENT_RESOURCES);
		if (rr != null && rr instanceof Queue){
			queue = (Queue) rr;
		}else{
			queue = new ConcurrentLinkedQueue<LabelValue>(); 
		}
		// remove old entry from queue if it existed before and insert at tail again
		queue.remove(res);
		queue.add(res);
		if (queue.size()>10){
			// only remember last 10 resources
			queue.remove();
		}
		// save back to session
		session.put(Constants.RECENT_RESOURCES, queue);
	}
	
	
	public void setResource_id(Long resource_id) {
		this.resource_id = resource_id;
	}

	public Long getResource_id() {
		return resource_id;
	}
	
	public T getResource() {
		return resource;
	}
	
	public void setResource(T resource) {
		this.resource = resource;
	}

	public void setSession(Map session) {
		this.session = session;
	}
	
	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getResourceType() {
		return resourceType;
	}
	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public Map<String, String> getResourceTypes() {
		return resourceTypes;
	}

	protected void setResourceTypes(Map<String, String> resourceTypes) {
		this.resourceTypes = resourceTypes;
	}

}
