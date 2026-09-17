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

public interface ResourceMetadataLoader {

  /**
   * Loads a resource's metadata from its eml.xml file located inside its resource directory. If no eml.xml file was
   * found, the resource is loaded with an empty EML instance.
   *
   * @param resource resource
   */
  void loadEml(Resource resource);

  /**
   * Loads a resource's metadata from its datapackage.json (for frictionless) or metadata.yaml (for ColDP) file located
   * inside its resource directory.
   * If no file was found, the resource is loaded with an empty metadata class instance.
   *
   * @param resource resource
   */
  void loadDatapackageMetadata(Resource resource);

  void loadMetadata(Resource resource);

}
