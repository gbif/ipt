package org.gbif.provider.webapp.action;

import java.util.Map;

import org.appfuse.model.User;
import org.gbif.provider.util.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.userdetails.UserDetails;


public class BaseAction extends org.appfuse.webapp.action.BaseAction {
    public static final String OCCURRENCE = "occ";
    public static final String TAXON = "tax";
    public static final String METADATA = "meta";
	@Autowired
	protected AppConfig cfg;

	public User getCurrentUser(){
		final SecurityContext secureContext = (SecurityContext) SecurityContextHolder.getContext();
	    // secure context will be null when running unit tests so leave userId as null
	    if (secureContext != null) {
	        final Authentication auth = (Authentication) ((SecurityContext) SecurityContextHolder.getContext()).getAuthentication();
	        if (auth.getPrincipal() instanceof UserDetails) {
	            final User user = (User) auth.getPrincipal();
	    		return user;
	        }
	    }
		return null;
	}
	
	public void setCancel(String cancel) {
		this.cancel = cancel;
	}
	public void setDelete(String delete) {
		this.delete = delete;
	}


	public AppConfig getCfg() {
		return cfg;
	}

	protected Map<String, String> translateI18nMap(Map<String, String> map){
		for (String key : map.keySet()){
			String i18Key = map.get(key);
			map.put(key, getText(i18Key));
		}
		return map;
	}
}
