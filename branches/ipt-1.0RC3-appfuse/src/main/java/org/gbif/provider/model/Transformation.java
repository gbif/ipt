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

import org.gbif.provider.model.voc.TransformationType;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;

import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

/**
 * TODO: Documentation.
 * 
 */
@Entity
public class Transformation implements Comparable, ResourceRelatedObject {
  private Long id;
  private DataResource resource;
  private TransformationType type;
  private SourceBase source;
  private String column;
  private ThesaurusVocabulary voc;
  private List<PropertyMapping> propertyMappings;
  private Set<TermMapping> termMappings;

  /**
   * @see java.lang.Comparable#compareTo(Object)
   */
  public int compareTo(Object object) {
    Transformation myClass = (Transformation) object;
    return new CompareToBuilder().append(this.resource, myClass.resource).append(
        this.type, myClass.type).append(this.voc, myClass.voc).append(
        this.column, myClass.column).append(this.id, myClass.id).toComparison();
  }

  public String getColumn() {
    return column;
  }

  @Transient
  public String[] getColumns() {
    return StringUtils.split(column, '|');
  }

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  public Long getId() {
    return id;
  }

  @OneToMany(mappedBy = "termTransformation")
  public List<PropertyMapping> getPropertyMappings() {
    return propertyMappings;
  }

  @ManyToOne(optional = false)
  public DataResource getResource() {
    return resource;
  }

  @Transient
  public Long getResourceId() {
    return resource.getId();
  }

  @ManyToOne(optional = false)
  public SourceBase getSource() {
    return source;
  }

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "transformation")
  public Set<TermMapping> getTermMappings() {
    return termMappings;
  }

  public TransformationType getType() {
    return type;
  }

  @ManyToOne
  public ThesaurusVocabulary getVoc() {
    return voc;
  }

  public void setColumn(String column) {
    this.column = column;
  }

  public void setColumns(String[] columns) {
    this.column = StringUtils.join(columns, '|');
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setPropertyMappings(List<PropertyMapping> propertyMappings) {
    this.propertyMappings = propertyMappings;
  }

  public void setResource(DataResource resource) {
    this.resource = resource;
  }

  public void setSource(SourceBase source) {
    this.source = source;
  }

  public void setTermMappings(Set<TermMapping> termMappings) {
    this.termMappings = termMappings;
  }

  public void setType(TransformationType type) {
    this.type = type;
  }

  public void setVoc(ThesaurusVocabulary voc) {
    this.voc = voc;
  }

}
