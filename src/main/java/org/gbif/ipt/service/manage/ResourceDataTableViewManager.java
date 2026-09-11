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

import org.gbif.ipt.model.User;
import org.gbif.ipt.model.datatable.DatatableRequest;
import org.gbif.ipt.model.datatable.DatatableResult;

public interface ResourceDataTableViewManager {

  /**
   * List all resources in the IPT whose last published version was public (at the time of publication). This
   * is used to populate the list of resources publicly shown on the IPT home page.
   * Views (Simplified) - contain only fields required for the resources' table.
   * </br>
   * If a resource is registered with GBIF, it is assumed the resource is public and therefore is included in the list.
   * Please note only resources published using IPT v2.2 or later store a VersionHistory.
   *
   * @return list of resources wrapped by DatatableResult class
   */
  DatatableResult listPublishedPublicResourceSummaries(DatatableRequest request);

  /**
   * list all resource that can be managed by a given user.
   *
   * @param user User
   * @param request request parameters
   *
   * @return list of resources wrapped by DatatableResult class
   */
  DatatableResult list(User user, DatatableRequest request);
}
