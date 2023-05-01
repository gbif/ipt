/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.ipt.model;

import java.io.Serializable;
import java.net.URL;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Data schema definition.
 */
public class DataSchema implements Serializable {

  private static final long serialVersionUID = -3130006092545816514L;

  /**
   * Schema unique identifier
   */
  private String identifier;

  /**
   * Schema full title
   */
  private String title;

  /**
   * Schema shortened title
   */
  private String shortTitle;

  /**
   * Schema name for internal usage
   */
  private String name;

  /**
   * Schema version
   */
  private String version;

  /**
   * Schema URL
   */
  private URL url;

  private Set<DataSubschema> subSchemas = new LinkedHashSet<>();

  private SubSchemaRequirement subSchemaRequirements = new SubSchemaRequirement();

  private String description;

  private boolean isLatest;

  private Date issued;

  public DataSubschema subschemaByName(String subSchemaName) {
    return subSchemas.stream()
        .filter(ds -> ds.getName().equals(subSchemaName))
        .findFirst().orElse(null);
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getShortTitle() {
    return shortTitle;
  }

  public void setShortTitle(String shortTitle) {
    this.shortTitle = shortTitle;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public URL getUrl() {
    return url;
  }

  public void setUrl(URL url) {
    this.url = url;
  }

  public Set<DataSubschema> getSubSchemas() {
    return subSchemas;
  }

  public void setSubSchemas(Set<DataSubschema> subSchemas) {
    this.subSchemas = subSchemas;
  }

  public SubSchemaRequirement getSubSchemaRequirements() {
    return subSchemaRequirements;
  }

  public void setSubSchemaRequirements(SubSchemaRequirement subSchemaRequirements) {
    this.subSchemaRequirements = subSchemaRequirements;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isLatest() {
    return isLatest;
  }

  public void setLatest(boolean latest) {
    isLatest = latest;
  }

  public Date getIssued() {
    return issued;
  }

  public void setIssued(Date issued) {
    this.issued = issued;
  }

  public void addDataSchemaFile(DataSubschema dataSubschema) {
    subSchemas.add(dataSubschema);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DataSchema)) return false;
    DataSchema that = (DataSchema) o;
    return isLatest == that.isLatest
        && Objects.equals(identifier, that.identifier)
        && Objects.equals(title, that.title)
        && Objects.equals(shortTitle, that.shortTitle)
        && Objects.equals(name, that.name)
        && Objects.equals(version, that.version)
        && Objects.equals(url, that.url)
        && Objects.equals(subSchemas, that.subSchemas)
        && Objects.equals(subSchemaRequirements, that.subSchemaRequirements)
        && Objects.equals(description, that.description)
        && Objects.equals(issued, that.issued);
  }

  @Override
  public int hashCode() {
    return Objects.hash(identifier, title, shortTitle, name, version, url, subSchemas, subSchemaRequirements, description, isLatest, issued);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", DataSchema.class.getSimpleName() + "[", "]")
        .add("identifier='" + identifier + "'")
        .add("title='" + title + "'")
        .add("title='" + shortTitle + "'")
        .add("name='" + name + "'")
        .add("version='" + version + "'")
        .add("url=" + url)
        .add("subSchemas=" + subSchemas)
        .add("subSchemaRequirement=" + subSchemaRequirements)
        .add("description='" + description + "'")
        .add("isLatest=" + isLatest)
        .add("issued=" + issued)
        .toString();
  }
}
