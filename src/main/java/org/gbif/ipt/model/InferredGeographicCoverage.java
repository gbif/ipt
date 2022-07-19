package org.gbif.ipt.model;

import org.gbif.metadata.eml.BBox;

import java.util.HashSet;
import java.util.Set;

public class InferredGeographicCoverage {

  private BBox data;
  private boolean inferred = false;
  private Set<String> errors = new HashSet<>();

  public BBox getData() {
    return data;
  }

  public void setData(BBox data) {
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
}
