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
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.model.voc.ExtensionType;
import org.gbif.provider.service.ExtensionManager;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.service.UploadEventManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.opensymphony.xwork2.Preparable;

public class OccMetadataAction extends BaseResourceMetadataAction<OccurrenceResource> implements Preparable, SessionAware {
	@Autowired
	public void setOccResourceManager(OccResourceManager occResourceManager) {
		this.resourceManager = occResourceManager;
	}

	@Override
	protected OccurrenceResource newResource() {
		return resourceFactory.newOccurrenceResourceInstance();
	}
	
	public void setOccurrenceResourceTypes(Map<String, String> occurrenceResourceTypes) {
		this.resourceTypes = translateI18nMap(occurrenceResourceTypes);
	}
}