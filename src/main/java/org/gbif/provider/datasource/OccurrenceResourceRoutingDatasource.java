package org.gbif.provider.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class OccurrenceResourceRoutingDatasource extends AbstractRoutingDataSource {

	@Override
	protected Object determineCurrentLookupKey() {
		// TODO determine current resource via session
		return "pontaurus";
	}

}
