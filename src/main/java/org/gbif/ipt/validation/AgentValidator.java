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
package org.gbif.ipt.validation;

import org.gbif.metadata.eml.Agent;

/**
 * Perform all kind of validation to the Agent object.
 */
public class AgentValidator extends BaseValidator {

  /**
   * This method validates whether an agent has the minimum information to be contacted.
   *
   * @return true if name (at least lastname) and email exist. Otherwise return false.
   */
  public static boolean hasCompleteContactInfo(Agent agent) {
    return agent != null && agent.getFullName() != null && !(agent.getFullName().length() == 0)
      && agent.getEmail() != null && !(agent.getEmail().length() == 0);

  }
}
