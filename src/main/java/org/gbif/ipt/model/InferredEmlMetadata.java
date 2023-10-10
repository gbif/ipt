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

import java.util.Date;
import java.util.Objects;
import java.util.StringJoiner;

public class InferredEmlMetadata implements InferredMetadata {

  private InferredEmlGeographicCoverage inferredEmlGeographicCoverage;
  private InferredEmlTaxonomicCoverage inferredEmlTaxonomicCoverage;
  private InferredEmlTemporalCoverage inferredEmlTemporalCoverage;
  private Date lastModified;

  public InferredEmlGeographicCoverage getInferredEmlGeographicCoverage() {
    return inferredEmlGeographicCoverage;
  }

  public void setInferredEmlGeographicCoverage(InferredEmlGeographicCoverage inferredEmlGeographicCoverage) {
    this.inferredEmlGeographicCoverage = inferredEmlGeographicCoverage;
  }

  public InferredEmlTaxonomicCoverage getInferredEmlTaxonomicCoverage() {
    return inferredEmlTaxonomicCoverage;
  }

  public void setInferredEmlTaxonomicCoverage(InferredEmlTaxonomicCoverage inferredEmlTaxonomicCoverage) {
    this.inferredEmlTaxonomicCoverage = inferredEmlTaxonomicCoverage;
  }

  public InferredEmlTemporalCoverage getInferredEmlTemporalCoverage() {
    return inferredEmlTemporalCoverage;
  }

  public void setInferredEmlTemporalCoverage(InferredEmlTemporalCoverage inferredEmlTemporalCoverage) {
    this.inferredEmlTemporalCoverage = inferredEmlTemporalCoverage;
  }

  public Date getLastModified() {
    return lastModified;
  }

  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof InferredEmlMetadata)) return false;
    InferredEmlMetadata that = (InferredEmlMetadata) o;
    return Objects.equals(inferredEmlGeographicCoverage, that.inferredEmlGeographicCoverage)
        && Objects.equals(inferredEmlTaxonomicCoverage, that.inferredEmlTaxonomicCoverage)
        && Objects.equals(inferredEmlTemporalCoverage, that.inferredEmlTemporalCoverage)
        && Objects.equals(lastModified, that.lastModified);
  }

  @Override
  public int hashCode() {
    return Objects.hash(inferredEmlGeographicCoverage, inferredEmlTaxonomicCoverage, inferredEmlTemporalCoverage, lastModified);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", InferredEmlMetadata.class.getSimpleName() + "[", "]")
        .add("inferredEmlGeographicCoverage=" + inferredEmlGeographicCoverage)
        .add("inferredEmlTaxonomicCoverage=" + inferredEmlTaxonomicCoverage)
        .add("inferredEmlTemporalCoverage=" + inferredEmlTemporalCoverage)
        .add("lastModified=" + lastModified)
        .toString();
  }
}
