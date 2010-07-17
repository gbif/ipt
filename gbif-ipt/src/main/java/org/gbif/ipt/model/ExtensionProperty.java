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
package org.gbif.ipt.model;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * TODO: Documentation.
 * 
 */
public class ExtensionProperty implements Comparable<ExtensionProperty> {
  private Extension extension;
  private String name;
  private String namespace;
  private String qualname;
  private String group;
  private int columnLength = 255; // sensible default
  private String link;
  private String examples;
  private String description;
  
  public String getExamples() {
	return examples;
}

public void setExamples(String examples) {
	this.examples = examples;
}

public String getDescription() {
	return description;
}

public void setDescription(String description) {
	this.description = description;
}

private boolean required;
  private Vocabulary vocabulary;

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
    setQualname(qualName);
  }

  /**
   * Compare by group and qualified name as default sorting order
   * 
   * @see java.lang.Comparable#compareTo(Object)
   */
  public int compareTo(ExtensionProperty prop) {
	  if (group!=null){
		  int x = this.group.compareTo(prop.group);
		  if (x!=0){
			  return x;
		  }
	  }
	  return this.qualname.compareTo(prop.qualname);
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
    return new EqualsBuilder().append(this.qualname, rhs.qualname).isEquals();
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

public void setQualname(String qualname) {
	this.qualname = qualname;
}

  public Vocabulary getVocabulary() {
    return vocabulary;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    int result = 17;
    result = (qualname != null ? qualname.hashCode() : 0);
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

  public void setLink(String link) {
    this.link = link;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
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

  public void setVocabulary(Vocabulary vocabulary) {
    this.vocabulary = vocabulary;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return qualname;
  }

}
