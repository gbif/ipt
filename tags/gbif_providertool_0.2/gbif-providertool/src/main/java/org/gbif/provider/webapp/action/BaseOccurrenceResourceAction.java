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
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.service.ResourceManager;
import org.gbif.provider.util.Constants;
import org.gbif.provider.model.OccurrenceResource;

import com.opensymphony.xwork2.Preparable;

public class BaseOccurrenceResourceAction extends org.appfuse.webapp.action.BaseAction{
	// new cant be used. Just call the getter new so the parameter becomes new. Neu is german for new ;)
    protected OccResourceManager occResourceManager;
	protected Long resource_id;
	
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

	public void setResource_id(Long resource_id) {
		this.resource_id = resource_id;
	}

	public Long getResource_id() {
		return resource_id;
	}

	public void setOccResourceManager(OccResourceManager occResourceManager) {
		this.occResourceManager = occResourceManager;
	}

}
