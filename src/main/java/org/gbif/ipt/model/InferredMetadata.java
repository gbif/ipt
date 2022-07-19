package org.gbif.ipt.model;

import java.util.Date;

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
}
