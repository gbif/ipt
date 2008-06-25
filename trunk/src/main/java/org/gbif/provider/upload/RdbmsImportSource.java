package org.gbif.provider.upload;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.appfuse.service.GenericManager;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.DwcExtension;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.model.UploadEvent;
import org.gbif.provider.model.ViewMapping;
import org.gbif.provider.service.DatasourceBasedResourceManager;
import org.gbif.provider.service.DatasourceInspectionManager;
import org.gbif.provider.service.UploadManager;

public class RdbmsImportSource implements ImportSource{
	protected static final Log log = LogFactory.getLog(RdbmsImportSource.class);
	// TODO: make it final
	private Collection<PropertyMapping> properties;
	private ResultSet rs;
	private boolean hasNext;
	private Integer coreIdColumnIndex;
	

    public static RdbmsImportSource getInstance(ResultSet rs, ViewMapping view){
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

	
	public Iterator<SourceRow> iterator() {
		return this;
	}

	public boolean hasNext() {
		return hasNext;
	}

	public SourceRow next() {
		SourceRow row = null;
		if (hasNext){
			try {
				row = new SourceRowImpl();
				row.setLocalId(rs.getString(coreIdColumnIndex));
		    	for (PropertyMapping pm : properties){
		    		if (pm.getColumn() != null){
						row.addPropertyValue(pm.getProperty(), rs.getString(pm.getColumn()));
		    		}else if (pm.getValue() != null){
						row.addPropertyValue(pm.getProperty(), pm.getValue());
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
