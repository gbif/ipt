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

package org.gbif.provider.datasource;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.model.DataResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.util.Assert;

public class ExternalResourceRoutingDatasource extends AbstractRoutingDataSource {
    protected final Log log = LogFactory.getLog(getClass());

    @Autowired
	private DatasourceRegistry registry;

	@Override
	protected Long determineCurrentLookupKey() {
		Long resourceId = DatasourceContextHolder.getResourceId();
		log.info("Active datasource determined: "+resourceId);
		return resourceId;
	}

	@Override
	protected DataSource determineTargetDataSource() {
		Long lookupKey = determineCurrentLookupKey();
		DataSource dataSource = (DataSource) this.registry.getDataSource(lookupKey);
		if (dataSource == null) {
			throw new IllegalStateException("Cannot determine target DataSource for resource key [" + lookupKey + "]");
		}
		// confirm that connection is open... just so we know whats happening
		try {
			if (dataSource.getConnection().isClosed()){
				log.warn("Datasource db connection is closed!");
			}
		} catch (SQLException e) {
			log.warn("Datasource db connection is closed!", e);
		}
		return dataSource;
	}

}
