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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.logging.log.I18nLog;
import org.gbif.logging.log.I18nLogFactory;
import org.gbif.provider.datasource.ImportRecord;
import org.gbif.provider.datasource.ImportSource;
import org.gbif.provider.datasource.ImportSourceException;
import org.gbif.provider.job.OccDbUploadJob;
import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.ViewCoreMapping;
import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.model.ColumnMapping;
import org.gbif.provider.model.ViewMappingBase;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;

/**
 * Import source for relational databases that maps a sql resultset into CoreRecords and allows to iterate over them.
 * @author markus
 *
 */
public class RdbmsImportSource implements ImportSource{
	private static I18nLog log = I18nLogFactory.getLog(OccDbUploadJob.class);

	private Connection conn;
	private Statement stmt;
	private ResultSet rs;
	private String viewSql;

	private boolean hasNext;
	private Integer maxRecords;
	
	private Collection<PropertyMapping> properties;
	private ColumnMapping coreIdColumn = new ColumnMapping();
	private ColumnMapping guidColumn = new ColumnMapping();
	private ColumnMapping linkColumn = new ColumnMapping();
	

    public static RdbmsImportSource newInstance(Connection conn, ViewMappingBase view, Integer maxRecords) throws ImportSourceException{
    	if (conn == null || view == null){
    		throw new NullPointerException();
    	}
    	RdbmsImportSource source = new RdbmsImportSource();
    	source.conn = conn;
    	//FIXME: clone mappings
    	source.viewSql=view.getSourceSql();
    	source.properties = view.getPropertyMappings().values();
    	source.coreIdColumn = view.getCoreIdColumn();
    	source.maxRecords = maxRecords;
    	try {
    		source.stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    		source.stmt.setFetchSize(Integer.MIN_VALUE);       
    		source.rs = source.stmt.executeQuery(source.viewSql);
    		source.hasNext = source.rs.next();
		} catch (SQLException e1) {
			log.error("Exception while creating RDBMS resultset import source", e1);
			source.hasNext = false;
			throw new ImportSourceException("Cant init sql result set", e1);
		}
    	return source;
    }
    
    public static RdbmsImportSource newInstance(Connection conn, ViewMappingBase view) throws ImportSourceException{
    	return newInstance(conn, view, null);
    }
    
    public static RdbmsImportSource newInstance(Connection conn, ViewCoreMapping view, Integer maxRecords) throws ImportSourceException{
    	ViewMappingBase extView = (ViewMappingBase) view;
    	RdbmsImportSource source = RdbmsImportSource.newInstance(conn, extView, maxRecords);
    	source.guidColumn = view.getGuidColumn();
    	source.linkColumn = view.getLinkColumn();
    	return source;
    }
    public static RdbmsImportSource newInstance(Connection conn, ViewCoreMapping view) throws ImportSourceException{
    	return newInstance(conn, view, null);
    }
    
	private RdbmsImportSource() {
		// non instantiable class. use above static factory
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
				row = new ImportRecord();
				//TODO: the mapping that takes place here should probably be done with a separate mapping class
				if (coreIdColumn != null){
					row.setLocalId(rs.getString(coreIdColumn.getColumnName()));	
				}
				if (guidColumn != null){
					row.setGuid(rs.getString(guidColumn.getColumnName()));					
				}
				if (linkColumn != null){
					row.setLink(rs.getString(linkColumn.getColumnName()));
				}
		    	for (PropertyMapping pm : properties){
		    		if (pm.getColumn() != null && pm.getColumn().getColumnName() != null && !pm.getColumn().getColumnName().startsWith("#")){
						row.setPropertyValue(pm.getProperty(), rs.getString(pm.getColumn().getColumnName()));
		    		}else if (pm.getValue() != null){
						row.setPropertyValue(pm.getProperty(), pm.getValue());
		    		}
		    	}
			} catch (SQLException e) {
				log.error("Exception while iterating RDBMS source", e);
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
						log.info("MaxRecords reached. Stop iterating through ImportSource");
					}
				}
			} catch (SQLException e2) {
				log.error("Exception while iterating RDBMS source", e2);
				hasNext = false;
			}
		}
		return row;
	}

	public void remove() {
	    throw new UnsupportedOperationException();
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
