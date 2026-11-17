/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.ipt.service.manage;

import java.util.UUID;

import javax.annotation.Nullable;

/**
 * The literature citing the resources registered with GBIF, as counted by the GBIF API. Fetched in the background
 * about once a day; pages only read the cached counts, so visitors never cause API calls.
 */
public interface ResourceCitationsManager {

  /**
   * The papers citing a registered resource, or null if it isn't registered or hasn't been fetched yet.
   *
   * @param key the resource's GBIF dataset key
   */
  @Nullable
  Long getCitations(@Nullable UUID key);

  /**
   * The papers citing any of the registered resources, each counted once; null if there are no registered resources
   * or the count hasn't been fetched yet.
   */
  @Nullable
  Long getCitationsTotal();
}
