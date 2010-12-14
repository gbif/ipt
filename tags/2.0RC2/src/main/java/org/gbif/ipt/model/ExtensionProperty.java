/*
 * Copyright 2009 GBIF. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.gbif.ipt.model;

import org.gbif.dwc.terms.ConceptTerm;
import org.gbif.dwc.terms.TermFactory;
import org.gbif.dwc.text.ArchiveField.DataType;

import static com.google.common.base.Objects.equal;

import com.google.common.base.Objects;

import java.io.Serializable;

/**
 * A single property of an extension. Often also known as concept or term.
 * It implements the ConceptTerm of the dwca reader and its equal method compares only the qualified name, so that any
 * ConceptTerm with the same qualified name is considered equal.
 * 
 * Natural sorting is based on the group and then the property name
 */
public class ExtensionProperty implements Comparable<ExtensionProperty>, ConceptTerm, Serializable {
  private static final long serialVersionUID = 698996553L;
  private Extension extension;
  private String name;
  private String namespace;
  private String qualname;
  private String group;
  private DataType type = DataType.string;
  private String link;
  private String examples;
  private String description;
  private boolean required;
  private Vocabulary vocabulary;

  public ExtensionProperty() {
    super();
  }

  /**
   * Construct a new property with a single qualified name. Parses out the name and sets the namespace to end with a
   * slash or #
   * 
   * @param qualName
   */
  public ExtensionProperty(String qualName) {
    super();
    setQualname(qualName);
  }

  /**
   * Compare by group and qualified name as default sorting order
   * 
   * @see java.lang.Comparable#compareTo(Object)
   */
  public int compareTo(ExtensionProperty prop) {
    if (group != null) {
      int x = this.group.compareTo(prop.group);
      if (x != 0) {
        return x;
      }
    }
    return this.qualname.compareTo(prop.qualname);
  }

  /**
   * Just compare the unique qualified names to see if extension properties are equal
   * 
   * @see java.lang.Object#equals(Object)
   */
  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof ExtensionProperty)) {
      return false;
    }
    ExtensionProperty o = (ExtensionProperty) other;
    return equal(extension, o.extension) && equal(qualname, o.qualname);
  }

  public String getDescription() {
    return description;
  }

  public String getExamples() {
    return examples;
  }

  public Extension getExtension() {
    return extension;
  }

  public String getGroup() {
    return group;
  }

  public String getLink() {
    return link;
  }

  public String getName() {
    return name;
  }

  public String getNamespace() {
    return namespace;
  }

  public String getQualname() {
    return qualname;
  }

  public DataType getType() {
    return type;
  }

  public Vocabulary getVocabulary() {
    return vocabulary;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return Objects.hashCode(extension, qualname);
  }

  public boolean hasTerms() {
    return vocabulary != null;
  }

  public boolean isRequired() {
    return required;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.dwc.terms.ConceptTerm#qualifiedName()
   */
  public String qualifiedName() {
    return qualname;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.dwc.terms.ConceptTerm#qualifiedNormalisedName()
   */
  public String qualifiedNormalisedName() {
    return TermFactory.normaliseTerm(qualifiedName());
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setExamples(String examples) {
    this.examples = examples;
  }

  public void setExtension(Extension extension) {
    this.extension = extension;
  }

  public void setGroup(String group) {
    this.group = group;
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

  public void setQualname(String qualname) {
    this.qualname = qualname;
  }

  public void setRequired(boolean required) {
    this.required = required;
  }

  // required by SAX parser
  public void setRequired(String required) {
    if ("TRUE".equalsIgnoreCase(required) || "T".equalsIgnoreCase(required) || "1".equalsIgnoreCase(required)) {
      this.required = true;
    } else if ("FALSE".equalsIgnoreCase(required) || "F".equalsIgnoreCase(required) || "0".equalsIgnoreCase(required)) {
      this.required = false;
    }

    // or we just don't change if not understood
  }

  public void setType(DataType type) {
    this.type = type;
  }

  public void setType(String type) {
    this.type = DataType.findByExtensionEnumTypeName(type);
    if (this.type == null) {
      this.type = DataType.string;
    }
  }

  public void setVocabulary(Vocabulary vocabulary) {
    this.vocabulary = vocabulary;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.dwc.terms.ConceptTerm#simpleName()
   */
  public String simpleName() {
    return name;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.dwc.terms.ConceptTerm#simpleNormalisedAlternativeNames()
   */
  public String[] simpleNormalisedAlternativeNames() {
    return new String[]{};
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.dwc.terms.ConceptTerm#simpleNormalisedName()
   */
  public String simpleNormalisedName() {
    return TermFactory.normaliseTerm(simpleName());
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return qualname;
  }

}
