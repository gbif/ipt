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
 * The base class used to indicate types of resource publication errors. All configuration must provide a message and a
 * cause.
 */
public class PublicationException extends RuntimeException {

  public enum TYPE {
    /**
     * Exception occurred while publishing DwC-A.
     */
    DWCA,
    /**
     * Exception occurred while publishing either the EML file.
     */
    EML,
    /**
     * Exception occurred while publishing either the RTF file.
     */
    RTF,
    /**
     * Exception occurred while communicating with the GBIF Registry.
     */
    REGISTRY,
    /**
     * Exception occurred performing a DOI related operation, e.g. reserve DOI.
     */
    DOI,
    /**
     * Exception occurred while trying to schedule the resource for its next publication.
     */
    SCHEDULING,
    /**
     * Exception occurred while trying to publish the resource even though it is already being published.
     */
    LOCKED
  }

  protected TYPE type;

  /**
   * All configuration errors must have a type and a message. The message is useful in logging but not for display, as
   * it must be internationalized.
   *
   * @param type    Is stored in the exception
   * @param message The message to use for logging (not display through the web application)
   */
  public PublicationException(TYPE type, String message) {
    super(message);
    this.type = type;
  }

  public PublicationException(TYPE type, String message, Exception e) {
    super(message, e);
    this.type = type;
  }

  /**
   * @return the type of configuration exception. This allows for internationalized display
   */
  public TYPE getType() {
    return type;
  }
}
