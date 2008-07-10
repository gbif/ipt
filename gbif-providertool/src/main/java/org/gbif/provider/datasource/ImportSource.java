package org.gbif.provider.datasource;

import java.util.Iterator;

import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.ExtensionRecord;

public interface ImportSource extends Iterator<ImportRecord>, Iterable<ImportRecord>{
	
}
