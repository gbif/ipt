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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.datasource.ImportRecord;
import org.gbif.provider.datasource.ImportSource;
import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.CoreViewMapping;
import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.model.ViewMapping;

/**
 * Import source for relational databases that maps a sql resultset into CoreRecords and allows to iterate over them.
 * @author markus
 *
 */
public class RdbmsImportSource implements ImportSource{
	protected static final Log log = LogFactory.getLog(RdbmsImportSource.class);
	// TODO: make it final
	private Collection<PropertyMapping> properties;
	private ResultSet rs;
	private boolean hasNext;
	private Integer coreIdColumnIndex;
	private Integer guidColumnIndex;
	private Integer linkColumnIndex;
	

    public static RdbmsImportSource newInstance(ResultSet rs, ViewMapping view){
    	if (rs == null || view == null){
    		throw new IllegalArgumentException();
    	}
    	RdbmsImportSource source = new RdbmsImportSource();
    	source.rs = rs;
    	source.properties = view.getPropertyMappings().values();
    	source.coreIdColumnIndex = view.getCoreIdColumnIndex();
    	try {
    		source.hasNext = rs.next();
		} catch (SQLException e) {
			log.error("Exception while creating RDBMS source", e);
			source.hasNext = false;
		}
    	return source;
    }
    
    public static RdbmsImportSource newInstance(ResultSet rs, CoreViewMapping view){
    	ViewMapping extView = (ViewMapping) view;
    	RdbmsImportSource source = RdbmsImportSource.newInstance(rs, extView);
    	source.guidColumnIndex = view.getGuidColumnIndex();
    	source.linkColumnIndex = view.getLinkColumnIndex();
    	return source;
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
				if (coreIdColumnIndex != null){
					row.setLocalId(rs.getString(coreIdColumnIndex));	
				}
				if (guidColumnIndex != null){
					row.setGuid(rs.getString(guidColumnIndex));					
				}
				if (linkColumnIndex != null){
					row.setLink(rs.getString(linkColumnIndex));
				}
		    	for (PropertyMapping pm : properties){
		    		if (pm.getColumn() != null){
						row.setPropertyValue(pm.getProperty(), rs.getString(pm.getColumn()));
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

}
