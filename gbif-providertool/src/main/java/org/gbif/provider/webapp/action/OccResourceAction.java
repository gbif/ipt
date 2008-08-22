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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.struts2.interceptor.SessionAware;
import org.appfuse.model.LabelValue;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.service.CacheManager;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.service.ResourceFactory;
import org.gbif.provider.service.UploadEventManager;
import org.gbif.provider.util.Constants;

import com.opensymphony.xwork2.Preparable;

public class OccResourceAction extends BaseOccurrenceResourceAction implements Preparable, SessionAware {
	private ResourceFactory resourceFactory;
	private GenericManager<Extension> extensionManager;
	private UploadEventManager uploadEventManager;
	private CacheManager cacheManager;
	protected Map session;

	private List<Extension> extensions;
	private List occResources;
	private OccurrenceResource occResource;
	private String gChartData;
	
	private final Map<String, String> jdbcDriverClasses = new HashMap<String, String>()   
	        {  
	            {  
	                put("com.mysql.jdbc.Driver", "MySQL");
	                put("org.postgresql.Driver", "Postrges");
	                put("org.h2.Driver", "H2");
	                put("net.sourceforge.jtds.jdbc.Driver", "MS SQL Server");  
	                put("oracle.jdbc.OracleDriver", "Oracle");  
	                put("org.hsqldb.jdbcDriver", "HSQL");  
	                put("org.apache.derby.jdbc.ClientDriver", "Derby");  
	            }  
	        };  
	  

	public void prepare() throws Exception{
		if (resource_id == null) {
			occResource = resourceFactory.newOccurrenceResourceInstance();
		} else {
			// get resource
			occResource = occResourceManager.get(resource_id);
			
			// update recently viewed resources in session
			LabelValue res = new LabelValue(occResource.getTitle(), resource_id.toString());
			Queue<LabelValue> queue; 
			Object rr = session.get(Constants.RECENT_RESOURCES);
			if (rr != null && rr instanceof Queue){
				queue = (Queue) rr;
			}else{
				queue = new ConcurrentLinkedQueue<LabelValue>(); 
			}
			// remove old entry from queue if it existed before and insert at tail again
			queue.remove(res);
			queue.add(res);
			if (queue.size()>10){
				// only remember last 10 resources
				queue.remove();
			}
			// save back to session
			log.debug("Recently viewed resources: "+queue.toString());
			session.put(Constants.RECENT_RESOURCES, queue);
			}
	}

	public String execute() {
		// create GoogleChart string
		gChartData = uploadEventManager.getGoogleChartData(resource_id, 400, 200);
		// get all availabel extensions for new mappings
		extensions = extensionManager.getAll();
		for (Extension ext : extensions) {
			if (ext.getId().equals(OccurrenceResource.CORE_EXTENSION_ID)) {
				// only show extensions sensu strictu. remove core "extension"
				extensions.remove(ext);
				break;
			}
		}
		// filter already mapped extensions
		for (ViewMappingBase map : occResource.getAllMappings()) {
			extensions.remove(map.getExtension());
		}
		return SUCCESS;
	}

	public String list() {
		occResources = occResourceManager.getResourcesByUser(getCurrentUser().getId());
		return SUCCESS;
	}

	public String edit() {
		return SUCCESS;
	}

	public String save() throws Exception {
		if (cancel != null) {
			return "cancel";
		}
		if (delete != null) {
			return delete();
		}

		boolean isNew = (occResource.getId() == null);
		occResource = occResourceManager.save(occResource);
		String key = (isNew) ? "occResource.added" : "occResource.updated";
		saveMessage(getText(key));
		return SUCCESS;
	}

	public String delete() {
		occResourceManager.remove(occResource.getId());
		saveMessage(getText("occResource.deleted"));
		return "delete";
	}


	
	
	public Map<String, String> getJdbcDriverClasses() {
		return jdbcDriverClasses;
	}

	public void setResourceFactory(ResourceFactory resourceFactory) {
		this.resourceFactory = resourceFactory;
	}

	public void setExtensionManager(GenericManager<Extension> extensionManager) {
		this.extensionManager = extensionManager;
	}

	public void setUploadEventManager(UploadEventManager uploadEventManager) {
		this.uploadEventManager = uploadEventManager;
	}

	public List getOccResources() {
		return occResources;
	}

	public List getExtensions() {
		return extensions;
	}

	public String getGChartData() {
		return gChartData;
	}

	public OccurrenceResource getOccResource() {
		return occResource;
	}

	public void setOccResource(OccurrenceResource occResource) {
		this.occResource = occResource;
	}

	public void setSession(Map session) {
		this.session = session;
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}
	
}