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
package org.gbif.provider.webapp.action.manage;

import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.SourceBase;
import org.gbif.provider.service.ExtensionManager;
import org.gbif.provider.service.SourceManager;
import org.gbif.provider.webapp.action.BaseDataResourceAction;

import com.opensymphony.xwork2.Preparable;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * TODO: Documentation.
 * 
 */
public class MappingAction extends BaseDataResourceAction implements Preparable {
  @Autowired
  private SourceManager sourceManager;
  @Autowired
  private ExtensionManager extensionManager;
  private List<Extension> extensions;
  private List<SourceBase> sources;
  private List<ExtensionMapping> extMappings;
  private ExtensionMapping coreMapping;

  @Override
  public String execute() {
    if (resource == null) {
      return RESOURCE404;
    }
    return SUCCESS;
  }

  public ExtensionMapping getCoreMapping() {
    return coreMapping;
  }

  public List<Extension> getExtensions() {
    return extensions;
  }

  public List<ExtensionMapping> getExtMappings() {
    return extMappings;
  }

  public List<SourceBase> getSources() {
    return sources;
  }

  @Override
  public void prepare() {
    super.prepare();
    sources = sourceManager.getAll(resourceId);
    coreMapping = resource.getCoreMapping();
    extMappings = resource.getExtensionMappings();
    extensions = extensionManager.getInstalledExtensions();
    // filter already mapped extensions
    for (ExtensionMapping map : resource.getAllMappings()) {
      extensions.remove(map.getExtension());
    }
  }
}
