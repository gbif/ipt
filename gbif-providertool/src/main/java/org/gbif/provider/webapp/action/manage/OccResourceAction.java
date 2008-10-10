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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.struts2.interceptor.SessionAware;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.model.voc.ExtensionType;
import org.gbif.provider.service.ExtensionManager;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.service.UploadEventManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Preparable;

public class OccResourceAction extends BaseResourceAction<OccurrenceResource> implements Preparable, SessionAware {
	@Autowired
	private ExtensionManager extensionManager;
	@Autowired
	private UploadEventManager uploadEventManager;

	private List<Extension> extensions;
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
        
	@Autowired
	public void sssutResourceManager(OccResourceManager occResourceManager) {
		this.resourceManager = occResourceManager;
	}

	@Override
	protected OccurrenceResource newResource() {
		return resourceFactory.newOccurrenceResourceInstance();
	}

	@Override
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
		for (ViewMappingBase map : resource.getAllMappings()) {
			extensions.remove(map.getExtension());
		}
		return SUCCESS;
	}

	@Override
	public String list() {
		resources = resourceManager.getResourcesByUser(getCurrentUser().getId());
		return SUCCESS;
	}

	public String edit() {
		return SUCCESS;
	}


	
	
	public Map<String, String> getJdbcDriverClasses() {
		return jdbcDriverClasses;
	}

	public List getExtensions() {
		return extensions;
	}

	public String getGChartData() {
		return gChartData;
	}

}