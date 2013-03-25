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
	public List<DwcRecord> getRecords(Long resourceId, String guid, Long taxonId, Long taxonLft, Long taxonRgt, Long regionId, Long regionLft, Long regionRgt, 
			String scientificName, String locality, String family, String typeStatus,
			String institutionCode, String collectionCode, String catalogNumber, String collector, String earliestDateCollected, String basisOfRecord, 
			Double minLatitude,	Double maxLatitude, Double minLongitude, Double maxLongitude, int maxResults) {
		
		// select is always the same
		String select = "SELECT dwc.guid as guid, taxon_fk, t.lft as taxon_lft, t.rgt as taxon_rgt, region_fk, r.lft as region_lft, r.rgt as region_rgt, scientific_name, family, type_status, locality, institution_code, collection_code, catalog_number, collector, earliest_date_collected, basis_of_record, lat, lon  " +
				" FROM darwin_core dwc  join taxon t on dwc.taxon_fk=t.id  join region r on dwc.region_fk=r.id";
		// build the where clause
		List<Object> params = new LinkedList<Object>();
		String where = buildWhere(resourceId, guid, taxonId, taxonLft, taxonRgt, regionId, regionLft, regionRgt, 
				scientificName, locality, family, typeStatus,
				institutionCode, collectionCode, catalogNumber, collector, earliestDateCollected, basisOfRecord, 
				minLatitude, maxLatitude, minLongitude, maxLongitude, params);
		
		// and the limit
		String limit = " LIMIT " + maxResults;
		
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
	private String buildWhere(Long resourceId, String guid, Long taxonId, Long taxonLft, Long taxonRgt, Long regionId, Long regionLft, Long regionRgt, 
			String scientificName, String locality, String family, String typeStatus, 
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
			where.append(" and dwc.taxon_fk = ?");
			params.add(taxonId);
		}
		if (taxonLft != null) {
			where.append(" and t.lft >= ?");
			params.add(taxonLft);
		}
		if (taxonRgt != null) {
			where.append(" and t.rgt <= ?");
			params.add(taxonRgt);
		}
		if (regionId != null) {
			where.append(" and dwc.region_fk = ?");
			params.add(regionId);
		}
		if (regionLft != null) {
			where.append(" and r.lft >= ?");
			params.add(regionLft);
		}
		if (regionRgt != null) {
			where.append(" and r.rgt <= ?");
			params.add(regionRgt);
		}
		if (scientificName != null) {
			where.append(" and dwc.scientific_name = ?");
			params.add(scientificName);
		}
		if (family != null) {
			where.append(" and dwc.family = ?");
			params.add(family);
		}
		if (typeStatus != null) {
			where.append(" and dwc.type_status = ?");
			params.add(typeStatus);
		}
		if (locality != null) {
			where.append(" and dwc.locality = ?");
			params.add(locality);
		}
		if (earliestDateCollected != null) {
			where.append(" and dwc.earliest_date_collected = ?");
			params.add(earliestDateCollected);
		}
		if (institutionCode != null) {
			where.append(" and dwc.institution_code = ?");
			params.add(institutionCode);
		}
		if (collectionCode != null) {
			where.append(" and dwc.collection_code = ?");
			params.add(collectionCode);
		}
		if (catalogNumber != null) {
			where.append(" and dwc.catalog_number = ?");
			params.add(catalogNumber);
		}
		if (collector != null) {
			where.append(" and dwc.collector = ?");
			params.add(collector);
		}
		if (basisOfRecord != null) {
			where.append(" and dwc.basis_of_record = ?");
			params.add(basisOfRecord);
		}
		if (minLatitude != null) {
			where.append(" and dwc.lat >= ?");
			params.add(minLatitude);
		}
		if (maxLatitude != null) {
			where.append(" and dwc.lat <= ?");
			params.add(maxLatitude);
		}
		if (minLongitude != null) {
			where.append(" and dwc.lon >= ?");
			params.add(minLongitude);
		}
		if (maxLongitude != null) {
			where.append(" and dwc.lon <= ?");
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
					rs.getLong("taxon_lft"),
					rs.getLong("taxon_rgt"),
					rs.getLong("region_fk"),
					rs.getLong("region_lft"),
					rs.getLong("region_rgt"),
					rs.getString("scientific_name"),
					rs.getString("family"),
					rs.getString("type_status"),
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
