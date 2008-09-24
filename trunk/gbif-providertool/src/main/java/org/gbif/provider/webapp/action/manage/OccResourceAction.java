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

package org.gbif.provider.webapp.action.manage;

import java.io.File;
import java.io.IOException;
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
import org.gbif.provider.model.voc.ExtensionType;
import org.gbif.provider.service.ExtensionManager;
import org.gbif.provider.service.ResourceFactory;
import org.gbif.provider.service.UploadEventManager;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.ResizeImage;
import org.gbif.provider.webapp.action.BaseOccurrenceResourceAction;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Preparable;

public class OccResourceAction extends BaseOccurrenceResourceAction implements Preparable, SessionAware {
	@Autowired
	private ResourceFactory resourceFactory;
	@Autowired
	private ExtensionManager extensionManager;
	@Autowired
	private UploadEventManager uploadEventManager;
	protected Map session;

	private List<Extension> extensions;
	private List occResources;
	private OccurrenceResource occResource;
	private String gChartData;

    private File file;
    private String fileContentType;
    private String fileFileName;

    
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
		// get all installed extensions for mappings
		extensions = extensionManager.getAllInstalled(ExtensionType.Occurrence);
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
		try{
			uploadLogo();
	        saveMessage(getText("resource.logoUploaded"));
		}catch (IOException e){
			saveMessage("Error uploading the logo file");
		}
		return SUCCESS;
	}

	public void uploadLogo() throws IOException{
        if ("".equals(fileFileName) || file == null) {
        	return;
        }
        // final logo destination
		File logoFile = cfg.getResourceLogoFile(resource_id);
        ResizeImage.resizeImage(file, logoFile, Constants.LOGO_SIZE, Constants.LOGO_SIZE);

		log.info(String.format("Logo %s uploaded and resized for resource %s", logoFile.getAbsolutePath(), resource_id));
	}
	
	public String delete() {
		occResourceManager.remove(occResource);
		saveMessage(getText("occResource.deleted"));

		// update recently viewed resources in session
		Object previousQueue = session.get(Constants.RECENT_RESOURCES);
		if (previousQueue != null && previousQueue instanceof Queue){
			Queue<LabelValue> queue = (Queue) previousQueue;
			LabelValue res = new LabelValue(occResource.getTitle(), resource_id.toString());
			// remove entry from queue if it existed before
			queue.remove(res);
			// save back to session
			session.put(Constants.RECENT_RESOURCES, queue);
		}		
		return "delete";
	}


	
	
	public Map<String, String> getJdbcDriverClasses() {
		return jdbcDriverClasses;
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

    public void setFile(File file) {
        this.file = file;
    }

    public void setFileContentType(String fileContentType) {
        this.fileContentType = fileContentType;
    }

    public void setFileFileName(String fileFileName) {
        this.fileFileName = fileFileName;
    }

    public File getFile() {
        return file;
    }

    public String getFileContentType() {
        return fileContentType;
    }

    public String getFileFileName() {
        return fileFileName;
    }
}