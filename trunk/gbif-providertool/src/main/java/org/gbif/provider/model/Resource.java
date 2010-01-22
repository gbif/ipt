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

import org.gbif.provider.model.eml.Eml;
import org.gbif.provider.model.eml.TaxonKeyword;
import org.gbif.provider.model.hibernate.Timestampable;
import org.gbif.provider.model.voc.PublicationStatus;
import org.gbif.provider.model.voc.ServiceType;
import org.gbif.provider.util.AppConfig;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.validator.NotNull;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/**
 * A generic resource describing any digital, online and non digital available
 * biological resources. Only keeps the basic properties, but links to a far
 * more expressive EML file via the embedded ResourceMetadata type.
 * 
 */
@Entity
public class Resource implements BaseObject, Comparable<Resource>,
    Timestampable {
  private Long id;
  @NotNull
  protected String guid = UUID.randomUUID().toString();
  // resource metadata
  protected ResourceMetadata meta = new ResourceMetadata();
  protected BBox geoCoverage;
  protected Set<String> keywords = new HashSet<String>();
  protected Map<String, String> services = new HashMap<String, String>();
  protected PublicationStatus status;
  protected String type;
  // resource meta-metadata
  protected User creator;
  protected Date created = new Date();
  protected User modifier;
  protected Date modified;

  public int compareTo(Resource object) {
    if (this.getTitle() != null) {
      return this.getTitle().compareToIgnoreCase(object.getTitle());
    } else {
      return "".compareToIgnoreCase(object.getTitle());
    }
  }

  /**
   * @see java.lang.Object#equals(Object)
   */
  @Override
  public boolean equals(Object object) {
    if (!(object instanceof Resource)) {
      return false;
    }
    Resource rhs = (Resource) object;
    return new EqualsBuilder().append(this.modified, rhs.modified).append(
        this.created, rhs.created).append(this.creator, rhs.creator).append(
        this.getTitle(), rhs.getTitle()).append(this.modifier, rhs.modifier).append(
        this.getDescription(), rhs.getDescription()).append(this.guid, rhs.guid).append(
        this.getLink(), rhs.getLink()).append(this.id, rhs.id).isEquals();
  }

  @Transient
  public String getContactEmail() {
    return getMeta().getContactEmail();
  }

  @Transient
  public String getContactName() {
    return getMeta().getContactName();
  }

  public Date getCreated() {
    return created;
  }

  @ManyToOne
  public User getCreator() {
    return creator;
  }

  @Transient
  public String getDescription() {
    return getMeta().getDescription();
  }

  public BBox getGeoCoverage() {
    return geoCoverage;
  }

  @Column(length = 128, unique = true)
  public String getGuid() {
    return guid;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  public Long getId() {
    return id;
  }

  @CollectionOfElements(fetch = FetchType.EAGER)
  public Set<String> getKeywords() {
    return keywords;
  }

  // DELEGATE METHODS
  @Transient
  public String getLink() {
    return getMeta().getLink();
  }

  public ResourceMetadata getMeta() {
    if (meta == null) {
      meta = new ResourceMetadata();
    }
    return meta;
  }

  public Date getModified() {
    return modified;
  }

  @ManyToOne
  public User getModifier() {
    return modifier;
  }

  @Transient
  public String getRegistryUrl() {
    if (StringUtils.trimToNull(getUddiID()) != null) {
      return AppConfig.getRegistryResourceUrl() + "/" + getUddiID();
    }
    return null;
  }

  @CollectionOfElements(fetch = FetchType.LAZY)
  public Map<String, String> getServices() {
    return services;
  }

  @Transient
  public String getServiceUUID(ServiceType type) {
    return this.services.get(type.code);
  }

  public PublicationStatus getStatus() {
    return status;
  }

  @Transient
  public String getTitle() {
    return getMeta().getTitle();
  }

  @Column(length = 64)
  public String getType() {
    return type;
  }

  @Transient
  public String getUddiID() {
    return getMeta().getUddiID();
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder(1501230247, -1510855635).append(this.modified).append(
        this.created).append(this.creator).append(this.getTitle()).append(
        this.modifier).append(this.getDescription()).append(this.guid).append(
        this.getLink()).toHashCode();
  }

  @Transient
  public boolean isDataResource() {
    return false;
  }

  @Transient
  public boolean isDirty() {
    if (status == null) {
      return true;
    }
    return getStatus().compareTo(PublicationStatus.published) < 0;
  }

  @Transient
  public boolean isPublic() {
    if (status == null) {
      return false;
    }
    return getStatus().compareTo(PublicationStatus.modified) >= 0;
  }

  @Transient
  public boolean isRegistered() {
    if (StringUtils.trimToNull(getUddiID()) == null) {
      return false;
    }
    return true;
  }

  public void putService(ServiceType type, String uuid) {
    if (StringUtils.trimToNull(uuid) != null) {
      this.services.put(type.code, StringUtils.trimToNull(uuid));
    } else {
      this.services.remove(type.code);
    }
  }

  public void setContactEmail(String contactEmail) {
    meta.setContactEmail(contactEmail);
  }

  public void setContactName(String contactName) {
    meta.setContactName(contactName);
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public void setCreator(User creator) {
    this.creator = creator;
  }

  public void setDescription(String description) {
    meta.setDescription(description);
  }

  public void setDirty() {
    if (status != null && status.equals(PublicationStatus.published)) {
      this.status = PublicationStatus.modified;
    }
  }

  public void setGuid(String guid) {
    this.guid = guid;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setLink(String link) {
    meta.setLink(link);
  }

  public void setMeta(ResourceMetadata meta) {
    this.meta = meta;
  }

  public void setModified(Date modified) {
    this.modified = modified;
  }

  public void setModifier(User modifier) {
    this.modifier = modifier;
  }

  public void setServices(Map<String, String> services) {
    this.services = services;
  }

  public void setStatus(PublicationStatus status) {
    // a not registered resource cant be uptodate
    if (status == null) {
      status = PublicationStatus.unpublished;
    } else if (status.equals(PublicationStatus.published) && !isRegistered()) {
      status = PublicationStatus.modified;
    }
    this.status = status;
  }

  public void setTitle(String title) {
    meta.setTitle(title);
  }

  public void setType(String type) {
    this.type = type;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this).append("created", this.created).append(
        "modified", this.modified).append("creator", this.creator).append(
        "description", this.getDescription()).append("id", this.id).append(
        "title", this.getTitle()).append("link", this.getLink()).append(
        "modifier", this.modifier).append("guid", this.guid).toString();
  }

  /**
   * updates persistent EML properties on resource based on EML values
   */
  public void updateWithMetadata(Eml eml) {
    // keywords
    Set<String> keys = new HashSet<String>();
    keys.addAll(eml.getKeywords());
    for (TaxonKeyword k : eml.getTaxonomicClassification()) {
      keys.add(k.getCommonName());
      keys.add(k.getScientificName());
    }
    this.keywords = keys;
    // geoCoverage
    this.geoCoverage = eml.getGeographicCoverage().getBoundingCoordinates();
  }

  /**
   * Persistent EML property. To change use eml.setGeoCoverage()
   * 
   * @param geoCoverage
   */
  private void setGeoCoverage(BBox geoCoverage) {
    this.geoCoverage = geoCoverage;
  }

  /**
   * Persistent EML property. To change use eml.setKeywords()
   * 
   * @param geoCoverage
   */
  private void setKeywords(Set<String> keywords) {
    this.keywords = keywords;
  }

}
