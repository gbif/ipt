package org.gbif.ipt.model;

import org.apache.commons.math3.util.Pair;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InferredTaxonomicCoverage {

  private Map<String, Set<Pair<String, String>>> data;
  private boolean inferred = false;
  private Set<String> errors = new HashSet<>();

  public Map<String, Set<Pair<String, String>>> getData() {
    return data;
  }

  public void setData(Map<String, Set<Pair<String, String>>> data) {
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
