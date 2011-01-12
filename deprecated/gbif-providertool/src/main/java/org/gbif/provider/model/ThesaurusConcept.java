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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

/**
 * TODO: Documentation.
 * 
 */
@Entity
public class ThesaurusConcept implements Comparable, BaseObject {
  private static Log log = LogFactory.getLog(ThesaurusConcept.class);

  private Long id;
  private String identifier;
  private String uri;
  private String link;
  private ThesaurusVocabulary vocabulary;
  private Integer conceptOrder;
  private Date issued;

  private Set<ThesaurusTerm> terms = new HashSet<ThesaurusTerm>();

  public void addTerm(ThesaurusTerm term) {
    if (terms == null) {
      terms = new HashSet<ThesaurusTerm>();
    }
    term.setConcept(this);
    terms.add(term);
  }

  /**
   * @see java.lang.Comparable#compareTo(Object)
   */
  public int compareTo(Object object) {
    ThesaurusConcept myClass = (ThesaurusConcept) object;
    return new CompareToBuilder().append(this.issued, myClass.issued).append(
        this.uri, myClass.uri).append(this.vocabulary, myClass.vocabulary).append(
        this.identifier, myClass.identifier).append(this.id, myClass.id).toComparison();
  }

  /**
   * @see java.lang.Object#equals(Object)
   */
  @Override
  public boolean equals(Object object) {
    if (!(object instanceof ThesaurusConcept)) {
      return false;
    }
    ThesaurusConcept rhs = (ThesaurusConcept) object;
    return new EqualsBuilder().append(this.issued, rhs.issued).append(this.uri,
        rhs.uri).append(this.vocabulary, rhs.vocabulary).append(
        this.identifier, rhs.identifier).append(this.id, rhs.id).isEquals();
  }

  public Integer getConceptOrder() {
    return conceptOrder;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  public Long getId() {
    return id;
  }

  @Column(length = 64)
  @org.hibernate.annotations.Index(name = "concept_identifier")
  public String getIdentifier() {
    return identifier;
  }

  public Date getIssued() {
    return issued;
  }

  public String getLink() {
    return link;
  }

  @OneToMany(mappedBy = "concept", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  public Set<ThesaurusTerm> getTerms() {
    return terms;
  }

  @org.hibernate.annotations.Index(name = "concept_uri")
  public String getUri() {
    return uri;
  }

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  public ThesaurusVocabulary getVocabulary() {
    return vocabulary;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder(-1161377931, -1626651913).append(this.issued).append(
        this.uri).append(this.vocabulary).append(this.identifier).append(
        this.id).toHashCode();
  }

  public void setConceptOrder(Integer conceptOrder) {
    this.conceptOrder = conceptOrder;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public void setIssued(Date issued) {
    this.issued = issued;
  }

  public void setIssuedXSDDateTime(String xmlDateTime) {
    setIssued(XMLDateUtils.toDate(xmlDateTime));
  }

  public void setLink(String link) {
    this.link = link;
  }

  public void setTerms(Set<ThesaurusTerm> terms) {
    this.terms = terms;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public void setVocabulary(ThesaurusVocabulary vocabulary) {
    this.vocabulary = vocabulary;
  }

  @Override
  public String toString() {
    return identifier;
  }

}
