package org.gbif.provider.geotools;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * DAO method
 * Probably wants to be replaced by Hibernate...
 * 
 * @author trobertson
 */
public class Dao extends JdbcDaoSupport {
	/**
	 * Mappers
	 */ 
	protected RowMapper dwcRowMapper = new DwCRecordRowMapper();
	
	/**
	 * This is pretty messy stuff - consider moving to hibernate?
	 * @param string 
	 * @param regionId 
	 * @param taxonId 
	 * @return All records that match the query
	 */
	@SuppressWarnings("unchecked")
	public List<DwcRecord> getRecords(Long resourceId, String guid, Long taxonId, Long regionId, String scientificName, String locality, 
			String institutionCode, String collectionCode, String catalogNumber, String collector, String earliestDateCollected, String basisOfRecord, 
			Double minLatitude,	Double maxLatitude, Double minLongitude, Double maxLongitude, int maxResults) {
		
		// select is always the same
		String select = "select dwc.guid as guid, taxon_fk, region_fk, scientific_name, locality, institution_code, collection_code, catalog_number, collector, earliest_date_collected, basis_of_record, lat, lon  from DARWIN_CORE dwc ";
		// join optional tables and make sure all included taxa are selected, not only the single TaxonID
		if (taxonId != null){
			select += " join taxon td on dwc.taxon_fk=td.id join taxon t on td.lft>=t.lft and td.rgt<=t.rgt";
		}
		// join optional tables and make sure all included taxa are selected, not only the single TaxonID
		if (regionId != null){
			select += " join region rd on dwc.region_fk=rd.id join region r on rd.lft>=r.lft and rd.rgt<=r.rgt";
		}
		// build the where clause
		List<Object> params = new LinkedList<Object>();
		String where = buildWhere(resourceId, guid, taxonId, regionId, scientificName, locality, 
				institutionCode, collectionCode, catalogNumber, collector, earliestDateCollected, basisOfRecord, 
				minLatitude, maxLatitude, minLongitude, maxLongitude, params);
		
		// and the limit
		String limit = " limit " + maxResults;
		
		List<DwcRecord> records = null;
		if (params.size() == 0) {
			logger.info(select + limit);
			records = (List<DwcRecord>)
			getJdbcTemplate().query(select + limit,new RowMapperResultSetExtractor(dwcRowMapper, JDBCDwCDatastore.MAX_RECORDS));
		} else {
			logger.info(select + where + limit);
			records = (List<DwcRecord>) getJdbcTemplate().query(select + where + limit, params.toArray(), new RowMapperResultSetExtractor(dwcRowMapper, JDBCDwCDatastore.MAX_RECORDS));
		}
		logger.info("SQL parameter: "+ params.toString());
		logger.info("Records found: "+ records.size());
		return records;
	}

	/**
	 * Builds the where clause
	 */
	private String buildWhere(Long resourceId, String guid, Long taxonId, Long regionId, String scientificName, String locality, 
			String institutionCode, String collectionCode, String catalogNumber, String collector, String earliestDateCollected, String basisOfRecord, 
			Double minLatitude,	Double maxLatitude, Double minLongitude, Double maxLongitude, 
			List<Object> params) {
		StringBuffer where = new StringBuffer(" where dwc.resource_fk = ? and dwc.deleted=false");
		params.add(resourceId);
		// dynamic filter
		if (guid != null) {
			where.append(" and dwc.guid = ?");
			params.add(guid);
		}
		if (taxonId != null) {
			where.append(" and t.id = ?");
			params.add(taxonId);
		}
		if (regionId != null) {
			where.append(" and r.id = ?");
			params.add(regionId);
		}
		if (scientificName != null) {
			where.append(" and scientific_name = ?");
			params.add(scientificName);
		}
		if (locality != null) {
			where.append(" and locality = ?");
			params.add(locality);
		}
		if (earliestDateCollected != null) {
			where.append(" and earliest_date_collected = ?");
			params.add(earliestDateCollected);
		}
		if (institutionCode != null) {
			where.append(" and institution_code = ?");
			params.add(institutionCode);
		}
		if (collectionCode != null) {
			where.append(" and collection_code = ?");
			params.add(collectionCode);
		}
		if (catalogNumber != null) {
			where.append(" and catalog_number = ?");
			params.add(catalogNumber);
		}
		if (collector != null) {
			where.append(" and collector = ?");
			params.add(collector);
		}
		if (basisOfRecord != null) {
			where.append(" and basis_of_record = ?");
			params.add(basisOfRecord);
		}
		if (minLatitude != null) {
			where.append(" and lat >= ?");
			params.add(minLatitude);
		}
		if (maxLatitude != null) {
			where.append(" and lat <= ?");
			params.add(maxLatitude);
		}
		if (minLongitude != null) {
			where.append(" and lon >= ?");
			params.add(minLongitude);
		}
		if (maxLongitude != null) {
			where.append(" and lon <= ?");
			params.add(maxLongitude);
		}
		return where.toString();
	}

	/**
	 * Maps to a CellDensity
	 */
	class DwCRecordRowMapper implements RowMapper {
		public DwcRecord mapRow(ResultSet rs, int rowNumber) throws SQLException {
			return new DwcRecord(
					rs.getString("guid"),
					rs.getLong("taxon_fk"),
					rs.getLong("region_fk"),
					rs.getString("scientific_name"),
					rs.getString("locality"),
					rs.getString("institution_code"),
					rs.getString("collection_code"),
					rs.getString("catalog_number"),
					rs.getString("collector"),
					rs.getString("earliest_date_collected"),
					rs.getString("basis_of_record"),
					rs.getDouble("lat"),
					rs.getDouble("lon"));
		}
	}

}
