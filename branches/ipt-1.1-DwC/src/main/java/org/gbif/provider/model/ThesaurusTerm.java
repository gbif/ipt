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

import org.gbif.provider.util.XMLDateUtils;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * TODO: Documentation.
 * 
 */
@Entity
public class ThesaurusTerm implements Comparable, BaseObject {
  private Long id;
  private ThesaurusConcept concept;
  private boolean preferred;
  private String title;
  private String lang;
  private String source;
  private String relation;
  private Date created;
  private Date modified;

  /**
   * @see java.lang.Comparable#compareTo(Object)
   */
  public int compareTo(Object object) {
    ThesaurusTerm myClass = (ThesaurusTerm) object;
    return new CompareToBuilder().append(this.modified, myClass.modified).append(
        this.created, myClass.created).append(this.lang, myClass.lang).append(
        this.title, myClass.title).append(this.preferred, myClass.preferred).append(
        this.concept, myClass.concept).append(this.relation, myClass.relation).append(
        this.source, myClass.source).append(this.id, myClass.id).toComparison();
  }

  /**
   * @see java.lang.Object#equals(Object)
   */
  @Override
  public boolean equals(Object object) {
    if (!(object instanceof ThesaurusTerm)) {
      return false;
    }
    ThesaurusTerm rhs = (ThesaurusTerm) object;
    return new EqualsBuilder().append(this.modified, rhs.modified).append(
        this.created, rhs.created).append(this.lang, rhs.lang).append(
        this.title, rhs.title).append(this.preferred, rhs.preferred).append(
        this.concept, rhs.concept).append(this.relation, rhs.relation).append(
        this.source, rhs.source).append(this.id, rhs.id).isEquals();
  }

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  public ThesaurusConcept getConcept() {
    return concept;
  }

  public Date getCreated() {
    return created;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  public Long getId() {
    return id;
  }

  @Column(length = 8)
  public String getLang() {
    return lang;
  }

  public Date getModified() {
    return modified;
  }

  public String getRelation() {
    return relation;
  }

  @Column(length = 128)
  public String getSource() {
    return source;
  }

  @org.hibernate.annotations.Index(name = "term_title")
  public String getTitle() {
    return title;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder(877564629, -1011155925).append(this.modified).append(
        this.created).append(this.lang).append(this.title).append(
        this.preferred).append(this.concept).append(this.relation).append(
        this.source).append(this.id).toHashCode();
  }

  public boolean isPreferred() {
    return preferred;
  }

  public void setConcept(ThesaurusConcept concept) {
    this.concept = concept;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public void setCreatedXSDDateTime(String created) {
    setCreated(XMLDateUtils.toDate(created));
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setLang(String lang) {
    this.lang = lang;
  }

  public void setModified(Date modified) {
    this.modified = modified;
  }

  public void setModifiedXSDDateTime(String modified) {
    setModified(XMLDateUtils.toDate(modified));
  }

  public void setPreferred(boolean preferred) {
    this.preferred = preferred;
  }

  public void setPreferred(String preferred) {
    if ("TRUE".equalsIgnoreCase(preferred) || "YES".equalsIgnoreCase(preferred)
        || "Y".equalsIgnoreCase(preferred) || "T".equalsIgnoreCase(preferred)
        || "1".equalsIgnoreCase(preferred)) {
      this.preferred = true;
    } else {
      this.preferred = false;
    }
  }

  public void setRelation(String relation) {
    this.relation = relation;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public String toString() {
    return String.format("%s [%s]", title, lang);
  }
}
