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

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.datasource.ImportRecord;
import org.gbif.provider.datasource.ImportSource;
import org.gbif.provider.datasource.ImportSourceException;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.model.SourceSql;
import org.gbif.provider.model.ViewCoreMapping;
import org.gbif.provider.model.ViewExtensionMapping;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.service.AnnotationManager;
import org.gbif.provider.service.TermMappingManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Import source for relational databases that maps a sql resultset into CoreRecords and allows to iterate over them.
 * @author markus
 *
 */
public class SqlImportSource implements ImportSource{
	private static final Integer FETCH_SIZE = 1000;
	private static Log log = LogFactory.getLog(SqlImportSource.class);
	@Autowired
	private TermMappingManager termMappingManager;
	@Autowired
	private AnnotationManager annotationManager;

	private Connection conn;
	private Statement stmt;
	private ResultSet rs;
	private String viewSql;

	private boolean hasNext;
	private Integer maxRecords;
	
	private Collection<PropertyMapping> properties;
	private String coreIdColumn;
	private String guidColumn;
	private String linkColumn;
	private String linkTemplate;
	private DataResource resource;
	private Long resourceId;
	// key=header column name, value=term mapping map
	private Map<String, Map<String, String>> vocMap = new HashMap<String, Map<String, String>>();


	public void init(DataResource resource, ViewCoreMapping view) throws ImportSourceException{
    	init(resource, view, null);
    	this.guidColumn = view.getGuidColumn();
    	this.linkColumn = view.getLinkColumn();
    	this.linkTemplate = view.getLinkTemplate();
    }
	public void init(DataResource resource, ViewExtensionMapping view) throws ImportSourceException{
		init(resource, view, null);
    }
	private void init(DataResource resource, ViewMappingBase view, Integer maxRecords) throws ImportSourceException{
		if (!(view.getSource() instanceof SourceSql)){
			throw new IllegalArgumentException("View needs to have a source of type SourceSql ");
		}
		SourceSql src = (SourceSql) view.getSource();
    	// try to connect to db via JDBC
		try {
			DataSource ds = resource.getDatasource();
			if (ds!=null){
				this.conn = ds.getConnection();				
			}else{
				throw new ImportSourceException("Can't connect to database");				
			}
		} catch (SQLException e) {
			throw new ImportSourceException("Can't connect to database", e);
		}
    	//FIXME: clone mappings
		this.resource = resource;
    	this.resourceId=resource.getId();
    	this.viewSql=src.getSql();
    	this.properties = view.getPropertyMappings().values();
    	this.coreIdColumn = view.getCoreIdColumn();
    	this.maxRecords = maxRecords;
    	try {
    		this.stmt = this.conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    		this.stmt.setFetchSize(FETCH_SIZE);       
    		log.debug("SQLImportSource executing for resourceId[" + resource.getId() + "], connection[" + resource.getJdbcUrl() +"], the following SQL: " + this.viewSql);
    		this.rs = this.stmt.executeQuery(this.viewSql);
    		this.hasNext = this.rs.next();
		} catch (SQLException e) {
			annotationManager.annotateResource(resource, "Exception while creating RDBMS resultset import source"+e.toString());
			this.hasNext = false;
			throw new ImportSourceException("Cant init sql result set", e);
		}
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
		return hasNext;
	}

	public ImportRecord next() {
		ImportRecord row = null;
		if (hasNext){
			try {
				row = new ImportRecord(resourceId, rs.getString(coreIdColumn));
				//TODO: the mapping that takes place here should probably be done with a separate mapping class
				if (guidColumn != null){
					row.setGuid(rs.getString(guidColumn));					
				}
				if (linkColumn != null){
					if (linkTemplate.contains(ViewCoreMapping.TEMPLATE_ID_PLACEHOLDER)){
						row.setLink( linkTemplate.replace(ViewCoreMapping.TEMPLATE_ID_PLACEHOLDER, rs.getString(linkColumn)) );
					}else{
						row.setLink(rs.getString(linkColumn));
					}
				}
		    	for (PropertyMapping pm : properties){
		    		if (pm.getColumn() != null && pm.getColumn() != null && !pm.getColumn().startsWith("#")){
		    			String column = pm.getColumn();
	    				String val = rs.getString(column);
	    				// lookup value in term mapping map
	    				if (vocMap.containsKey(column)){
	    					if (vocMap.get(column).containsKey(val)){
	    						val = vocMap.get(column).get(val);
	    					}
		    			}
						row.setPropertyValue(pm.getProperty(), val);
		    		}else if (pm.getValue() != null){
						row.setPropertyValue(pm.getProperty(), pm.getValue());
		    		}
		    	}
			} catch (SQLException e) {
				annotationManager.annotateResource(resource, "Exception while retrieving RDBMS source record"+e.toString());
				hasNext = false;
				row=null;
			}
			
			try {
				// forward rs cursor
				hasNext = rs.next();
				// dont iterate any further if the max number of records to be iterated has been reached
				if (maxRecords != null){
					maxRecords--;					
					if (maxRecords < 0){
						hasNext=false;
						annotationManager.annotateResource(resource, "MaxRecords reached. Stop iterating through ImportSource");
					}
				}
			} catch (SQLException e2) {
				annotationManager.annotateResource(resource, "Exception while iterating RDBMS source"+e2.toString());
				hasNext = false;
			}
		}
		return row;
	}

	public void remove() {
	    throw new UnsupportedOperationException();
	}
	
	public Long getResourceId() {
		return resourceId;
	}

	public void close() {
		hasNext=false;
		try {
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
