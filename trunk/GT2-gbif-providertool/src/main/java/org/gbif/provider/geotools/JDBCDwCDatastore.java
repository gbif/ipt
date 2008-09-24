/***************************************************************************
 * Copyright (C) 2005 Global Biodiversity Information Facility Secretariat.
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
package org.gbif.provider.geotools;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.geotools.data.AbstractDataStore;
import org.geotools.data.DefaultFeatureReader;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.feature.AttributeType;
import org.geotools.feature.DefaultFeatureTypeFactory;
import org.geotools.feature.FeatureType;
import org.geotools.feature.SchemaException;
import org.geotools.feature.type.GeometricAttributeType;
import org.geotools.feature.type.TextualAttributeType;
import org.geotools.filter.Filter;
import org.geotools.filter.Filters;

import com.vividsolutions.jts.geom.Point;

/**
 * A simple JDBC property store that connects to a DwC table
 * and maps a Float latitude and longitude to the output
 * @author trobertson
 */
public class JDBCDwCDatastore extends AbstractDataStore {
	protected Log log = LogFactory.getLog(this.getClass());

	/**
	 * The feature types
	 */
	protected static AttributeType[] types = {
		new TextualAttributeType("ResourceId", false, 1, 1, "0", Filter.INCLUDE),
		new TextualAttributeType("GUID", true, 1, 1, "0", Filter.INCLUDE),
		new TextualAttributeType("TaxonId", true, 1, 1, "0", Filter.INCLUDE),
		new TextualAttributeType("RegionId", true, 1, 1, "0", Filter.INCLUDE),
		new TextualAttributeType("ScientificName", true, 1, 1, "0", Filter.INCLUDE),
		new TextualAttributeType("Locality", true, 1, 1, "0", Filter.INCLUDE),
		new TextualAttributeType("InstitutionCode", true, 1, 1, "0", Filter.INCLUDE),
		new TextualAttributeType("CollectionCode", true, 1, 1, "0", Filter.INCLUDE),
		new TextualAttributeType("CatalogNumber", true, 1, 1, "0", Filter.INCLUDE),
		new TextualAttributeType("Collector", true, 1, 1, "0", Filter.INCLUDE),
		new TextualAttributeType("DateCollected", true, 1, 1, "0", Filter.INCLUDE),
		new TextualAttributeType("BasisOfRecord", true, 1, 1, "0", Filter.INCLUDE),
		new GeometricAttributeType("Geom", Point.class, false, null, null, null)};		
	
	
	/**
	 * The singleton DAO
	 */
	protected static Dao dao;
		
	public JDBCDwCDatastore(Map<String, String> params) {
		log.info("Creating database pool [" + params.get("url") +"] [" + params.get("user") + "]");
		setupPool(params);
	}
	
	/**
	 * @param params The parameters for the DB connection
	 */
	protected synchronized static void setupPool(Map<String, String> params) {
		try {
			// TODO - move to UI driven config (e.g. passed in params from the factory)
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
		}
        ObjectPool connectionPool = new GenericObjectPool(null);
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(params.get("url"),params.get("user"), params.get("password"));
        // this is unused but it takes in the connection pool so I presume that this does the callback to set the factory for the pool 
        @SuppressWarnings("unused") PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,connectionPool,null,null,false,true);
        DataSource ds = new PoolingDataSource(connectionPool);
        dao = new Dao();
        dao.setDataSource(ds);
    }		
	
	/**
	 * @see org.geotools.data.AbstractDataStore#getFeatureReader(java.lang.String)
	 */
	@Override
	protected FeatureReader getFeatureReader(String typeName) throws IOException {
		return getFeatureReader(typeName, null);
	}
	
	/**
	 * @see org.geotools.data.AbstractDataStore#getFeatureReader(java.lang.String)
	 */
	@Override
	protected FeatureReader getFeatureReader(String typeName, Query query) throws IOException {
		log.info("Filter supplied: " + query.getFilter());
		
		// parse out the values from the query
		OGCQueryVisitor parsedQuery = new OGCQueryVisitor();
		Filters.accept( query.getFilter(), parsedQuery );
		
		List<DwcRecord> records = dao.getRecords(
				parsedQuery.getResourceId(), 
				parsedQuery.getGuid(), 
				parsedQuery.getTaxonId(), 
				parsedQuery.getRegionId(), 
				parsedQuery.getScientificName(), 
				parsedQuery.getLocality(), 
				parsedQuery.getInstitutionCode(), 
				parsedQuery.getCollectionCode(), 
				parsedQuery.getCatalogNumber(), 
				parsedQuery.getCollector(), 
				parsedQuery.getDateCollected(), 
				parsedQuery.getBasisOfRecord(), 
				parsedQuery.getMinY(), 
				parsedQuery.getMaxY(), 
				parsedQuery.getMinX(), 
				parsedQuery.getMaxX(), 
				1000); // TODO - pass in in the factory...
		
		try {
			return new DefaultFeatureReader(new DwcRecordFeatureAttributeReader(records));
		} catch (SchemaException e1) {
			// Should not happen
			throw new IOException("Unable to get data - schema is corrupt: " + e1.getMessage());
		}
	}

	/**
	 * Returns the schema that is supported
	 * Geotools is a constantly developing project and there is deprecated code
	 * without an obvious replacement
	 * @see org.geotools.data.AbstractDataStore#getSchema(java.lang.String)
	 */
	@SuppressWarnings("deprecation")
	@Override
	public FeatureType getSchema(String typeName) throws IOException {
		DefaultFeatureTypeFactory factory = new DefaultFeatureTypeFactory();
		for (AttributeType type : JDBCDwCDatastore.types) {
			factory.addType(type);
		}
		factory.setAbstract(false);
		factory.setName("occurrence");
		try {
			return factory.getFeatureType();
		} catch (SchemaException e) {
			log.error("Error building the feature type in the getSchema(): " + e.getMessage(), e);
			return null;
		}
	}

	/**
	 * These are not the feature types, but the types of things that 
	 * a datastore can access.  In this case, it is only 1 thing, the 
	 * occurrence data
	 * @see org.geotools.data.AbstractDataStore#getTypeNames()
	 */
	@Override
	public String[] getTypeNames() throws IOException {
		return new String[]{"occurrence"};
	}

}
