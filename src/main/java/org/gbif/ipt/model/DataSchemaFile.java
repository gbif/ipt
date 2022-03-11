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
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * A single schema file of {@link DataSchema}
 */
public class DataSchemaFile implements Serializable {

  private static final long serialVersionUID = 3929428113035839253L;

  private String identifier;
  private URL url;

  private String name;
  private String title;
  private String description;
  private List<DataSchemaField> fields = new ArrayList<>();

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public URL getUrl() {
    return url;
  }

  public void setUrl(URL url) {
    this.url = url;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<DataSchemaField> getFields() {
    return fields;
  }

  public void setFields(List<DataSchemaField> fields) {
    this.fields = fields;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DataSchemaFile that = (DataSchemaFile) o;
    return Objects.equals(identifier, that.identifier)
        && Objects.equals(title, that.title)
        && Objects.equals(name, that.name)
        && Objects.equals(url, that.url)
        && Objects.equals(description, that.description)
        && Objects.equals(fields, that.fields);
  }

  @Override
  public int hashCode() {
    return Objects.hash(identifier, title, name, url, description, fields);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", DataSchemaFile.class.getSimpleName() + "[", "]")
        .add("identifier='" + identifier + "'")
        .add("title='" + title + "'")
        .add("name='" + name + "'")
        .add("url=" + url)
        .add("description='" + description + "'")
        .add("fields=" + fields)
        .toString();
  }
}
