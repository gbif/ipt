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
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.IndexColumn;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

/**
 * An extension with a list of ExtensionProperties defined to extend some
 * CoreRecord entity
 * 
 */
@Entity
public class Extension implements BaseObject, Comparable<Extension> {
  private Long id;
  private String title;
  private String name; // table & file naming. no whitespace allowed
  private String namespace;
  private String link; // to documentation
  private boolean installed;
  private List<ExtensionProperty> properties = new ArrayList<ExtensionProperty>();
  private boolean core = false;
  private Date modified = new Date();

  public void addProperty(ExtensionProperty property) {
    property.setExtension(this);
    properties.add(property);
  }

  /**
   * @see java.lang.Comparable#compareTo(Object)
   */
  public int compareTo(Extension object) {
    return new CompareToBuilder().append(this.id, object.id).toComparison();
  }

  /**
   * @see java.lang.Object#equals(Object)
   */
  @Override
  public boolean equals(Object object) {
    if (!(object instanceof Extension)) {
      return false;
    }
    Extension rhs = (Extension) object;
    return new EqualsBuilder().append(this.link, rhs.link).append(this.id,
        rhs.id).isEquals();
  }

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  public Long getId() {
    return id;
  }

  @Column(length = 255)
  public String getLink() {
    return link;
  }

  public Date getModified() {
    return modified;
  }

  @Column(length = 64)
  public String getName() {
    return name;
  }

  @Column(length = 128)
  public String getNamespace() {
    return namespace;
  }

  @OneToMany(cascade = CascadeType.ALL)
  @IndexColumn(name = "property_order", base = 0, nullable = false)
  @JoinColumn(name = "extension_fk", nullable = false)
  public List<ExtensionProperty> getProperties() {
    return properties;
  }

  @Transient
  public String getRowType() {
    if (namespace.endsWith("/") || namespace.endsWith("#")) {
      return (this.namespace + this.name);
    } else {
      return (this.namespace + "/" + this.name);
    }
  }

  @Column(length = 128)
  public String getTitle() {
    return title;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    int result = 17;
    result = (id != null ? id.hashCode() : 0);
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (namespace != null ? namespace.hashCode() : 0);
    result = 31 * result + (link != null ? link.hashCode() : 0);
    return result;
  }

  public boolean isCore() {
    return this.core;
  }

  public boolean isInstalled() {
    return installed;
  }

  public void setCore(boolean core) {
    this.core = core;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setInstalled(boolean installed) {
    this.installed = installed;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public void setModified(Date modified) {
    this.modified = modified;
  }

  public void setName(String name) {
    this.name = name.replaceAll("\\s", "_");
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public void setProperties(List<ExtensionProperty> properties) {
    this.properties = properties;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this).append("name", this.name).append("id",
        this.id).toString();
  }

}
