package org.gbif.provider.datasource;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.model.DatasourceBasedResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class ExternalResourceRoutingDatasource extends AbstractRoutingDataSource {
    protected final Log log = LogFactory.getLog(getClass());

    @Autowired
	private DatasourceRegistry registry;

	@Override
	protected Object determineCurrentLookupKey() {
		//TODO: implement session based lookup of current resourceId
		log.info("Active datasources: "+registry.keySet());
		Long resourceId = 2L;
		return resourceId;
	}

}
