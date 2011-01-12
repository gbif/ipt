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
package org.gbif.provider.webapp.action.admin;

import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.hibernate.IptNamingStrategy;
import org.gbif.provider.service.ExtensionManager;
import org.gbif.provider.webapp.action.BaseAction;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO: Documentation.
 * 
 */
public class ExtensionAction extends BaseAction {
  @Autowired
  private ExtensionManager extensionManager;
  @Autowired
  private IptNamingStrategy namingStrategy;
  private List<Extension> extensions;
  private Extension extension;
  private final Map<String, List<ExtensionProperty>> properties = new HashMap<String, List<ExtensionProperty>>();
  private String tableName;
  private Long id;

  public String add() {
    extension = extensionManager.get(id);
    extensionManager.installExtension(extension);
    tableName = namingStrategy.extensionTableName(extension);
    return SUCCESS;
  }

  @Override
  public String execute() {
    extension = extensionManager.get(id);
    for (ExtensionProperty prop : extension.getProperties()) {
      String group = prop.getGroup() == null ? extension.getName()
          : prop.getGroup();
      if (!properties.containsKey(group)) {
        properties.put(group, new ArrayList<ExtensionProperty>());
      }
      properties.get(group).add(prop);
    }
    tableName = namingStrategy.extensionTableName(extension);
    return SUCCESS;
  }

  public Extension getExtension() {
    return extension;
  }

  public List<Extension> getExtensions() {
    return extensions;
  }

  public Long getId() {
    return id;
  }

  public Map<String, List<ExtensionProperty>> getProperties() {
    return properties;
  }

  public String getTableName() {
    return tableName;
  }

  public String list() {
    extensions = extensionManager.getAll();
    return SUCCESS;
  }

  public String remove() {
    extension = extensionManager.get(id);
    extensionManager.removeExtension(extension);
    tableName = namingStrategy.extensionTableName(extension);
    return SUCCESS;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String synchroniseAll() {
    extensionManager.synchroniseExtensionsWithRepository();
    return SUCCESS;
  }

}
