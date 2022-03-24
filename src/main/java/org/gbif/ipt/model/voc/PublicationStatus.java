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
package org.gbif.ipt.model.voc;

/**
 * Enumeration of possible publication statuses for the dataset. The status or visibility of a resource determines who
 * will be able to view it, whether viewing is limited (PRIVATE), open (PUBLIC), discoverable through the
 * GBIF Registry (REGISTERED), or deleted but still PUBLIC but since it was assigned a DOI (DELETED).
 */
public enum PublicationStatus {
  /**
   * The resource can only be seen by managers with rights.
   */
  PRIVATE,
  /**
   * The resource can be seen by the public.
   */
  PUBLIC,
  /**
   * The resource has been registered in the GBIF Registry and made globally discoverable.
   */
  REGISTERED,
  /**
   * The resource has been deleted, and previously assigned a DOI. It may or may not have been registered in GBIF.
   */
  DELETED
}
