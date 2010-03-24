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

import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.validator.NotNull;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/**
 * TODO: Documentation.
 * 
 */
@Entity
public class Annotation implements ResourceRelatedObject {
  private Long id;
  private String guid;
  private String sourceId;
  @NotNull
  private Resource resource;
  @NotNull
  private String type;
  private String note;
  private Integer probability;
  private Map<ExtensionProperty, String> proposal = new HashMap<ExtensionProperty, String>();
  private boolean removeDuringImport = false;
  private String creator;
  private Date created = new Date();

  public Date getCreated() {
    return created;
  }

  @Column(length = 64)
  public String getCreator() {
    return creator;
  }

  /**
   * The GUID of the annotated record
   * 
   * @return
   */
  @Column(length = 128)
  @org.hibernate.annotations.Index(name = "annotation_guid")
  public String getGuid() {
    return guid;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  public Long getId() {
    return id;
  }

  @Lob
  public String getNote() {
    return note;
  }

  public Integer getProbability() {
    return probability;
  }

  // @MapKey(columns = @Column(name = "property_fk"))
  @CollectionOfElements
  public Map<ExtensionProperty, String> getProposal() {
    return proposal;
  }

  @ManyToOne
  public Resource getResource() {
    return resource;
  }

  @Transient
  public Long getResourceId() {
    return resource.getId();
  }

  @Column(length = 128)
  public String getSourceId() {
    return sourceId;
  }

  @Column(length = 32)
  public String getType() {
    return type;
  }

  public boolean isRemoveDuringImport() {
    return removeDuringImport;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public void setCreator(String creator) {
    this.creator = creator;
  }

  public void setGuid(String guid) {
    this.guid = guid;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public void setProbability(Integer probability) {
    this.probability = probability;
  }

  public void setProposal(Map<ExtensionProperty, String> proposal) {
    this.proposal = proposal;
  }

  public void setRemoveDuringImport(boolean removeDuringImport) {
    this.removeDuringImport = removeDuringImport;
  }

  public void setResource(Resource resource) {
    this.resource = resource;
  }

  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return String.format("%s [%s] %s - %s", this.note, this.type, this.creator,
        this.created);
  }

}
