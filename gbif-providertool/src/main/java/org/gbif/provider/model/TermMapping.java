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

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * TODO: Documentation.
 * 
 */
@Entity
public class TermMapping implements BaseObject, Comparable {
  private Long id;
  private Transformation transformation;
  private String term;
  private String targetTerm;

  public TermMapping() {
  }

  public TermMapping(Transformation transformation, String term) {
    this.transformation = transformation;
    this.term = term;
  }

  /**
   * @see java.lang.Comparable#compareTo(Object)
   */
  public int compareTo(Object object) {
    TermMapping myClass = (TermMapping) object;
    return new CompareToBuilder().append(this.transformation,
        myClass.transformation).append(this.term, myClass.term).append(
        this.targetTerm, myClass.targetTerm).append(this.id, myClass.id).toComparison();
  }

  /**
   * @see java.lang.Object#equals(Object)
   */
  @Override
  public boolean equals(Object object) {
    if (!(object instanceof TermMapping)) {
      return false;
    }
    TermMapping rhs = (TermMapping) object;
    return new EqualsBuilder().append(this.transformation, rhs.transformation).append(
        this.term, rhs.term).append(this.targetTerm, rhs.targetTerm).append(
        this.id, rhs.id).isEquals();
  }

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  public Long getId() {
    return id;
  }

  @Column(length = 128)
  public String getTargetTerm() {
    return targetTerm;
  }

  public String getTerm() {
    return term;
  }

  @ManyToOne(optional = false)
  public Transformation getTransformation() {
    return transformation;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder(1318785267, 1875601279).append(
        this.transformation).append(this.term).append(this.targetTerm).append(
        this.id).toHashCode();
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setTargetTerm(String targetTerm) {
    this.targetTerm = targetTerm;
  }

  public void setTerm(String term) {
    this.term = term;
  }

  public void setTransformation(Transformation transformation) {
    this.transformation = transformation;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this).append("term", this.term).append(
        "targetTerm", this.targetTerm).toString();
  }

}
