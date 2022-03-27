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
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import com.google.gson.annotations.SerializedName;

/**
 * Field constraints.
 */
public class DataSchemaFieldConstraints implements Serializable {

  private static final long serialVersionUID = 3098053774204591658L;

  private Boolean required;
  private Boolean unique;
  private Integer maximum;
  private Integer minimum;
  private String pattern;
  @SerializedName("enum")
  private List<String> vocabulary;

  public Boolean getRequired() {
    return required;
  }

  public void setRequired(Boolean required) {
    this.required = required;
  }

  public Boolean getUnique() {
    return unique;
  }

  public void setUnique(Boolean unique) {
    this.unique = unique;
  }

  public Integer getMaximum() {
    return maximum;
  }

  public void setMaximum(Integer maximum) {
    this.maximum = maximum;
  }

  public Integer getMinimum() {
    return minimum;
  }

  public void setMinimum(Integer minimum) {
    this.minimum = minimum;
  }

  public String getPattern() {
    return pattern;
  }

  public void setPattern(String pattern) {
    this.pattern = pattern;
  }

  public List<String> getVocabulary() {
    return vocabulary;
  }

  public void setVocabulary(List<String> vocabulary) {
    this.vocabulary = vocabulary;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DataSchemaFieldConstraints that = (DataSchemaFieldConstraints) o;
    return Objects.equals(required, that.required)
        && Objects.equals(unique, that.unique)
        && Objects.equals(maximum, that.maximum)
        && Objects.equals(minimum, that.minimum)
        && Objects.equals(pattern, that.pattern)
        && Objects.equals(vocabulary, that.vocabulary);
  }

  @Override
  public int hashCode() {
    return Objects.hash(required, unique, maximum, minimum, pattern, vocabulary);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", DataSchemaFieldConstraints.class.getSimpleName() + "[", "]")
        .add("required=" + required)
        .add("unique=" + unique)
        .add("maximum=" + maximum)
        .add("minimum=" + minimum)
        .add("pattern='" + pattern + "'")
        .add("vocabulary=" + vocabulary)
        .toString();
  }
}
