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
import org.gbif.ipt.model.DataPackageFieldConstraints;
import org.gbif.ipt.model.DataPackageFieldMapping;
import org.gbif.ipt.model.DataPackageMapping;
import org.gbif.ipt.model.DataPackageRowMode;
import org.gbif.ipt.model.DataPackageTableSchema;
import org.gbif.ipt.model.DataPackageTableSchemaForeignKey;
import org.gbif.ipt.model.DataPackageTableSchemaName;
import org.gbif.ipt.model.RecordFilter;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.DataPackageValueKind;
import org.gbif.ipt.model.SourceWithHeader;
import org.gbif.ipt.service.admin.DataPackageSchemaManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.struts2.RequirePost;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.task.DataPackageRowBuilder;
import org.gbif.ipt.task.DataPackageRowsPreview;
import org.gbif.ipt.task.GenerateDataPackage;

import jakarta.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.ActionContext;
import org.apache.struts2.ActionInvocation;
import org.apache.struts2.interceptor.ValidationErrorAware;
import org.apache.struts2.interceptor.parameter.StrutsParameter;

import io.frictionlessdata.tableschema.field.Field;
import io.frictionlessdata.tableschema.schema.Schema;

import lombok.Getter;
import lombok.Setter;

import static org.gbif.ipt.config.Constants.CANCEL;

/**
 * Similar to {@link MappingAction}, but manage data package mappings.
 */
public class DataPackageMappingAction extends ManagerBaseAction implements ValidationErrorAware {

  @Serial
  private static final long serialVersionUID = -2005597864256786458L;

  private static final Logger LOG = LogManager.getLogger(DataPackageMappingAction.class);

  private static final Pattern NAME_IGNORED_CHARACTERS_PATTERN = Pattern.compile("[\\W_]+");

  // choices for a field mapping of a field not in the installed schema, see unresolvedFieldActions
  static final String UNRESOLVED_DISCARD = "discard";
  static final String UNRESOLVED_MOVE_TO_PREFIX = "field:";

  /**
   * What happened to a field mapping of a field not in the installed schema on save.
   */
  enum UnresolvedFieldResult {
    KEPT, DISCARDED, MOVED, TARGET_MAPPED
  }

  private final DataPackageSchemaManager schemaManager;
  private final SourceManager sourceManager;

  @Getter
  private DataPackageSchema dataPackageSchema;
  @Getter
  private Integer mid;
  private DataPackageMapping mapping;
  @Getter
  private List<String> columns;
  @Getter
  private List<String[]> peek;
  @Setter
  private List<DataPackageFieldMapping> fields;
  @Getter
  private Map<String, Integer> fieldsIndices;
  private List<String> newTableSchemas = new ArrayList<>();
  private List<String> newSources = new ArrayList<>();
  // what to do with field mappings of fields not in the installed schema, by field name: kept unless "discard", or
  // moved to the field named after "field:"
  private final Map<String, String> unresolvedFieldActions = new HashMap<>();
  // fields filled in from matching column names on this visit, not saved yet
  @Getter
  private int automappedFields;
  // the rows the saved mapping makes from the first source rows, see getPreview()
  private DataPackageRowsPreview preview;
  // the sample values publishing rejects, see checkSamples()
  private Map<String, String> badSampleCells;
  private Map<String, List<String>> badTranslations;
  private Map<String, String> badConstants;

  @Inject
  public DataPackageMappingAction(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      RegistrationManager registrationManager,
      ResourceManager resourceManager,
      DataPackageSchemaManager schemaManager,
      SourceManager sourceManager) {
    super(textProvider, cfg, registrationManager, resourceManager);
    this.schemaManager = schemaManager;
    this.sourceManager = sourceManager;
  }

  @Override
  public String save() throws IOException {
    if (dataPackageSchema == null) {
      dataPackageSchema = schemaManager.get(id);
    }

    // the mapping is made against the installed version
    mapping.setVersion(dataPackageSchema.getVersion());

    // a new mapping?
    boolean isNew = resource.getDataPackageMapping(mid) == null;
    if (isNew) {
      mid = resource.addDataPackageMapping(mapping);
    } else {
      // save field mappings, and the ones of fields not in the installed schema unless discarded or moved
      List<DataPackageFieldMapping> savedFields = new ArrayList<>(fields);
      for (DataPackageFieldMapping unresolved : mapping.getUnresolvedFieldMappings()) {
        String fieldName = unresolved.getField().getName();
        String action = unresolvedFieldActions.get(fieldName);
        UnresolvedFieldResult result = applyUnresolvedFieldAction(unresolved, action, fields);
        if (result == UnresolvedFieldResult.TARGET_MAPPED) {
          addActionWarning(getText("manage.mapping.unresolved.targetMapped",
              new String[] {fieldName, StringUtils.removeStart(action, UNRESOLVED_MOVE_TO_PREFIX)}));
        }
        if (result == UnresolvedFieldResult.KEPT || result == UnresolvedFieldResult.TARGET_MAPPED) {
          savedFields.add(unresolved);
        }
      }
      readGeneratedValues();
      mapping.setFields(savedFields);
      int fieldsMapped = (int) fields.stream().filter(DataPackageFieldMapping::isMapped).count();
      mapping.setFieldsMapped(fieldsMapped);
    }
    readRows();
    // update last modified dates
    Date lastModified = new Date();
    mapping.setLastModified(lastModified);
    resource.setMappingsModified(lastModified);

    // save entire resource config
    saveResource();
    // missing required fields and values publishing rejects are shown on the fields (dataPackageMapping.ftl)
    if (!isNew) {
      addActionMessage(getText("manage.mapping.saved"));
    }

    // back to the page with a GET, which shows the saved mapping
    return "save";
  }

  /**
   * Reads how the rows are made (the Rows section) from the request. Read here, not bound by Struts: a list of columns
   * that's emptied or a merge that's turned off posts nothing, which must clear the saved value.
   */
  private void readRows() {
    if (req == null) {
      return;
    }
    DataPackageRowMode mode = EnumUtils.getEnum(DataPackageRowMode.class, StringUtils.trimToNull(req.getParameter("rowMode")));
    mapping.setRowMode(mode == DataPackageRowMode.ROW ? null : mode);
    List<Integer> splitColumns = new ArrayList<>();
    String[] split = req.getParameterValues("splitColumns");
    if (split != null) {
      for (String c : split) {
        int i = NumberUtils.toInt(c, -1);
        if (i >= 0 && !splitColumns.contains(i)) {
          splitColumns.add(i);
        }
      }
    }
    mapping.setSplitColumns(mode == DataPackageRowMode.ITEMS && !splitColumns.isEmpty() ? splitColumns : null);
    String separator = req.getParameter("separator");
    mapping.setSeparator(mode == DataPackageRowMode.ITEMS && StringUtils.isNotEmpty(separator)
        && !DataPackageRowBuilder.DEFAULT_SEPARATOR.equals(separator) ? separator : null);
    boolean merge = mode == DataPackageRowMode.DISTINCT || (mode == DataPackageRowMode.ITEMS && req.getParameter("merge") != null);
    mapping.setMergeKey(merge ? StringUtils.trimToNull(req.getParameter("mergeKey")) : null);
  }

  /**
   * Reads the generated values of the fields (valueKind_N, template_N) from the request, for the same reason as
   * {@link #readRows()}, and because an empty choice isn't an enum value Struts can convert.
   */
  private void readGeneratedValues() {
    if (req == null || fields == null) {
      return;
    }
    for (int i = 0; i < fields.size(); i++) {
      DataPackageFieldMapping fm = fields.get(i);
      DataPackageValueKind kind = EnumUtils.getEnum(DataPackageValueKind.class,
          StringUtils.trimToNull(req.getParameter("valueKind_" + i)));
      fm.setValueKind(kind);
      fm.setTemplate(kind == DataPackageValueKind.TEMPLATE ? StringUtils.trimToNull(req.getParameter("template_" + i)) : null);
      // a generated value replaces the column, except an ID made from it
      if (kind != null && kind != DataPackageValueKind.ID) {
        fm.setIndex(null);
      }
    }
  }

  /**
   * @return the rows the saved mapping makes from the first 50 source rows, null for a new mapping
   */
  public DataPackageRowsPreview getPreview() {
    if (preview == null && mapping != null && mid != null && resource.getDataPackageMapping(mid) != null
        && mapping.getSource() != null && mapping.getSource().isReadable()) {
      preview = new DataPackageRowsPreview(mapping, resource.getDataPackageMappings(), sourceManager, 50, 20);
    }
    return preview;
  }

  /**
   * @return the text of a problem a mapping has making its rows
   */
  public String problemText(DataPackageRowBuilder.Problem problem) {
    return getText(problem.key(), problem.params());
  }

  public DataPackageRowMode[] getRowModes() {
    return DataPackageRowMode.values();
  }

  /**
   * Applies the choice made for a field mapping of a field not in the installed schema: discarding it, or moving its
   * column, default value and translations to an unmapped field of the installed schema. Anything else keeps it.
   *
   * @param unresolved field mapping of a field not in the installed schema
   * @param action "discard", "field:" followed by the name of the field to move it to, or null to keep it
   * @param fields field mappings of the installed schema fields
   * @return what happened to it
   */
  static UnresolvedFieldResult applyUnresolvedFieldAction(DataPackageFieldMapping unresolved, String action,
                                                          List<DataPackageFieldMapping> fields) {
    if (UNRESOLVED_DISCARD.equals(action)) {
      return UnresolvedFieldResult.DISCARDED;
    }
    if (action == null || !action.startsWith(UNRESOLVED_MOVE_TO_PREFIX)) {
      return UnresolvedFieldResult.KEPT;
    }

    String targetName = action.substring(UNRESOLVED_MOVE_TO_PREFIX.length());
    DataPackageFieldMapping target = fields.stream()
        .filter(fm -> fm.getField() != null && targetName.equals(fm.getField().getName()))
        .findFirst()
        .orElse(null);
    if (target == null) {
      return UnresolvedFieldResult.KEPT;
    }
    if (target.getIndex() != null || StringUtils.isNotEmpty(target.getDefaultValue())) {
      return UnresolvedFieldResult.TARGET_MAPPED;
    }

    target.setIndex(unresolved.getIndex());
    target.setDefaultValue(unresolved.getDefaultValue());
    target.setTranslation(unresolved.getTranslation());
    return UnresolvedFieldResult.MOVED;
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
        addActionWarning(getText("manage.mapping.source.no.columns", new String[]{src.getName()}));
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
    if (notFound) {
      return NOT_FOUND;
    }
    return INPUT;
  }

  /**
   * Create new mappings. One mapping is required and optional number of additional mappings.
   */
  @RequirePost
  public String create() {
    if (!isHttpPost()) {
      return CANCEL;
    }
    if (notFound) {
      return NOT_FOUND;
    }

    int newMappingsNumber = Math.min(newTableSchemas.size(), newSources.size());

    for (int i = 0; i < newMappingsNumber; i++) {
      String sourceName = newSources.get(i);
      String tableSchemaName = newTableSchemas.get(i);

      DataPackageMapping newMapping = new DataPackageMapping();

      Source source = resource.getSource(sourceName);
      newMapping.setSource(source);
      newMapping.setDataPackageSchema(dataPackageSchema);
      newMapping.setVersion(dataPackageSchema.getVersion());
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

    addActionMessage(getText("manage.mapping.multiple.created", new String[]{String.valueOf(newMappingsNumber)}));

    return SUCCESS;
  }

  @Override
  public void prepare() {
    super.prepare();

    // get mapping sequence id from parameters as setters are not called yet
    String midStr = StringUtils.trimToNull(req.getParameter("mid"));
    if (midStr != null) {
      // not a number: -1, which matches no mapping (404)
      mid = NumberUtils.toInt(midStr, -1);
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
        mapping = resource.getDataPackageMapping(mid);
        if (mapping == null) {
          notFound("manage.404.mapping.title", "manage.404.mapping.body", resource.getShortname());
          return;
        }
      }
    } else {
      // worst case, just redirect to resource not found page
      notFound = true;
    }

    if (!isPrepareBeforeDelete() && !cancel && mapping != null && mapping.getDataPackageSchema() != null) {
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
      DataPackageTableSchema tableSchema = getTableSchema();
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
        int automapped = automap(fields, columns);
        // not on a POST: the message would be carried over to the page it redirects to
        if (automapped > 0 && !isHttpPost()) {
          // the page is shown with fields that aren't saved: it starts with its save bar (js/mapping.js)
          automappedFields = automapped;
          addActionMessage(getText("manage.mapping.automaped.unsaved", new String[]{String.valueOf(automapped)}));
        }
      }
    }
  }

  /**
   * @return the table schema of this mapping in the installed schema, null if it isn't there
   */
  public DataPackageTableSchema getTableSchema() {
    if (dataPackageSchema == null || mapping == null || mapping.getDataPackageTableSchemaName() == null) {
      return null;
    }
    return dataPackageSchema.tableSchemaByName(mapping.getDataPackageTableSchemaName().getName());
  }

  /**
   * @return the fields of the table's primary key, empty if it has none
   */
  public List<String> getPrimaryKey() {
    DataPackageTableSchema tableSchema = getTableSchema();
    if (tableSchema == null || StringUtils.isBlank(tableSchema.getPrimaryKey())) {
      return new ArrayList<>();
    }
    List<String> keys = new ArrayList<>();
    for (String key : tableSchema.getPrimaryKey().split(",")) {
      if (StringUtils.isNotBlank(key)) {
        keys.add(key.trim());
      }
    }
    return keys;
  }

  /**
   * @return per field, the table and field its values refer to, e.g. "event.event_pk"; only fields that are a foreign
   * key on their own
   */
  public Map<String, String> getForeignKeys() {
    Map<String, String> references = new HashMap<>();
    DataPackageTableSchema tableSchema = getTableSchema();
    if (tableSchema == null || tableSchema.getForeignKeys() == null) {
      return references;
    }
    for (DataPackageTableSchemaForeignKey fk : tableSchema.getForeignKeys()) {
      if (fk.getFields() == null || fk.getFields().contains(",") || fk.getReference() == null) {
        continue;
      }
      // a reference to no table is one to this table
      String table = StringUtils.defaultIfBlank(fk.getReference().getResource(), tableSchema.getName());
      references.put(fk.getFields().trim(), table + "." + StringUtils.trimToEmpty(fk.getReference().getFields()));
    }
    return references;
  }

  /**
   * What a field accepts, besides its enum, for the message about values it doesn't: e.g. "integer, minimum 0".
   * Type, format and constraint names are the schema's.
   *
   * @return per field name, empty for a string field without constraints
   */
  public Map<String, String> getFieldExpectations() {
    Map<String, String> expectations = new HashMap<>();
    for (DataPackageFieldMapping fm : fields) {
      DataPackageField field = fm.getField();
      List<String> parts = new ArrayList<>();
      String type = StringUtils.defaultIfBlank(field.getType(), "string");
      String format = field.getFormat();
      boolean defaultFormat = StringUtils.isBlank(format) || "default".equals(format);
      if (!"string".equals(type) || !defaultFormat) {
        parts.add(defaultFormat ? type : type + " (" + format + ")");
      }
      parts.addAll(limits(field));
      expectations.put(field.getName(), String.join(", ", parts));
    }
    return expectations;
  }

  /**
   * @return per field name, its constraints for the field definition, e.g. "required", "minimum 0"; enum values aren't
   * included, the page lists them on their own
   */
  public Map<String, List<String>> getFieldConstraints() {
    Map<String, List<String>> constraints = new HashMap<>();
    for (DataPackageFieldMapping fm : fields) {
      DataPackageField field = fm.getField();
      List<String> parts = new ArrayList<>();
      if (field.getConstraints() != null) {
        if (Boolean.TRUE.equals(field.getConstraints().getRequired())) {
          parts.add("required");
        }
        if (Boolean.TRUE.equals(field.getConstraints().getUnique())) {
          parts.add("unique");
        }
      }
      parts.addAll(limits(field));
      constraints.put(field.getName(), parts);
    }
    return constraints;
  }

  /**
   * @return a field's minimum, maximum and pattern, named as in the schema
   */
  private static List<String> limits(DataPackageField field) {
    List<String> limits = new ArrayList<>();
    DataPackageFieldConstraints c = field.getConstraints();
    if (c != null) {
      if (c.getMinimum() != null) {
        limits.add("minimum " + number(c.getMinimum()));
      }
      if (c.getMaximum() != null) {
        limits.add("maximum " + number(c.getMaximum()));
      }
      if (StringUtils.isNotBlank(c.getPattern())) {
        limits.add("pattern " + c.getPattern());
      }
    }
    return limits;
  }

  private static String number(Double d) {
    return d == Math.rint(d) && !Double.isInfinite(d) ? String.valueOf(d.longValue()) : String.valueOf(d);
  }

  /**
   * The sample values publishing rejects, per field, for every column, so the page can flag them after the column is
   * changed.
   *
   * @return per field name, the rejected cells as "column.row", separated by spaces
   */
  public Map<String, String> getBadSampleCells() {
    checkSamples();
    return badSampleCells;
  }

  /**
   * @return per field name, the translated values (of its saved translations) publishing rejects
   */
  public Map<String, List<String>> getBadTranslations() {
    checkSamples();
    return badTranslations;
  }

  /**
   * @return per field name, its saved constant if publishing rejects it
   */
  public Map<String, String> getBadConstants() {
    checkSamples();
    return badConstants;
  }

  /**
   * Checks the sample values, saved translations and saved constants as publishing does
   * (GenerateDataPackage.checkValues): trimmed, written as in the field's enum, and read by the installed table
   * schema's field, constraints included. Empty values aren't checked here: a constant can fill them in, and a required
   * field's empty values are flagged by the page.
   */
  private void checkSamples() {
    if (badSampleCells != null) {
      return;
    }
    badSampleCells = new HashMap<>();
    badTranslations = new HashMap<>();
    badConstants = new HashMap<>();
    DataPackageTableSchema tableSchema = getTableSchema();
    if (tableSchema == null || fields == null) {
      return;
    }
    Schema schema;
    try {
      File schemaFile = schemaManager.getTableSchemaFile(dataPackageSchema, tableSchema.getName());
      if (!schemaFile.exists()) {
        return;
      }
      schema = Schema.fromJson(schemaFile, true);
    } catch (Exception e) {
      LOG.debug("Can't read table schema {} to check the sample values", tableSchema.getName(), e);
      return;
    }
    for (DataPackageFieldMapping fm : fields) {
      DataPackageField schemaField = fm.getField();
      Field<?> field = schema.getField(schemaField.getName());
      if (field == null) {
        continue;
      }
      UnaryOperator<String> asWritten = GenerateDataPackage.EnumValues.asWritten(schemaField);
      Map<String, Boolean> checked = new HashMap<>();
      List<String> cells = new ArrayList<>();
      for (int r = 0; peek != null && r < peek.size(); r++) {
        String[] row = peek.get(r);
        for (int c = 0; row != null && c < row.length && c < columns.size(); c++) {
          if (!accepts(field, asWritten, row[c], checked)) {
            cells.add(c + "." + r);
          }
        }
      }
      if (!cells.isEmpty()) {
        badSampleCells.put(schemaField.getName(), String.join(" ", cells));
      }
      if (fm.getTranslation() != null) {
        List<String> bad = fm.getTranslation().values().stream()
            .filter(v -> !accepts(field, asWritten, v, checked))
            .distinct()
            .toList();
        if (!bad.isEmpty()) {
          badTranslations.put(schemaField.getName(), bad);
        }
      }
      if (!accepts(field, asWritten, fm.getDefaultValue(), checked)) {
        badConstants.put(schemaField.getName(), fm.getDefaultValue().trim());
      }
    }
  }

  private static boolean accepts(Field<?> field, UnaryOperator<String> asWritten, String value,
                                 Map<String, Boolean> checked) {
    if (StringUtils.isBlank(value)) {
      return true;
    }
    return checked.computeIfAbsent(value, v -> {
      try {
        field.castValue(asWritten.apply(v.trim()));
        return true;
      } catch (Exception e) {
        // a cast or constraint error, or any other: publishing fails on all of them
        return false;
      }
    });
  }

  /**
   * Auto-maps a source's columns to the table schema fields with the same name. A column matching the field name
   * (ignoring case) wins over one that only matches after normalization, see {@link #normalizeName(String)}.
   *
   * @param fields field mappings of the table schema
   * @param columns source columns
   * @return the number of fields that have been automapped
   */
  static int automap(List<DataPackageFieldMapping> fields, List<String> columns) {
    int automapped = 0;

    for (DataPackageFieldMapping f : fields) {
      String fieldName = f.getField().getName();
      int index = indexOfColumn(columns, column -> fieldName.equalsIgnoreCase(withoutPrefix(column)));
      if (index == -1) {
        String normalizedFieldName = normalizeName(fieldName);
        index = indexOfColumn(columns, column -> normalizedFieldName.equals(normalizeName(column)));
      }
      if (index != -1) {
        f.setIndex(index);
        automapped++;
      }
    }

    return automapped;
  }

  private static int indexOfColumn(List<String> columns, Predicate<String> matches) {
    for (int i = 0; i < columns.size(); i++) {
      if (StringUtils.isNotBlank(columns.get(i)) && matches.test(columns.get(i))) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Strips a namespace prefix, e.g. "dwc:eventID" becomes "eventID".
   */
  private static String withoutPrefix(String name) {
    String trimmed = name.trim();
    return trimmed.contains(":") ? StringUtils.substringAfter(trimmed, ":") : trimmed;
  }

  /**
   * Normalizes a column or field name for comparison: no namespace prefix, lower case, letters and digits only.
   * Applied to both sides, so e.g. "Assembly Name" matches "assembly_name" and "event-pk" matches "event_pk".
   */
  static String normalizeName(String name) {
    return NAME_IGNORED_CHARACTERS_PATTERN.matcher(withoutPrefix(name).toLowerCase()).replaceAll("");
  }

  /**
   * Populate a DataSchemaFieldMapping from an DataSchemaField. If the DataSchemaField is already mapped, preserves
   * the existing DataSchemaFieldMapping. Otherwise, creates a brand new DataSchemaFieldMapping.
   *
   * @param field DataSchemaField
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
  @RequirePost
  public String delete() {
    if (notFound) {
      return NOT_FOUND;
    }
    String sourceName = Optional.ofNullable(mapping.getSource())
        .map(Source::getName)
        .orElse("unknown");
    String tableSchemaName = Optional.ofNullable(mapping.getDataPackageTableSchemaName())
        .map(DataPackageTableSchemaName::getName)
        .orElse("unknown");

    if (resource.deleteMapping(mapping)) {
      addActionMessage(getText("manage.mapping.deleted", new String[]{sourceName + " → " + tableSchemaName}));
      // set mappings modified date
      resource.setMappingsModified(new Date());
      // save resource
      saveResource();
    } else {
      addActionMessage(getText("manage.mapping.couldnt.delete", new String[]{sourceName + " → " + tableSchemaName}));
    }
    return SUCCESS;
  }

  // by default DefaultWorkflowInterceptor returns "input" on action errors, e.g. an unknown schema id from prepare().
  // The result set in prepare() ("error", the overview page) is defined for every mapping of this action. The errors
  // can also be ones stored for a redirect, so a request that matched nothing is still a 404.
  @Override
  public String actionErrorOccurred(String currentResultName) {
    return notFound ? NOT_FOUND : defaultResult;
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
      if (field.getIndex() != null && field.getIndex() >= 0 && field.getIndex() < columns.size()) {
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

  private boolean isPrepareBeforeDelete() {
    ActionInvocation invocation = ActionContext.getContext().getActionInvocation();
    String actionMethod = invocation.getProxy().getMethod();
    return "delete".equals(actionMethod);
  }

  @StrutsParameter(depth = 2)
  public DataPackageMapping getMapping() {
    return mapping;
  }

  @StrutsParameter(depth = 2)
  public List<String> getNewSources() {
    return newSources;
  }

  @StrutsParameter(depth = 2)
  public List<String> getNewTableSchemas() {
    return newTableSchemas;
  }

  @StrutsParameter(depth = 2)
  public List<DataPackageFieldMapping> getFields() {
    return fields;
  }

  @StrutsParameter(depth = 2)
  public Map<String, String> getUnresolvedFieldActions() {
    return unresolvedFieldActions;
  }
}
