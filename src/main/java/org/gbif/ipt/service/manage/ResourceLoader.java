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

import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.utils.ActionLogger;

import javax.annotation.Nullable;
import java.io.File;

public interface ResourceLoader {

  /**
   * Calls {@link #load(File, User, ActionLogger, ResourceLoadCallbacks)}, inserting a new instance of ActionLogger.
   *
   * @param resourceDir   resource directory
   * @param creator       User that created resource (only used to populate creator when missing)
   * @param callbacks     additional callbacks
   * @return loaded Resource
   */
  Resource load(File resourceDir, @Nullable User creator, ResourceLoadCallbacks callbacks);

  /**
   * Reads a complete resource configuration (resource config & eml) from the resource config folder
   * and returns the Resource instance for the internal in memory cache.
   */
  Resource load(File resourceDir, @Nullable User creator, ActionLogger alog, ResourceLoadCallbacks callbacks) throws InvalidConfigException;

  /**
   * Loads a resource's inferred metadata from the XML file located inside its resource directory.
   * If no inferredMetadata.xml file was found, the resource is loaded with an empty InferredMetadata instance.
   *
   * @param resource resource
   */
  void loadInferredMetadata(Resource resource);
}
