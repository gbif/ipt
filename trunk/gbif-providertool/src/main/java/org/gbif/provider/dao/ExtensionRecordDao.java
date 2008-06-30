package org.gbif.provider.dao;

import java.util.Collection;
import java.util.Map;

import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.ExtensionRecord;

public interface ExtensionRecordDao {
	public void insertExtensionRecord(ExtensionRecord record);
	public void insertExtensionRecords(ExtensionRecord[] records);
	/**
	 * Delete all extension records for a given resource that are linked to a core record which is flagged as deleted
	 * @param extension
	 * @param resourceId
	 */
	public void deleteOrphans(Extension extension, Long resourceId);
}
