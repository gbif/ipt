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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.Taxon;
import org.gbif.provider.model.ViewExtensionMapping;
import org.gbif.provider.model.dto.CommonName;
import org.gbif.provider.model.dto.Distribution;
import org.gbif.provider.model.dto.ExtendedRecord;
import org.gbif.provider.model.dto.ExtensionRecord;
import org.gbif.provider.model.dto.ExtensionRecordsWrapper;
import org.gbif.provider.model.hibernate.IptNamingStrategy;
import org.gbif.provider.model.voc.ExtensionType;
import org.gbif.provider.service.ExtensionManager;
import org.gbif.provider.service.ExtensionRecordManager;
import org.gbif.provider.tapir.Filter;
import org.gbif.provider.util.Constants;
import org.gbif.provider.util.H2Utils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly=true)
public class ExtensionRecordManagerJDBC implements ExtensionRecordManager {	
    protected static final Log log = LogFactory.getLog(ExtensionRecordManagerJDBC.class);
    @Autowired
	private SessionFactory sessionFactory;		
	@Autowired
	private IptNamingStrategy namingStrategy;
	@Autowired
	private ExtensionManager extensionManager;

	private Connection getConnection() {
		Session s = getSession();
		Connection cn = s.connection();
		return cn;
	}
	private Session getSession() {
		return SessionFactoryUtils.getSession(sessionFactory, false);
	}

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
		Connection cn = getConnection();
		try {
			Statement st = cn.createStatement();
			st.execute(sql);
		} catch (SQLException e) {
			if (rec.getExtension()==null){
				log.error(String.format("Couldn't insert record for extension=NULL", rec.getExtension()), e);
			}else{
				log.error(String.format("Couldn't insert record for extension %s", rec.getExtension().getName()), e);
			}
		}
	}

	@Transactional(readOnly=false)
	public int removeAll(Extension extension, Long resourceId) {
		String table = namingStrategy.extensionTableName(extension);
		String sql = String.format("delete from %s where resource_fk=%s", table, resourceId);
		Connection cn = getConnection();
		int count = 0;
		try {
			Statement st = cn.createStatement();			
			count = st.executeUpdate(sql);
			log.debug(String.format("Removed %s records for extension %s", count, extension.getName()));
		} catch (SQLException e) {
			log.error(String.format("Couldn't rmove all records for extension %s", extension.getName()));
			e.printStackTrace();
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
	private int executeCount(String sql){
		Connection cn = getConnection();
		int count = 0;
		try {
			Statement st = cn.createStatement();			
			ResultSet result = st.executeQuery(sql);
			// create extension records from JDBC resultset
			while (result.next()){
				count = result.getInt(1);
		    }
		} catch (SQLException e) {
			log.error(String.format("Couldn't execute count SQL per JDBC: %s", sql), e);
		}
		return count;
	}
	public List<Object> getDistinct(ExtensionProperty property, Long resourceId, int start, int limit) {
		String table = namingStrategy.extensionTableName(property.getExtension());
		String column = namingStrategy.columnName(property.getName());
		String sql = String.format("select distinct %s from %s where resource_fk=%s order by %s limit %s offset %s", column, table, resourceId, column, limit, start);
		Connection cn = getConnection();
		List<Object> values = new ArrayList();
		try {
			Statement st = cn.createStatement();			
			ResultSet result = st.executeQuery(sql);
			// create extension records from JDBC resultset
			while (result.next()){
				values.add(result.getObject(1));
		    }
		} catch (SQLException e) {
			log.error(String.format("Couldn't execute select distinct SQL per JDBC: %s", sql), e);
		}
		return values;
	}
	public List<Map<ExtensionProperty, Object>> getDistinct(List<ExtensionProperty> properties, Filter filter, Long resourceId, int start, int limit) {
		Map<Extension, String> extension2Alias = new HashMap<Extension, String>();
		// build sql
		List<ExtensionProperty> fromProperties = new ArrayList<ExtensionProperty>(properties);
		String where = buildWhereSql(filter, fromProperties, extension2Alias);
		String from = buildFromSql(fromProperties, extension2Alias);
		String columns = buildSelectSql(properties, extension2Alias);		
		String sql = String.format("select distinct %s from %s where c.resource_fk=%s order by %s limit %s offset %s", columns, from, resourceId, columns, limit, start);
		// execute query
		Connection cn = getConnection();
		List<Map<ExtensionProperty, Object>> values = new ArrayList<Map<ExtensionProperty, Object>>();
		try {
			Statement st = cn.createStatement();			
			ResultSet result = st.executeQuery(sql);
			// create result list
			while (result.next()){
				Map<ExtensionProperty, Object> row = new HashMap<ExtensionProperty, Object>();
				int i = 1;
				for (ExtensionProperty prop : properties){
					row.put(prop, result.getObject(i));
					i++;
				}
				values.add(row);
		    }
		} catch (SQLException e) {
			log.error(String.format("Couldn't execute select distinct SQL per JDBC: %s", sql), e);
		}
		return values;
	}
	
	private String buildWhereSql(Filter filter,
			List<ExtensionProperty> fromProperties,
			Map<Extension, String> extension2Alias) {
		// TODO Auto-generated method stub
		return null;
	}
	private String buildFromSql(List<ExtensionProperty> properties, Map<Extension, String> extension2Alias) {
		ExtensionType type = properties.get(0).getExtension().getType();
		if (type==null){
			// this is a core extension. check IDs
			if (properties.get(0).getExtension().getId() == ExtensionType.Occurrence.extensionID){
				type = ExtensionType.Occurrence;
			}else{
				type = ExtensionType.Checklist;
			}
		}
		String from = String.format("%s c", type.tableName);
		Integer i = 0;
		for (ExtensionProperty prop : properties){				
			if (!prop.getExtension().isCore() && prop.getExtension().getType()!=type){
				throw new IllegalArgumentException("All properties need to be of the same extension type");
			}
			if (!extension2Alias.containsKey(prop.getExtension())){
				if (prop.getExtension().isCore()){
					extension2Alias.put(prop.getExtension(), "c");
					continue;
				}
				i++;
				String alias = "e"+i;
				extension2Alias.put(prop.getExtension(), alias);
				from += String.format(" left join %s %s on c.id=%s.coreid", namingStrategy.extensionTableName(prop.getExtension()), alias, alias);
			}
		}
		log.debug("SQL FROM part: "+from);
		return from;
	}
	private String buildSelectSql(List<ExtensionProperty> properties, Map<Extension, String> extension2Alias){
		String columns = "";
		for (ExtensionProperty prop : properties){
			if (columns.length() > 0){
				columns += ",";
			}
			if (prop.getExtension().isCore()){
				columns += String.format("c.%s", namingStrategy.columnName(prop.getName()));
			}else{
				columns += String.format("%s.%s", extension2Alias.get(prop.getExtension()), namingStrategy.columnName(prop.getName()));
			}
		}
		log.debug("SQL SELECT part: "+columns);
		return columns;
	}
	public List<CommonName> getCommonNames(Long taxonId) {
		List<CommonName> cnames = new ArrayList<CommonName>();
		String sql = String.format("SELECT name, language, region FROM TAX_COMMON_NAMES where coreid=%s", taxonId);
		Connection cn = getConnection();
		try {
			Statement st = cn.createStatement();			
			ResultSet result = st.executeQuery(sql); 
			// create extension records from JDBC resultset
			while (result.next()){
				cnames.add(new CommonName(result.getString("name"), result.getString("language"), result.getString("region")));
			}
		} catch (SQLException e) {
			log.warn("Couldn't read common names");
		}
		return cnames;
	}
	public List<Distribution> getDistributions(Long taxonId) {
		List<Distribution> distributions = new ArrayList<Distribution>();
		String sql = String.format("SELECT region, status FROM TAX_Description where coreid=%s", taxonId);
		Connection cn = getConnection();
		try {
			Statement st = cn.createStatement();			
			ResultSet result = st.executeQuery(sql); 
			// create extension records from JDBC resultset
			while (result.next()){
				distributions.add(new Distribution(result.getString("region"), result.getString("status")));
			}
		} catch (SQLException e) {
			log.warn("Couldn't read distributions");
		}
		return distributions;
	}


	private List<ExtensionRecord> getExtensionRecords(Extension extension, List<ExtensionProperty> properties, Set<Long> coreIds, Long resourceId) {
		List<ExtensionRecord> records = new ArrayList<ExtensionRecord>();
		String table = namingStrategy.extensionTableName(extension);
		String sql = String.format("select * from %s where coreid in (%s) and resource_fk=%s", table, StringUtils.join(coreIds,","), resourceId);
		Connection cn = getConnection();
		try {
			Statement st = cn.createStatement();			
			ResultSet result = st.executeQuery(sql); 
			// create extension records from JDBC resultset
			while (result.next()){
				ExtensionRecord rec = new ExtensionRecord(result.getLong("coreid"), resourceId);
				for (ExtensionProperty p : extension.getProperties()){
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
			log.error(String.format("Couldn't read extension records for extension=%s", extension), e);
		}
		
		return records;
	}


	public List<ExtendedRecord> extendCoreRecords(DataResource resource, CoreRecord[] coreRecords) {
		LinkedHashMap<Long, ExtendedRecord> extendedRecords = new LinkedHashMap<Long, ExtendedRecord>();
		for (CoreRecord core : coreRecords){
			extendedRecords.put(core.getCoreId(), new ExtendedRecord(core));
		}
		for (ViewExtensionMapping view : resource.getExtensionMappings()){
			List<ExtensionRecord> extensionRecords = getExtensionRecords(view.getExtension(), view.getMappedProperties(), extendedRecords.keySet(), resource.getId());
			for (ExtensionRecord erec : extensionRecords){
				extendedRecords.get(erec.getCoreId()).addExtensionRecord(erec);
			}
		}
		return new ArrayList<ExtendedRecord>(extendedRecords.values());
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	//FIXME: maybe remove and replace with ExtendedRecord ???
	public ExtensionRecordsWrapper getExtensionRecords(DataResource resource, Long coreid) {
		ExtensionRecordsWrapper wrapper = new ExtensionRecordsWrapper(coreid);
		for (ViewExtensionMapping view : resource.getExtensionMappings()){
			wrapper.addExtensionRecords(getExtensionRecords(view.getExtension(), coreid, resource.getId()));
		}
		return wrapper;
	}
	public List<ExtensionRecord> getExtensionRecords(Extension extension, Long coreId, Long resourceId) {
		List<ExtensionRecord> records = new ArrayList<ExtensionRecord>();
		String table = namingStrategy.extensionTableName(extension);
		String sql = String.format("select * from %s where coreid=%s and resource_fk=%s", table, coreId, resourceId);
		Connection cn = getConnection();
		try {
			Statement st = cn.createStatement();			
			ResultSet result = st.executeQuery(sql); 
			// create extension records from JDBC resultset
			while (result.next()){
				ExtensionRecord rec = new ExtensionRecord(coreId, resourceId);
				for (ExtensionProperty p : extension.getProperties()){
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
			log.error(String.format("Couldn't read extension records for extension=%s", extension), e);
		}
		
		return records;
	}

}
