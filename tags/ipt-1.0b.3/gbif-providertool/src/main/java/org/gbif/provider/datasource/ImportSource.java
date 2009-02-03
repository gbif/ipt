package org.gbif.provider.datasource;

import java.util.Iterator;

import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.dto.ExtensionRecord;

public interface ImportSource extends Iterator<ImportRecord>, Iterable<ImportRecord>{
	public void close();
}
