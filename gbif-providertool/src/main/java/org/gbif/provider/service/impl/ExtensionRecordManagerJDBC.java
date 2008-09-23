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

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionRecord;
import org.gbif.provider.model.Resource;
import org.gbif.provider.service.ExtensionRecordManager;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
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
	@Qualifier("dataSource")
	private DataSource dataSource;

	private Connection getConnection() throws SQLException {
		Session s = SessionFactoryUtils.getSession(sessionFactory, false);
		Connection cn = s.connection();
//		Connection cn = dataSource.getConnection();
		return cn;
	}
	
	private void executeSQL(String sql) throws SQLException{
		//FIXME: implement extension upload
//		Connection cn = getConnection();
//		PreparedStatement ps = cn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
//		try {
//			ps.execute();
//		}finally{
//			ps.close();
//		}
	}

	@Transactional(readOnly=false)
	public void insertExtensionRecord(ExtensionRecord record) {
		//FIXME implement
		String sql = "insert into play set text='hallo'";
		try {
			executeSQL(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Transactional(readOnly=false)
	public void insertExtensionRecords(ExtensionRecord[] records) {
		//FIXME implement
	}

	@Transactional(readOnly=false)
	public int removeAll(Extension extension, Long resourceId) {
		log.debug(String.format("Removed %s records for extension %s", 0, extension.getName()));
		//FIXME implement
		return 0;
	}
}
