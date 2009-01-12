/***************************************************************************
* Copyright (C) 2008 Global Biodiversity Information Facility Secretariat.
* All Rights Reserved.
*
* The contents of this file are subject to the Mozilla Public
* License Version 1.1 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of
* the License at http://www.mozilla.org/MPL/
*
* Software distributed under the License is distributed on an "AS
* IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
* implied. See the License for the specific language governing
* rights and limitations under the License.

***************************************************************************/

package org.gbif.provider.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.DarwinCore;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.dto.CommonName;
import org.gbif.provider.model.dto.Distribution;
import org.gbif.provider.model.dto.ExtendedRecord;
import org.gbif.provider.model.dto.ExtensionRecord;
import org.gbif.provider.model.dto.ExtensionRecordsWrapper;
import org.gbif.provider.tapir.Filter;

public interface ExtensionRecordManager {
	public ExtensionRecordsWrapper getExtensionRecords(DataResource resource, Long coreid);
	public List<ExtensionRecord> getExtensionRecords(Extension extension, Long coreid, Long resourceId);
	/**
	 * @param property
	 * @param resourceId
	 * @param start start record. First=0
	 * @param limit number of maximum records to be returned
	 * @return
	 */
	public List<Object> getDistinct(ExtensionProperty property, Long resourceId, int start, int limit);
	// TAPIR operations
	public List<ExtendedRecord> extendCoreRecords(DataResource resource, CoreRecord[] coreRecords);
	/** get a list of distinct value tuples for a list of properties.
	 * All properties must belong to extensions of the same core, i.e. the extension is either the core itself or its type indicates the same ExtensionType
	 * @param properties
	 * @param resourceId
	 * @param start start record. First=0
	 * @param limit number of maximum records to be returned
	 * @return
	 */
	public List<Map<ExtensionProperty, Object>> getDistinct(List<ExtensionProperty> properties, Filter filter, Long resourceId, int start, int limit);

	public List<CommonName> getCommonNames(Long taxonId);
	public List<Distribution> getDistributions(Long taxonId);
	public void insertExtensionRecord(ExtensionRecord record);
	/**
	 * Delete all extension records for a given resource that are linked to a core record which is flagged as deleted
	 * @param extension
	 * @param resourceId
	 */
	public int removeAll(Extension extension, Long resourceId);
	public int count(Extension extension, Long resourceId);
	public int countDistinct(ExtensionProperty property, Long resourceId);
}
