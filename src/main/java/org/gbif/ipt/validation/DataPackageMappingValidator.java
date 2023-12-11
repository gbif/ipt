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
package org.gbif.ipt.validation;

import org.gbif.ipt.model.DataSchema;
import org.gbif.ipt.model.DataSchemaField;
import org.gbif.ipt.model.DataSchemaMapping;
import org.gbif.ipt.model.DataSubschema;
import org.gbif.ipt.model.Resource;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class DataPackageMappingValidator {

  public static class ValidationStatus {

    private final List<DataSchemaField> missingRequiredFields = new ArrayList<>();

    public void addMissingRequiredField(DataSchemaField missingRequiredField) {
      this.missingRequiredFields.add(missingRequiredField);
    }

    public List<DataSchemaField> getMissingRequiredFields() {
      return missingRequiredFields;
    }

    public boolean isValid() {
      return missingRequiredFields.isEmpty();
    }
  }

  public ValidationStatus validate(DataSchemaMapping mapping, Resource resource, List<String> columns) {
    ValidationStatus v = new ValidationStatus();

    // check required fields
    DataSchema dataSchema = mapping.getDataSchema();
    String resourceName = mapping.getDataSchemaFile().getName();

    if (dataSchema != null) {
      for (DataSubschema subSchema : dataSchema.getTableSchemas()) {
        if (resourceName.equals(subSchema.getName())) {
          for (DataSchemaField field : subSchema.getFields()) {
            String fieldName = field.getName();
            // required, but not mapped (index is NULL) or no default value
            if (isRequiredSchemaField(field)
              && (mapping.getField(fieldName) == null
              || (mapping.getField(fieldName).getIndex() == null && StringUtils.isEmpty(mapping.getField(fieldName).getDefaultValue())))) {
              v.addMissingRequiredField(field);
            }
          }
        }
      }
    }
    return v;
  }

  private boolean isRequiredSchemaField(DataSchemaField field) {
    return field.getConstraints() != null && field.getConstraints().getRequired() != null && field.getConstraints().getRequired();
  }
}
