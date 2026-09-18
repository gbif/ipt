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

import java.util.function.Consumer;

public interface ResourceLoadCallbacks {

  /**
   * Called on a non-data-package resource after a load, to sync its EML version/GUID/keywords
   * with the resource's current state (currently {@code ResourceManagerImpl::syncEmlWithResource})
   */
  void syncEml(Resource resource);

  /**
   * Called to persist a resource whose data package version was backfilled during a load
   * (currently {@code ResourceManagerImpl::save})
   */
  void save(Resource resource);

  static ResourceLoadCallbacks of(Consumer<Resource> emlSyncer, Consumer<Resource> resourceSaver) {
    return new ResourceLoadCallbacks() {
      @Override
      public void syncEml(Resource resource) {
        emlSyncer.accept(resource);
      }

      @Override
      public void save(Resource resource) {
        resourceSaver.accept(resource);
      }
    };
  }
}
