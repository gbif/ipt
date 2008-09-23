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

package org.gbif.provider.webapp.action.test;

import java.util.List;

import javax.servlet.ServletContext;

import org.apache.struts2.util.ServletContextAware;
import org.gbif.provider.util.Constants;
import org.gbif.provider.webapp.action.BaseAction;
import org.gbif.provider.webapp.action.BaseResourceAction;
import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.service.CacheManager;
import org.gbif.provider.service.GenericResourceManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Preparable;

/**
 * Homepage of the application giving initial statistics and listing imported resources
 * @author markus
 *
 */
public class ResourceTestAction extends BaseResourceAction {
	public String execute(){
		resource=resourceManager.get(Constants.TEST_RESOURCE_ID);
		return SUCCESS;
	}
}
