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
package org.gbif.ipt.action.manage;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.model.DataPackageField;
import org.gbif.ipt.model.DataPackageFieldMapping;
import org.gbif.ipt.model.DataPackageMapping;
import org.gbif.ipt.model.DataPackageTableSchema;
import org.gbif.ipt.model.DataPackageTableSchemaName;
import org.gbif.ipt.model.RecordFilter;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.SourceWithHeader;
import org.gbif.ipt.service.admin.DataPackageSchemaManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.validation.DataPackageMappingValidator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import lombok.Getter;
import lombok.Setter;

import static org.gbif.ipt.config.Constants.CANCEL;

/**
 * Similar to {@link MappingAction}, but manage data package mappings.
 */
public class DataPackageMappingAction extends ManagerBaseAction {

  private static final long serialVersionUID = -2005597864256786458L;

  private static final Pattern FIELD_FORBIDDEN_CHARACTERS_PATTERN = Pattern.compile("[\\W\\s_0-9]+");

  private final DataPackageSchemaManager schemaManager;
  private final SourceManager sourceManager;

  @Getter
  private DataPackageSchema dataPackageSchema;
  @Getter
  private Integer mid;
  @Getter
  private DataPackageMapping mapping;
  @Getter
  private List<String> columns;
  @Getter
  private List<String[]> peek;
  @Getter
  @Setter
  private List<DataPackageFieldMapping> fields;
  @Getter
  private Map<String, Integer> fieldsIndices;
  @Setter
  @Getter
  private List<String> newTableSchemas = new ArrayList<>();
  @Setter
  @Getter
  private List<String> newSources = new ArrayList<>();

  @Inject
  public DataPackageMappingAction(SimpleTextProvider textProvider, AppConfig cfg,
                                  RegistrationManager registrationManager, ResourceManager resourceManager,
                                  DataPackageSchemaManager schemaManager, SourceManager sourceManager) {
    super(textProvider, cfg, registrationManager, resourceManager);
    this.schemaManager = schemaManager;
    this.sourceManager = sourceManager;
  }

  @Override
  public String save() throws IOException {
    if (dataPackageSchema == null) {
      dataPackageSchema = schemaManager.get(id);
    }

    // a new mapping?
    if (resource.getDataPackageMapping(mid) == null) {
      mid = resource.addDataPackageMapping(mapping);
    } else {
      // save field mappings
      mapping.setFields(fields);
      int fieldsMapped = fields.stream()
          .map(DataPackageFieldMapping::getIndex)
          .filter(Objects::nonNull)
          .map(f -> 1)
          .reduce(0, Integer::sum);
      mapping.setFieldsMapped(fieldsMapped);
    }
    // update last modified dates
    Date lastModified = new Date();
    mapping.setLastModified(lastModified);
    resource.setMappingsModified(lastModified);

    // save entire resource config
    saveResource();
    // report validation without skipping this save
    validateAndReport();

    return defaultResult;
  }

  private void readSource() {
    Source src = mapping.getSource();
    if (src == null) {
      columns = new ArrayList<>();
    } else {
      peek = sourceManager.peek(src, 5);
      // If user wants to import a source without a header lines, the columns are going to be numbered with the first
      // non-null value as an example. Otherwise, read the file/database normally.
      if ((src.isUrlSource() || src.isFileSource())
          && ((SourceWithHeader) src).getIgnoreHeaderLines() == 0) {
        columns = mapping.getColumns(peek);
      } else {
        columns = sourceManager.columns(src);
      }
      if (columns.isEmpty() && src.getName() != null) {
        addActionWarning(getText("manage.mapping.source.no.columns", new String[] {src.getName()}));
      }
    }
  }

  private List<String> readSource(DataPackageMapping mapping) {
    Source src = mapping.getSource();
    List<String> columns;

    if (src == null) {
      columns = new ArrayList<>();
    } else {
      List<String[]> peek = sourceManager.peek(src, 5);
      // If user wants to import a source without a header lines, the columns are going to be numbered with the first
      // non-null value as an example. Otherwise, read the file/database normally.
      if ((src.isUrlSource() || src.isFileSource())
          && ((SourceWithHeader) src).getIgnoreHeaderLines() == 0) {
        columns = mapping.getColumns(peek);
      } else {
        columns = sourceManager.columns(src);
      }
    }

    return columns;
  }

  public String source() {
    return INPUT;
  }

  /**
   * Create new mappings. One mapping is required and optional number of additional mappings.
   */
  public String create() {
    if (!isHttpPost()) {
      return CANCEL;
    }

    int newMappingsNumber = Math.min(newTableSchemas.size(), newSources.size());

    for (int i = 0; i < newMappingsNumber; i++) {
      String sourceName = newSources.get(i);
      String tableSchemaName = newTableSchemas.get(i);

      DataPackageMapping newMapping = new DataPackageMapping();

      Source source = resource.getSource(sourceName);
      newMapping.setSource(source);
      newMapping.setDataPackageSchema(dataPackageSchema);
      newMapping.setDataPackageTableSchemaName(new DataPackageTableSchemaName(tableSchemaName));
      newMapping.setFilter(new RecordFilter());

      List<DataPackageFieldMapping> newMappingFields = new ArrayList<>();

      // inspect source
      List<String> sourceColumns = readSource(newMapping);

      // prepare fields
      DataPackageTableSchema tableSchema = dataPackageSchema.tableSchemaByName(tableSchemaName);
      if (tableSchema != null) {
        for (DataPackageField field : tableSchema.getFields()) {
          DataPackageFieldMapping pm = populateDataSchemaFieldMapping(field);
          newMappingFields.add(pm);
        }
      }

      automap(newMappingFields, sourceColumns);

      newMapping.setFields(newMappingFields);
      newMapping.setLastModified(new Date());

      mid = resource.addDataPackageMapping(newMapping);
    }

    addActionMessage(getText("manage.mapping.multiple.created", new String[] {String.valueOf(newMappingsNumber)}));

    return SUCCESS;
  }

  @Override
  public void prepare() {
    super.prepare();

    // get mapping sequence id from parameters as setters are not called yet
    String midStr = StringUtils.trimToNull(req.getParameter("mid"));
    if (midStr != null) {
      mid = Integer.valueOf(midStr);
    }

    if (id != null) {
      if (mid == null) {
        DataPackageSchema ds = schemaManager.get(id);
        if (ds != null) {
          mapping = new DataPackageMapping();
          mapping.setDataPackageSchema(ds);
        }
        // The data schema could have been null if:
        // 1. The user tried to add a mapping with the select help option, no schema would have been found
        // 2. No schema could be retrieved for the id
        // The result should be the user stays on the overview page, and displays a warning informing them that they
        // need to perform another selection.
        else {
          addActionError(getText("manage.overview.mappings.select.invalid"));
          defaultResult = "error";
        }
      } else {
        List<DataPackageMapping> maps = resource.getDataPackageMappings();
        mapping = maps.get(mid);
      }
    } else {
      // worst case, just redirect to resource not found page
      notFound = true;
    }

    if (!cancel && mapping != null && mapping.getDataPackageSchema() != null) {
      dataPackageSchema = mapping.getDataPackageSchema();

      // reload schema if sub-schemas are empty
      if (dataPackageSchema == null || CollectionUtils.isEmpty(dataPackageSchema.getTableSchemas())) {
        dataPackageSchema = schemaManager.get(id);
      }

      // is source assigned yet?
      if (mapping.getSource() == null) {
        // get source parameter as setters are not called yet
        String source = StringUtils.trimToNull(req.getParameter("source"));
        String tableSchemaName = StringUtils.trimToNull(req.getParameter("tableSchema"));
        if (tableSchemaName != null) {
          mapping.setDataPackageTableSchemaName(new DataPackageTableSchemaName(tableSchemaName));
        }

        if (source != null) {
          Source src = resource.getSource(source);
          mapping.setSource(src);
        } else {
          // show set source form
          defaultResult = "source";
          return;
        }
      }

      // set empty filter if not existing
      if (mapping.getFilter() == null) {
        mapping.setFilter(new RecordFilter());
      }

      fields = new ArrayList<>();
      fieldsIndices = new HashMap<>();

      // inspect source
      readSource();

      // prepare fields
      DataPackageTableSchema tableSchema = mapping.getDataPackageSchema().tableSchemaByName(mapping.getDataPackageTableSchemaName().getName());
      fieldsIndices = new HashMap<>();
      int index = 0;
      if (tableSchema != null) {
        for (DataPackageField field : tableSchema.getFields()) {
          DataPackageFieldMapping pm = populateDataSchemaFieldMapping(field);
          fields.add(pm);
          fieldsIndices.put(field.getName(), index++);
        }
      }

      // do automapping if no fields are found
      List<DataPackageFieldMapping> fieldsBySchema = mapping.getFields();
      boolean mappingEmpty = fieldsBySchema.isEmpty();

      if (mappingEmpty) {
        int automapped = automap();
        if (automapped > 0) {
          addActionMessage(getText("manage.mapping.automaped", new String[] {String.valueOf(automapped)}));
        }
      }

      if (!isHttpPost()) {
        validateAndReport();
      }
    }
  }

  /**
   * Validate the mapping and report any warning or errors, shown on the mapping page.
   */
  private void validateAndReport() {
    if (mapping.getSource() == null) {
      return;
    }

    DataPackageMappingValidator validator = new DataPackageMappingValidator();
    DataPackageMappingValidator.ValidationStatus v = validator.validate(mapping, resource, columns);
    if (v != null && !v.isValid()) {
      for (DataPackageField field : v.getMissingRequiredFields()) {
        addActionWarning(getText("validation.required", new String[] {field.getName()}));
      }
    }
  }

  /**
   * This method auto-maps a source's columns.
   *
   * @return the number of fields that have been automapped
   */
  private int automap() {
    // keep track of how many fields were automapped
    int automapped = 0;

    // next, try to automap the source's remaining columns against the data schema fields
    for (DataPackageFieldMapping f : fields) {
      int idx2 = 0;
      for (String col : columns) {
        String normCol = normalizeColumnName(col);
        if (f.getField().getName().equalsIgnoreCase(normCol)) {
          f.setIndex(idx2);
          // we have automapped the field, so increment automapped counter and exit
          automapped++;
          break;
        }
        idx2++;
      }
    }

    return automapped;
  }

  private int automap(List<DataPackageFieldMapping> fields, List<String> columns) {
    // keep track of how many fields were automapped
    int automapped = 0;

    // next, try to automap the source's remaining columns against the data schema fields
    for (DataPackageFieldMapping f : fields) {
      int idx2 = 0;
      for (String col : columns) {
        String normCol = normalizeColumnName(col);
        if (f.getField().getName().equalsIgnoreCase(normCol)) {
          f.setIndex(idx2);
          // we have automapped the field, so increment automapped counter and exit
          automapped++;
          break;
        }
        idx2++;
      }
    }

    return automapped;
  }

  /**
   * Normalizes an incoming column name so that it can later be compared against a schema's field name.
   * This method converts the incoming string to lower case, and will take the substring up to, but not including the
   * first ":".
   *
   * @param col column name
   * @return the normalized column name, or null if the incoming name was null or empty
   */
  private String normalizeColumnName(String col) {
    String result;
    if (StringUtils.isNotBlank(col)) {
      // exclude _id field
      if (!"_id".equals(col.trim())) {
        result = FIELD_FORBIDDEN_CHARACTERS_PATTERN.matcher(col.toLowerCase()).replaceAll("");
      } else {
        result = col.trim();
      }
      if (result.contains(":")) {
        result = StringUtils.substringAfter(col, ":");
      }
      return result;
    }
    return null;
  }

  /**
   * Populate a DataSchemaFieldMapping from an DataSchemaField. If the DataSchemaField is already mapped, preserves
   * the existing DataSchemaFieldMapping. Otherwise, creates a brand new DataSchemaFieldMapping.
   *
   * @param field DataSchemaField
   *
   * @return DataSchemaFieldMapping created
   */
  private DataPackageFieldMapping populateDataSchemaFieldMapping(DataPackageField field) {
    // mapped already?
    DataPackageFieldMapping fm = mapping.getField(field.getName());
    if (fm == null) {
      // no, create brand new DataSchemaFieldMapping
      fm = new DataPackageFieldMapping();
    }
    fm.setField(field);
    return fm;
  }

  @Override
  public String cancel() {
    // remove empty mapping on cancel
    if (mapping != null && mapping.getSource() == null && mapping.getFields().isEmpty()) {
      resource.deleteMapping(mapping);
      // save resource
      saveResource();
    }
    return CANCEL;
  }

  @Override
  public String delete() {
    if (resource.deleteMapping(mapping)) {
      addActionMessage(getText("manage.mapping.deleted", new String[] {id}));
      // set mappings modified date
      resource.setMappingsModified(new Date());
      // save resource
      saveResource();
    } else {
      addActionMessage(getText("manage.mapping.couldnt.delete", new String[] {id}));
    }
    return SUCCESS;
  }

  public RecordFilter.Comparator[] getComparators() {
    return RecordFilter.Comparator.values();
  }

  /**
   * @return list of columns in the source data that have not been mapped to field(s) yet, or an empty list if the
   * source data has no columns
   */
  public List<String> getNonMappedColumns() {
    List<String> mapped = new ArrayList<>();

    // return an empty list if source data has no columns
    if (columns.isEmpty()) {
      return mapped;
    }

    // get a list of all columns mapped to fields
    for (DataPackageFieldMapping field : fields) {
      if (field.getIndex() != null && field.getIndex() >=0 && field.getIndex() < columns.size()) {
        String sourceColumn = columns.get(field.getIndex());
        if (sourceColumn != null) {
          mapped.add(sourceColumn);
        }
      }
    }

    // return list all source columns excluding those mapped
    List<String> nonMapped = new ArrayList<>(columns);
    nonMapped.removeAll(mapped);
    return nonMapped;
  }
}
