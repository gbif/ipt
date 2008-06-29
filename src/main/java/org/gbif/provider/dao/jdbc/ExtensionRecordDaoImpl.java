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

package org.gbif.provider.dao.jdbc;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.gbif.provider.dao.DarwinCoreDao;
import org.gbif.provider.dao.DatasourceInspectionDao;
import org.gbif.provider.dao.ExtensionRecordDao;
import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionRecord;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;
import org.springframework.stereotype.Repository;

public class ExtensionRecordDaoImpl extends SimpleJdbcDaoSupport implements ExtensionRecordDao {	

	public void insertExtensionRecord(ExtensionRecord record) {
		String sql = "";
		try {
			PreparedStatement ps = this.getConnection().prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = ps.executeQuery();
		} catch (CannotGetJdbcConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void insertExtensionRecords(ExtensionRecord[] records) {
		// TODO Auto-generated method stub
		
	}

	public void deleteOrphans(Extension extension, Long resourceId) {
		// TODO Auto-generated method stub
		
	}
}
