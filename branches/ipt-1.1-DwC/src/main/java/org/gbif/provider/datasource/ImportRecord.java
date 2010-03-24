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
package org.gbif.provider.datasource;

import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * A raw record with column values mapped to an extension property already, the
 * properties map.
 * 
 */
public class ImportRecord {
  private Map<ExtensionProperty, String> properties = new HashMap<ExtensionProperty, String>();
  private Extension extension;
  private final Long resourceId;
  private final String sourceId;
  private String guid;
  private String link;

  public ImportRecord(Long resourceId, String sourceId) {
    super();
    this.sourceId = sourceId;
    this.resourceId = resourceId;
  }

  /**
   * Get the extension this record belongs to. The property is being set by the
   * set properties methods which guarantee that all properties of this record
   * belong to the same extension.
   * 
   * @return
   */
  public Extension getExtension() {
    return extension;
  }

  public String getGuid() {
    return guid;
  }

  public String getLink() {
    return link;
  }

  public Map<ExtensionProperty, String> getProperties() {
    return properties;
  }

  public String getPropertyValue(ExtensionProperty property) {
    return properties.get(property);
  }

  public Long getResourceId() {
    return resourceId;
  }

  public String getSourceId() {
    return sourceId;
  }

  public void setGuid(String guid) {
    this.guid = guid;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public void setPropertyValue(ExtensionProperty property, String value) {
    // check if this is the first property ever set.
    // If so, remember the extension and check all further added properties
    if (extension == null) {
      extension = property.getExtension();
    } else {
      if (!extension.equals(property.getExtension())) {
        throw new IllegalArgumentException();
      }
    }
    properties.put(property, value);
  }

  private void setProperties(Map<ExtensionProperty, String> properties) {
    this.properties = properties;
  }
}
