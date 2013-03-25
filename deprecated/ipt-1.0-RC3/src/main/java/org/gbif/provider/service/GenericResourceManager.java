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

import org.gbif.provider.model.BBox;
import org.gbif.provider.model.Resource;

import java.util.List;

/**
 * TODO: Documentation.
 * 
 * @param <T>
 */
public interface GenericResourceManager<T extends Resource> extends
    GenericManager<T> {
  /**
   * Get resource by GUID.
   * 
   * @param guid of resource to be returned
   * @return
   */
  T get(String guid);

  /**
   * retrieve all resource IDs that have been published already.
   * 
   * @return list of resourceIDs
   */
  List<Long> getPublishedResourceIDs();

  List<T> getPublishedResources();

  /**
   * Return all resources created by that user.
   * 
   * @param userId
   * @return
   */
  List<T> getResourcesByUser(Long userId);

  /**
   * get latest modified resources.
   * 
   * @param startPage starting page, first page = 1
   * @param pageSize
   * @return
   */
  List<T> latest(int startPage, int pageSize);

  /**
   * Publishes a resource, creating a new EML document version and registering
   * the resource with GBIF if not already registered. Also tries to
   * write/update the geoserver entry and lucene index (which doesnt happen
   * through simple saves)
   * 
   * @param resourceId
   */
  Resource publish(Long resourceId);

  List<T> search(String q);

  List<T> searchByBBox(BBox bbox);

  List<T> searchByKeyword(String keyword);

  /**
   * Unpublishes a resource, i.e. removes the GBIF registry entry and flag the
   * resource object & lucene index entry so it doesnt show up in the public
   * portal / searches anymore. Also removes the geoserver entry in case of
   * occurrence resources
   * 
   * leaves all archived EML documents, but doesnt advertise them anymore.
   * 
   * @param resourceId
   */
  void unPublish(Long resourceId);

}
