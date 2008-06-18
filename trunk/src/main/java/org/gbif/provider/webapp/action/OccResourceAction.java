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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.struts2.interceptor.SessionAware;
import org.appfuse.service.GenericManager;
import org.gbif.provider.datasource.DatasourceInterceptor;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.DwcExtension;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.ViewMapping;
import org.gbif.provider.service.DatasourceInspectionManager;
import org.gbif.provider.webapp.Constants;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Preparable;

public class OccResourceAction extends BaseResourceAction implements Preparable{
    private GenericManager<DwcExtension, Long> dwcExtensionManager;
    private GenericManager<ViewMapping, Long> viewMappingManager;
    private List occResources;
    private List<DwcExtension> extensions;
    private OccurrenceResource occResource;

    public void setDwcExtensionManager(
			GenericManager<DwcExtension, Long> dwcExtensionManager) {
		this.dwcExtensionManager = dwcExtensionManager;
	}

	public void setViewMappingManager(
			GenericManager<ViewMapping, Long> viewMappingManager) {
		this.viewMappingManager = viewMappingManager;
	}
	
	public List getOccResources() {
        return occResources;
    }

	public List getExtensions() {
		return extensions;
	}

	public DatasourceBasedResource getOccResource() {
        return occResource;
    }
    public void setOccResource(OccurrenceResource occResource) {
        this.occResource = occResource;
    }

	public void prepare() {
    	if (getResourceId() != null && !isNew()){
        	occResource = occResourceManager.get(getResourceId());
        }else{
        	occResource = new OccurrenceResource();
        }
    }

	public String execute(){
    	extensions = dwcExtensionManager.getAll();
    	DwcExtension dwcExtension = dwcExtensionManager.get(Constants.DARWIN_CORE_EXTENSION_ID);
		extensions.remove(dwcExtension);
    	// filter already mapped extensions
    	for (ViewMapping map: occResource.getMappings().values()){
			extensions.remove(map.getExtension());
    	}
    	return SUCCESS;
    }

    public String list() {
        occResources = occResourceManager.getAll();
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
        // set new current resource in session
    	session.put(DatasourceInterceptor.SESSION_ATTRIBUTE, occResource.getId());
        return SUCCESS;
    }
    
    public String delete() {
        occResourceManager.remove(occResource.getId());
        saveMessage(getText("occResource.deleted"));
        // remove resource from session
    	session.put(DatasourceInterceptor.SESSION_ATTRIBUTE, null);
        return "delete";
    }

    
}