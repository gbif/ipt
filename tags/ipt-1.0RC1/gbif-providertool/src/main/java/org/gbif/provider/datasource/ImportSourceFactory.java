package org.gbif.provider.datasource;

import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.ExtensionMapping;

public interface ImportSourceFactory {
	public ImportSource newInstance(DataResource resource, ExtensionMapping view) throws ImportSourceException;
}