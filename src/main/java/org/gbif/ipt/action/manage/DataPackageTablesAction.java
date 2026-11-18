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
import org.gbif.ipt.model.DataPackageField;
import org.gbif.ipt.model.DataPackageFieldMapping;
import org.gbif.ipt.model.DataPackageMapping;
import org.gbif.ipt.model.DataPackageRowMode;
import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.model.DataPackageTableSchema;
import org.gbif.ipt.model.DataPackageTableSchemaForeignKey;
import org.gbif.ipt.model.DataPackageTableSchemaName;
import org.gbif.ipt.model.DataPackageValueKind;
import org.gbif.ipt.model.RecordFilter;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.service.admin.DataPackageSchemaManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.struts2.RequirePost;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.task.DataPackageRowBuilder;
import org.gbif.utils.file.ClosableReportingIterator;

import jakarta.inject.Inject;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Getter;

/**
 * "Split into tables" (reached from "Map tables"): suggests DwC-DP mappings of one flat source, one per table, from
 * what's in the data, and creates the ones the user ticks. Each is an ordinary data package mapping: its rows are made
 * by a rule ({@link DataPackageRowMode}) and its keys and links by generated values ({@link DataPackageValueKind}).
 */
public class DataPackageTablesAction extends ManagerBaseAction {

  @Serial
  private static final long serialVersionUID = 2793162873912405881L;

  private static final Logger LOG = LogManager.getLogger(DataPackageTablesAction.class);

  // rows shown per suggestion
  private static final int PREVIEW_ROWS = 3;
  // how many rows the suggestions read
  static final int SAMPLE_ROWS = 10_000;

  static final String OCCURRENCE = "occurrence";
  static final String EVENT = "event";
  static final String AGENT = "agent";
  static final String OCCURRENCE_AGENT_ROLE = "occurrence-agent-role";
  static final String EVENT_AGENT_ROLE = "event-agent-role";
  static final String IDENTIFICATION = "identification";
  static final String MATERIAL = "material";
  static final String RECORDED_BY = "recordedBy";

  // DwC agent-name terms, the role of the person in each, and the term with their IDs in the same order
  private static final Map<String, String> NAME_COLUMNS = new LinkedHashMap<>();
  private static final Map<String, String> ID_COLUMNS = new LinkedHashMap<>();

  static {
    NAME_COLUMNS.put(RECORDED_BY, "recorder");
    NAME_COLUMNS.put("identifiedBy", "identifier");
    ID_COLUMNS.put(RECORDED_BY, "recordedByID");
    ID_COLUMNS.put("identifiedBy", "identifiedByID");
  }

  // a source with one of these columns describes specimens: each row is also a material
  private static final List<String> MATERIAL_TERMS = List.of("materialEntityID", "catalogNumber");
  // the material key when there's no materialEntityID, from the ones present, e.g. "NHMD:INV:12345"
  private static final List<String> MATERIAL_KEY_TERMS = List.of("institutionCode", "collectionCode", "catalogNumber");

  // the date and place terms an event key is made from when there's no eventID, best first
  private static final List<List<String>> DATE_TERMS = List.of(List.of("eventDate"), List.of("year", "month", "day"),
      List.of("verbatimEventDate"));
  private static final List<List<String>> PLACE_TERMS = List.of(List.of("locationID"),
      List.of("decimalLatitude", "decimalLongitude"), List.of("locality"), List.of("verbatimLocality"));

  private static final Pattern PLACEHOLDER = Pattern.compile("\\{([^{}]+)}");

  private final DataPackageSchemaManager schemaManager;
  private final SourceManager sourceManager;

  @Getter
  private Source source;
  @Getter
  private DataPackageSchema dataPackageSchema;
  @Getter
  private List<String> columns = new ArrayList<>();
  @Getter
  private int sampleRows;
  // source columns no suggested table reads: their data isn't published unless mapped by hand
  @Getter
  private List<String> unusedColumns = new ArrayList<>();
  @Getter
  private final List<Suggestion> suggestions = new ArrayList<>();
  // why there are no suggestions, a text key
  @Getter
  private String unavailable;

  /**
   * A required field the source doesn't have, which the user fills in with one value for every row.
   */
  @Getter
  public static class Ask {
    private final String field;
    private final String value;

    Ask(String field, String value) {
      this.field = field;
      this.value = value;
    }
  }

  /**
   * A suggested mapping, with why it's suggested and how many rows it makes from the rows read.
   */
  @Getter
  public static class Suggestion {
    private final String id;
    private final DataPackageMapping mapping;
    private boolean checked;
    // already mapped the same way from this source: unticked, so a second run doesn't add a copy
    private boolean existing;
    private final String whyKey;
    private final String[] whyParams;
    private final List<String> warnings = new ArrayList<>();
    private final List<Ask> asks = new ArrayList<>();
    // fields given a fixed value because of the data (occurrenceStatus "detected"), with the column that shows why
    private final Map<String, String> filledFromData = new LinkedHashMap<>();
    private int rows;
    // the first rows it makes from the rows read, to show what the table looks like
    private final List<String> previewFields = new ArrayList<>();
    private final List<String[]> preview = new ArrayList<>();
    private String role;

    Suggestion(String id, DataPackageMapping mapping, boolean checked, String whyKey, String... whyParams) {
      this.id = id;
      this.mapping = mapping;
      this.checked = checked;
      this.whyKey = whyKey;
      this.whyParams = whyParams;
    }

    public String getTable() {
      return mapping.getDataPackageTableSchemaName().getName();
    }
  }

  @Inject
  public DataPackageTablesAction(
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
  public void prepare() {
    super.prepare();
    if (resource == null) {
      return;
    }
    String name = StringUtils.trimToNull(req.getParameter("source"));
    source = name == null ? null : resource.getSource(name);
    if (source == null) {
      notFound("manage.404.source.title", "manage.404.source.body", resource.getShortname(), StringUtils.defaultString(name));
      return;
    }
    dataPackageSchema = resource.getDataPackageIdentifier() == null ? null : schemaManager.get(resource.getDataPackageIdentifier());
    suggest();
    markExisting();
  }

  /**
   * Reads the first rows of the source and suggests the mappings.
   */
  void suggest() {
    suggestions.clear();
    if (dataPackageSchema == null || dataPackageSchema.tableSchemaByName(OCCURRENCE) == null) {
      unavailable = "manage.tables.unavailable.schema";
      return;
    }
    if (!source.isReadable()) {
      unavailable = "manage.tables.unavailable.source";
      return;
    }
    columns = sourceManager.columns(source);
    List<String[]> sample = read();
    sampleRows = sample.size();
    suggestions.addAll(suggest(dataPackageSchema, source, columns, sample));
    if (suggestions.isEmpty()) {
      unavailable = "manage.tables.unavailable.noOccurrenceId";
    }
    unusedColumns = unused(columns, suggestions.stream().map(Suggestion::getMapping).toList());
  }

  /**
   * @return the columns none of the mappings reads: by its column, its list columns or a template
   */
  static List<String> unused(List<String> columns, List<DataPackageMapping> mappings) {
    Set<String> used = new HashSet<>();
    for (DataPackageMapping m : mappings) {
      if (m.getSplitColumns() != null) {
        m.getSplitColumns().stream().filter(c -> c != null && c >= 0 && c < columns.size()).forEach(c -> used.add(columns.get(c)));
      }
      for (DataPackageFieldMapping fm : m.getFields()) {
        if (fm.getIndex() != null && fm.getIndex() >= 0 && fm.getIndex() < columns.size()) {
          used.add(columns.get(fm.getIndex()));
        }
        if (fm.getPairColumns() != null) {
          fm.getPairColumns().stream().filter(c -> c != null && c >= 0 && c < columns.size())
              .forEach(c -> used.add(columns.get(c)));
        }
        if (fm.getValueKind() == DataPackageValueKind.TEMPLATE && fm.getTemplate() != null) {
          Matcher matcher = PLACEHOLDER.matcher(fm.getTemplate());
          while (matcher.find()) {
            used.add(matcher.group(1));
          }
        }
      }
    }
    return columns.stream().filter(c -> StringUtils.isNotBlank(c) && !used.contains(c)).toList();
  }

  private List<String[]> read() {
    List<String[]> sample = new ArrayList<>();
    try (ClosableReportingIterator<String[]> iter = sourceManager.rowIterator(source)) {
      while (iter.hasNext() && sample.size() < SAMPLE_ROWS) {
        String[] in = iter.next();
        if (in != null && in.length > 0 && !iter.hasRowError()) {
          sample.add(in);
        }
      }
    } catch (Exception e) {
      LOG.warn("Couldn't read source {} for the table suggestions: {}", source.getName(), e.getMessage());
    }
    return sample;
  }

  /**
   * The suggestions for a flat source: rules taken from the data only. Package visible for tests.
   */
  static List<Suggestion> suggest(DataPackageSchema schema, Source source, List<String> columns, List<String[]> sample) {
    List<Suggestion> result = new ArrayList<>();
    DataPackageTableSchema occTable = schema.tableSchemaByName(OCCURRENCE);
    DataPackageTableSchema eventTable = schema.tableSchemaByName(EVENT);
    DataPackageTableSchema agentTable = schema.tableSchemaByName(AGENT);
    DataPackageTableSchema roleTable = schema.tableSchemaByName(OCCURRENCE_AGENT_ROLE);
    DataPackageTableSchema eventRoleTable = schema.tableSchemaByName(EVENT_AGENT_ROLE);
    DataPackageTableSchema identificationTable = schema.tableSchemaByName(IDENTIFICATION);
    DataPackageTableSchema materialTable = schema.tableSchemaByName(MATERIAL);

    int occCol = column(columns, "occurrenceID");
    if (occTable == null || occCol < 0) {
      return result;
    }
    int eventCol = column(columns, "eventID");
    Map<String, Integer> nameCols = new LinkedHashMap<>();
    for (String term : NAME_COLUMNS.keySet()) {
      int c = column(columns, term);
      if (c >= 0) {
        nameCols.put(term, c);
      }
    }
    // the people's IDs, in lists matching the names: recordedByID for recordedBy
    Map<String, Integer> idCols = new LinkedHashMap<>();
    for (String term : nameCols.keySet()) {
      int c = column(columns, ID_COLUMNS.get(term));
      if (c >= 0) {
        idCols.put(term, c);
      }
    }
    Set<Integer> peopleCols = new HashSet<>(nameCols.values());
    peopleCols.addAll(idCols.values());

    // the event key: the eventID column, otherwise the date and place columns (one event per date and place),
    // otherwise none (every row in one event)
    List<Integer> eventKeyCols = eventCol >= 0 ? List.of(eventCol) : dateAndPlace(columns);

    // event: the columns with one value inside every event go there; the ones that differ stay out
    Set<Integer> eventCols = new LinkedHashSet<>();
    Map<String, List<String>> varying = new LinkedHashMap<>();
    Map<String, List<String[]>> groups = new LinkedHashMap<>();
    if (eventTable != null) {
      Set<String> eventFields = fieldNames(eventTable);
      for (String[] row : sample) {
        groups.computeIfAbsent(eventKey(row, eventKeyCols), k -> new ArrayList<>()).add(row);
      }
      for (int c = 0; c < columns.size(); c++) {
        if (c == occCol || c == eventCol || !matchesField(columns.get(c), eventFields)) {
          continue;
        }
        List<String> differ = differInside(groups, c);
        if (differ.isEmpty()) {
          eventCols.add(c);
        } else if (!peopleCols.contains(c)) {
          // people that differ inside an event aren't lost: they're linked to each occurrence instead (roles below)
          varying.put(columns.get(c), differ);
        }
      }
    }
    boolean withEvent = eventTable != null;
    // in DwC-DP 1.0 the recorders belong to the event: there when they're the same inside every event
    Integer recordedByCol = nameCols.get(RECORDED_BY);
    List<String> recordersDiffer = recordedByCol == null || !withEvent ? List.of() : differInside(groups, recordedByCol);
    boolean recordersOnEvent = recordedByCol != null && withEvent && eventRoleTable != null
        && eventCols.contains(recordedByCol);
    if (!recordersOnEvent) {
      // recorders per occurrence: their names and IDs don't go to the event
      eventCols.remove(recordedByCol);
      eventCols.remove(idCols.get(RECORDED_BY));
    }

    // occurrence: one per row
    DataPackageMapping occ = mapping(schema, source, occTable);
    List<String> occColumns = new ArrayList<>(columns);
    for (Integer c : eventCols) {
      occColumns.set(c, "");
    }
    if (withEvent && eventCol >= 0) {
      occColumns.set(eventCol, "");
    }
    DataPackageMappingAction.automap(occ.getFields(), occColumns);
    setColumn(occ, occTable.getPrimaryKey(), occCol);
    if (withEvent) {
      setKind(occ, foreignKeyTo(occTable, EVENT), DataPackageValueKind.LINK);
    }
    Suggestion occS = new Suggestion(OCCURRENCE, occ, true, "manage.tables.why.occurrence",
        columns.get(occCol), String.valueOf(distinct(sample, occCol)), String.valueOf(sample.size()));
    fillFromData(occS, sample, columns);
    addAsks(occS);
    result.add(occS);

    if (withEvent) {
      DataPackageMapping ev = mapping(schema, source, eventTable);
      ev.setRowMode(DataPackageRowMode.DISTINCT);
      ev.setMergeKey(eventTable.getPrimaryKey());
      List<String> evColumns = new ArrayList<>();
      for (int c = 0; c < columns.size(); c++) {
        evColumns.add(eventCols.contains(c) || c == eventCol ? columns.get(c) : "");
      }
      DataPackageMappingAction.automap(ev.getFields(), evColumns);
      List<String> evNames = eventCols.stream().map(columns::get).toList();
      String inside = evNames.isEmpty() ? "-" : String.join(", ", evNames);
      DataPackageFieldMapping evPk = ev.getField(eventTable.getPrimaryKey());
      Suggestion evS;
      if (eventCol >= 0) {
        setColumn(ev, eventTable.getPrimaryKey(), eventCol);
        evS = new Suggestion(EVENT, ev, true, "manage.tables.why.event", columns.get(eventCol),
            String.valueOf(sample.size()), String.valueOf(groups.size()), inside);
      } else if (!eventKeyCols.isEmpty()) {
        // a readable key from the date and place, e.g. "2024-07-01 Ystad"; occurrence links with the same rule
        if (evPk != null) {
          evPk.setIndex(null);
          evPk.setValueKind(DataPackageValueKind.TEMPLATE);
          evPk.setTemplate(eventKeyCols.stream().map(c -> "{" + columns.get(c) + "}").collect(Collectors.joining(" ")));
        }
        evS = new Suggestion(EVENT, ev, true, "manage.tables.why.event.made",
            String.join(", ", eventKeyCols.stream().map(columns::get).toList()),
            String.valueOf(sample.size()), String.valueOf(groups.size()), inside);
      } else {
        // nothing to tell events apart: every row in one event, its ID asked for (the source name until then)
        if (evPk != null) {
          evPk.setIndex(null);
          evPk.setDefaultValue(source.getName());
        }
        evS = new Suggestion(EVENT, ev, true, "manage.tables.why.event.one", inside);
        evS.getAsks().add(new Ask(eventTable.getPrimaryKey(), source.getName()));
      }
      varying.forEach((col, keys) -> evS.getWarnings().add(col + "|" + String.join(", ", keys.subList(0, Math.min(3, keys.size())))));
      addAsks(evS);
      result.add(evS);
    }

    if (agentTable != null && !nameCols.isEmpty()) {
      String separator = separator(sample, nameCols.values());
      DataPackageMapping ag = mapping(schema, source, agentTable);
      ag.setRowMode(DataPackageRowMode.ITEMS);
      ag.setSplitColumns(new ArrayList<>(nameCols.values()));
      ag.setSeparator(DataPackageRowBuilder.DEFAULT_SEPARATOR.equals(separator) ? null : separator);
      ag.setMergeKey(agentTable.getPrimaryKey());
      setKind(ag, agentTable.getPrimaryKey(), DataPackageValueKind.ID);
      setKind(ag, "preferredAgentName", DataPackageValueKind.ITEM);
      DataPackageFieldMapping agentId = ag.getField("agentID");
      boolean withIds = agentId != null && !idCols.isEmpty();
      if (withIds) {
        // the ID at the same place as the name: recordedByID for recordedBy, identifiedByID for identifiedBy
        setKind(ag, "agentID", DataPackageValueKind.PAIRED);
        agentId.setPairColumns(nameCols.keySet().stream().map(idCols::get).toList());
      }
      Set<String> names = new HashSet<>();
      for (String[] row : sample) {
        for (Integer c : nameCols.values()) {
          for (String part : StringUtils.splitByWholeSeparator(StringUtils.defaultString(value(row, c)), separator)) {
            String n = DataPackageRowBuilder.normalize(part);
            if (!n.isEmpty()) {
              names.add(n);
            }
          }
        }
      }
      Suggestion agS = new Suggestion(AGENT, ag, true, withIds ? "manage.tables.why.agent.ids" : "manage.tables.why.agent",
          String.join(", ", nameCols.keySet().stream().map(t -> columns.get(nameCols.get(t))).toList()), separator,
          String.valueOf(names.size()), String.join(", ", idCols.values().stream().map(columns::get).toList()));
      addAsks(agS);
      result.add(agS);

      // a role row per name: recorders on the event when they're the same inside every event, otherwise per occurrence
      for (Map.Entry<String, Integer> e : nameCols.entrySet()) {
        boolean onEvent = recordersOnEvent && RECORDED_BY.equals(e.getKey());
        DataPackageTableSchema table = onEvent ? eventRoleTable : roleTable;
        if (table == null) {
          continue;
        }
        String roleName = NAME_COLUMNS.get(e.getKey());
        DataPackageMapping role = mapping(schema, source, table);
        role.setRowMode(DataPackageRowMode.ITEMS);
        role.setSplitColumns(List.of(e.getValue()));
        role.setSeparator(ag.getSeparator());
        setKind(role, foreignKeyTo(table, onEvent ? EVENT : OCCURRENCE), DataPackageValueKind.LINK);
        setKind(role, foreignKeyTo(table, AGENT), DataPackageValueKind.LINK);
        setDefault(role, "agentRole", roleName);
        setKind(role, "agentRoleOrder", DataPackageValueKind.POSITION);
        String id = (onEvent ? EVENT_AGENT_ROLE : OCCURRENCE_AGENT_ROLE) + "-" + e.getKey();
        Suggestion roleS;
        if (onEvent) {
          // every occurrence of the event repeats its recorders: each row once
          role.setMergeKey(DataPackageMapping.WHOLE_ROW);
          roleS = new Suggestion(id, role, true, "manage.tables.why.role.event", columns.get(e.getValue()), roleName);
        } else if (RECORDED_BY.equals(e.getKey()) && !recordersDiffer.isEmpty()) {
          roleS = new Suggestion(id, role, true, "manage.tables.why.role.varies", columns.get(e.getValue()), roleName,
              String.join(", ", recordersDiffer.subList(0, Math.min(3, recordersDiffer.size()))));
        } else {
          roleS = new Suggestion(id, role, true, "manage.tables.why.role", columns.get(e.getValue()), roleName);
        }
        roleS.role = roleName;
        addAsks(roleS);
        result.add(roleS);
      }
    }

    // material: each row is also a specimen when there's a materialEntityID or catalogNumber
    int materialIdCol = column(columns, "materialEntityID");
    List<Integer> keyCols = MATERIAL_KEY_TERMS.stream().map(t -> column(columns, t)).filter(c -> c >= 0).toList();
    boolean specimens = MATERIAL_TERMS.stream().anyMatch(t -> column(columns, t) >= 0);
    if (materialTable != null && specimens) {
      DataPackageMapping mat = mapping(schema, source, materialTable);
      // the columns occurrence and event don't take: the identification stays with the occurrence
      Set<Integer> taken = new HashSet<>(eventCols);
      taken.add(occCol);
      if (eventCol >= 0) {
        taken.add(eventCol);
      }
      occ.getFields().stream().map(DataPackageFieldMapping::getIndex).filter(Objects::nonNull).forEach(taken::add);
      List<String> matColumns = new ArrayList<>();
      for (int c = 0; c < columns.size(); c++) {
        matColumns.add(taken.contains(c) && c != materialIdCol ? "" : columns.get(c));
      }
      DataPackageMappingAction.automap(mat.getFields(), matColumns);
      String key;
      if (materialIdCol >= 0) {
        setColumn(mat, materialTable.getPrimaryKey(), materialIdCol);
        key = columns.get(materialIdCol);
      } else {
        DataPackageFieldMapping pk = mat.getField(materialTable.getPrimaryKey());
        key = keyCols.stream().map(c -> "{" + columns.get(c) + "}").collect(Collectors.joining(":"));
        if (pk != null) {
          pk.setIndex(null);
          pk.setValueKind(DataPackageValueKind.TEMPLATE);
          pk.setTemplate(key);
        }
      }
      setKind(mat, foreignKeyTo(materialTable, OCCURRENCE), DataPackageValueKind.LINK);
      if (withEvent) {
        setKind(mat, foreignKeyTo(materialTable, EVENT), DataPackageValueKind.LINK);
      }
      Suggestion matS = new Suggestion(MATERIAL, mat, true, "manage.tables.why.material",
          String.join(", ", MATERIAL_TERMS.stream().filter(t -> column(columns, t) >= 0).toList()), key);
      addAsks(matS);
      result.add(matS);
    }

    if (identificationTable != null) {
      DataPackageMapping id = mapping(schema, source, identificationTable);
      List<String> idColumns = new ArrayList<>(columns);
      idColumns.set(occCol, "");
      DataPackageMappingAction.automap(id.getFields(), idColumns);
      DataPackageFieldMapping pk = id.getField(identificationTable.getPrimaryKey());
      if (pk != null) {
        pk.setIndex(null);
        pk.setValueKind(DataPackageValueKind.TEMPLATE);
        pk.setTemplate("{" + columns.get(occCol) + "}-id");
      }
      setKind(id, foreignKeyTo(identificationTable, OCCURRENCE), DataPackageValueKind.LINK);
      Suggestion idS = new Suggestion(IDENTIFICATION, id, false, "manage.tables.why.identification");
      addAsks(idS);
      result.add(idS);
    }

    // how many rows each makes from the rows read
    List<DataPackageMapping> all = result.stream().map(Suggestion::getMapping).toList();
    for (Suggestion s : result) {
      s.rows = countRows(s, all, columns, sample);
    }
    return result;
  }

  /**
   * @return the keys of the events a column has more than one value inside, empty if it's the same inside every event
   */
  private static List<String> differInside(Map<String, List<String[]>> groups, int col) {
    List<String> differ = new ArrayList<>();
    for (Map.Entry<String, List<String[]>> g : groups.entrySet()) {
      if (g.getValue().stream().map(r -> value(r, col)).distinct().count() > 1) {
        differ.add(StringUtils.defaultIfEmpty(g.getKey(), "-"));
      }
    }
    return differ;
  }

  /**
   * occurrenceStatus is required: "detected" when every row has a count above 0, otherwise it's asked for.
   */
  private static void fillFromData(Suggestion occ, List<String[]> sample, List<String> columns) {
    DataPackageFieldMapping status = occ.getMapping().getField("occurrenceStatus");
    if (status == null || status.isMapped() || sample.isEmpty()) {
      return;
    }
    int quantity = column(columns, "organismQuantity");
    if (quantity < 0) {
      quantity = column(columns, "individualCount");
    }
    if (quantity < 0) {
      return;
    }
    final int q = quantity;
    boolean allCounted = sample.stream().allMatch(r -> NumberUtils.toDouble(value(r, q), 0) > 0);
    if (allCounted) {
      status.setDefaultValue("detected");
      occ.getFilledFromData().put("occurrenceStatus", columns.get(q));
    }
  }

  /**
   * Required fields still unmapped are asked for.
   */
  private static void addAsks(Suggestion s) {
    for (DataPackageFieldMapping fm : s.getMapping().getFields()) {
      if (fm.getField() != null && fm.getField().isRequired() && !fm.isMapped()) {
        s.getAsks().add(new Ask(fm.getField().getName(), ""));
      }
    }
  }

  /**
   * Counts the rows a suggestion makes from the rows read, and keeps the first ones to show.
   */
  private static int countRows(Suggestion s, List<DataPackageMapping> all, List<String> columns,
                               List<String[]> sample) {
    DataPackageMapping m = s.getMapping();
    List<String> out = m.getFields().stream().filter(DataPackageFieldMapping::isMapped)
        .map(fm -> fm.getField().getName()).toList();
    s.previewFields.clear();
    s.previewFields.addAll(out);
    s.preview.clear();
    DataPackageRowBuilder builder = new DataPackageRowBuilder(m, out, all, columns);
    Set<String> keys = new HashSet<>();
    int rows = 0;
    for (String[] in : sample) {
      for (String[] r : builder.build(in)) {
        if (!builder.isMerging() || keys.add(builder.mergeKey(r))) {
          rows++;
          if (s.preview.size() < PREVIEW_ROWS) {
            s.preview.add(r);
          }
        }
      }
    }
    return rows;
  }

  @Override
  @RequirePost
  public String save() {
    if (notFound) {
      return NOT_FOUND;
    }
    String[] use = req.getParameterValues("use");
    Set<String> ticked = use == null ? Set.of() : Set.of(use);
    int created = 0;
    for (Suggestion s : suggestions) {
      if (!ticked.contains(s.getId())) {
        continue;
      }
      DataPackageMapping m = s.getMapping();
      for (Ask ask : s.getAsks()) {
        String value = StringUtils.trimToNull(req.getParameter("ask_" + s.getId() + "_" + ask.getField()));
        if (value != null) {
          setDefault(m, ask.getField(), value);
        }
      }
      m.setFieldsMapped((int) m.getFields().stream().filter(DataPackageFieldMapping::isMapped).count());
      m.setLastModified(new Date());
      resource.addDataPackageMapping(m);
      created++;
    }
    if (created > 0) {
      resource.setMappingsModified(new Date());
      saveResource();
    }
    addActionMessage(getText("manage.tables.created", new String[] {String.valueOf(created)}));
    return SUCCESS;
  }

  // ---------- helpers ----------

  private static DataPackageMapping mapping(DataPackageSchema schema, Source source, DataPackageTableSchema table) {
    DataPackageMapping m = new DataPackageMapping();
    m.setSource(source);
    m.setDataPackageSchema(schema);
    m.setVersion(schema.getVersion());
    m.setDataPackageTableSchemaName(new DataPackageTableSchemaName(table.getName()));
    m.setFilter(new RecordFilter());
    List<DataPackageFieldMapping> fields = new ArrayList<>();
    for (DataPackageField f : table.getFields()) {
      DataPackageFieldMapping fm = new DataPackageFieldMapping();
      fm.setField(f);
      fields.add(fm);
    }
    m.setFields(fields);
    return m;
  }

  private static Set<String> fieldNames(DataPackageTableSchema table) {
    Set<String> names = new HashSet<>();
    table.getFields().forEach(f -> names.add(DataPackageMappingAction.normalizeName(f.getName())));
    return names;
  }

  private static boolean matchesField(String column, Set<String> normalizedFields) {
    return StringUtils.isNotBlank(column) && normalizedFields.contains(DataPackageMappingAction.normalizeName(column));
  }

  /**
   * @return the index of the column named like the term, -1 if none
   */
  static int column(List<String> columns, String term) {
    String n = DataPackageMappingAction.normalizeName(term);
    for (int i = 0; i < columns.size(); i++) {
      if (StringUtils.isNotBlank(columns.get(i)) && n.equals(DataPackageMappingAction.normalizeName(columns.get(i)))) {
        return i;
      }
    }
    return -1;
  }

  /**
   * @return the columns of the first date terms the source has, eventTime, and the first place terms it has
   */
  static List<Integer> dateAndPlace(List<String> columns) {
    List<Integer> key = new ArrayList<>(firstPresent(columns, DATE_TERMS));
    int time = column(columns, "eventTime");
    if (time >= 0) {
      key.add(time);
    }
    key.addAll(firstPresent(columns, PLACE_TERMS));
    return key;
  }

  private static List<Integer> firstPresent(List<String> columns, List<List<String>> choices) {
    for (List<String> terms : choices) {
      List<Integer> cols = terms.stream().map(t -> column(columns, t)).toList();
      if (cols.stream().allMatch(c -> c >= 0)) {
        return cols;
      }
    }
    return List.of();
  }

  private static String eventKey(String[] row, List<Integer> keyCols) {
    // as the key template fills it in, so the groups are the events publishing makes
    return keyCols.stream().map(c -> StringUtils.defaultString(value(row, c))).collect(Collectors.joining(" "));
  }

  private static String value(String[] row, int c) {
    return c >= 0 && c < row.length ? row[c] : null;
  }

  private static long distinct(List<String[]> sample, int c) {
    return sample.stream().map(r -> StringUtils.defaultString(value(r, c))).distinct().count();
  }

  /**
   * @return the separator the name lists use: "|" or ";" when found, "|" otherwise
   */
  private static String separator(List<String[]> sample, Collection<Integer> cols) {
    for (String sep : List.of("|", ";")) {
      for (String[] row : sample) {
        for (Integer c : cols) {
          if (StringUtils.contains(value(row, c), sep)) {
            return sep;
          }
        }
      }
    }
    return DataPackageRowBuilder.DEFAULT_SEPARATOR;
  }

  /**
   * @return the field of the table that links to the other table, null if none
   */
  static String foreignKeyTo(DataPackageTableSchema table, String target) {
    if (table.getForeignKeys() == null) {
      return null;
    }
    for (DataPackageTableSchemaForeignKey fk : table.getForeignKeys()) {
      if (fk.getReference() != null && target.equals(fk.getReference().getResource()) && fk.getFields() != null
          && !fk.getFields().contains(",")) {
        return fk.getFields().trim();
      }
    }
    return null;
  }

  private static void setColumn(DataPackageMapping m, String field, int column) {
    DataPackageFieldMapping fm = field == null ? null : m.getField(field);
    if (fm != null) {
      fm.setIndex(column);
      fm.setValueKind(null);
    }
  }

  private static void setKind(DataPackageMapping m, String field, DataPackageValueKind kind) {
    DataPackageFieldMapping fm = field == null ? null : m.getField(field);
    if (fm != null) {
      fm.setIndex(null);
      fm.setDefaultValue(null);
      fm.setValueKind(kind);
    }
  }

  private static void setDefault(DataPackageMapping m, String field, String value) {
    DataPackageFieldMapping fm = m.getField(field);
    if (fm != null) {
      fm.setDefaultValue(value);
    }
  }

  /**
   * @return the text of a "why" with its parameters
   */
  public String why(Suggestion s) {
    return getText(s.getWhyKey(), s.getWhyParams());
  }

  /**
   * @return the rule of a mapping, for display: "one row per source row" and so on
   */
  public String rule(DataPackageMapping m) {
    DataPackageRowMode mode = m.getRowModeOrDefault();
    return switch (mode) {
      case ROW -> getText("manage.tables.rule.ROW");
      // named by where the key comes from: "one row per different eventID", not by the key field
      case DISTINCT -> getText("manage.tables.rule.DISTINCT", new String[] {keySource(m)});
      case ITEMS -> {
        List<String> lists = m.getSplitColumns() == null ? List.of()
            : m.getSplitColumns().stream().map(c -> c < columns.size() ? columns.get(c) : "#" + c).toList();
        yield getText(m.isWholeRowMerge() ? "manage.tables.rule.ITEMS.identical"
            : m.getEffectiveMergeKey() != null ? "manage.tables.rule.ITEMS.merged" : "manage.tables.rule.ITEMS",
            new String[] {String.join(", ", lists)});
      }
    };
  }

  private String keySource(DataPackageMapping m) {
    DataPackageFieldMapping fm = m.getMergeKey() == null ? null : m.getField(m.getMergeKey());
    if (fm != null && fm.getValueKind() == null && fm.getIndex() != null && fm.getIndex() < columns.size()) {
      return columns.get(fm.getIndex());
    }
    if (fm != null && fm.getValueKind() == DataPackageValueKind.TEMPLATE && fm.getTemplate() != null) {
      return fm.getTemplate().replaceAll("[{}]", "").trim().replaceAll("\\s+", " + ");
    }
    return StringUtils.defaultString(m.getMergeKey());
  }

  /**
   * @return the tables each suggested table links to, for the overview above the suggestions
   */
  public Map<String, Set<String>> getLinks() {
    Map<String, Set<String>> links = new LinkedHashMap<>();
    for (Suggestion sg : suggestions) {
      Set<String> to = links.computeIfAbsent(sg.getTable(), t -> new LinkedHashSet<>());
      for (String f : sg.getPreviewFields()) {
        String target = linkTable(sg.getMapping(), f);
        if (!target.isEmpty() && !target.equals(sg.getTable())) {
          to.add(target);
        }
      }
    }
    return links;
  }

  /**
   * @return the table a field links to, or "" when it isn't a link
   */
  public String linkTable(DataPackageMapping m, String field) {
    String target = linkTarget(m, field);
    return target.contains(".") ? target.substring(0, target.lastIndexOf('.')) : target;
  }

  /**
   * @return true for the field that names each row of the table
   */
  public boolean isKey(DataPackageMapping m, String field) {
    DataPackageTableSchema t = m.getTableSchema();
    return t != null && field.equals(t.getPrimaryKey());
  }

  /**
   * @return where a field's value comes from, in words, for display
   */
  public String fieldRule(DataPackageMapping m, String field) {
    DataPackageFieldMapping fm = m.getField(field);
    if (fm == null) {
      return "";
    }
    if (fm.getValueKind() == null) {
      if (fm.getIndex() != null && fm.getIndex() < columns.size()) {
        return getText("manage.tables.field.column", new String[] {columns.get(fm.getIndex())});
      }
      return StringUtils.isNotBlank(fm.getDefaultValue())
          ? getText("manage.tables.field.fixed", new String[] {fm.getDefaultValue()}) : "";
    }
    String lists = m.getSplitColumns() == null ? ""
        : m.getSplitColumns().stream().map(c -> c < columns.size() ? columns.get(c) : "#" + c)
            .collect(Collectors.joining(", "));
    return switch (fm.getValueKind()) {
      case ITEM -> getText("manage.tables.field.item", new String[] {lists});
      case POSITION -> getText("manage.tables.field.position");
      case ID -> getText("manage.tables.field.id", new String[] {fm.getIndex() != null && fm.getIndex() < columns.size()
          ? columns.get(fm.getIndex()) : getText("manage.tables.field.listItem")});
      case LINK -> getText("manage.tables.field.link", new String[] {linkTable(m, field)});
      case TEMPLATE -> getText("manage.tables.field.template", new String[] {StringUtils.defaultString(fm.getTemplate())});
      case PAIRED -> getText("manage.tables.field.paired", new String[] {fm.getPairColumns() == null ? ""
          : fm.getPairColumns().stream().filter(Objects::nonNull).map(c -> c < columns.size() ? columns.get(c) : "#" + c)
              .collect(Collectors.joining(", "))});
    };
  }

  private static String linkTarget(DataPackageMapping m, String field) {
    DataPackageTableSchema t = m.getTableSchema();
    if (t != null && t.getForeignKeys() != null) {
      for (DataPackageTableSchemaForeignKey fk : t.getForeignKeys()) {
        if (fk.getFields() != null && field.equals(fk.getFields().trim()) && fk.getReference() != null) {
          return StringUtils.defaultIfBlank(fk.getReference().getResource(), t.getName()) + "."
              + StringUtils.trimToEmpty(fk.getReference().getFields());
        }
      }
    }
    return "";
  }

  /**
   * Unticks the suggestions this source already has a mapping for: the same table, and for one row per list item the
   * same list columns (the recorder and identifier roles are two mappings of one table).
   */
  void markExisting() {
    for (Suggestion s : suggestions) {
      DataPackageMapping sm = s.getMapping();
      for (DataPackageMapping m : resource.getDataPackageMappings()) {
        if (m.getSource() != null && source.getName().equals(m.getSource().getName())
            && m.getDataPackageTableSchemaName() != null
            && s.getTable().equals(m.getDataPackageTableSchemaName().getName())
            && (sm.getRowMode() != DataPackageRowMode.ITEMS || m.getRowMode() != DataPackageRowMode.ITEMS
                || Objects.equals(sm.getSplitColumns(), m.getSplitColumns()))) {
          s.existing = true;
          s.checked = false;
          break;
        }
      }
    }
  }
}
