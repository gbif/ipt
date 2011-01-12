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

import java.io.File;
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
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.Filters;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.h2.jdbcx.JdbcDataSource;
import org.opengis.feature.simple.SimpleFeatureType;
import org.springframework.util.StringUtils;

import com.vividsolutions.jts.geom.Point;

/**
 * A simple JDBC property store that connects to a DwC table
 * and maps a Float latitude and longitude to the output
 * @author mdoering
 */
public class JDBCDwCDatastore extends AbstractDataStore {
	public static final int MAX_RECORDS = 10000;
	protected static Log log = LogFactory.getLog(JDBCDwCDatastore.class);
	
	/**
	 * The feature type
	 */
	protected static SimpleFeatureType type;
	
	
	/**
	 * The singleton DAO
	 */
	protected static Dao dao;
		
	public JDBCDwCDatastore(Map<String, String> params) {
		// indicates that this is a read-only DataStore
		super(false);
		log.info("Creating datasource [" + params.get("datadir") +"] [" + params.get("user") +"] [" + params.get("password") + "]");
		setupPool(params);
		type = getFeatureType("dwcore");
	}
	
	private SimpleFeatureType getFeatureType(String name) {
		SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();

		//set the name
		b.setName(name);
		b.setNamespaceURI( "http://rs.tdwg.org/dwc/terms/" );

		//add some properties
		b.add( "SampleID", String.class );
		b.add( "TaxonId", Integer.class );
		b.add( "TaxonLft", Integer.class ); // non dwc attribute
		b.add( "TaxonRgt", Integer.class ); // non dwc attribute
		b.add( "SamplingLocationID", Integer.class );
		b.add( "SamplingLocationLft", Integer.class ); // non dwc attribute
		b.add( "SamplingLocationRgt", Integer.class ); // non dwc attribute
		b.add( "ScientificName", String.class );
		b.add( "Family", String.class );
		b.add( "TypeStatus", String.class );
		b.add( "Locality", String.class );
		b.add( "InstitutionCode", String.class );
		b.add( "CollectionCode", String.class );
		b.add( "CatalogNumber", String.class );
		b.add( "Collector", String.class );
		b.add( "EarliestDateCollected", String.class );
		b.add( "BasisOfRecord", String.class );
		//add a geometry property
		b.setCRS( DefaultGeographicCRS.WGS84 );
		b.add( "Geom", Point.class );

		//build the type
		return b.buildFeatureType();		
	}

	/**
	 * @param params The parameters for the H2 DB connection
	 */
	protected synchronized static void setupPool(Map<String, String> params) {
		try {
			Class.forName("org.h2.Driver");
		} catch (ClassNotFoundException e) {
		}
		String dataDir = params.get("datadir");
		if (dataDir==null || StringUtils.trimWhitespace(dataDir).equals("")){
			// default IPT data dir is webapps/ipt/data
			dataDir="webapps/ipt/data";
			
		}
        log.info("Using dataDir: " + dataDir);
		// convert relative paths into absolute ones...
		File d = new File(dataDir);		
        String url = String.format("jdbc:h2:%s/db/ipt;auto_server=true", d.getAbsolutePath());
        String user = "sa"; // params.get("user")
        String pass = "";  // params.get("password") 
        log.info("Using JDBC URL: " + url);

//        // embedded H2 is apparently faster without connection pools. See H2 documentation 
//        JdbcDataSource ds = new JdbcDataSource();
//        ds.setURL(url); 
//        ds.setUser(user);  
//        ds.setPassword(pass); 

        // NO IT ISNT!!!
        // 100 inserts took 16000 milliseconds, with pool only 250 !!!
        
        ObjectPool connectionPool = new GenericObjectPool(null);
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(url, user, pass);
        // this is unused but it takes in the connection pool so I presume that this does the callback to set the factory for the pool 
        // http://commons.apache.org/dbcp/apidocs/org/apache/commons/dbcp/package-summary.html#package_description
        @SuppressWarnings("unused") 
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,connectionPool,null,null,false,true);
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
		log.debug("Layer requested: " + typeName);
		log.debug("Filter supplied: " + query.getFilter());
		// parse out the resourceId from the layer name.
		// convention is to name layers as resource1, resource2, resource31, etc
		Long resourceId=null;
		// support weird layer names. Try to find just any number
		try {
			resourceId = Long.valueOf(typeName.replaceAll("[^0-9]", ""));
		} catch (NumberFormatException e2) {
			throw new IOException("Couldnt find resourceId in layer name");
		}
		log.debug("Found IPT resource " + resourceId);
		
		// parse out the values from the query
		OGCQueryVisitor parsedQuery = new OGCQueryVisitor();
		Filters.accept( query.getFilter(), parsedQuery );
		
		List<DwcRecord> records = dao.getRecords(
				resourceId,
				parsedQuery.getGuid(), 
				parsedQuery.getTaxonId(), 
				parsedQuery.getTaxonLft(), 
				parsedQuery.getTaxonRgt(), 
				parsedQuery.getRegionId(), 
				parsedQuery.getRegionLft(), 
				parsedQuery.getRegionRgt(), 
				parsedQuery.getScientificName(), 
				parsedQuery.getFamily(), 
				parsedQuery.getTypeStatus(), 
				parsedQuery.getLocality(), 
				parsedQuery.getInstitutionCode(), 
				parsedQuery.getCollectionCode(), 
				parsedQuery.getCatalogNumber(), 
				parsedQuery.getCollector(), 
				parsedQuery.getEarliestDateCollected(), 
				parsedQuery.getBasisOfRecord(), 
				parsedQuery.getMinY(), 
				parsedQuery.getMaxY(), 
				parsedQuery.getMinX(), 
				parsedQuery.getMaxX(), 
				MAX_RECORDS); // TODO - pass in in the factory...
		
		try {
			log.debug("Found "+records.size() + " DwcRecords");
			return new DefaultFeatureReader(new DwcRecordFeatureAttributeReader(records), getFeatureType(typeName));
		} catch (SchemaException e1) {
			// Should not happen
			throw new IOException("Unable to get data - schema is corrupt: " + e1.getMessage());
		}
	}

	/**
	 * Returns the schema that is supported
	 * @see org.geotools.data.AbstractDataStore#getSchema(java.lang.String)
	 */
	@Override
	public SimpleFeatureType getSchema(String typeName) throws IOException {
		return getFeatureType(typeName);
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
