/*
 * Copyright 2009 GBIF. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.gbif.ipt.model;

import static com.google.common.base.Objects.equal;

import com.google.common.base.Objects;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang.builder.CompareToBuilder;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * TODO: Documentation.
 * 
 */
public class Vocabulary implements Comparable, Serializable {
  private static final long serialVersionUID = 22000013267L;
  @SerializedName("identifier")
  private String uri; // identifier for JSON.
  private URL url; // url to its definition
  private String title;
  private String description;
  private String subject;
  private URL link; // to further documentation
  private List<VocabularyConcept> concepts = new LinkedList<VocabularyConcept>();
  // the data this local vocabulary copy was last updated
  private Date lastUpdate = new Date();

  public void addConcept(VocabularyConcept concept) {
    if (concepts == null) {
      concepts = new LinkedList<VocabularyConcept>();
    }
    concept.setVocabulary(this);

    if (concept.getOrder() == -1) {
      // set the order to be the next one
      int maxOrder = 0;
      for (VocabularyConcept tc : concepts) {
        if (tc.getOrder() >= 0 && maxOrder < tc.getOrder()) {
          maxOrder = tc.getOrder();
        }
      }
      concept.setOrder(maxOrder + 1);
    }
    concepts.add(concept);
  }

  /**
   * @see java.lang.Comparable#compareTo(Object)
   */
  public int compareTo(Object object) {
    Vocabulary myClass = (Vocabulary) object;
    return new CompareToBuilder().append(this.uri, myClass.uri).toComparison();
  }

  /**
   * @see java.lang.Object#equals(Object)
   */
  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Vocabulary)) {
      return false;
    }
    Vocabulary o = (Vocabulary) other;
    return equal(uri, o.uri);
  }

  public VocabularyConcept findConcept(String term) {
    // try codes
    for (VocabularyConcept c : concepts) {
      if (c.getIdentifier().equalsIgnoreCase(term)) {
        return c;
      }
    }
    // try preferred
    for (VocabularyConcept c : concepts) {
      for (VocabularyTerm t : c.getPreferredTerms()) {
        if (t.getTitle().equalsIgnoreCase(term)) {
          return c;
        }
      }
    }
    // try alt
    for (VocabularyConcept c : concepts) {
      for (VocabularyTerm t : c.getAlternativeTerms()) {
        if (t.getTitle().equalsIgnoreCase(term)) {
          return c;
        }
      }
    }
    return null;
  }

  public List<VocabularyConcept> getConcepts() {
    return concepts;
  }

  public String getDescription() {
    return description;
  }

  public Date getLastUpdate() {
    return lastUpdate;
  }

  public URL getLink() {
    return link;
  }

  public String getSubject() {
    return subject;
  }

  public String getTitle() {
    return title;
  }

  public String getUri() {
    return uri;
  }

  public URL getUrl() {
    return url;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return Objects.hashCode(uri);
  }

  public void setConcepts(List<VocabularyConcept> concepts) {
    this.concepts = concepts;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setLastUpdate(Date lastUpdate) {
    this.lastUpdate = lastUpdate;
  }

  public void setLink(String link) {
    URL url;
    try {
      url = new URL(link);
      this.link = url;
    } catch (MalformedURLException e) {
    }
  }

  public void setLink(URL link) {
    this.link = link;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public void setUrl(URL url) {
    this.url = url;
  }

}
