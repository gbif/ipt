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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.ViewExtensionMapping;
import org.gbif.provider.model.dto.ExtensionRecord;
import org.gbif.provider.model.dto.ExtensionRecordsWrapper;
import org.gbif.provider.model.hibernate.IptNamingStrategy;
import org.gbif.provider.service.ExtensionRecordManager;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.NamingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly=true)
public class ExtensionRecordManagerJDBC implements ExtensionRecordManager {	
    protected static final Log log = LogFactory.getLog(ExtensionRecordManagerJDBC.class);
    @Autowired
	private SessionFactory sessionFactory;		
	@Autowired
	private IptNamingStrategy namingStrategy;

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
		String sql = String.format("insert into %s set coreid=%s, resource_fk=%s", table, rec.getCoreId(), rec.getResourceId());
		for (ExtensionProperty p : rec){
			sql += String.format(", %s='%s'", namingStrategy.propertyToColumnName(p.getName()), rec.getPropertyValue(p));
		}
		Connection cn = getConnection();
		PreparedStatement ps = null;
		try {
			ps = cn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			int count = ps.executeUpdate(sql);
		} catch (SQLException e) {
			if (rec.getExtension()==null){
				log.error(String.format("Couldn't insert record for extension=NULL", rec.getExtension()), e);
			}else{
				log.error(String.format("Couldn't insert record for extension %s", rec.getExtension().getName()), e);
			}
		}finally{
			if (ps!=null){
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Transactional(readOnly=false)
	public int removeAll(Extension extension, Long resourceId) {
		String table = namingStrategy.extensionTableName(extension);
		String sql = String.format("delete from %s where resource_fk=%s", table, resourceId);
		Connection cn = getConnection();
		PreparedStatement ps = null;
		try {
			ps = cn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			int count = ps.executeUpdate(sql);
			log.debug(String.format("Removed %s records for extension %s", count, extension.getName()));
		} catch (SQLException e) {
			log.error(String.format("Couldn't rmove all records for extension %s", extension.getName()));
			e.printStackTrace();
		}finally{
			if (ps!=null){
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return 0;
	}
	
	
	public ExtensionRecordsWrapper getExtensionRecords(DatasourceBasedResource resource, Long coreid) {
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
		PreparedStatement ps = null;
		try {
			ps = cn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet result = ps.executeQuery(sql);
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
		}finally{
			if (ps!=null){
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
		return records;
	}
}
