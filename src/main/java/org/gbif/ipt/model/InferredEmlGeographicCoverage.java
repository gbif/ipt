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

import org.gbif.metadata.eml.ipt.model.GeospatialCoverage;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

public class InferredEmlGeographicCoverage {

  private GeospatialCoverage data;
  private boolean inferred = false;
  private Set<String> errors = new HashSet<>();

  public GeospatialCoverage getData() {
    return data;
  }

  public void setData(GeospatialCoverage data) {
    this.data = data;
  }

  public boolean isInferred() {
    return inferred;
  }

  public void setInferred(boolean inferred) {
    this.inferred = inferred;
  }

  public Set<String> getErrors() {
    return errors;
  }

  public void setErrors(Set<String> errors) {
    this.errors = errors;
  }

  public void addError(String error) {
    errors.add(error);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof InferredEmlGeographicCoverage)) return false;
    InferredEmlGeographicCoverage that = (InferredEmlGeographicCoverage) o;
    return inferred == that.inferred
        && Objects.equals(data, that.data)
        && Objects.equals(errors, that.errors);
  }

  @Override
  public int hashCode() {
    return Objects.hash(data, inferred, errors);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", InferredEmlGeographicCoverage.class.getSimpleName() + "[", "]")
        .add("data=" + data)
        .add("inferred=" + inferred)
        .add("errors=" + errors)
        .toString();
  }
}
