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
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.gbif.logging.log.I18nLog;
import org.gbif.logging.log.I18nLogFactory;
import org.gbif.provider.datasource.ImportRecord;
import org.gbif.provider.datasource.ImportSource;
import org.gbif.provider.datasource.ImportSourceException;
import org.gbif.provider.model.ColumnMapping;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.model.SourceBase;
import org.gbif.provider.model.SourceFile;
import org.gbif.provider.model.ViewCoreMapping;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.TabFileReader;

/**
 * Import source for relational databases that maps a sql resultset into CoreRecords and allows to iterate over them.
 * @author markus
 *
 */
public class FileImportSource implements ImportSource{
	private static I18nLog log = I18nLogFactory.getLog(FileImportSource.class);

	private TabFileReader reader;
	private String[] currentLine;
	private Map<String, Integer> headerMap = new HashMap<String, Integer>();

	private Collection<PropertyMapping> properties;
	private ColumnMapping coreIdColumn;
	private ColumnMapping guidColumn;
	private ColumnMapping linkColumn;
	private Long resourceId;

    protected static FileImportSource newInstance(DataResource resource, ViewMappingBase view) throws ImportSourceException{
		if (!(view.getSource() instanceof SourceFile)){
			throw new IllegalArgumentException("View needs to have a source of type SourceFile");
		}
		SourceFile src = (SourceFile) view.getSource();
    	FileImportSource source = new FileImportSource();
    	// try to setup FileReader
		try {
			source.reader = new TabFileReader(AppConfig.getResourceSourceFile(resource.getId(), src.getFilename()));
			Integer i = 0;
			for (String h : source.reader.getHeader()){
				source.headerMap.put(h, i);
				i++;
			}
		} catch (Exception e) {
			throw new ImportSourceException("Cant read source file "+src.getFilename(), e);
		}
    	//FIXME: clone mappings
    	source.resourceId=resource.getId();
    	source.properties = view.getPropertyMappings().values();
    	source.coreIdColumn = view.getCoreIdColumn();
    	return source;
    }
    
    protected static FileImportSource newInstance(DataResource resource, ViewCoreMapping view) throws ImportSourceException{
    	ViewMappingBase extView = (ViewMappingBase) view;
    	FileImportSource source = FileImportSource.newInstance(resource, extView);
    	source.guidColumn = view.getGuidColumn();
    	source.linkColumn = view.getLinkColumn();
    	return source;
    }
    
	FileImportSource() {
		// non instantiable class. use above static factory
	}
	
	public Iterator<ImportRecord> iterator() {
		return this;
	}

	public boolean hasNext() {
		return reader.hasNext();
	}

	private String getCurrentValue(String columnName){
		Integer col = headerMap.get(columnName);		
		return currentLine[col];
	}
	public ImportRecord next() {
		ImportRecord row = null;
		currentLine = reader.next();
		if (hasNext()){
			try {
				row = new ImportRecord(resourceId, getCurrentValue(coreIdColumn.getColumnName()));
				//TODO: the mapping that takes place here should probably be done with a separate mapping class
				if (guidColumn != null){
					row.setGuid(getCurrentValue(guidColumn.getColumnName()));					
				}
				if (linkColumn != null){
					row.setLink(getCurrentValue(linkColumn.getColumnName()));
				}
		    	for (PropertyMapping pm : properties){
		    		if (pm.getColumn() != null && pm.getColumn().getColumnName() != null && !pm.getColumn().getColumnName().startsWith("#")){
						row.setPropertyValue(pm.getProperty(), getCurrentValue(pm.getColumn().getColumnName()));
		    		}else if (pm.getValue() != null){
						row.setPropertyValue(pm.getProperty(), pm.getValue());
		    		}
		    	}
			} catch (Exception e) {
				log.error("Exception while retrieving FILE source record", e);
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
