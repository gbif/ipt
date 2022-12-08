package org.gbif.ipt.model;

import org.gbif.ipt.action.portal.OrganizedTaxonomicCoverage;
import org.gbif.metadata.eml.ipt.model.TaxonomicCoverage;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

public class InferredTaxonomicCoverage {

  private TaxonomicCoverage data;
  // for UI representation
  private OrganizedTaxonomicCoverage organizedData;
  private boolean inferred = false;
  private Set<String> errors = new HashSet<>();

  public TaxonomicCoverage getData() {
    return data;
  }

  public void setData(TaxonomicCoverage data) {
    this.data = data;
  }

  public OrganizedTaxonomicCoverage getOrganizedData() {
    return organizedData;
  }

  public void setOrganizedData(OrganizedTaxonomicCoverage organizedData) {
    this.organizedData = organizedData;
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
    if (!(o instanceof  InferredTaxonomicCoverage)) return false;
    InferredTaxonomicCoverage that = (InferredTaxonomicCoverage) o;
    return inferred == that.inferred
        && Objects.equals(data, that.data)
        && Objects.equals(organizedData, that.organizedData)
        && Objects.equals(errors, that.errors);
  }

  @Override
  public int hashCode() {
    return Objects.hash(data, organizedData, inferred, errors);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", InferredTaxonomicCoverage.class.getSimpleName() + "[", "]")
        .add("data=" + data)
        .add("organizedData=" + organizedData)
        .add("inferred=" + inferred)
        .add("errors=" + errors)
        .toString();
  }
}
