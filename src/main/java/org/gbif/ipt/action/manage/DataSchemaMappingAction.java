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
import org.gbif.ipt.model.DataSchema;
import org.gbif.ipt.model.DataSchemaField;
import org.gbif.ipt.model.DataSchemaFieldMapping;
import org.gbif.ipt.model.DataSubschema;
import org.gbif.ipt.model.DataSchemaMapping;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.SourceWithHeader;
import org.gbif.ipt.service.admin.DataSchemaManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

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

import static org.gbif.ipt.config.Constants.CANCEL;

/**
 * Similar to {@link MappingAction}, but manage data schema mappings.
 */
public class DataSchemaMappingAction extends ManagerBaseAction {

  private static final long serialVersionUID = -2005597864256786458L;

  private static final Pattern FIELD_FORBIDDEN_CHARACTERS_PATTERN = Pattern.compile("[\\W\\s_0-9]+");

  private final DataSchemaManager schemaManager;
  private final SourceManager sourceManager;

  private DataSchema dataSchema;
  private Integer mid;
  private DataSchemaMapping mapping;
  private List<String> columns;
  private List<String[]> peek;
  private List<DataSchemaFieldMapping> fields;
  private Map<String, Integer> fieldsIndices;

  @Inject
  public DataSchemaMappingAction(SimpleTextProvider textProvider, AppConfig cfg,
                                 RegistrationManager registrationManager, ResourceManager resourceManager,
                                 DataSchemaManager schemaManager, SourceManager sourceManager) {
    super(textProvider, cfg, registrationManager, resourceManager);
    this.schemaManager = schemaManager;
    this.sourceManager = sourceManager;
  }

  @Override
  public String save() throws IOException {
    if (dataSchema == null) {
      dataSchema = schemaManager.get(id);
    }

    // a new mapping?
    if (resource.getDataSchemaMapping(mid) == null) {
      mid = resource.addDataSchemaMapping(mapping);
    } else {
      // save field mappings
      mapping.setFields(fields);
      int fieldsMapped = fields.stream()
          .map(DataSchemaFieldMapping::getIndex)
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
        DataSchema ds = schemaManager.get(id);
        if (ds != null) {
          mapping = new DataSchemaMapping();
          mapping.setDataSchema(ds);
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
        List<DataSchemaMapping> maps = resource.getDataSchemaMappings();
        mapping = maps.get(mid);
      }
    } else {
      // worst case, just redirect to resource not found page
      notFound = true;
    }

    if (!cancel && mapping != null && mapping.getDataSchema() != null) {
      dataSchema = mapping.getDataSchema();

      // reload schema if sub-schemas are empty
      if (dataSchema == null || CollectionUtils.isEmpty(dataSchema.getSubSchemas())) {
        dataSchema = schemaManager.get(id);
      }

      // is source assigned yet?
      if (mapping.getSource() == null) {
        // get source parameter as setters are not called yet
        String source = StringUtils.trimToNull(req.getParameter("source"));
        String schemaFile = StringUtils.trimToNull(req.getParameter("schemaFile"));
        if (schemaFile != null) {
          mapping.setDataSchemaFile(schemaFile);
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

      fields = new ArrayList<>();
      fieldsIndices = new HashMap<>();

      // inspect source
      readSource();

      // prepare fields
      DataSubschema dataSubschema = mapping.getDataSchema().subschemaByName(mapping.getDataSchemaFile());
      fieldsIndices = new HashMap<>();
      int index = 0;
      for (DataSchemaField field : dataSubschema.getFields()) {
        DataSchemaFieldMapping pm = populateDataSchemaFieldMapping(field);
        fields.add(pm);
        fieldsIndices.put(field.getName(), index++);
      }

      // do automapping if no fields are found
      // TODO: 05/10/2022 auto-mapping only for one subschema
      List<DataSchemaFieldMapping> fieldsBySchema = mapping.getFields();
      boolean mappingEmpty = fieldsBySchema.isEmpty();

      if (mappingEmpty) {
        int automapped = automap();
        if (automapped > 0) {
          addActionMessage(getText("manage.mapping.automaped", new String[] {String.valueOf(automapped)}));
        }
      }
    }
  }

  /**
   * This method auto-maps a source's columns.
   *
   * @return the number of terms that have been automapped
   */
  private int automap() {
    // keep track of how many fields were automapped
    int automapped = 0;

    // next, try to automap the source's remaining columns against the data schema fields
    for (DataSchemaFieldMapping f : fields) {
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
  private DataSchemaFieldMapping populateDataSchemaFieldMapping(DataSchemaField field) {
    // mapped already?
    DataSchemaFieldMapping fm = mapping.getField(field.getName());
    if (fm == null) {
      // no, create brand new DataSchemaFieldMapping
      fm = new DataSchemaFieldMapping();
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

  public DataSchema getDataSchema() {
    return dataSchema;
  }

  public DataSchemaMapping getMapping() {
    return mapping;
  }

  public List<String> getColumns() {
    return columns;
  }

  public Integer getMid() {
    return mid;
  }

  public List<DataSchemaFieldMapping> getFields() {
    return fields;
  }

  public void setFields(List<DataSchemaFieldMapping> fields) {
    this.fields = fields;
  }

  public Map<String, Integer> getFieldsIndices() {
    return fieldsIndices;
  }

  public List<String[]> getPeek() {
    return peek;
  }
}
