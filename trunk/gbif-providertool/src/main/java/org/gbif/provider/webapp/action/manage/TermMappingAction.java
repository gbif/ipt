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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.model.ThesaurusVocabulary;
import org.gbif.provider.model.ViewExtensionMapping;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.service.ExtensionManager;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.service.SourceInspectionManager;
import org.gbif.provider.service.SourceManager;
import org.gbif.provider.service.ThesaurusManager;
import org.gbif.provider.webapp.action.BaseDataResourceAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.opensymphony.xwork2.Preparable;

public class TermMappingAction extends BaseDataResourceAction implements Preparable{
	private static final long serialVersionUID = 14321432161l;
	@Autowired
    private SourceInspectionManager sourceInspectionManager;
	@Autowired
    private SourceManager sourceManager;
	@Autowired
	@Qualifier("viewMappingManager")
    private GenericManager<ViewMappingBase> viewMappingManager;
	@Autowired
	private ExtensionManager extensionManager;
	@Autowired
	@Qualifier("propertyMappingManager")
    private GenericManager<PropertyMapping> propertyMappingManager;
	@Autowired
    private ThesaurusManager thesaurusManager;
	
	// persistent stuff
	private Long pmid;
    private PropertyMapping propMapping;
	// temp stuff

    
	@Override
	public void prepare(){
		super.prepare();
        if (pmid != null) {
    		// get existing property mapping
        	propMapping = propertyMappingManager.get(pmid);
		}
	}

	public String execute(){
		return SUCCESS;
	}

	public String save() throws Exception {
        if (cancel != null) {
            return "cancel";
        }
        if (delete!= null) {
            return delete();
        }
        return SUCCESS;
    }	
	
	public String delete(){
        return SUCCESS;
	}

	public Long getPmid() {
		return pmid;
	}

	public void setPmid(Long pmid) {
		this.pmid = pmid;
	}

	public Long getMid() {
		return propMapping.getViewMapping().getId();
	}

	public PropertyMapping getPropMapping() {
		return propMapping;
	}

	public void setPropMapping(PropertyMapping propMapping) {
		this.propMapping = propMapping;
	}

}
