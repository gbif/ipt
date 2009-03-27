package org.gbif.provider.datasource;

import java.util.Iterator;

import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.ExtensionMapping;

public interface ImportSource extends Iterator<ImportRecord>, Iterable<ImportRecord>{
	public void close();
	public void init(DataResource resource, ExtensionMapping view) throws ImportSourceException;
}
