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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents what {@link DataPackageTableSchema} are required in the data package.
 */
public class DataPackageTableSchemaRequirement {

  private String description;
  private List<String> applicableIfPresentAny = new ArrayList<>();
  private List<DataPackageTableSchemaRequirement> allOf = new ArrayList<>();
  private List<DataPackageTableSchemaRequirement> anyOf = new ArrayList<>();
  private List<DataPackageTableSchemaRequirement> oneOf = new ArrayList<>();
  private List<String> required = new ArrayList<>();
  private List<String> requiredAny = new ArrayList<>();
  private List<String> prohibited = new ArrayList<>();

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<String> getApplicableIfPresentAny() {
    return applicableIfPresentAny;
  }

  public void setApplicableIfPresentAny(List<String> applicableIfPresentAny) {
    this.applicableIfPresentAny = applicableIfPresentAny;
  }

  public List<DataPackageTableSchemaRequirement> getAllOf() {
    return allOf;
  }

  public void setAllOf(List<DataPackageTableSchemaRequirement> required) {
    this.allOf = required;
  }

  public List<DataPackageTableSchemaRequirement> getAnyOf() {
    return anyOf;
  }

  public void setAnyOf(List<DataPackageTableSchemaRequirement> anyOf) {
    this.anyOf = anyOf;
  }

  public List<DataPackageTableSchemaRequirement> getOneOf() {
    return oneOf;
  }

  public void setOneOf(List<DataPackageTableSchemaRequirement> oneOf) {
    this.oneOf = oneOf;
  }

  public List<String> getRequired() {
    return required;
  }

  public void setRequired(List<String> required) {
    this.required = required;
  }

  public List<String> getRequiredAny() {
    return requiredAny;
  }

  public void setRequiredAny(List<String> requiredAny) {
    this.requiredAny = requiredAny;
  }

  public List<String> getProhibited() {
    return prohibited;
  }

  public void setProhibited(List<String> prohibited) {
    this.prohibited = prohibited;
  }

  public ValidationResult validate(Set<String> schemas) {
    ValidationResult result = new ValidationResult();

    // all conditions are empty, skip validation?
    if (allOf.isEmpty() && anyOf.isEmpty() && oneOf.isEmpty() && required.isEmpty() && requiredAny.isEmpty() && prohibited.isEmpty()) {
      result.setValid(true);
      return result;
    }

    // check validation applicable
    Collection<String> applicableSchemas = CollectionUtils.intersection(schemas, applicableIfPresentAny);
    if (!applicableIfPresentAny.isEmpty() && applicableSchemas.isEmpty()) {
      return result;
    }

    // check simple conditions first
    // if prohibited present - then validation fails
    if (!prohibited.isEmpty()) {
      Collection<String> intersection = CollectionUtils.intersection(schemas, prohibited);
      if (!intersection.isEmpty()) {
        result.setValid(false);
        result.setReason(String.format("Prohibited schemas found: %s. The following schemas are prohibited: %s", intersection, prohibited));
        return result;
      }
    }

    if (!required.isEmpty()) {
      boolean allRequiredSchemasPresent = CollectionUtils.containsAll(schemas, required);
      if (!allRequiredSchemasPresent) {
        result.setValid(false);
        result.setReason(String.format("All required schemas must be present: %s", required));
        return result;
      }
    }

    if (!requiredAny.isEmpty()) {
      Collection<String> intersection = CollectionUtils.intersection(schemas, requiredAny);
      if (intersection.size() == 0) {
        result.setValid(false);
        result.setReason(String.format("At least one of required schemas must be present: %s", requiredAny));
        return result;
      }
    }

    // check complex conditions
    if (!allOf.isEmpty()) {
      for (DataPackageTableSchemaRequirement subRequirement : allOf) {
        ValidationResult subResult = subRequirement.validate(schemas);
        if (!subResult.isValid()) {
          result.setValid(false);
          result.setReason(subResult.getReason());
        }
      }
    } else if (!anyOf.isEmpty()) {
      result.setValid(false);
      result.setReason(String.format("At least one valid required, none found: %s", anyOf));
      for (DataPackageTableSchemaRequirement subRequirement : anyOf) {
        ValidationResult subResult = subRequirement.validate(schemas);
        if (subResult.isValid()) {
          result.setValid(true);
          result.setReason("");
          break;
        }
      }
    } else if (!oneOf.isEmpty()) {
      int numberOfValid = 0;
      for (DataPackageTableSchemaRequirement subRequirement : oneOf) {
        ValidationResult subResult = subRequirement.validate(schemas);
        if (subResult.isValid()) {
          numberOfValid = numberOfValid + 1;
          System.out.println("valid: " + subRequirement.description);
        }
      }

      if (numberOfValid != 1) {
        result.setValid(false);
        result.setReason(String.format("Only one required: %s", oneOf));
      }
    }

    return result;
  }

  public static class ValidationResult {

    private boolean valid = true;
    private String reason = "";

    public boolean isValid() {
      return valid;
    }

    public void setValid(boolean valid) {
      this.valid = valid;
    }

    public String getReason() {
      return reason;
    }

    public void setReason(String reason) {
      this.reason = reason;
    }

    @Override
    public String toString() {
      return new StringJoiner(", ", ValidationResult.class.getSimpleName() + "[", "]")
        .add("valid=" + valid)
        .add("reason=" + reason)
        .toString();
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DataPackageTableSchemaRequirement that = (DataPackageTableSchemaRequirement) o;
    return Objects.equals(description, that.description)
        && Objects.equals(allOf, that.allOf)
        && Objects.equals(anyOf, that.anyOf)
        && Objects.equals(oneOf, that.oneOf)
        && Objects.equals(required, that.required)
        && Objects.equals(requiredAny, that.requiredAny)
        && Objects.equals(prohibited, that.prohibited);
  }

  @Override
  public int hashCode() {
    return Objects.hash(description, allOf, anyOf, oneOf, required, requiredAny, prohibited);
  }

  @Override
  public String toString() {
    StringJoiner sj = new StringJoiner(", ");

    if (StringUtils.isNotEmpty(description)) {
      sj.add("\"" + description + "\"");
    } else {
      if (!allOf.isEmpty()) {
        sj.add("allOf=" + allOf);
      }

      if (!anyOf.isEmpty()) {
        sj.add("anyOf=" + anyOf);
      }

      if (!oneOf.isEmpty()) {
        sj.add("oneOf=" + oneOf);
      }

      if (!required.isEmpty()) {
        sj.add("required=" + required);
      }

      if (!requiredAny.isEmpty()) {
        sj.add("requiredAny=" + requiredAny);
      }

      if (!prohibited.isEmpty()) {
        sj.add("prohibited=" + prohibited);
      }
    }

    return sj.toString();
  }
}
