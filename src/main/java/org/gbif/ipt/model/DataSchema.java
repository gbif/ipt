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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Data schema definition.
 */
public class DataSchema implements Serializable {

  private static final long serialVersionUID = -3130006092545816514L;

  private String identifier;
  private String title;
  private String name;
  private String version;
  private URL url;
  private List<DataSchemaFile> subSchemas = new ArrayList<>();
  private String description;
  private boolean isLatest;
  private Date issued;

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

  public List<DataSchemaFile> getSubSchemas() {
    return subSchemas;
  }

  public void setSubSchemas(List<DataSchemaFile> subSchemas) {
    this.subSchemas = subSchemas;
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

  public void addDataSchemaFile(DataSchemaFile dataSchemaFile) {
    subSchemas.add(dataSchemaFile);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DataSchema that = (DataSchema) o;
    return isLatest == that.isLatest
        && Objects.equals(identifier, that.identifier)
        && Objects.equals(title, that.title)
        && Objects.equals(name, that.name)
        && Objects.equals(version, that.version)
        && Objects.equals(url, that.url)
        && Objects.equals(subSchemas, that.subSchemas)
        && Objects.equals(description, that.description)
        && Objects.equals(issued, that.issued);
  }

  @Override
  public int hashCode() {
    return Objects.hash(identifier, title, name, version, url, subSchemas, description, isLatest, issued);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", DataSchema.class.getSimpleName() + "[", "]")
        .add("identifier='" + identifier + "'")
        .add("title='" + title + "'")
        .add("name='" + name + "'")
        .add("version='" + version + "'")
        .add("url=" + url)
        .add("subSchemas=" + subSchemas)
        .add("description='" + description + "'")
        .add("isLatest=" + isLatest)
        .add("issued=" + issued)
        .toString();
  }
}
