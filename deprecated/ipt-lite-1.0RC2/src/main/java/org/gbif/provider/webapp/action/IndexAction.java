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

import java.util.Date;
import java.util.List;

import org.appfuse.service.UserManager;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.util.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Homepage of the application giving initial statistics and listing imported resources
 * @author markus
 *
 */
public class IndexAction extends BaseAction{
    @Autowired
    @Qualifier("resourceManager")
    protected GenericResourceManager<Resource> metaResourceManager;
    private List<Resource> resources;
	
	public String execute(){
		resources = metaResourceManager.getPublishedResources();
		return SUCCESS;
	}
	
	public String about(){
		return SUCCESS;
	}

	
	public List<Resource> getResources() {
		return resources;
	}

	public AppConfig getCfg() {
		return cfg;
	}

}
