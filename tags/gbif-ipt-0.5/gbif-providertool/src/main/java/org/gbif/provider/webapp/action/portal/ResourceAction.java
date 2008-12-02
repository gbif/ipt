package org.gbif.provider.webapp.action.portal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.interceptor.SessionAware;
import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.eml.Eml;
import org.gbif.provider.model.eml.Role;
import org.gbif.provider.model.eml.TaxonKeyword;
import org.gbif.provider.model.voc.Rank;
import org.gbif.provider.service.EmlManager;
import org.gbif.provider.webapp.action.BaseMetadataResourceAction;
import org.gbif.provider.webapp.action.BaseResourceAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.opensymphony.xwork2.Preparable;

public class ResourceAction extends BaseMetadataResourceAction implements Preparable{
	@Autowired
	private EmlManager emlManager;
	private Eml eml;
	private String format;
    private List<Resource> resources;
		
	
	public String execute(){
		if (resource!=null){
			eml = emlManager.load(resource);
		}		
		return SUCCESS;
	}
	
	public String forward(){
		if (resource instanceof OccurrenceResource) {
			return OCCURRENCE;
		}else if (resource instanceof ChecklistResource) {
			return TAXON;
		}else{
			return METADATA;
		}
	}

	public String rss(){
		resources = resourceManager.getAll();
		return SUCCESS;
	}

	
	
	
	
	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
	
	public Eml getEml() {
		return eml;
	}

	public void setEml(Eml eml) {
		this.eml = eml;
	}

	public List<Resource> getResources() {
		return resources;
	}
		
}
