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

import java.util.Set;
import java.util.concurrent.Future;

/**
 * TODO: Documentation.
 * 
 */
public interface CacheManager {
  /**
   * Analyze cache database, updating statistics to speed up selects
   * 
   */
  void analyze();

  /**
   * @param resourceId
   */
  void cancelUpload(Long resourceId);

  /**
   * Remove all data in cache related to this resource apart from the resource
   * instance itself. I.e. the resource will be in a state afterwards just as if
   * it has just been newly created. The filesystem is not touched. This is
   * handled by the ResourceManager.remove() method.
   * 
   * Be very careful when using this method as it removes all resource data!
   * When preparing a new upload this method should *not* be used as it also
   * removes the upload history and all core records as opposed to just flag
   * them for deletion. Use prepareUpload instead.
   * 
   * @param resource
   */
  void clear(Long resourceId);

  /**
   * Retrieve the currently running upload jobs
   * 
   * @return
   */
  Set<Long> currentUploads();

  /**
   * Get a simple human readable one liner that explains the current status of
   * an upload job
   * 
   * @param resource
   * @return
   */
  String getUploadStatus(Long resourceId);

  /**
   * Returns true if a resource is busy uploading or postprocessing data
   * 
   * @param resourceId
   * @return
   */
  boolean isBusy(Long resourceId);

  /**
   * Clear all cached upload artifacts but leaves data which is supposed to last
   * between multiple uploads, i.e uploadEvents and darwin core records. Flags
   * all darwin core records as deleted though. When preparing a new upload this
   * method should be used to clean up old upload artifacts.
   * 
   * @param resource
   */
  void prepareUpload(Long resourceId);

  /**
   * Submit a new upload job (incl postprocessing) to the executor service.
   * Throws an exception in case this resource has already a scheduled or
   * running upload job
   * 
   * @param maxRecords stop the upload after this maximum amount of record has
   *          been uploaded. Mainly for testing.
   * @param resource
   * @return
   */
  Future runUpload(Long resourceId);
}
