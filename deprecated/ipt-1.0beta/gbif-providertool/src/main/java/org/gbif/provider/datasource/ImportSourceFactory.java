package org.gbif.provider.datasource;

import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.ViewMappingBase;

public interface ImportSourceFactory {

	public ImportSource newInstance(DataResource resource, ViewMappingBase view)
			throws ImportSourceException;

}