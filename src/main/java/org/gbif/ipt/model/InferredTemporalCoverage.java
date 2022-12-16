package org.gbif.ipt.model;

import org.gbif.metadata.eml.ipt.model.TemporalCoverage;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

public class InferredTemporalCoverage {

  private TemporalCoverage data;
  private boolean inferred = false;
  private Set<String> errors = new HashSet<>();

  public TemporalCoverage getData() {
    return data;
  }

  public void setData(TemporalCoverage data) {
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
    if (!(o instanceof  InferredTemporalCoverage)) return false;
    InferredTemporalCoverage that = (InferredTemporalCoverage) o;
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
    return new StringJoiner(", ", InferredTemporalCoverage.class.getSimpleName() + "[", "]")
        .add("data=" + data)
        .add("inferred=" + inferred)
        .add("errors=" + errors)
        .toString();
  }
}
