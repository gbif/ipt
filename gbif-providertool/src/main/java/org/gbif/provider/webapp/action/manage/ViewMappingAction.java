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

import java.sql.SQLException;
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

import com.opensymphony.xwork2.Preparable;

public class ViewMappingAction extends BaseOccurrenceResourceAction implements Preparable, SessionAware{
	private static Integer FIXED_TERMS_IDX = 1000;
    private DatasourceInspectionManager datasourceInspectionManager;
    private GenericManager<Extension> extensionManager;
    private GenericManager<ViewMappingBase> viewMappingManager;
    private ViewMappingBase mapping;
    private List<PropertyMapping> mappings;
    private ArrayList<String> viewColumnHeaders;
    private List preview;
    private Map<ExtensionProperty, Map> mapOptions;
    private SortedMap<String, String> columnOptions;
	private Long mapping_id;
	private Long extension_id;
	private OccurrenceResource resource;
    private Set<ViewMappingBase> existingDbMappings;
    private Map session;
		

	@SuppressWarnings("unchecked")
	public void prepare() throws Exception {
		// get resource
		resource = occResourceManager.get(resource_id);

		// get/create view mapping
        if (mapping_id != null) {
        	mapping = viewMappingManager.get(mapping_id);
        }else if (extension_id != null) {
        	mapping = new ViewExtensionMapping();
        	mapping.setResource(resource);
        	mapping.setExtension(extensionManager.get(extension_id));
        }
        
        // prepare list of property mappings to create form with
        mappings = new ArrayList<PropertyMapping>();
        int filledMappings = 0;
        if (mapping != null){
        	for (ExtensionProperty prop : mapping.getExtension().getProperties()){
        		// is this property mapped already?
        		if (mapping.hasMappedProperty(prop)){
        			// add existing mapping
                	mappings.add(mapping.getMappedProperty(prop));
                	filledMappings++;
        		}else{
        			// create new empty one. Remember to link them to the ViewMapping before they get saved
            		PropertyMapping propMap = PropertyMapping.newInstance(prop);
                	mappings.add(propMap);
        		}
        	}
        }
		log.debug(mappings.size() + " mappings prepared with "+filledMappings+" existing ones");

	}
	private void prepareWithDatasource(){
        prepareSourceDataPreview();
		prepareMappingsOptions();
		prepareExistingMappings();
	}
	private void prepareExistingMappings() {
		existingDbMappings = new HashSet<ViewMappingBase>();
		for (ViewMappingBase vm : resource.getAllMappings()){
			if (vm.getSourceSql() != null && vm.getSourceSql().trim().length() > 5){
				existingDbMappings.add(vm);
			}
		}
	}
	private void prepareSourceDataPreview(){
		log.debug("prepareSourceDataPreview");
        // get resultset preview and number of available comuns for mapping
        viewColumnHeaders = new ArrayList<String>();
        if (mapping.getSourceSql() !=null){
			try {
	            // get first 5 rows into list of list for previewing data
	            preview = datasourceInspectionManager.getPreview(mapping.getSourceSql());
	            viewColumnHeaders = (ArrayList<String>) preview.remove(0);
	        } catch (SQLException e) {
	            String msg = getText("mapping.sqlError");
	            saveMessage(msg);
	            log.warn(msg, e);
			}
        }
	}

	private void prepareMappingsOptions() {
		prepareSourceDataPreview();
		log.debug("prepareMappingsOptions for columns "+viewColumnHeaders.toString());
    	// create sorted mapping options for each extension property
		mapOptions = new HashMap<ExtensionProperty, Map>();
        Extension extension = mapping.getExtension();
        // generate basic column mapping options
        columnOptions = new TreeMap<String, String>();
    	if (viewColumnHeaders != null){
    		for (String head : viewColumnHeaders){
    			columnOptions.put(head, head);
    		}
    	}
        for (ExtensionProperty prop : extension.getProperties()){        	
        	//get real controlled vocabulary for properties from db
        	int i=FIXED_TERMS_IDX;
        	SortedMap<String, String> options;
        	if (prop.hasTerms()){
        		// create new option map with controlled vocabulary
            	options = new TreeMap<String, String>(columnOptions);
            	options.put("", "--- Fixed values ---");
            	for (String term : prop.getTerms()){
            		i++;
                	options.put("#"+term, term);
            	}
        	}else{
        		// reuse the basic mapping options
            	options = columnOptions;
        	}
        	mapOptions.put(prop, options);
        }		
	}
	
	public String edit(){
		prepareWithDatasource();
		return SUCCESS;
	}

    public String saveSource() throws Exception {
        if (cancel != null) {
            return "cancel";
        }
        if (delete != null) {
            return delete();
        }
        prepareWithDatasource();
        
        // cascade-save view mapping
        boolean isNew = (mapping.getId() == null);
        mapping = viewMappingManager.save(mapping);
        mapping_id = mapping.getId(); 
        String key = (isNew) ? "mapping.added" : "mapping.updated";
        saveMessage(getText(key));
        return SUCCESS;
    }
	
	public String saveProperties() throws Exception {
        if (cancel != null) {
            return "cancel";
        }
        if (delete != null) {
            return delete();
        }
		prepareWithDatasource();
        // update property mapping values
        for (PropertyMapping pm : mappings){
        	if (pm !=null && pm.getColumn().getColumnName()!=null){
        		String key = pm.getColumn().getColumnName();
        		// copy controlled terms into value property...
        		if (key.startsWith("#")){
        			Map<Integer, String> options = mapOptions.get(pm.getProperty()); 
            		pm.setValue(options.get(key));
            		pm.getColumn().setColumnName(null);
        		}
        	}
        	// save only non-empty property mappings
        	if (!pm.isEmpty()){
        		mapping.addPropertyMapping(pm);
        	}
        }
        // cascade-save view mapping
        boolean isNew = (mapping.getId() == null);
        mapping = viewMappingManager.save(mapping);
        String key = (isNew) ? "viewMapping.added" : "viewMapping.updated";
        saveMessage(getText(key));
        return SUCCESS;
    }

    
    public String delete() {
    	resource.removeExtensionMapping(mapping);
    	viewMappingManager.remove(mapping.getId());
        saveMessage(getText("viewMapping.deleted"));
        return "cancel";
    }

    
    

    

	public void setDatasourceInspectionManager(DatasourceInspectionManager datasourceInspectionManager) {
		this.datasourceInspectionManager = datasourceInspectionManager;
	}

	public void setExtensionManager(GenericManager<Extension> extensionManager) {
		this.extensionManager = extensionManager;
	}

	public void setViewMappingManager(GenericManager<ViewMappingBase> viewMappingManager) {
		this.viewMappingManager = viewMappingManager;
	}

	public OccurrenceResource getResource() {
		return resource;
	}

	public ViewMappingBase getMapping() {
		return mapping;
	}
	
	public void setMapping(ViewMappingBase mapping) {
		this.mapping = mapping;
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

	public Map<ExtensionProperty, Map> getMapOptions() {
		return mapOptions;
	}
	
	public SortedMap<String, String> getColumnOptions() {
		return columnOptions;
	}
	public Set<ViewMappingBase> getExistingDbMappings() {
		return existingDbMappings;
	}
	public void setSession(Map session) {
		this.session=session;
	}
    
}
