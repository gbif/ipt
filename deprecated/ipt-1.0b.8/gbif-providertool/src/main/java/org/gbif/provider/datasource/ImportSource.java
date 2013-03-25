package org.gbif.provider.datasource;

import java.util.Iterator;

import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.ViewCoreMapping;
import org.gbif.provider.model.ViewExtensionMapping;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.model.dto.ExtensionRecord;

public interface ImportSource extends Iterator<ImportRecord>, Iterable<ImportRecord>{
	public void close();
	public void init(DataResource resource, ViewCoreMapping view) throws ImportSourceException;
	public void init(DataResource resource, ViewExtensionMapping view) throws ImportSourceException;
}
