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
package org.gbif.ipt.service.manage.impl;

import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.service.manage.ResourceUpdateListener;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResourceCoordinator implements ResourceUpdateListener {

  private static final Logger LOG = LogManager.getLogger(ResourceCoordinator.class);

  private final ResourceManager resourceManager;

  @Inject
  public ResourceCoordinator(SourceManager sourceManager, ResourceManager resourceManager) {
    this.resourceManager = resourceManager;
    sourceManager.addListener(this);
  }

  @Override
  public void onSourceUpdated(Resource resource) {
    LOG.info("Resource {} saved after changes to its source", resource.getShortname());
    resourceManager.save(resource);
  }
}

