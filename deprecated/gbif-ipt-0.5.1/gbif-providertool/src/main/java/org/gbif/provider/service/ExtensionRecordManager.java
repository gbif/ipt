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
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.dto.ExtensionRecord;
import org.gbif.provider.model.dto.ExtensionRecordsWrapper;

public interface ExtensionRecordManager {
	public ExtensionRecordsWrapper getExtensionRecords(DataResource resource, Long coreid);
	public List<ExtensionRecord> getExtensionRecords(Extension extension, Long coreid, Long resourceId);
	public void insertExtensionRecord(ExtensionRecord record);
	/**
	 * Delete all extension records for a given resource that are linked to a core record which is flagged as deleted
	 * @param extension
	 * @param resourceId
	 */
	public int removeAll(Extension extension, Long resourceId);
}
