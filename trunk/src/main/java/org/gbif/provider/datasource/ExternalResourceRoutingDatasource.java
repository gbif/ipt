package org.gbif.provider.datasource;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.model.DatasourceBasedResource;
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
		return dataSource;
	}

}
