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

package org.gbif.provider.datasource.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.datasource.ImportRecord;
import org.gbif.provider.datasource.ImportSource;
import org.gbif.provider.datasource.ImportSourceException;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.model.SourceFile;
import org.gbif.provider.model.ViewCoreMapping;
import org.gbif.provider.model.ViewExtensionMapping;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.service.AnnotationManager;
import org.gbif.provider.service.TermMappingManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.TabFileReader;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Import source for relational databases that maps a sql resultset into CoreRecords and allows to iterate over them.
 * @author markus
 *
 */
public class FileImportSource implements ImportSource{
	protected final Log log = LogFactory.getLog(getClass());
	@Autowired
	private TermMappingManager termMappingManager;
	@Autowired
	private AnnotationManager annotationManager;
	
	private TabFileReader reader;
	private String[] currentLine;
	// key=header column name
	private Map<String, Integer> headerMap = new HashMap<String, Integer>();
	// key=header column name, value=term mapping map
	private Map<String, Map<String, String>> vocMap = new HashMap<String, Map<String, String>>();

	private Collection<PropertyMapping> properties;
	private String coreIdColumn;
	private String guidColumn;
	private String linkColumn;
	private String linkTemplate;
	private Long resourceId;

	public void init(DataResource resource, ViewCoreMapping view) throws ImportSourceException{
    	ViewMappingBase extView = (ViewMappingBase) view;
    	init(resource, extView);
    	this.guidColumn = view.getGuidColumn();
    	this.linkColumn = view.getLinkColumn();
    	this.linkTemplate = view.getLinkTemplate();
    }
	public void init(DataResource resource, ViewExtensionMapping view) throws ImportSourceException{
    	ViewMappingBase extView = (ViewMappingBase) view;
    	init(resource, extView);
    }
	private void init(DataResource resource, ViewMappingBase view) throws ImportSourceException{
		if (!(view.getSource() instanceof SourceFile)){
			throw new IllegalArgumentException("View needs to have a source of type SourceFile");
		}
		SourceFile src = (SourceFile) view.getSource();
    	// try to setup FileReader
		try {
			this.reader = new TabFileReader(AppConfig.getResourceSourceFile(resource.getId(), src.getFilename()));
			Integer i = 0;
			for (String h : this.reader.getHeader()){
				this.headerMap.put(h, i);
				i++;
			}
		} catch (Exception e) {
			throw new ImportSourceException("Cant read source file "+src.getFilename(), e);
		}
    	//FIXME: clone mappings
		this.resourceId=resource.getId();
		this.properties = view.getPropertyMappings().values();
		this.coreIdColumn = view.getCoreIdColumn();
		// see if term mappings exist and keep them in vocMap in that case
		for (PropertyMapping pm : this.properties){
			Map<String, String> tmap = termMappingManager.getMappingMap(pm.getTermTransformationId());
			if (!tmap.isEmpty()){
				vocMap.put(pm.getColumn(), tmap);
			}
    	}
    }

    
    
    
	public Iterator<ImportRecord> iterator() {
		return this;
	}

	public boolean hasNext() {
		return reader.hasNext();
	}

	private String getCurrentValue(String columnName){
		if (headerMap.containsKey(columnName)){
			return currentLine[headerMap.get(columnName)];
		}else{
			return null;
		}
	}
	public ImportRecord next() {
		ImportRecord row = null;
		currentLine = reader.next();
		if (hasNext()){
			try {
				row = new ImportRecord(resourceId, getCurrentValue(coreIdColumn));
				//TODO: the mapping that takes place here should probably be done with a separate mapping class
				//
				if (guidColumn != null){
					row.setGuid(getCurrentValue(guidColumn));					
				}
				if (linkColumn != null){
					if (linkTemplate.contains(ViewCoreMapping.TEMPLATE_ID_PLACEHOLDER)){
						row.setLink( linkTemplate.replace(ViewCoreMapping.TEMPLATE_ID_PLACEHOLDER, getCurrentValue(linkColumn)) );
					}else{
						row.setLink(getCurrentValue(linkColumn));
					}
				}
		    	for (PropertyMapping pm : properties){
		    		if (StringUtils.trimToNull(pm.getColumn()) != null){
		    			String column = pm.getColumn();
	    				String val = getCurrentValue(column);
	    				// lookup value in term mapping map
	    				if (vocMap.containsKey(column)){
	    					if (vocMap.get(column).containsKey(val)){
	    						val = vocMap.get(column).get(val);
	    					}
		    			}
						row.setPropertyValue(pm.getProperty(), val);
		    		}else if (StringUtils.trimToNull(pm.getValue()) != null){
						row.setPropertyValue(pm.getProperty(), pm.getValue());
		    		}
		    	}
			} catch (Exception e) {
				log.warn("FileImportSource empty row because of error",e);
				row=null;
			}
			
		}
		return row;
	}

	public Long getResourceId() {
		return resourceId;
	}

	public void remove() {
	    throw new UnsupportedOperationException();
	}
	
	public void close() {
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
