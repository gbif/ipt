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
 * Enumeration of possible publication modes for the dataset. The mode determines whether or not the dataset must be
 * republished manually every time, or whether it can be automatically published by the IPT.
 */
public enum PublicationMode {
  /**
   * The dataset will be auto-published. The resource manager has made a decision to auto-publish the dataset.
   */
  AUTO_PUBLISH_ON,
  /**
   * The dataset will not be auto-published.
   */
  AUTO_PUBLISH_OFF
}
