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

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.security.Authentication;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.apache.struts2.interceptor.SessionAware;
import org.appfuse.model.LabelValue;
import org.appfuse.model.User;
import org.gbif.provider.service.ResourceManager;
import org.gbif.provider.util.Constants;
import org.gbif.provider.model.OccurrenceResource;

import com.opensymphony.xwork2.Preparable;

public class BaseOccurrenceResourceAction extends org.appfuse.webapp.action.BaseAction implements Preparable, SessionAware{
	// new cant be used. Just call the getter new so the parameter becomes new. Neu is german for new ;)
    protected ResourceManager<OccurrenceResource> occResourceManager;
	protected Long resource_id;
	protected OccurrenceResource occResource;
	protected Map session;
	
    public void setOccResourceManager(ResourceManager<OccurrenceResource> occResourceManager) {
		this.occResourceManager = occResourceManager;
	}
    
	public void setResource_id(Long resource_id) {
		this.resource_id = resource_id;
	}

	public Long getResource_id() {
		return resource_id;
	}

	public User getCurrentUser(){
		SecurityContext secureContext = (SecurityContext) SecurityContextHolder.getContext();
	    // secure context will be null when running unit tests so leave userId as null
	    if (secureContext != null) {
	        Authentication auth = (Authentication) ((SecurityContext) SecurityContextHolder.getContext()).getAuthentication();
	        if (auth.getPrincipal() instanceof UserDetails) {
	            User user = (User) auth.getPrincipal();
	    		return user;
	        }
	    }
		return null;
	}

	public void prepare() throws Exception{
		if (resource_id != null) {
			// get resource
			occResource = occResourceManager.get(resource_id);
			
			// update recently viewed resources in session
			LabelValue res = new LabelValue(occResource.getTitle(), resource_id.toString());
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
			log.debug("Recently viewed resources: "+queue.toString());
			session.put(Constants.RECENT_RESOURCES, queue);
		}
	}

	public void setSession(Map session) {
		this.session=session;
	}

}
