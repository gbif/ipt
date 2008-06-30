package org.gbif.provider.service;

import org.gbif.provider.datasource.ImportSource;
import org.gbif.provider.model.DatasourceBasedResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.UploadEvent;
import java.util.Map;
import java.util.List;

public interface OccurrenceUploadManager {
	
	/**
	 * Upload data for the core and return a mapping from localIDs to coreIDs.
	 * The coreIDs are used by the extensions to relate to the core record.
	 * @param source the iterable ImportSource to import from
	 * @param resource the resource the data will be attached to
	 * @param extension the description of the core properties
	 * @param event the empty upload event going to be filled with upload statistics
	 * @return
	 */
	public Map<String, Long> uploadCore(ImportSource source, OccurrenceResource resource, UploadEvent event);
	
	/**
	 * Upload data for an extension and relate it to the core via the supplied idMap
	 * @param source the iterable ImportSource to import from
	 * @param idMap a mapping from local IDs to coreIDs
	 * @param resource the resource the data will be attached to
	 * @param extension description of the extension properties
	 */
	public void uploadExtension(ImportSource source, Map<String, Long> idMap, OccurrenceResource resource, Extension extension);
}
