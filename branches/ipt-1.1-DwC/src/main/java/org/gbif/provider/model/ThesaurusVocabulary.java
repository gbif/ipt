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
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 * TODO: Documentation.
 * 
 */
@Entity
public class ThesaurusVocabulary implements Comparable {
  private Long id;
  private String uri;
  private String title;
  private String link;
  private List<ThesaurusConcept> concepts = new LinkedList<ThesaurusConcept>();
  private Date modified = new Date();

  public void addConcept(ThesaurusConcept concept) {
    if (concepts == null) {
      concepts = new LinkedList<ThesaurusConcept>();
    }
    concept.setVocabulary(this);

    if (concept.getConceptOrder() == null) {
      // set the order to be the next one
      int maxOrder = 0;
      for (ThesaurusConcept tc : concepts) {
        if (tc.getConceptOrder() != null && maxOrder < tc.getConceptOrder()) {
          maxOrder = tc.getConceptOrder();
        }
      }
      concept.setConceptOrder(maxOrder + 1);
    }
    concepts.add(concept);
  }

  /**
   * @see java.lang.Comparable#compareTo(Object)
   */
  public int compareTo(Object object) {
    ThesaurusVocabulary myClass = (ThesaurusVocabulary) object;
    return new CompareToBuilder().append(this.modified, myClass.modified).append(
        this.title, myClass.title).append(this.uri, myClass.uri).append(
        this.id, myClass.id).toComparison();
  }

  /**
   * @see java.lang.Object#equals(Object)
   */
  @Override
  public boolean equals(Object object) {
    if (!(object instanceof ThesaurusVocabulary)) {
      return false;
    }
    ThesaurusVocabulary rhs = (ThesaurusVocabulary) object;
    return new EqualsBuilder().append(this.modified, rhs.modified).append(
        this.title, rhs.title).append(this.uri, rhs.uri).append(this.id, rhs.id).isEquals();
  }

  @OneToMany(mappedBy = "vocabulary", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @IndexColumn(name = "conceptOrder", base = 0, nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  public List<ThesaurusConcept> getConcepts() {

    // a hack by Tim to quickly support the concept order where it is not set
    /*
     * int maxConceptOrder = 0; for (ThesaurusConcept tc : concepts) { if
     * (tc.getConceptOrder() != null) { maxConceptOrder = tc.getConceptOrder();
     * } } for (ThesaurusConcept tc : concepts) { if (tc.getConceptOrder() ==
     * null) { tc.setConceptOrder(maxConceptOrder++); } }
     */
    return concepts;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  public Long getId() {
    return id;
  }

  public String getLink() {
    return link;
  }

  public Date getModified() {
    return modified;
  }

  public String getTitle() {
    return title;
  }

  public String getUri() {
    return uri;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder(-1469035489, -1512452511).append(this.modified).append(
        this.title).append(this.uri).append(this.id).toHashCode();
  }

  public void setConcepts(List<ThesaurusConcept> concepts) {
    this.concepts = concepts;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public void setModified(Date modified) {
    this.modified = modified;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  @Override
  public String toString() {
    return title;
  }
}
