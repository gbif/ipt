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
package org.gbif.provider.model;

import org.gbif.provider.model.voc.RegionType;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.annotations.Index;

import javax.persistence.Entity;

/**
 * TODO: Documentation.
 * 
 */
@Entity
@org.hibernate.annotations.Table(appliesTo = "Region", indexes = {
    @Index(name = "reg_label", columnNames = {"label"}),
    @Index(name = "reg_lft", columnNames = {"lft"}),
    @Index(name = "reg_rgt", columnNames = {"rgt"})})
public class Region extends TreeNodeBase<Region, RegionType> implements
    ResourceRelatedObject {
  protected static final Log log = LogFactory.getLog(Region.class);

  public static Region newInstance(DataResource resource) {
    Region region = new Region();
    region.resource = resource;
    return region;
  }

  /**
   * @see java.lang.Object#equals(Object)
   */
  @Override
  public boolean equals(Object object) {
    if (object == this) {
      return true;
    }
    if (!(object instanceof Region)) {
      return false;
    }
    Region region = (Region) object;
    return this.hashCode() == region.hashCode();
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + (resource != null ? resource.hashCode() : 0);
    result = 31 * result + (getParent() != null ? getParent().hashCode() : 0);
    result = 31 * result + (getType() != null ? getType().hashCode() : 0);
    result = 31 * result + (getLabel() != null ? getLabel().hashCode() : 0);
    return result;
  }

  @Override
  protected int compareWithoutHierarchy(Region first, Region second) {
    return new CompareToBuilder().append(first.resource, second.resource).append(
        first.getLabel(), second.getLabel()).toComparison();
  }

}
