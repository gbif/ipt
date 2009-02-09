package org.gbif.provider.datasource;

import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.ViewCoreMapping;
import org.gbif.provider.model.ViewExtensionMapping;
import org.gbif.provider.model.ViewMappingBase;

public interface ImportSourceFactory {

	public ImportSource newInstance(DataResource resource, ViewCoreMapping view) throws ImportSourceException;
	public ImportSource newInstance(DataResource resource, ViewExtensionMapping view) throws ImportSourceException;

}