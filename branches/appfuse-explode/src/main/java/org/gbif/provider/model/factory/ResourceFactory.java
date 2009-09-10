/*
 * Copyright 2009 GBIF.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gbif.provider.model.factory;

import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.util.Constants;

/**
 * TODO: Documentation.
 * 
 */
public class ResourceFactory {
  private final GenericManager<Extension> extensionManager;

  public ResourceFactory(GenericManager<Extension> extensionManager) {
    super();
    this.extensionManager = extensionManager;
  }

  public ChecklistResource newChecklistResourceInstance() {
    ChecklistResource resource = new ChecklistResource();
    initCoreMapping(resource);
    return resource;
  }

  public Resource newMetadataResourceInstance() {
    Resource resource = new Resource();
    return resource;
  }

  public OccurrenceResource newOccurrenceResourceInstance() {
    OccurrenceResource resource = new OccurrenceResource();
    initCoreMapping(resource);
    return resource;
  }

  public Resource newResourceInstance(Class resourceClass) {
    Resource res = null;
    if (resourceClass.isAssignableFrom(OccurrenceResource.class)) {
      res = newOccurrenceResourceInstance();
    } else if (resourceClass.isAssignableFrom(ChecklistResource.class)) {
      res = newChecklistResourceInstance();
    } else {
      res = newMetadataResourceInstance();
    }
    return res;
  }

  private void initCoreMapping(DataResource resource) {
    ExtensionMapping coreVM = new ExtensionMapping();
    Extension core = extensionManager.get(Constants.DARWIN_CORE_EXTENSION_ID);
    coreVM.setExtension(core);
    resource.addExtensionMapping(coreVM);
  }
}
