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

import org.gbif.provider.model.Resource;
import org.gbif.provider.model.ResourceRelatedObject;

import java.util.List;

/**
 * TODO: Documentation
 * 
 * @param <T>
 */
public interface GenericResourceRelatedManager<T extends ResourceRelatedObject>
    extends GenericManager<T> {
  int count(Long resourceId);

  /**
   * Retrieves all records linked to a given resource.
   * 
   * @param resourceId of the resource in question
   * @return number of deleted instances of T
   */
  List<T> getAll(Long resourceId);

  /**
   * Delete all records linked to a given resource.
   * 
   * @param resource that contains the records to be removed
   * @return number of deleted instances of T
   */
  int removeAll(Resource resource);
}
