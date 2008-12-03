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
import org.gbif.provider.model.ViewExtensionMapping;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.service.ExtensionManager;
import org.gbif.provider.service.SourceInspectionManager;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.service.SourceManager;
import org.gbif.provider.webapp.action.BaseDataResourceAction;
import org.gbif.provider.webapp.action.BaseOccurrenceResourceAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.opensymphony.xwork2.Preparable;

public class PropertyMappingAction extends BaseDataResourceAction implements Preparable{
	private static Integer FIXED_TERMS_IDX = 1000;
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
	
	// persistent stuff
	private Long mid;
	private Long eid;
	private Long sid;
	private ViewMappingBase view;
    private List<PropertyMapping> mappings;
	// temp stuff
    private List<String> sourceColumns;
		

	@Override
	public void prepare(){
		super.prepare();
        if (mid != null) {
    		// get existing view mapping
        	view = viewMappingManager.get(mid);
        	if (view.getSource()==null && sid != null){
        		// this is probably the default core mapping without a source assigned yet.
            	view.setSource(sourceManager.get(sid));
            	viewMappingManager.save(view);
        	}
        }else if (eid != null && sid != null) {
        	// create new view mapping
        	view = new ViewExtensionMapping();
        	view.setResource(resource);
        	view.setExtension(extensionManager.get(eid));
        	view.setSource(sourceManager.get(sid));
        	viewMappingManager.save(view);
        	mid = view.getId();
        }else{
        	log.warn("No view mapping could be loaded or created");
        }
        // generate basic column mapping options
		try {
	        sourceColumns = sourceInspectionManager.getHeader(view.getSource());
		} catch (Exception e) {
			sourceColumns = new ArrayList<String>();
			log.debug("Cant read datasource column headers", e);
		}
        // prepare list of property mappings to create form with and to be filled by params interceptor
		mappings = new ArrayList<PropertyMapping>();
        int filledMappings = 0;
    	for (ExtensionProperty prop : view.getExtension().getProperties()){
        	if (prop == null){
        		continue;
        	}
    		// is this property mapped already?
    		if (view.hasMappedProperty(prop)){
    			// add existing mapping
            	mappings.add(view.getMappedProperty(prop));
            	filledMappings++;
    		}else{
    			// create new empty one. Remember to link them to the ViewMapping before they get saved
        		PropertyMapping propMap = PropertyMapping.newInstance(prop);
            	mappings.add(propMap);
    		}
    	}
		log.debug(mappings.size() + " mappings prepared with "+filledMappings+" existing ones");
        
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
        // update property mapping values
        for (PropertyMapping pm : mappings){
        	if (pm !=null && pm.getColumn() !=null){
        		String key = StringUtils.trimToEmpty(pm.getColumn().getColumnName());
        	}
        	// save non-empty property mappings
        	if (!pm.isEmpty()){
        		view.addPropertyMapping(pm);
        	// and remove empty ones that are still persistent
        	}else if(pm.getId()!=null){
        		view.removePropertyMapping(pm);
        		propertyMappingManager.remove(pm);
        	}
        }
        // cascade-save view mapping
        view = viewMappingManager.save(view);
        return SUCCESS;
    }	
	
	public String delete(){
		resource.removeExtensionMapping(view);
        viewMappingManager.remove(view);
        return SUCCESS;
	}
	
	

	public List<PropertyMapping> getMappings() {
		return mappings;
	}
	public void setMappings(List<PropertyMapping> mappings) {
		this.mappings = mappings;
	}

	public Long getMid() {
		return mid;
	}

	public void setMid(Long mapping_id) {
		this.mid = mapping_id;
	}

	public Long getEid() {
		return eid;
	}

	public void setEid(Long extension_id) {
		this.eid = extension_id;
	}
	
	public List<String> getColumnOptions() {
		return sourceColumns;
	}

	public ViewMappingBase getView() {
		return view;
	}

	public List<String> getSourceColumns() {
		return sourceColumns;
	}

	public Long getSid() {
		return sid;
	}

	public void setSid(Long sid) {
		this.sid = sid;
	}
	
}
