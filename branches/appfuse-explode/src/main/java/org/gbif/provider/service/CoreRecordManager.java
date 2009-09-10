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
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.dto.ValueListCount;
import org.gbif.provider.tapir.filter.Filter;

import org.hibernate.ScrollableResults;

import java.util.List;

/**
 * TODO: Documentation.
 * 
 * @param <T>
 */
public interface CoreRecordManager<T extends CoreRecord> extends
    GenericResourceRelatedManager<T> {

  /**
   * Find a core record via its local ID within a given resource. This method
   * assures to return a single record, as the combination is guaranteed to be
   * unique in the database.
   * 
   * @See save
   * @param sourceId the local identifier used in the source
   * @param resourceId the resource identifier for the source
   * @return
   */
  T findBySourceId(String sourceId, Long resourceId);

  /**
   * Flag all core records for a given resource as deleted by setting
   * coreRecord.isDeleted=true
   * 
   * @param resource that contains the core records to be flagged
   */
  void flagAllAsDeleted(DataResource resource);

  /**
   * get single record by its GUID
   * 
   * @param Id
   * @return
   */
  T get(String guid);

  List<T> getAll(final Long resourceId);

  List<ValueListCount> inventory(Long resourceId,
      List<ExtensionProperty> properties, Filter filter, int start, int limit);

  int inventoryCount(Long resourceId, List<ExtensionProperty> properties,
      Filter filter);

  /**
   * get latest modified resources
   * 
   * @param startPage starting page, first page = 1
   * @param pageSize
   * @return
   */
  List<T> latest(Long resourceId, int startPage, int pageSize);

  /**
   * Generic method to save a core record - handles both update and insert. Make
   * sure the local_id + resource_fk combination is unique, otherwise you will
   * get an EntityExistsException
   * 
   * @param object the object to save
   * @return the updated object
   */
  T save(T object);

  ScrollableResults scrollResource(final Long resourceId);

  List<T> search(Long resourceId, Filter filter, int start, int limit);

  /**
   * full text search in all core records of a given resource
   * 
   * @param resourceId
   * @param q
   * @return
   */
  List<T> search(Long resourceId, String q);

  int searchCount(Long resourceId, Filter filter);

}
