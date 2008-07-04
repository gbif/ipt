package org.gbif.provider.geotools;


import java.sql.ResultSet;
import java.sql.SQLException;
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
	 * @return All records that match the query
	 */
	@SuppressWarnings("unchecked")
	public List<DwcRecord> getRecords(Integer resourceId, String kingdom, String phylum,
			String klass, String order, String family, String genus,
			String scientificName, String basisOfRecord,
			Double minLatitude, Double maxLatitude,
			Double minLongitude, Double maxLongitude, int maxResults) {
		
		// select is always the same
		String select = "select resourceId, kingdom, phylum, classs, orderrr, family, genus, scientificName, basisOfRecord, latitude, longitude from VIEW_OGC_DWC ";
		
		// build the where clause
		List<Object> params = new LinkedList<Object>();
		String where = buildWhere(resourceId, kingdom, phylum, klass, order, family, genus,
				scientificName, basisOfRecord, minLatitude, maxLatitude,
				minLongitude, maxLongitude, params);
		
		// and the limit
		String limit = " limit " + maxResults;
		
		if (params.size() == 0) {
			logger.info(select + limit);
			return (List<DwcRecord>)
				getJdbcTemplate().query(select,new RowMapperResultSetExtractor(dwcRowMapper,1000));			
		} else {
			logger.info(select + where + limit);
			return (List<DwcRecord>)
				getJdbcTemplate().query(select + where + limit,
					params.toArray(),
				new RowMapperResultSetExtractor(dwcRowMapper,1000));
		}
	}

	/**
	 * Builds the where clause
	 */
	private String buildWhere(Integer resourceId, String kingdom, String phylum,
			String klass, String order, String family, String genus,
			String scientificName, String basisOfRecord, Double minLatitude,
			Double maxLatitude, Double minLongitude, Double maxLongitude,
			List<Object> params) {
		StringBuffer where = new StringBuffer(" where ");
		if (resourceId != null) {
			where.append(" and resourceId = ?");
			params.add(resourceId);
		}
		if (kingdom != null) {
			where.append(" and kingdom = ?");
			params.add(kingdom);
		}
		if (phylum != null) {
			where.append(" and phylum = ?");
			params.add(phylum);
		}
		if (klass != null) {
			where.append(" and class = ?");
			params.add(klass);
		}
		if (order != null) {
			// grrrroovy Markus
			where.append(" and orderrr = ?");
			params.add(order);
		}
		if (family != null) {
			where.append(" and family = ?");
			params.add(family);
		}
		if (genus != null) {
			where.append(" and genus = ?");
			params.add(genus);
		}
		if (scientificName != null) {
			where.append(" and scientificName = ?");
			params.add(scientificName);
		}
		if (basisOfRecord != null) {
			where.append(" and basisOfRecord = ?");
			params.add(basisOfRecord);
		}
		if (minLatitude != null) {
			where.append(" and latitude >= ?");
			params.add(minLatitude);
		}
		if (maxLatitude != null) {
			where.append(" and latitude <= ?");
			params.add(maxLatitude);
		}
		if (minLongitude != null) {
			where.append(" and longitude >= ?");
			params.add(minLongitude);
		}
		if (maxLongitude != null) {
			where.append(" and longitude <= ?");
			params.add(maxLongitude);
		}
		String whereAsString = where.toString();
		return (whereAsString.replaceFirst(" and ", ""));
	}

	/**
	 * Maps to a CellDensity
	 */
	class DwCRecordRowMapper implements RowMapper {
		public DwcRecord mapRow(ResultSet rs, int rowNumber) throws SQLException {
			return new DwcRecord(rs.getInt("resourceId"),
					rs.getString("kingdom"),
					rs.getString("phylum"),
					rs.getString("classs"),
					rs.getString("orderrr"),
					rs.getString("family"),
					rs.getString("genus"),
					rs.getString("scientificName"),
					rs.getString("basisOfRecord"),
					rs.getDouble("latitude"),
					rs.getDouble("longitude"));
		}
	}
}
