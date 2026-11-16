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
package org.gbif.ipt.service.admin;

import org.gbif.ipt.model.BulkPublication;
import org.gbif.ipt.model.Resource;

import java.util.Collection;
import java.util.List;

/**
 * Publishes many resources at once in the background, each as on the resource overview: a registered resource
 * updates its own GBIF registration when it's published. The IPT's own registration is updated on the GBIF
 * registration page, not here. One run at a time; the last run is kept in memory until the next.
 */
public interface BulkPublicationManager {

  /**
   * @return the running or last finished run since the IPT started, or null
   */
  BulkPublication getCurrent();

  /**
   * @return true if a run is in progress
   */
  boolean isRunning();

  /**
   * @return why the resource can't be published now, or null if it can
   */
  BulkPublication.NotReady notReady(Resource resource);

  /**
   * @return true if the resource is included in the "changed since last published" scope
   */
  boolean isChanged(Resource resource);

  /**
   * @return the resources the scope covers, ready or not, in the order they're published
   */
  List<Resource> resourcesInScope(BulkPublication.Scope scope, Collection<String> selected, Collection<String> excluded);

  /**
   * Starts a run in the background and returns at once.
   *
   * @param scope     which resources to publish
   * @param selected  shortnames for the SELECTED scope
   * @param excluded  shortnames for the EXCLUDED scope
   * @param startedBy name of the admin who started it
   *
   * @return the run
   *
   * @throws IllegalStateException if a run is in progress
   */
  BulkPublication start(BulkPublication.Scope scope, Collection<String> selected, Collection<String> excluded,
                        String startedBy);
}
