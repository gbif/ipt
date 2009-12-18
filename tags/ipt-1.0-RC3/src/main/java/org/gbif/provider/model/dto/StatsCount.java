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

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * TODO: Documentation.
 * 
 */
public class StatsCount implements Comparable {
  private final Long id;
  private final String label;
  private final Object value;
  private final Long count;

  public StatsCount(Long id, String label, Object value, Long count) {
    super();
    this.id = id;
    this.label = (label == null ? "???" : label);
    this.value = value;
    this.count = (count == null ? 0L : count);
  }

  public StatsCount(String label, Long count) {
    this(null, label, label, count);
  }

  /**
   * @see java.lang.Comparable#compareTo(Object)
   */
  public int compareTo(Object object) {
    StatsCount myClass = (StatsCount) object;
    return new CompareToBuilder().append(myClass.count, this.count).append(
        this.label, myClass.label).toComparison();
  }

  /**
   * @see java.lang.Object#equals(Object)
   */
  @Override
  public boolean equals(Object object) {
    if (!(object instanceof StatsCount)) {
      return false;
    }
    StatsCount rhs = (StatsCount) object;
    return new EqualsBuilder().append(this.value, rhs.value).append(this.label,
        rhs.label).append(this.count, rhs.count).isEquals();
  }

  public Long getCount() {
    return count;
  }

  public Long getId() {
    return id;
  }

  public String getLabel() {
    return label;
  }

  public Object getValue() {
    return value;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder(-385111033, -2132492275).append(this.value).append(
        this.label).append(this.count).toHashCode();
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this).append("count", this.count).append(
        "label", this.label).toString();
  }

}
