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
package org.gbif.ipt.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Singleton;

@Singleton
public class ConfigWarnings {

  private static final Logger LOG = LogManager.getLogger(ConfigWarnings.class);
  private final List<String> startupErrors = new ArrayList<String>();

  public void addStartupError(Exception e) {
    if (e.getMessage() != null) {
      startupErrors.add(e.getMessage());
    }
    LOG.warn(e);
  }

  public void addStartupError(String message) {
    startupErrors.add(message);
  }

  public void addStartupError(String message, Exception e) {
    startupErrors.add(message);
    LOG.warn(message, e);
  }

  public List<String> getStartupErrors() {
    return startupErrors;
  }

  public boolean hasStartupErrors() {
    return !startupErrors.isEmpty();
  }

  /**
   * Clear startup errors.
   */
  public void clearStartupErrors() {
    getStartupErrors().clear();
  }

}
