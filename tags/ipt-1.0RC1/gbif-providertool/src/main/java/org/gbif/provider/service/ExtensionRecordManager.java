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

import java.util.List;

import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.dto.ExtendedRecord;
import org.gbif.provider.model.dto.ExtensionRecord;

public interface ExtensionRecordManager {
	public ExtendedRecord extendCoreRecord(DataResource resource, CoreRecord coreRecord);
	public List<ExtendedRecord> extendCoreRecords(DataResource resource, CoreRecord[] coreRecords);
	public void insertExtensionRecord(DataResource resource, ExtensionRecord record);
	public int updateCoreIds(Extension extension, DataResource resource);
	/**
	 * Delete all extension records for a given resource that are linked to a core record which is flagged as deleted
	 * @param extension
	 * @param resourceId
	 */
	public int removeAll(Extension extension, Long resourceId);
	public int count(Extension extension, Long resourceId);
	public int countDistinct(ExtensionProperty property, Long resourceId);
}
