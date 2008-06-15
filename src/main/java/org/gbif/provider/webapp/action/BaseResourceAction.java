package org.gbif.provider.webapp.action;

import java.util.Map;

import org.springframework.security.Authentication;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.apache.struts2.interceptor.SessionAware;
import org.appfuse.model.User;
import org.appfuse.service.GenericManager;
import org.gbif.provider.datasource.DatasourceInterceptor;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;

public class BaseResourceAction extends org.appfuse.webapp.action.BaseAction implements SessionAware{
	// new cant be used. Just call the getter new so the parameter becomes new. Neu is german for new ;)
	private Boolean neu;
    protected GenericManager<OccurrenceResource, Long> occResourceManager;
    protected Map session;
    
	public void setOccResourceManager(
			GenericManager<OccurrenceResource, Long> occResourceManager) {
		this.occResourceManager = occResourceManager;
	}
	
	public Long getResourceId() {
	    return (Long) session.get(DatasourceInterceptor.SESSION_ATTRIBUTE);			
	}
	
    public boolean isNew() {
    	if (neu != null && neu){
    		return true;
    	}
		return false;
	}
    public void setNew(Boolean neu) {
		neu = neu;
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
