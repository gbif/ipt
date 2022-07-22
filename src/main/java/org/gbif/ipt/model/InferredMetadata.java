package org.gbif.ipt.model;

import java.util.Date;
import java.util.Objects;
import java.util.StringJoiner;

public class InferredMetadata {

  private InferredGeographicCoverage inferredGeographicCoverage;
  private InferredTaxonomicCoverage inferredTaxonomicCoverage;
  private InferredTemporalCoverage inferredTemporalCoverage;
  private Date lastModified;

  public InferredGeographicCoverage getInferredGeographicCoverage() {
    return inferredGeographicCoverage;
  }

  public void setInferredGeographicCoverage(InferredGeographicCoverage inferredGeographicCoverage) {
    this.inferredGeographicCoverage = inferredGeographicCoverage;
  }

  public InferredTaxonomicCoverage getInferredTaxonomicCoverage() {
    return inferredTaxonomicCoverage;
  }

  public void setInferredTaxonomicCoverage(InferredTaxonomicCoverage inferredTaxonomicCoverage) {
    this.inferredTaxonomicCoverage = inferredTaxonomicCoverage;
  }

  public InferredTemporalCoverage getInferredTemporalCoverage() {
    return inferredTemporalCoverage;
  }

  public void setInferredTemporalCoverage(InferredTemporalCoverage inferredTemporalCoverage) {
    this.inferredTemporalCoverage = inferredTemporalCoverage;
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
    if (!(o instanceof  InferredMetadata)) return false;
    InferredMetadata that = (InferredMetadata) o;
    return Objects.equals(inferredGeographicCoverage, that.inferredGeographicCoverage)
        && Objects.equals(inferredTaxonomicCoverage, that.inferredTaxonomicCoverage)
        && Objects.equals(inferredTemporalCoverage, that.inferredTemporalCoverage)
        && Objects.equals(lastModified, that.lastModified);
  }

  @Override
  public int hashCode() {
    return Objects.hash(inferredGeographicCoverage, inferredTaxonomicCoverage, inferredTemporalCoverage, lastModified);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", InferredMetadata.class.getSimpleName() + "[", "]")
        .add("inferredGeographicCoverage=" + inferredGeographicCoverage)
        .add("inferredTaxonomicCoverage=" + inferredTaxonomicCoverage)
        .add("inferredTemporalCoverage=" + inferredTemporalCoverage)
        .add("lastModified=" + lastModified)
        .toString();
  }
}
