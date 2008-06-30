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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.appfuse.service.GenericManager;
import org.gbif.provider.datasource.DatasourceInterceptor;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.model.ViewMapping;
import org.gbif.provider.service.DatasourceInspectionManager;
import org.hibernate.type.SortedMapType;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysql.jdbc.DatabaseMetaData;
import com.opensymphony.xwork2.Preparable;

public class ViewMappingAction extends BaseResourceAction implements Preparable{
	private static Integer FIXED_TERMS_IDX = 1000;
    private DatasourceInspectionManager datasourceInspectionManager;
    private GenericManager<Extension, Long> extensionManager;
    private GenericManager<ViewMapping, Long> viewMappingManager;
    private ViewMapping mapping;
    private List<PropertyMapping> mappings;
    private ArrayList<String> viewColumnHeaders;
    private List preview;
    private Map<ExtensionProperty, Map> mapOptions;
    private SortedMap<Integer, String> columnOptions;
	private Long mapping_id;
	private Long extension_id;
		
	public void setDatasourceInspectionManager(
			DatasourceInspectionManager datasourceInspectionManager) {
		this.datasourceInspectionManager = datasourceInspectionManager;
	}

	public void setExtensionManager(GenericManager<Extension, Long> extensionManager) {
		this.extensionManager = extensionManager;
	}

	public void setViewMappingManager(
			GenericManager<ViewMapping, Long> viewMappingManager) {
		this.viewMappingManager = viewMappingManager;
	}

	public ViewMapping getMapping() {
		return mapping;
	}
	
	public void setMapping(ViewMapping mapping) {
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
	
	public SortedMap<Integer, String> getColumnOptions() {
		return columnOptions;
	}

	@SuppressWarnings("unchecked")
	public void prepare() throws Exception {
        if (mapping_id != null) {
        	mapping = viewMappingManager.get(mapping_id);
        }else{
            if (extension_id != null) {
            	mapping = new ViewMapping();
            	mapping.setResource(occResourceManager.get(getResourceId()));
            	mapping.setExtension(extensionManager.get(extension_id));
            }
        }
        
        // prepare list of property mappings to create form with
        mappings = new ArrayList<PropertyMapping>();
        int filledMappings = 0;
    	for (ExtensionProperty prop : mapping.getExtension().getProperties()){
    		// is this property mapped already?
    		if (mapping.hasMappedProperty(prop)){
    			// add existing mapping
            	mappings.add(mapping.getMappedProperty(prop));
            	filledMappings++;
    		}else{
    			// create new empty one. Remember to link them to the ViewMapping before they get saved
        		PropertyMapping propMap = new PropertyMapping();
        		propMap.setProperty(prop);
            	mappings.add(propMap);
    		}
    	}
		log.debug(mappings.size() + " mappings prepared with "+filledMappings+" existing ones");

	}

	private void prepareSourceDataPreview(){
		log.debug("prepareSourceDataPreview");
        // get resultset preview and number of available comuns for mapping
        viewColumnHeaders = new ArrayList<String>();
        if (mapping.getViewSql() !=null){
			try {
	    		ResultSet rs = datasourceInspectionManager.executeViewSql(mapping.getViewSql());
	            ResultSetMetaData meta = rs.getMetaData();
	            int columnNum = meta.getColumnCount();
	            for (int i=1; i<=columnNum; i++){
	            	viewColumnHeaders.add(meta.getTableName(i)+"."+meta.getColumnName(i));
	            }
	            // get first 5 rows into list of list for previewing data
	            preview = new ArrayList();
	            int row=0;
	            while (row < 5 && rs.next()){
	            	row += 1;
	            	List rowList=new ArrayList(columnNum);
	                for (int i=1; i<=columnNum; i++){
	                	rowList.add(rs.getObject(i));
	                }
	                preview.add(rowList);
	            }
	        } catch (SQLException e) {
	            String msg = getText("mapping.sqlError");
	            saveMessage(msg);
	            log.warn(msg);
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
        columnOptions = new TreeMap<Integer, String>();
    	Integer i = 1;
    	if (viewColumnHeaders != null){
    		for (String head : viewColumnHeaders){
    			columnOptions.put(i, "Column "+i+ " ["+head+"]");
    			i++;
    		}
    	}
        for (ExtensionProperty prop : extension.getProperties()){        	
        	//get real controlled vocabulary for properties from db
        	i=FIXED_TERMS_IDX;
        	SortedMap<Integer, String> options;
        	if (prop.hasTerms()){
        		// create new option map with controlled vocabulary
            	options = new TreeMap<Integer, String>(columnOptions);
            	options.put(i, "--- Fixed values ---");
            	for (String term : prop.getTerms()){
            		i++;
                	options.put(i, term);
            	}
        	}else{
        		// reuse the basic mapping options
            	options = columnOptions;
        	}
        	mapOptions.put(prop, options);
        }		
	}
	
	public String editSource(){
        prepareSourceDataPreview();
        return SUCCESS;
	}

    public String saveSource() throws Exception {
        if (cancel != null) {
            return "cancel";
        }
        if (delete != null) {
            return delete();
        }
        prepareSourceDataPreview();
        // cascade-save view mapping
        boolean isNew = (mapping.getId() == null);
        mapping = viewMappingManager.save(mapping);
        String key = (isNew) ? "mapping.added" : "mapping.updated";
        saveMessage(getText(key));
        return SUCCESS;
    }

	public String editProperties(){
		prepareMappingsOptions();
		return SUCCESS;
	}
	
	public String saveProperties() throws Exception {
        if (cancel != null) {
            return "cancel";
        }
		prepareMappingsOptions();
        // update property mapping values
        for (PropertyMapping pm : mappings){
        	if (pm !=null && pm.getColumn()!=null && pm.getColumn() >= 1000){
        		Integer key = pm.getColumn();
        		if (key == 1000){
            		pm.setColumn(null);
        		}else{
        			Map<Integer, String> options = mapOptions.get(pm.getProperty()); 
            		pm.setValue(options.get(key));
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
    	viewMappingManager.remove(mapping.getId());
        saveMessage(getText("viewMapping.deleted"));
        return "cancel";
    }

}
