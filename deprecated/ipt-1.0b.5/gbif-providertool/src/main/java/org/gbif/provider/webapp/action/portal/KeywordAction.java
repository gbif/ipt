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
import org.gbif.provider.service.ResourceKeywordManager;
import org.gbif.provider.webapp.action.BaseAction;
import org.gbif.provider.webapp.action.BaseMetadataResourceAction;
import org.gbif.provider.webapp.action.BaseResourceAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.opensymphony.xwork2.Preparable;

public class KeywordAction extends BaseAction{
	@Autowired
	private ResourceKeywordManager keywordManager;
    private List<String> keywords;
    private Map<String, Integer> tagcloud;
	private String prefix="A";
	
	public String execute(){
		keywords = keywordManager.getKeywords(prefix);
		return SUCCESS;
	}


	
	
	public List<String> getKeywords() {
		return keywords;
	}

	public Map<String, Integer> getTagcloud() {
		return tagcloud;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
			
}
