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

import org.gbif.api.model.common.DOI;
import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.PublicationException;

import java.math.BigDecimal;

import javax.annotation.Nullable;

/**
 * Registers and updates the DOIs of published resources with the DOI registration agency.
 */
public interface ResourceDoiService {

  /**
   * Depending on the state of the resource and its DOI, execute one of the following operations:
   * - Register DOI
   * - Update DOI
   * - Register DOI and replace previous DOI
   * <p>
   * Nothing is done if the resource has no DOI, isn't publicly available, or its DOI isn't
   * PUBLIC/PUBLIC_PENDING_PUBLICATION.
   *
   * @param resource        resource published
   * @param version         resource version being published
   * @param versionReplaced resource version being replaced
   * @param action          action
   * @throws PublicationException thrown if any part of DOI workflow failed
   */
  void executeDoiWorkflow(Resource resource, BigDecimal version, BigDecimal versionReplaced, BaseAction action)
      throws PublicationException;

  /**
   * Update DOI metadata. The DOI URI isn't changed. This is done for each minor version change.
   *
   * @param resource resource whose DOI will be updated
   */
  void updateDoi(Resource resource);

  /**
   * Replace DOI currently assigned to resource with new DOI that has been reserved for resource.
   * This corresponds to a new major version change.
   *
   * @param resource        resource whose DOI will be registered
   * @param version         new version
   * @param replacedVersion previous version being replaced
   */
  void replaceDoi(Resource resource, BigDecimal version, BigDecimal replacedVersion);

  /**
   * Register DOI. Corresponds to a major version change.
   *
   * @param resource resource whose DOI will be registered
   * @param replaced DOI of the version this one replaces, if any
   */
  void registerDoi(Resource resource, @Nullable DOI replaced);
}
