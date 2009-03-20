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

package org.gbif.provider.service.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.dto.CommonName;
import org.gbif.provider.model.dto.Distribution;
import org.gbif.provider.model.dto.ExtendedRecord;
import org.gbif.provider.model.dto.ExtensionRecord;
import org.gbif.provider.service.ExtensionManager;
import org.gbif.provider.service.ExtensionRecordManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly=true)
public class ExtensionRecordManagerJDBC extends BaseManagerJDBC implements ExtensionRecordManager {	
    @Autowired
	private ExtensionManager extensionManager;

	@Transactional(readOnly=false)
	public void insertExtensionRecord(ExtensionRecord rec) {
		String table = namingStrategy.extensionTableName(rec.getExtension());
		String cols = "coreid, resource_fk";
		String vals = String.format("%s, %s", rec.getCoreId(), rec.getResourceId());
		for (ExtensionProperty p : rec){
			if (rec.getPropertyValue(p)!=null){
				cols += String.format(",%s", namingStrategy.propertyToColumnName(p.getName()));
				vals += String.format(",'%s'", rec.getPropertyValue(p));
			}
		}
		String sql = String.format("insert into %s (%s) VALUES (%s)", table, cols, vals);
		Connection cn = null;
		try {
			cn=getConnection();
			Statement st = cn.createStatement();
			st.execute(sql);
		} catch (SQLException e) {
			if (rec.getExtension()==null){
				log.error(String.format("Couldn't insert record for extension=NULL", rec.getExtension()), e);
			}else{
				log.error(String.format("Couldn't insert record for extension %s", rec.getExtension().getName()), e);
			}
		} finally {
			if (cn!=null){
//				try {
//					cn.close();
//				} catch (SQLException e) {
//					e.printStackTrace();
//				}
			}
		}
	}

	@Transactional(readOnly=false)
	public int removeAll(Extension extension, Long resourceId) {
		String table = namingStrategy.extensionTableName(extension);
		String sql = String.format("delete from %s where resource_fk=%s", table, resourceId);
		Connection cn = null;
		int count = 0;
		try {
			cn=getConnection();
			Statement st = cn.createStatement();			
			count = st.executeUpdate(sql);
			log.debug(String.format("Removed %s records for extension %s", count, extension.getName()));
		} catch (SQLException e) {
			log.error(String.format("Couldn't rmove all records for extension %s", extension.getName()));
			e.printStackTrace();
		} finally {
			if (cn!=null){
//				try {
//					cn.close();
//				} catch (SQLException e) {
//					e.printStackTrace();
//				}
			}
		}
		return count;
	}
	
	public int count(Extension extension, Long resourceId) {
		String table = namingStrategy.extensionTableName(extension);
		String sql = String.format("select count(*) from %s where resource_fk=%s", table, resourceId);
		return executeCount(sql);
	}
	public int countDistinct(ExtensionProperty property, Long resourceId) {
		String table = namingStrategy.extensionTableName(property.getExtension());
		String column = namingStrategy.columnName(property.getName());
		String sql = String.format("select count(distinct %s) from %s where resource_fk=%s", column, table, resourceId);
		return executeCount(sql);
	}
	public List<CommonName> getCommonNames(Long taxonId) {
		List<CommonName> cnames = new ArrayList<CommonName>();
		String sql = String.format("SELECT name, language, region FROM TAX_COMMON_NAMES where coreid=%s", taxonId);
		Connection cn = null;
		try {
			cn =  getConnection();
			Statement st = cn.createStatement();			
			ResultSet result = st.executeQuery(sql); 
			// create extension records from JDBC resultset
			while (result.next()){
				cnames.add(new CommonName(result.getString("name"), result.getString("language"), result.getString("region")));
			}
		} catch (SQLException e) {
			log.warn("Couldn't read common names");
		} finally {
			if (cn!=null){
//				try {
//					cn.close();
//				} catch (SQLException e) {
//					e.printStackTrace();
//				}
			}
		}
		return cnames;
	}
	public List<Distribution> getDistributions(Long taxonId) {
		List<Distribution> distributions = new ArrayList<Distribution>();
		String sql = String.format("SELECT region, status FROM TAX_Description where coreid=%s", taxonId);
		Connection cn = null;
		try {
			cn = getConnection();
			Statement st = cn.createStatement();			
			ResultSet result = st.executeQuery(sql); 
			// create extension records from JDBC resultset
			while (result.next()){
				distributions.add(new Distribution(result.getString("region"), result.getString("status")));
			}
		} catch (SQLException e) {
			log.warn("Couldn't read distributions");
		} finally {
			if (cn!=null){
//				try {
//					cn.close();
//				} catch (SQLException e) {
//					e.printStackTrace();
//				}
			}
		}
		return distributions;
	}


	private List<ExtensionRecord> getExtensionRecords(Extension extension, List<ExtensionProperty> properties, Long coreId, Long resourceId) {
		String table = namingStrategy.extensionTableName(extension);
		String sql = String.format("select * from %s where coreid=%s and resource_fk=%s", table, coreId, resourceId);
		return queryExtensionRecords(resourceId, sql, properties);
	}
	private List<ExtensionRecord> getExtensionRecords(Extension extension, List<ExtensionProperty> properties, Set<Long> coreIds, Long resourceId) {
		String table = namingStrategy.extensionTableName(extension);
		String sql = String.format("select * from %s where coreid in (%s) and resource_fk=%s", table, StringUtils.join(coreIds,","), resourceId);
		return queryExtensionRecords(resourceId, sql, properties);
	}
	private List<ExtensionRecord> queryExtensionRecords(Long resourceId, String sql, List<ExtensionProperty> properties) {
		List<ExtensionRecord> records = new ArrayList<ExtensionRecord>();
		Connection cn = null;
		try {
			cn = getConnection();
			Statement st = cn.createStatement();			
			ResultSet result = st.executeQuery(sql); 
			// create extension records from JDBC resultset
			while (result.next()){
				ExtensionRecord rec = new ExtensionRecord(result.getLong("coreid"), resourceId);
				for (ExtensionProperty p : properties){
					String value = result.getString(namingStrategy.propertyToColumnName(p.getName()));
					if (value!=null){
						rec.setPropertyValue(p, value);
					}
				}
				if (!rec.isEmpty()){
					records.add(rec);
				}
			}
		} catch (SQLException e) {
			log.error(String.format("Couldn't read extension records for sql: %s", sql), e);
		} finally {
			if (cn!=null){
//				try {
//					cn.close();
//				} catch (SQLException e) {
//					e.printStackTrace();
//				}
			}
		}
		return records;
	}

	public ExtendedRecord extendCoreRecord(DataResource resource, CoreRecord coreRecord) {
		ExtendedRecord extRec = new ExtendedRecord(coreRecord);
		for (ExtensionMapping view : resource.getExtensionMappings()){
			// Extension extension, List<ExtensionProperty> properties, Set<Long> coreIds, Long resourceId) {
			List<ExtensionRecord> extensionRecords = getExtensionRecords(view.getExtension(), view.getMappedProperties(), coreRecord.getCoreId(), resource.getId());
			for (ExtensionRecord erec : extensionRecords){
				extRec.addExtensionRecord(erec);
			}
		}
		return extRec;
	}
	public List<ExtendedRecord> extendCoreRecords(DataResource resource, CoreRecord[] coreRecords) {
		LinkedHashMap<Long, ExtendedRecord> extendedRecords = new LinkedHashMap<Long, ExtendedRecord>();
		for (CoreRecord core : coreRecords){
			extendedRecords.put(core.getCoreId(), new ExtendedRecord(core));
		}
		for (ExtensionMapping view : resource.getExtensionMappings()){
			List<ExtensionRecord> extensionRecords = getExtensionRecords(view.getExtension(), view.getMappedProperties(), extendedRecords.keySet(), resource.getId());
			for (ExtensionRecord erec : extensionRecords){
				extendedRecords.get(erec.getCoreId()).addExtensionRecord(erec);
			}
		}
		return new ArrayList<ExtendedRecord>(extendedRecords.values());
	}
}
