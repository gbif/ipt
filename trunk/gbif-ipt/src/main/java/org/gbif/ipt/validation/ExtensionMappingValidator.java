/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.ipt.validation;

import org.gbif.dwc.terms.ConceptTerm;
import org.gbif.dwc.text.ArchiveField.DataType;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.Resource;
import org.gbif.metadata.DateUtils;

import org.apache.commons.lang.xwork.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author markus
 */
public class ExtensionMappingValidator {
  public static class ValidationStatus {
    private List<ConceptTerm> missingRequiredFields = new ArrayList<ConceptTerm>();
    private List<ConceptTerm> wrongDataTypeFields = new ArrayList<ConceptTerm>();
    private String idProblem;
    private String[] idProblemParams;

    public void addMissingRequiredField(ConceptTerm missingRequiredField) {
      this.missingRequiredFields.add(missingRequiredField);
    }

    public void addWrongDataTypeField(ConceptTerm wrongDataTypeField) {
      this.wrongDataTypeFields.add(wrongDataTypeField);
    }

    /**
     * @return the i18n key to the message for the problem or null if none
     */
    public String getIdProblem() {
      return idProblem;
    }

    /**
     * @return list of parameters to use for formatting the idProblem i18n message
     */
    public String[] getIdProblemParams() {
      return idProblemParams;
    }

    public List<ConceptTerm> getMissingRequiredFields() {
      return missingRequiredFields;
    }

    public List<ConceptTerm> getWrongDataTypeFields() {
      return wrongDataTypeFields;
    }

    public boolean isValid() {
      return idProblem == null && missingRequiredFields.isEmpty() && wrongDataTypeFields.isEmpty();
    }

    public void setIdProblem(String idProblem) {
      this.idProblem = idProblem;
    }

    public void setIdProblemParams(String[] idProblemParams) {
      this.idProblemParams = idProblemParams;
    }

  }

  private boolean isValidDataType(DataType dt, PropertyMapping pm, List<String[]> peek) {
    // shortcut for strings and nulls
    if (dt == null || dt == DataType.string) {
      return true;
    }

    // prepare set of strings to test
    Set<String> testData = new HashSet<String>();
    testData.add(pm.getDefaultValue());
    if (pm.getIndex() != null) {
      for (String[] row : peek) {
        if (row.length >= pm.getIndex() && pm.getIndex() >= 0) {
          testData.add(row[pm.getIndex()]);
        }
      }
    }
    for (String val : testData) {
      val = StringUtils.trimToNull(val);
      if (val == null) {
        continue;
      }
      try {
        if (DataType.bool == dt) {
          if (val.equals("1")) {
            continue;
          } else {
            Boolean b = Boolean.parseBoolean(val);
          }
        } else if (DataType.date == dt) {
          Date d = DateUtils.parse(val, DateUtils.isoDateFormat);
          if (d == null) {
            return false;
          }
        } else if (DataType.decimal == dt) {
          Float.parseFloat(val);
        } else if (DataType.integer == dt) {
          Integer.parseInt(val);
        } else if (DataType.uri == dt) {
          URI uri = new URI(val);
        }
      } catch (NumberFormatException e) {
        return false;
      } catch (URISyntaxException e) {
        return false;
      }
    }
    return true;
  }

  public ValidationStatus validate(ExtensionMapping mapping, Resource resource, List<String[]> peek) {
    ValidationStatus v = new ValidationStatus();
    // check required fields
    Extension ext = mapping.getExtension();
    if (ext != null) {
      for (ExtensionProperty p : ext.getProperties()) {
        if (p.isRequired() && !mapping.isMapped(p)) {
          v.addMissingRequiredField(p);
        }
      }

      // check data types for default mappings
      for (PropertyMapping pm : mapping.getFields()) {
        ExtensionProperty extProperty = mapping.getExtension().getProperty(pm.getTerm());
        DataType type = extProperty != null ? extProperty.getType() : null;
        if (type != null && DataType.string != type) {
          // non string data type. check
          if (!isValidDataType(type, pm, peek)) {
            v.addWrongDataTypeField(pm.getTerm());
          }
        }
      }

      // check id mapping
      if (mapping.getIdColumn() == null) {
        // no id, ok if this is a core
        if (!ext.isCore()) {
          v.setIdProblem("validation.mapping.coreid.missing");
        }
      } else if (mapping.getIdColumn().equals(ExtensionMapping.IDGEN_LINE_NUMBER)) {
        // pure integers are not allowed as line numbers plus integers will result in duplicates
        if (StringUtils.isNumericSpace(StringUtils.trimToNull(mapping.getIdSuffix()))) {
          v.setIdProblem("validation.mapping.coreid.linenumber.integer");
        }
        // if its core make sure all other mappings to the same extensions dont use linenumber with the same suffix or
        if (ext.isCore()) {
          Set<ExtensionMapping> maps = new HashSet<ExtensionMapping>(resource.getMappings(ext.getRowType()));
          maps.remove(mapping);
          if (!maps.isEmpty()) {
            // we more mappings to the same extension, compare their id policy
            for (ExtensionMapping m : maps) {
              if (m.getIdColumn() != null && m.getIdColumn().equals(ExtensionMapping.IDGEN_LINE_NUMBER)) {
                // do we have different suffices?
                if (StringUtils.trimToEmpty(mapping.getIdSuffix()).equals(m.getIdSuffix())) {
                  v.setIdProblem("validation.mapping.coreid.linenumber.samesufix");
                }
              }
            }
          }
        } else {
          // linenumbers for extensions only make sense when there is a core with the same suffix
          boolean found = false;
          for (ExtensionMapping m : resource.getCoreMappings()) {
            if (m.getIdColumn() != null && m.getIdColumn().equals(ExtensionMapping.IDGEN_LINE_NUMBER)) {
              // do they have the same suffix?
              if (StringUtils.trimToEmpty(mapping.getIdSuffix()).equals(StringUtils.trimToEmpty(m.getIdSuffix()))) {
                found = true;
                break;
              }
            }
          }
          if (!found) {
            v.setIdProblem("validation.mapping.coreid.linenumber.nocoresuffix");
          }
        }
      } else if (mapping.getIdColumn().equals(ExtensionMapping.IDGEN_UUID)) {
        // not allowed for extensions
        if (!ext.isCore()) {
          v.setIdProblem("validation.mapping.coreid.uuid.extension");
        } else {
          // if there are any extensions existing the cant link to the new UUIDs
          for (ExtensionMapping m : resource.getMappings()) {
            if (!m.isCore()) {
              v.setIdProblem("validation.mapping.coreid.uuid.extensions.exist");
            }
          }
        }
      } else {
        // anything to check for regular columns?
      }
    }
    return v;
  }
}
