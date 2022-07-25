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
import java.util.Objects;
import java.util.StringJoiner;

/**
 * A field in schema file of {@link DataSchema}.
 */
public class DataSchemaField implements Serializable {

  private static final long serialVersionUID = 2049952968649028260L;

  private String name;
  private String type;
  private String format;
  private String description;
  private Object example;
  private DataSchemaFieldConstraints constraints;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Object getExample() {
    return example;
  }

  public void setExample(Object example) {
    this.example = example;
  }

  public DataSchemaFieldConstraints getConstraints() {
    return constraints;
  }

  public void setConstraints(DataSchemaFieldConstraints constraints) {
    this.constraints = constraints;
  }

  public String qualifiedName(String namespace) {
    return namespace + '/' + name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DataSchemaField)) return false;
    DataSchemaField that = (DataSchemaField) o;
    return Objects.equals(name, that.name)
        && Objects.equals(type, that.type)
        && Objects.equals(format, that.format)
        && Objects.equals(description, that.description)
        && Objects.equals(example, that.example)
        && Objects.equals(constraints, that.constraints);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type, format, description, example, constraints);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", DataSchemaField.class.getSimpleName() + "[", "]")
        .add("name='" + name + "'")
        .add("type='" + type + "'")
        .add("format='" + format + "'")
        .add("description='" + description + "'")
        .add("example='" + example + "'")
        .add("constraints=" + constraints)
        .toString();
  }
}
