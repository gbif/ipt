package org.gbif.provider.webapp.action;

import org.springframework.security.Authentication;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.appfuse.model.User;

public class BaseAction extends org.appfuse.webapp.action.BaseAction {
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
}
