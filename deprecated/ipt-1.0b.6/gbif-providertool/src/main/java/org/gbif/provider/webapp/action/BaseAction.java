package org.gbif.provider.webapp.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.appfuse.model.User;
import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.voc.ExtensionType;
import org.gbif.provider.util.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.userdetails.UserDetails;


public class BaseAction extends org.appfuse.webapp.action.BaseAction {
    public static final String OCCURRENCE = ExtensionType.Occurrence.alias;
    public static final String CHECKLIST = ExtensionType.Checklist.alias;
    public static final String METADATA = ExtensionType.Metadata.alias;
    public static final String RECORD404 = "record404";
    public static final String RESOURCE404 = "resource404";
	@Autowired
	protected AppConfig cfg;
	private List<String> supportedLocales = new ArrayList<String>();
	
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
	public String getLocaleLanguage(){
		return this.getLocale().getLanguage();		
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
	protected List<String> splitMultiValueParameter(String value){
		if (value==null){
			return new ArrayList<String>();
		}
		String[] paras = StringUtils.split(value, ", ");
		return Arrays.asList(paras);
	}

	public List<String> getSupportedLocales() {
		return supportedLocales;
	}

	public void setSupportedLocales(List<String> supportedLocales) {
		this.supportedLocales = supportedLocales;
	}
}
