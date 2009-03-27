package org.gbif.provider.webapp.action.admin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.appfuse.model.LabelValue;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.factory.ResourceFactory;
import org.gbif.provider.model.voc.PublicationStatus;
import org.gbif.provider.model.voc.ResourceType;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.ResizeImage;
import org.gbif.provider.webapp.action.BaseMetadataResourceAction;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Preparable;

public class MetadataAction extends BaseMetadataResourceAction implements Preparable, ServletRequestAware{
	protected List<? extends Resource> resources;
	private Map<String, String> resourceTypeMap = translateI18nMap(new HashMap<String, String>(ResourceType.htmlSelectMap));
	protected HttpServletRequest request;
    
	public String execute(){
		if (resource==null){
			return RESOURCE404;
		}
		return SUCCESS;
	}

	public String list(){
		resource=null;
		resources = resourceManager.getAll();
		return SUCCESS;
	}
	
	public String save(){
		if (resource==null){
			return RESOURCE404;
		}
		if (cancel != null) {
			return "cancel";
		}

		resource.setDirty();
		resource = resourceManager.save(resource);								
		return SUCCESS;
	}

	
	public List<?> getResources() {
		return resources;
	}

	public Map<String, String> getResourceTypeMap() {
		return resourceTypeMap;
	}
	public void setServletRequest(HttpServletRequest request) {
		this.request=request;
	}
}
