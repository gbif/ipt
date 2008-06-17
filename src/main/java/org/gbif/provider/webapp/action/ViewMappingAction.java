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
import org.gbif.provider.model.DwcExtension;
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
	private static Long FIXED_TERMS_IDX = 1000L;
    private DatasourceInspectionManager datasourceInspectionManager;
    private GenericManager<DwcExtension, Long> dwcExtensionManager;
    private GenericManager<ViewMapping, Long> viewMappingManager;
    private ViewMapping mapping;
    private List<PropertyMapping> mappings;
    private ArrayList<String> viewColumnHeaders;
    private List preview;
    private Map<ExtensionProperty, Map>mapOptions;
	private Long mapping_id;
	private Long extension_id;
		
	public void setDatasourceInspectionManager(
			DatasourceInspectionManager datasourceInspectionManager) {
		this.datasourceInspectionManager = datasourceInspectionManager;
	}

	public void setDwcExtensionManager(
			GenericManager<DwcExtension, Long> dwcExtensionManager) {
		this.dwcExtensionManager = dwcExtensionManager;
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

	
	@SuppressWarnings("unchecked")
	public void prepare() throws Exception {
        if (mapping_id != null) {
        	mapping = viewMappingManager.get(mapping_id);
        }else{
            if (extension_id != null) {
            	mapping = new ViewMapping();
            	mapping.setResource(occResourceManager.get(getResourceId()));
            	mapping.setExtension(dwcExtensionManager.get(extension_id));
        		// initializse empty propertyMappings
            	for (ExtensionProperty prop : mapping.getExtension().getProperties()){
            		PropertyMapping propMap = new PropertyMapping();
            		propMap.setProperty(prop);
            		mapping.addPropertyMapping(propMap);
            	}
            }
        }
        
        // prepare list of property mappings to create form with
        mappings = mapping.getPropertyMappings();
        
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
	            String msg = getText("viewMapping.sqlError");
	            saveMessage(msg);
	            log.warn(msg);
			}


        }

        // create mapping options
        mapOptions = new HashMap<ExtensionProperty, Map>();
        DwcExtension extension = mapping.getExtension();
        for (ExtensionProperty prop : extension.getProperties()){
        	SortedMap<Long, String> options = new TreeMap();
            // add column mapping options
        	Long i = 1L;
        	if (viewColumnHeaders != null){
        		for (String head : viewColumnHeaders){
                	options.put(i, "Column "+i+ " ["+head+"]");
        			i++;
        		}
        	}
        	//get real controlled vocabulary for properties from db
        	i=FIXED_TERMS_IDX;
        	if (prop.hasTerms()){
            	options.put(i, "--- Fixed values ---");
            	for (String term : prop.getTerms()){
            		i++;
                	options.put(i, term);
            	}
        	}
        	
        	mapOptions.put(prop, options);
        }
	}
	
	
	public String edit(){
        return SUCCESS;
	}
        
    public String save() throws Exception {
        if (cancel != null) {
            return "cancel";
        }
        if (delete != null) {
            return delete();
        }
        // update property mapping values
        for (PropertyMapping pm : mapping.getPropertyMappings()){
        	if (pm !=null && pm.getColumn()!=null && pm.getColumn() == 1000L){
        		pm.setColumn(null);
        	}
        }
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


    public String testSql(){
    	//datasourceInspectionManager.executeViewSql(mapping.getViewSql());
    	return null;
    }
}
