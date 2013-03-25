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
package org.gbif.provider.service.impl;

import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.model.Resource;
import org.gbif.provider.service.ExtensionRecordManager;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.service.ViewMappingManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

/**
 * TODO: Documentation.
 * 
 */
public class ViewMappingManagerHibernate extends
    GenericResourceRelatedManagerHibernate<ExtensionMapping> implements
    ViewMappingManager {
  @Autowired
  private ExtensionRecordManager extensionRecordManager;
  @Autowired
  @Qualifier("propertyMappingManager")
  private GenericManager<PropertyMapping> propertyMappingManager;

  public ViewMappingManagerHibernate() {
    super(ExtensionMapping.class);
  }

  @Override
  public void remove(ExtensionMapping obj) {
    remove(obj, false);
  }

  @Override
  public int removeAll(Resource resource) {
    // make sure all existing extension records are removed too!
    List<ExtensionMapping> views = this.getAll(resource.getId());
    int i = 0;
    for (ExtensionMapping vm : views) {
      remove(vm, true);
      i++;
    }
    return i;
  }

  private void remove(ExtensionMapping obj, boolean force) {
    // cant delete core extension mappings unless forced. They will just be
    // emptied to look like new ones
    if (obj.isCore() && !force) {
      // reset core mapping
      obj.reset();
      this.save(obj);
      // remove property mappings for this view
      List<PropertyMapping> pms = obj.getPropertyMappingsSorted();
      for (PropertyMapping pm : pms) {
        propertyMappingManager.remove(pm);
      }
    } else {
      DataResource res = obj.getResource();
      // make sure all existing extension records are removed too!
      extensionRecordManager.removeAll(obj.getExtension(),
          obj.getResource().getId());
      res.removeExtensionMapping(obj);
      super.remove(obj);
      universalSave(res);
    }
  }
}
