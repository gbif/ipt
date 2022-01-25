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
package org.gbif.ipt.model.voc;

/**
 * Enumeration that describes the status of an identifier.
 */
public enum IdentifierStatus {
  /**
   * An identifier that is not reserved, meaning it is not known to the DOI registration agency at all yet.
   */
  UNRESERVED,
  /**
   * An identifier that is not public, but will go public the next time the resource is published so long as
   * the resource is public.
   */
  PUBLIC_PENDING_PUBLICATION,
  /**
   * An identifier that is public, meaning that it is known to resolvers.
   */
  PUBLIC,
  /**
   * An identifier that is public, but the object it references is no longer available.
   */
  UNAVAILABLE
}
