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

import org.gbif.ipt.model.InferredMetadata;
import org.gbif.ipt.model.Resource;

/**
 * Keeps the metadata inferred from the source data of resources up to date: small datasets are inferred right away,
 * big ones in the background, so that pages don't wait for them.
 */
public interface InferredMetadataManager {

  /**
   * Inferred metadata to show on a coverage page. If it is outdated, or re-inferring is requested, it is inferred
   * again: right away for small datasets, in the background for big ones, returning the stored metadata meanwhile.
   *
   * @param resource resource
   * @param reinfer  whether re-inferring was requested
   *
   * @return inferred metadata of the type of the resource (EML or Camtrap DP), empty if never inferred
   */
  InferredMetadata getInferredMetadata(Resource resource, boolean reinfer);

  /**
   * Up-to-date inferred metadata, e.g. for publishing: waits for inferring in the background to finish if it has
   * started, runs it right away if it is still waiting behind other resources' jobs, and infers right away if the
   * stored metadata is outdated or the data can have changed (SQL and URL sources).
   *
   * @param resource resource
   *
   * @return inferred metadata of the type of the resource (EML or Camtrap DP)
   */
  InferredMetadata getUpToDateInferredMetadata(Resource resource);

  /**
   * @param shortname resource shortname
   *
   * @return whether the metadata of the resource is being inferred in the background
   */
  boolean isInferring(String shortname);
}
