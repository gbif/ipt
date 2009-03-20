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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.interceptor.SessionAware;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.model.SourceBase;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.voc.ExtensionType;
import org.gbif.provider.service.ExtensionManager;
import org.gbif.provider.service.SourceInspectionManager;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.service.SourceManager;
import org.gbif.provider.webapp.action.BaseDataResourceAction;
import org.gbif.provider.webapp.action.BaseOccurrenceResourceAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.opensymphony.xwork2.Preparable;

public class MappingAction extends BaseDataResourceAction implements Preparable{
	@Autowired
    private SourceManager sourceManager;
	@Autowired
    private ExtensionManager extensionManager;
	private List<Extension> extensions;
	private List<SourceBase> sources;
	private List<ExtensionMapping> extMappings;
	private ExtensionMapping coreMapping;
		

	@Override
	public void prepare(){
		super.prepare();
		sources = sourceManager.getAll(resource_id);
		coreMapping = resource.getCoreMapping();
		extMappings = resource.getExtensionMappings();
		extensions = extensionManager.getInstalledExtensions();
		// filter already mapped extensions
		for (ExtensionMapping map : resource.getAllMappings()) {
			extensions.remove(map.getExtension());
		}
	}

	public String execute(){
		if (resource==null){
			return RESOURCE404;
		}
		return SUCCESS;
	}
	
	
	
	public List<Extension> getExtensions() {
		return extensions;
	}
	public List<SourceBase> getSources() {
		return sources;
	}
	public List<ExtensionMapping> getExtMappings() {
		return extMappings;
	}
	public ExtensionMapping getCoreMapping() {
		return coreMapping;
	}	
}
