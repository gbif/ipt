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

import org.springframework.security.Authentication;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.apache.struts2.interceptor.SessionAware;
import org.appfuse.model.User;
import org.gbif.provider.service.ResourceManager;
import org.gbif.provider.datasource.DatasourceInterceptor;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;

public class BaseOccurrenceResourceAction extends org.appfuse.webapp.action.BaseAction implements SessionAware{
	// new cant be used. Just call the getter new so the parameter becomes new. Neu is german for new ;)
    protected ResourceManager<OccurrenceResource> occResourceManager;
    protected Map session;
	private Boolean neu;
	
	public void setNeu(Boolean neu) {
		this.neu = neu;
	}
	
    public void setOccResourceManager(ResourceManager<OccurrenceResource> occResourceManager) {
		this.occResourceManager = occResourceManager;
	}



	/**
     * is the acton intended to create a new entity? New cant be used as a java property, 
     * so the german translation "neu" is used as the underlying property
     * @return
     */
    public boolean isNew() {
    	if (neu != null && neu){
    		return true;
    	}
		return false;
	}
    
    public Long getResourceId() {
	    return (Long) session.get(DatasourceInterceptor.SESSION_ATTRIBUTE);			
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

	public void setSession(Map arg0) {
		session = arg0;
	}
}
