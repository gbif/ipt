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

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Represents what {@link DataSubschema} are required in the data package.
 */
public class SubSchemaRequirement {

  private List<String> required;
  private List<String> optional;
  private List<String> anyOf;
  private List<String> oneOf;

  public List<String> getRequired() {
    return required;
  }

  public void setRequired(List<String> required) {
    this.required = required;
  }

  public List<String> getOptional() {
    return optional;
  }

  public void setOptional(List<String> optional) {
    this.optional = optional;
  }

  public List<String> getAnyOf() {
    return anyOf;
  }

  public void setAnyOf(List<String> anyOf) {
    this.anyOf = anyOf;
  }

  public List<String> getOneOf() {
    return oneOf;
  }

  public void setOneOf(List<String> oneOf) {
    this.oneOf = oneOf;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SubSchemaRequirement that = (SubSchemaRequirement) o;
    return Objects.equals(required, that.required)
        && Objects.equals(optional, that.optional)
        && Objects.equals(anyOf, that.anyOf)
        && Objects.equals(oneOf, that.oneOf);
  }

  @Override
  public int hashCode() {
    return Objects.hash(required, optional, anyOf, oneOf);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", SubSchemaRequirement.class.getSimpleName() + "[", "]")
        .add("required=" + required)
        .add("optional=" + optional)
        .add("anyOf=" + anyOf)
        .add("oneOf=" + oneOf)
        .toString();
  }
}
