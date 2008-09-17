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

import org.apache.struts2.interceptor.SessionAware;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.model.ViewExtensionMapping;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.service.DatasourceInspectionManager;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.webapp.action.BaseOccurrenceResourceAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.opensymphony.xwork2.Preparable;

public class PropertyMappingAction extends BaseOccurrenceResourceAction implements Preparable, SessionAware{
	private static Integer FIXED_TERMS_IDX = 1000;
	@Autowired
    private DatasourceInspectionManager datasourceInspectionManager;
	@Autowired
	@Qualifier("viewMappingManager")
    private GenericManager<ViewMappingBase> viewMappingManager;
	// persistent stuff
	private Long mapping_id;
	private Long extension_id;
	private ViewMappingBase view;
	private OccurrenceResource resource;
    private List<PropertyMapping> mappings;
	// temp stuff
    private List<String> columnOptions;
    private Map<Long, List> mapOptions;
    private List<String> viewColumnHeaders;
    private List preview;
    private Map session;
		

	@SuppressWarnings("unchecked")
	public void prepare() throws Exception {
		assert(resource_id!=null && mapping_id!=null);
		// get resource & view
		resource = occResourceManager.get(resource_id);
    	view = viewMappingManager.get(mapping_id);
    	assert view.hasValidSource();
    	
        // prepare list of property mappings to create form with and to be filled
		mappings = new ArrayList<PropertyMapping>();
        int filledMappings = 0;
    	for (ExtensionProperty prop : view.getExtension().getProperties()){
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
	
	private void prepareUI(){
        // generate basic column mapping options based on source headers
		columnOptions = new ArrayList<String>();
		try {
			columnOptions = datasourceInspectionManager.getHeader(view);
		} catch (Exception e) {
			log.debug("Cant read datasource column headers", e);
		}

    	// create specific, sorted mapping options for each extension property based on columnOptions & controlled vocabulary
		mapOptions = new HashMap<Long, List>();
        for (ExtensionProperty prop : view.getExtension().getProperties()){        	
        	//get real controlled vocabulary for properties from db
        	int i=FIXED_TERMS_IDX;
        	List<String> options;
        	if (prop.hasTerms()){
        		// create new option map with controlled vocabulary
            	options = new ArrayList<String>();
            	options.add("--- Fixed values ---");
            	for (String term : prop.getTerms()){
            		i++;
                	options.add("#"+term);
            	}
            	options.add("--- Source columns ---");
            	options.addAll(columnOptions);
        	}else{
        		// reuse the basic mapping options
            	options = columnOptions;
        	}
        	mapOptions.put(prop.getId(), options);
        }		
	}
	

	public String sourcePreview(){
		log.debug("prepareSourceDataPreview");
        // get resultset preview and number of available comuns for mapping
        viewColumnHeaders = new ArrayList<String>();
		try {
            // get first 5 rows into list of list for previewing data
            preview = datasourceInspectionManager.getPreview(view);
            viewColumnHeaders = (List<String>) preview.remove(0);
        } catch (Exception e) {
            String msg = getText("mapping.sqlError");
            saveMessage(msg);
            log.warn(msg, e);
		}
		return SUCCESS;
	}
	
	public String uploadPreview(){
		return SUCCESS;
	}
	
	public String edit(){
		prepareUI();
		return SUCCESS;
	}
	
	public String save() throws Exception {
        if (cancel != null) {
            return "cancel";
        }
		prepareUI();
        // update property mapping values
        for (PropertyMapping pm : mappings){
        	if (pm !=null && pm.getColumn().getColumnName()!=null){
        		String key = pm.getColumn().getColumnName();
        		// copy controlled terms into value property...
        		if (key.startsWith("#")){
            		pm.setValue(key.substring(1));
            		pm.getColumn().setColumnName(null);
        		}
        	}
        	// save only non-empty property mappings
        	if (!pm.isEmpty()){
        		view.addPropertyMapping(pm);
        	}
        }
        // cascade-save view mapping
        view = viewMappingManager.save(view);
        saveMessage(getText("viewMapping.updated"));
        return SUCCESS;
    }

    

	
	

    

	public OccurrenceResource getResource() {
		return resource;
	}

	public List<PropertyMapping> getMappings() {
		return mappings;
	}
	public void setMappings(List<PropertyMapping> mappings) {
		this.mappings = mappings;
	}

	public Long getMapping_id() {
		return mapping_id;
	}

	public void setMapping_id(Long mapping_id) {
		this.mapping_id = mapping_id;
	}

	public Long getExtension_id() {
		return extension_id;
	}

	public void setExtension_id(Long extension_id) {
		this.extension_id = extension_id;
	}
	
	public List<String> getViewColumnHeaders() {
		return viewColumnHeaders;
	}

	public List getPreview() {
		return preview;
	}

	public void setSession(Map session) {
		this.session=session;
	}

	public List<String> getColumnOptions() {
		return columnOptions;
	}

	public Map<Long, List> getMapOptions() {
		return mapOptions;
	}

	public ViewMappingBase getView() {
		return view;
	}
}
