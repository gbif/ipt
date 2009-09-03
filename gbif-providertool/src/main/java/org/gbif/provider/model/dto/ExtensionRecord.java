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
package org.gbif.provider.model.dto;

import org.gbif.provider.datasource.ImportRecord;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.Record;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * A record with property values inside the properties map for any extension as
 * opposed to a CoreRecord. The ExtensionRecord is not managed by Hibernate
 * (therefore it is no Entity) as the number and name of properties as well as
 * the extension it is attached to varies
 * 
 */
public class ExtensionRecord implements Iterable<ExtensionProperty>, Record {
  private class PropertyIterator implements Iterator<ExtensionProperty> {
    private int index = 0;
    private final ExtensionProperty[] props;

    protected PropertyIterator() {
      index = 0;
      this.props = properties.keySet().toArray(
          new ExtensionProperty[properties.keySet().size()]);
    }

    public boolean hasNext() {
      return index < props.length;
    }

    public ExtensionProperty next() {
      if (hasNext()) {
        return props[index++];
      } else {
        throw new NoSuchElementException();
      }
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  public static ExtensionRecord newInstance(ImportRecord iRec) {
    ExtensionRecord extRec = new ExtensionRecord(iRec.getResourceId(),
        iRec.getSourceId(), iRec.getProperties());
    return extRec;
  }

  private Long coreId;
  private final Long resourceId;
  private String sourceId;

  private Extension extension;

  private Map<ExtensionProperty, String> properties = new HashMap<ExtensionProperty, String>();

  public ExtensionRecord(Long resourceId, Long coreId) {
    super();
    this.resourceId = resourceId;
    this.coreId = coreId;
  }

  public ExtensionRecord(Long resourceId, String sourceId) {
    super();
    this.resourceId = resourceId;
    this.sourceId = sourceId;
  }

  public ExtensionRecord(Long resourceId, String sourceId,
      Map<ExtensionProperty, String> properties) {
    super();
    this.resourceId = resourceId;
    this.sourceId = sourceId;
    this.properties = properties;
  }

  /**
   * @see java.lang.Object#equals(Object)
   */
  @Override
  public boolean equals(Object object) {
    if (!(object instanceof ExtensionRecord)) {
      return false;
    }
    ExtensionRecord rhs = (ExtensionRecord) object;
    return new EqualsBuilder().append(this.sourceId, rhs.sourceId).append(
        this.properties, rhs.properties).isEquals();
  }

  public Long getCoreId() {
    return coreId;
  }

  public Extension getExtension() {
    if (extension == null) {
      for (ExtensionProperty p : properties.keySet()) {
        if (p != null) {
          extension = p.getExtension();
          break;
        }
      }
    }
    return extension;
  }

  public List<ExtensionProperty> getProperties() {
    return new ArrayList<ExtensionProperty>(properties.keySet());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.model.Record#getPropertyValue(org.gbif.provider.model
   * .ExtensionProperty)
   */
  public String getPropertyValue(ExtensionProperty property) {
    return properties.get(property);
  }

  public Long getResourceId() {
    return resourceId;
  }

  public String getSourceId() {
    return sourceId;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder(890875527, 2143130705).append(this.sourceId).append(
        this.properties).toHashCode();
  }

  public boolean isEmpty() {
    return properties.isEmpty();
  }

  public Iterator<ExtensionProperty> iterator() {
    return new PropertyIterator();
  }

  public void setCoreId(Long coreId) {
    this.coreId = coreId;
  }

  public void setPropertyValue(ExtensionProperty property, String value) {
    properties.put(property, value);
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this).append("properties", this.properties).append(
        "coreId", this.sourceId).toString();
  }

}
