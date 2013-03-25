package org.gbif.provider.webapp.action.portal;

import java.util.Date;
import java.util.List;

import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.eml.Eml;
import org.gbif.provider.service.EmlManager;
import org.gbif.provider.webapp.action.BaseMetadataResourceAction;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Preparable;

public class ResourceAction extends BaseMetadataResourceAction implements Preparable{
	@Autowired
	private EmlManager emlManager;
	private Eml eml;
	private String format;
    // for feed
    private List<? extends Resource> resources;
    private Date now = new Date();
	private Integer page=1;
	
	public String execute(){
		if (resource!=null){
			eml = emlManager.load(resource);
			return SUCCESS;
		}		
		return RESOURCE404;
	}
	
	public String forward(){
		if (resource instanceof OccurrenceResource) {
			return OCCURRENCE;
		}else if (resource instanceof ChecklistResource) {
			return CHECKLIST;
		}else{
			return METADATA;
		}
	}

	public String list(){
		resource=null;
		resources = getResourceTypeMatchingManager().latest(page, 500);
		return SUCCESS;
	}

	public String rss(){
		resources = resourceManager.latest(page, 25);
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

	public List<? extends Resource> getResources() {
		return resources;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public Date getNow() {
		return now;
	}
		
}
