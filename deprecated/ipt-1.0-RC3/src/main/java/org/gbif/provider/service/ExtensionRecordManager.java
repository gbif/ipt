/*
 * Copyright 2009 GBIF.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gbif.provider.service;

import org.gbif.provider.model.CoreRecord;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.dto.ExtendedRecord;
import org.gbif.provider.model.dto.ExtensionRecord;

import java.util.List;

/**
 * TODO: Documentation.
 * 
 */
public interface ExtensionRecordManager {
  int count(Extension extension, Long resourceId);

  int countDistinct(ExtensionProperty property, Long resourceId);

  ExtendedRecord extendCoreRecord(DataResource resource, CoreRecord coreRecord);

  List<ExtendedRecord> extendCoreRecords(DataResource resource,
      CoreRecord[] coreRecords);

  void insertExtensionRecord(DataResource resource, ExtensionRecord record);

  /**
   * Delete all extension records for a given resource that are linked to a core
   * record which is flagged as deleted
   * 
   * @param extension
   * @param resourceId
   */
  int removeAll(Extension extension, Long resourceId);

  int updateCoreIds(Extension extension, DataResource resource);
}
