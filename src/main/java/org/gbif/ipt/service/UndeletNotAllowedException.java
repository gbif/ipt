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
package org.gbif.ipt.service;

/**
 * Exception thrown when undeleting a resource fails for some reason.
 */
public class UndeletNotAllowedException extends Exception {

  public enum Reason {
    /**
     * Because the prefix of the DOI does not match the prefix of the DOI account activated in the IPT.
     */
    DOI_PREFIX_NOT_MATCHING,
    /**
     * Because the resource organisation is no longer associated to the IPT.
     */
    ORGANISATION_NOT_ASSOCIATED_TO_IPT,
    /**
     * Because the DOI status is not deleted (after resolving it with DOI registration agency).
     */
    DOI_NOT_DELETED,
    /**
     * Because the DOI is no longer registered (no longer resolves).
     */
    DOI_DOES_NOT_EXIST,
    /**
     * Because some DOI Registration Agency error occurred.
     */
    DOI_REGISTRATION_AGENCY_ERROR,
    /**
     * Because the resource is registered with an organization that no longer exists.
     */
    RESOURCE_DOI_REGISTERED_WITH_ORGANISATION,
  }

  protected Reason reason;

  public UndeletNotAllowedException(Reason reason, String message) {
    super(message);
    this.reason = reason;
  }

  /**
   * @return the reason why the undelete operation is not possible. This allows for internationalized display
   */
  public Reason getReason() {
    return reason;
  }
}
