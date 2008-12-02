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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

import com.vividsolutions.jts.geom.Point;

/**
 * A simple JDBC property store that connects to a DwC table
 * and maps a Float latitude and longitude to the output
 * @author mdoering
 */
public class JDBCDwCDatastore extends AbstractDataStore {
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
		b.setNamespaceURI( "http://rs.tdwg.org/dwc/dwcore" );

		//add some properties
		b.add( "GlobalUniqueIdentifier", String.class );
		b.add( "TaxonId", Integer.class ); // non dwc attribute
		b.add( "RegionId", Integer.class ); // non dwc attribute
		b.add( "ScientificName", String.class );
		b.add( "Locality", String.class );
		b.add( "InstitutionCode", String.class );
		b.add( "CollectionCode", String.class );
		b.add( "CatalogNumber", String.class );
		b.add( "Collector", String.class );
		b.add( "DateCollected", String.class );
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
		// embedded H2 is apparently faster without connection pools. See H2 documentation 
        JdbcDataSource ds = new JdbcDataSource();
        String url = String.format("jdbc:h2:%s/db/ipt;auto_server=true", params.get("datadir")); 
        log.debug("Using JDBC URL: " + url);
        ds.setURL(url); 
        ds.setUser("sa"); // params.get("user") 
        ds.setPassword(""); // params.get("password") 
        log.debug(ds);
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
		log.info("Layer requested: " + typeName);
		log.info("Filter supplied: " + query.getFilter());
		// parse out the resourceId from the layer name.
		// convention is to name layers as resource1, resource2, resource31, etc
		Long resourceId=null;
		// support weird layer names. Try to find just any number
		try {
			resourceId = Long.valueOf(typeName.replaceAll("[^0-9]", ""));
		} catch (NumberFormatException e2) {
			throw new IOException("Couldnt find resourceId in layer name");
		}
		log.info("Found IPT resource " + resourceId);
		
		// parse out the values from the query
		OGCQueryVisitor parsedQuery = new OGCQueryVisitor();
		Filters.accept( query.getFilter(), parsedQuery );
		
		List<DwcRecord> records = dao.getRecords(
				resourceId,
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
				5000); // TODO - pass in in the factory...
		
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
