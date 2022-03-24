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

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Base of all manager implementations.
 */
public abstract class BaseManager {

  protected final Logger LOG = LogManager.getLogger(this.getClass());
  protected AppConfig cfg;
  protected DataDir dataDir;

  private BaseManager() {
  }

  public BaseManager(AppConfig cfg, DataDir dataDir) {
    this.cfg = cfg;
    this.dataDir = dataDir;
  }

}
