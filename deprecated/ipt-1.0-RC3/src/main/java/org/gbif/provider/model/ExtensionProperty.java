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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/**
 * TODO: Documentation.
 * 
 */
@Entity
public class ExtensionProperty implements BaseObject,
    Comparable<ExtensionProperty> {
  private Long id;
  private Extension extension;
  private String name;
  private String namespace;
  private String group;
  private int columnLength = 255; // sensible default
  private String link;
  private boolean required;
  private ThesaurusVocabulary vocabulary;

  public ExtensionProperty() {
    super();
  }

  /**
   * Construct a new property with a single qualified name. Parses out the name
   * and sets the namespace to end with a slash or #
   * 
   * @param qualName
   */
  public ExtensionProperty(String qualName) {
    super();
    setQualName(qualName);
  }

  /**
   * Simply compare by ID so we can store any comparison order when designing
   * new extensions
   * 
   * @see java.lang.Comparable#compareTo(Object)
   */
  public int compareTo(ExtensionProperty prop) {
    return this.id.compareTo(prop.id);
  }

  /**
   * Just compare the unique qualified names to see if extension properties are
   * equal
   * 
   * @see java.lang.Object#equals(Object)
   */
  @Override
  public boolean equals(Object object) {
    if (!(object instanceof ExtensionProperty)) {
      return false;
    }
    ExtensionProperty rhs = (ExtensionProperty) object;
    return new EqualsBuilder().append(this.namespace, rhs.namespace).append(
        this.name, rhs.name).isEquals();
  }

  /**
   * The length of the database column to be generated when the extension
   * property is installed. Also used to trim incoming data before SQL insert is
   * generated. For LOB columns use -1 or any negative value
   * 
   * @return
   */
  public int getColumnLength() {
    return columnLength;
  }

  @ManyToOne(optional = false)
  @JoinColumn(name = "extension_fk", insertable = false, updatable = false, nullable = false)
  public Extension getExtension() {
    return extension;
  }

  @Column(length = 64, name = "groupp")
  @org.hibernate.annotations.Index(name = "idx_extension_property_group")
  public String getGroup() {
    return group;
  }

  @Transient
  public String getHQLName() {
    return StringUtils.uncapitalize(name);
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

  @Column(length = 64)
  @org.hibernate.annotations.Index(name = "idx_extension_property_name")
  public String getName() {
    return name;
  }

  @Column(length = 128)
  @org.hibernate.annotations.Index(name = "idx_extension_property_ns")
  public String getNamespace() {
    return namespace;
  }

  @Transient
  public String getQualName() {
    if (namespace.endsWith("/") || namespace.endsWith("#")) {
      return (this.namespace + this.name);
    } else {
      return (this.namespace + "/" + this.name);
    }
  }

  @ManyToOne(optional = true)
  public ThesaurusVocabulary getVocabulary() {
    return vocabulary;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    int result = 17;
    result = (id != null ? id.hashCode() : 0);
    result = 31 * result + (namespace != null ? namespace.hashCode() : 0);
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + columnLength;
    result = 31 * result + (required ? 1 : 0);
    result = 31 * result + (link != null ? link.hashCode() : 0);
    return result;
  }

  public boolean hasTerms() {
    return vocabulary != null;
  }

  public boolean isRequired() {
    return required;
  }

  public void setColumnLength(int columnLength) {
    this.columnLength = columnLength;
  }

  // required for SAX parser
  public void setColumnLength(String columnLength) {
    try {
      this.columnLength = Integer.parseInt(columnLength);
    } catch (NumberFormatException e) {
      // swallow stupidity
    }
  }

  public void setExtension(Extension extension) {
    this.extension = extension;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public void setQualName(String qualName) {
    String n = qualName;
    String ns = "";
    if (qualName.lastIndexOf("#") > 0) {
      n = qualName.substring(qualName.lastIndexOf("#") + 1);
      ns = qualName.substring(0, qualName.lastIndexOf("#"));
    } else if (qualName.lastIndexOf("/") > 0) {
      n = qualName.substring(qualName.lastIndexOf("/") + 1);
      ns = qualName.substring(0, qualName.lastIndexOf("/"));
    } else if (qualName.lastIndexOf("@") > 0) {
      n = qualName.substring(0, qualName.lastIndexOf("@"));
      ns = qualName.substring(qualName.lastIndexOf("@") + 1);
    }
    if (StringUtils.trimToNull(this.name) == null) {
      this.name = n;
    }
    if (StringUtils.trimToNull(this.namespace) == null) {
      this.namespace = ns;
    }
  }

  public void setRequired(boolean required) {
    this.required = required;
  }

  // required by SAX parser
  public void setRequired(String required) {
    if ("TRUE".equalsIgnoreCase(required) || "T".equalsIgnoreCase(required)
        || "1".equalsIgnoreCase(required)) {
      this.required = true;
    } else if ("FALSE".equalsIgnoreCase(required)
        || "F".equalsIgnoreCase(required) || "0".equalsIgnoreCase(required)) {
      this.required = false;
    }

    // or we just don't change if not understood
  }

  public void setVocabulary(ThesaurusVocabulary vocabulary) {
    this.vocabulary = vocabulary;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return name != null ? name : id.toString();
  }

}
