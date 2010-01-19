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

import org.gbif.provider.model.DataResource;

import java.util.List;

/**
 * The full text indexing creation and searching.
 * 
 */
public interface FullTextSearchManager {

  /**
   * Builds the indexes required for the data resource which may include.
   * taxonomic or occurrence sources
   * 
   * @param resource to build
   */
  void buildDataResourceIndex(DataResource resource);

  /**
   * (Re)builds the whole metadata index spanning all resources (e.g. metadata
   * only)
   */
  void buildResourceIndex();

  /**
   * (Re)builds the metadata index for a single resource.
   * 
   * @param resourceId
   */
  void buildResourceIndex(Long resourceId);

  /**
   * @param resourceId To search within
   * @param q unparsed query string
   * @return List of core entity GUIDs
   */
  List<String> search(Long resourceId, String q);

  /**
   * do full text search on metadata of all published resources.
   * 
   * @param q unparsed query string
   * @return list of GUIDs matching
   */
  List<String> search(String q);

  /**
   * do full text search on metadata of all resources accessible to a given
   * user.
   * 
   * @param q unparsed query string
   * @return list of GUIDs for resources matching
   */
  List<String> search(String q, Long userId);
}
