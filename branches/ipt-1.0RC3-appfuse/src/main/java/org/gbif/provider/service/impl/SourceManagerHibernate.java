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

import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.SourceBase;
import org.gbif.provider.model.SourceFile;
import org.gbif.provider.service.SourceManager;
import org.gbif.provider.service.ViewMappingManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * TODO: Documentation.
 * 
 */
public class SourceManagerHibernate extends
    GenericResourceRelatedManagerHibernate<SourceBase> implements SourceManager {
  @Autowired
  private ViewMappingManager viewMappingManager;

  public SourceManagerHibernate() {
    super(SourceBase.class);
    // TODO Auto-generated constructor stub
  }

  public SourceFile getSourceByFilename(Long resourceId, String filename) {
    return (SourceFile) query(
        "from SourceFile s WHERE s.resource.id = :resourceId and s.name = :filename").setLong(
        "resourceId", resourceId).setString("filename", filename).uniqueResult();
  }

  @Override
  @Transactional(readOnly = false)
  public void remove(SourceBase obj) {
    // also remove all ViewMappings that are based on this source
    List<ExtensionMapping> views = viewMappingManager.getAll(obj.getResource().getId());
    for (ExtensionMapping vm : views) {
      if (vm != null && vm.getSource().equals(obj)) {
        // view mapping uses this source, so also delete it!
        viewMappingManager.remove(vm);
      }
    }
    // finally remove the source itself
    super.remove(obj);
  }

}
