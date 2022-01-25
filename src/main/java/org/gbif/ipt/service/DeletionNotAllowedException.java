/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
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
 * Exception thrown when removing an entity is not allowed for some reason.
 */
public class DeletionNotAllowedException extends Exception {

  public enum Reason {
    /**
     * Because this is the last administrator. Used while deleting a user.
     */
    LAST_ADMIN,
    /**
     * Because this is the last resource manager. Used while deleting a user.
     */
    LAST_RESOURCE_MANAGER,
    /**
     * Because this is the original creator of the resource, which cannot be deleted. Used while deleting a user.
     */
    IS_RESOURCE_CREATOR,
    /**
     * Because the extension has been mapped in at least one resource. Used while deleting an extension.
     */
    EXTENSION_MAPPED,
    /**
     * Because the vocabulary is a default vocabulary. Used while deleting a vocabulary.
     */
    BASE_VOCABULARY,
    /**
     * Because there is at least one resource registered to this organization. Used while deleting an organization.
     */
    RESOURCE_REGISTERED_WITH_ORGANISATION,
    /**
     * Because the IPT is registered against this organization. Used while deleting an organization.
     */
    IPT_REGISTERED_WITH_ORGANISATION,
    /**
     * Because some registry error occurred.
     */
    REGISTRY_ERROR,
    /**
     * Because some DOI Registration Agency error occurred.
     */
    DOI_REGISTRATION_AGENCY_ERROR,
    /**
     * Because there is at least one resource whose DOI is registered with this organization. Used while deleting
     * an organization.
     */
    RESOURCE_DOI_REGISTERED_WITH_ORGANISATION,
  }

  protected Reason reason;

  public DeletionNotAllowedException(Reason reason) {
    this.reason = reason;
  }

  public DeletionNotAllowedException(Reason reason, String message) {
    super(message);
    this.reason = reason;
  }

  /**
   * @return the reason why the deletion is not possible. This allows for internationalized display
   */
  public Reason getReason() {
    return reason;
  }
}
