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

import org.gbif.common.parsers.core.ParseResult;
import org.gbif.common.parsers.date.DateParsers;
import org.gbif.common.parsers.date.TemporalParser;
import org.gbif.dwc.ArchiveField.DataType;
import org.gbif.dwc.terms.Term;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.model.PropertyMapping;
import org.gbif.ipt.model.Resource;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;

public class ExtensionMappingValidator {

  private static final TemporalParser TEXTDATE_PARSER = DateParsers.defaultTemporalParser();

  public static class ValidationStatus {

    private List<Term> missingRequiredFields = Lists.newArrayList();
    private List<Term> wrongDataTypeFields = Lists.newArrayList();
    private List<String> multipleTranslationsForSameColumn = Lists.newArrayList();
    private String idProblem;
    private String[] idProblemParams;

    public void addMissingRequiredField(Term missingRequiredField) {
      this.missingRequiredFields.add(missingRequiredField);
    }

    public void addWrongDataTypeField(Term wrongDataTypeField) {
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

    public List<Term> getMissingRequiredFields() {
      return missingRequiredFields;
    }

    public List<Term> getWrongDataTypeFields() {
      return wrongDataTypeFields;
    }

    public boolean isValid() {
      return idProblem == null && missingRequiredFields.isEmpty() && wrongDataTypeFields.isEmpty()
             && multipleTranslationsForSameColumn.isEmpty();
    }

    public void setIdProblem(String idProblem) {
      this.idProblem = idProblem;
    }

    public void setIdProblemParams(String[] idProblemParams) {
      this.idProblemParams = idProblemParams;
    }

    /**
     * @return list of column names (from original source file) that have been translated multiple times
     */
    public List<String> getMultipleTranslationsForSameColumn() {
      return multipleTranslationsForSameColumn;
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
          }
        } else if (DataType.date == dt) {
          ParseResult<TemporalAccessor> parsedDateResult = TEXTDATE_PARSER.parse(val);
          TemporalAccessor parsedDateTa = parsedDateResult.getPayload();
          if (parsedDateTa == null) {
            return false;
          }
          if (!parsedDateTa.isSupported(ChronoField.YEAR)) {
            return false;
          }
        } else if (DataType.decimal == dt) {
          Float.parseFloat(val);
        } else if (DataType.integer == dt) {
          Integer.parseInt(val);
        } else if (DataType.uri == dt) {
          new URI(val);
        }
      } catch (NumberFormatException e) {
        return false;
      } catch (URISyntaxException e) {
        return false;
      }
    }
    return true;
  }

  public ValidationStatus validate(ExtensionMapping mapping, Resource resource, List<String[]> peek, List<String> columns) {
    ValidationStatus v = new ValidationStatus();
    // check required fields
    Extension ext = mapping.getExtension();
    if (ext != null) {
      for (ExtensionProperty p : ext.getProperties()) {
        if (p.isRequired() && !mapping.isMapped(p)) {
          v.addMissingRequiredField(p);
        }
      }

      Set<Integer> translatedColumns = Sets.newHashSet();
      for (PropertyMapping pm : mapping.getFields()) {
        // non string data type. check
        ExtensionProperty extProperty = mapping.getExtension().getProperty(pm.getTerm());
        DataType type = extProperty != null ? extProperty.getType() : null;
        if (type != null && DataType.string != type) {
          if (!isValidDataType(type, pm, peek)) {
            v.addWrongDataTypeField(pm.getTerm());
          }
        }

        // check if there are multiple translations for the same column (in source file)
        if (pm.getIndex() != null && pm.getTranslation() != null) {
          if (translatedColumns.contains(pm.getIndex())) {
            String columnName = columns.get(pm.getIndex());
            v.getMultipleTranslationsForSameColumn().add(columnName);
          } else {
            translatedColumns.add(pm.getIndex());
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
        if (ext.isCore()) {
          // if there are any extensions existing the cant link to the new UUIDs
          for (ExtensionMapping m : resource.getMappings()) {
            if (!m.isCore()) {
              v.setIdProblem("validation.mapping.coreid.uuid.extensions.exist");
            }
          }
        } else {
          v.setIdProblem("validation.mapping.coreid.uuid.extension");
        }
      } // else {
      // TODO: anything to check for regular columns?
      // }
    }
    return v;
  }
}
