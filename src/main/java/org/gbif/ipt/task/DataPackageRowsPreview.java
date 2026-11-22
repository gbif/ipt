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
package org.gbif.ipt.task;

import org.gbif.ipt.model.DataPackageField;
import org.gbif.ipt.model.DataPackageFieldMapping;
import org.gbif.ipt.model.DataPackageMapping;
import org.gbif.ipt.model.DataPackageTableSchema;
import org.gbif.ipt.model.DataPackageTableSchemaForeignKey;
import org.gbif.ipt.model.RecordFilter;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.utils.file.ClosableReportingIterator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Getter;

/**
 * The rows a data package mapping makes from the first rows of its source, as publishing makes them
 * ({@link DataPackageRowBuilder}), with what publishing would reject: rows with the same key but different values,
 * empty required fields and links to rows that aren't there.
 */
@Getter
public class DataPackageRowsPreview {

  private static final Logger LOG = LogManager.getLogger(DataPackageRowsPreview.class);

  /** a row made, with the source line it was made from */
  public record Row(long line, String[] values) {}

  /** a foreign key checked against the rows the linked table's mapping makes from the same source rows */
  public record LinkCheck(String field, String target, int checked, List<String> missing) {}

  private final List<String> columns = new ArrayList<>();
  private final List<Row> rows = new ArrayList<>();
  private final List<DataPackageRowBuilder.Problem> problems = new ArrayList<>();
  private final List<DataPackageRowBuilder.Conflict> conflicts = new ArrayList<>();
  private final Map<String, Integer> emptyRequired = new LinkedHashMap<>();
  private final List<LinkCheck> linkChecks = new ArrayList<>();
  private int sourceRows;
  private int rowsMade;
  private int merged;
  /** rows of a merging mapping whose key field is empty: they aren't merged */
  private int unkeyed;
  /** the field rows merge by, null when they don't merge or merge as identical rows */
  private String mergeKey;
  private final int shown;

  /**
   * @return the checks that failed: what keeps the rows from being made, rows with the same key but different values,
   * required fields left empty and links to rows that aren't there. They stop publishing too
   */
  public int getProblemCount() {
    return problems.size() + conflicts.size() + emptyRequired.size()
        + (int) linkChecks.stream().filter(l -> !l.missing().isEmpty()).count();
  }

  /**
   * @return the rows without a key to tell about: none when the key is a required field, already reported as empty
   */
  public int getUnkeyedShown() {
    return mergeKey != null && emptyRequired.containsKey(mergeKey) ? 0 : unkeyed;
  }

  /**
   * @param mapping the mapping to preview
   * @param allMappings all data package mappings of the resource
   * @param sourceManager reads the source
   * @param limit how many source rows to read
   * @param shown how many of the rows made to keep for display
   */
  public DataPackageRowsPreview(DataPackageMapping mapping, List<DataPackageMapping> allMappings,
                                SourceManager sourceManager, int limit, int shown) {
    this.shown = shown;
    DataPackageTableSchema table = mapping.getTableSchema();
    if (table == null || mapping.getSource() == null) {
      return;
    }
    columns.addAll(mappedColumns(mapping, table));
    if (columns.isEmpty()) {
      return;
    }

    List<String[]> sample = read(mapping, sourceManager, limit);
    sourceRows = sample.size();
    List<String> sourceColumns = sourceManager.columns(mapping.getSource());

    DataPackageRowBuilder builder = new DataPackageRowBuilder(mapping, columns, allMappings, sourceColumns);
    problems.addAll(builder.getProblems());
    List<Row> made = make(mapping, builder, sample);
    rowsMade = made.size();

    List<Row> result = builder.isMerging() ? merge(made, builder) : made;
    for (int i = 0; i < Math.min(shown, result.size()); i++) {
      rows.add(result.get(i));
    }

    // empty required fields
    for (DataPackageField field : table.getFields()) {
      int c = columns.indexOf(field.getName());
      if (field.isRequired() && c >= 0) {
        int empty = (int) result.stream().filter(r -> StringUtils.isBlank(r.values()[c])).count();
        if (empty > 0) {
          emptyRequired.put(field.getName(), empty);
        }
      }
    }

    if (builder.isMerging() && !mapping.isWholeRowMerge()) {
      mergeKey = mapping.getEffectiveMergeKey();
    }

    checkLinks(mapping, table, allMappings, sourceManager, sample, sourceColumns, result);
  }

  /**
   * @return the mapped fields, in the table schema's order
   */
  static List<String> mappedColumns(DataPackageMapping mapping, DataPackageTableSchema table) {
    Set<String> mapped = new HashSet<>();
    for (DataPackageFieldMapping fm : mapping.getFields()) {
      if (fm.getField() != null && fm.isMapped()) {
        mapped.add(fm.getField().getName());
      }
    }
    return table.getFields().stream().map(DataPackageField::getName).filter(mapped::contains).toList();
  }

  private static List<String[]> read(DataPackageMapping mapping, SourceManager sourceManager, int limit) {
    List<String[]> sample = new ArrayList<>();
    try (ClosableReportingIterator<String[]> iter = sourceManager.rowIterator(mapping.getSource())) {
      while (iter.hasNext() && sample.size() < limit) {
        String[] in = iter.next();
        if (in != null && in.length > 0 && !iter.hasRowError()) {
          sample.add(in);
        }
      }
    } catch (Exception e) {
      LOG.warn("Couldn't read the source {} for the preview: {}", mapping.getSource().getName(), e.getMessage());
    }
    return sample;
  }

  /**
   * The rows a mapping makes from the sample, after its filter and translations, as publishing does.
   */
  private static List<Row> make(DataPackageMapping mapping, DataPackageRowBuilder builder, List<String[]> sample) {
    List<Row> made = new ArrayList<>();
    RecordFilter filter = mapping.getFilter();
    boolean filtering = filter != null && filter.getColumn() != null && filter.getComparator() != null
        && filter.getParam() != null;
    long line = 0;
    for (String[] raw : sample) {
      line++;
      String[] in = translated(mapping, raw);
      boolean after = filtering && filter.getFilterTime() == RecordFilter.FilterTime.AfterTranslation;
      if (filtering && !filter.matches(after ? in : raw)) {
        continue;
      }
      for (String[] values : builder.build(in)) {
        made.add(new Row(line, values));
      }
    }
    return made;
  }

  private static String[] translated(DataPackageMapping mapping, String[] raw) {
    String[] in = raw.clone();
    for (DataPackageFieldMapping fm : mapping.getFields()) {
      Integer i = fm.getIndex();
      if (i != null && i >= 0 && i < in.length && fm.getTranslation() != null && fm.getTranslation().containsKey(in[i])) {
        in[i] = fm.getTranslation().get(in[i]);
      }
    }
    return in;
  }

  private List<Row> merge(List<Row> made, DataPackageRowBuilder builder) {
    Map<String, Row> byKey = new LinkedHashMap<>();
    Set<String> conflicting = new HashSet<>();
    int noKey = 0;
    for (Row r : made) {
      String key = builder.mergeKey(r.values());
      if (key == null) {
        // no key: a row of its own
        byKey.put("\u0000" + noKey++, r);
        unkeyed++;
        continue;
      }
      Row first = byKey.get(key);
      if (first == null) {
        // a copy: empty values get filled from later rows
        byKey.put(key, new Row(r.line(), r.values().clone()));
        continue;
      }
      merged++;
      List<DataPackageRowBuilder.Conflict> c = DataPackageRowBuilder.compare(columns, key, first.values(),
          first.line(), r.values(), r.line());
      if (!c.isEmpty() && conflicting.add(key)) {
        conflicts.add(c.get(0));
      }
      DataPackageRowBuilder.fill(first.values(), r.values());
    }
    return new ArrayList<>(byKey.values());
  }

  private void checkLinks(DataPackageMapping mapping, DataPackageTableSchema table, List<DataPackageMapping> allMappings,
                          SourceManager sourceManager, List<String[]> sample, List<String> sourceColumns,
                          List<Row> result) {
    if (table.getForeignKeys() == null) {
      return;
    }
    String sourceName = mapping.getSource().getName();
    for (DataPackageTableSchemaForeignKey fk : table.getForeignKeys()) {
      if (fk.getFields() == null || fk.getFields().contains(",") || fk.getReference() == null) {
        continue;
      }
      String field = fk.getFields().trim();
      int c = columns.indexOf(field);
      if (c < 0) {
        continue;
      }
      String target = StringUtils.defaultIfBlank(fk.getReference().getResource(), table.getName());
      String targetField = StringUtils.trimToEmpty(fk.getReference().getFields());
      // the target table's rows made from the same source rows; mappings of other sources can't be checked here
      List<DataPackageMapping> targets = allMappings.stream()
          .filter(m -> m.getDataPackageTableSchemaName() != null
              && target.equals(m.getDataPackageTableSchemaName().getName())
              && m.getSource() != null && Objects.equals(sourceName, m.getSource().getName()))
          .toList();
      if (targets.isEmpty()) {
        continue;
      }
      Set<String> keys = new HashSet<>();
      for (DataPackageMapping tm : targets) {
        DataPackageTableSchema tt = tm.getTableSchema();
        if (tt == null) {
          continue;
        }
        List<String> tcols = mappedColumns(tm, tt);
        int k = tcols.indexOf(targetField);
        if (k < 0) {
          continue;
        }
        DataPackageRowBuilder tb = new DataPackageRowBuilder(tm, tcols, allMappings, sourceColumns);
        for (Row r : make(tm, tb, sample)) {
          keys.add(StringUtils.defaultString(r.values()[k]));
        }
      }
      Set<String> missing = new LinkedHashSet<>();
      for (Row r : result) {
        String v = r.values()[c];
        if (StringUtils.isNotBlank(v) && !keys.contains(v)) {
          missing.add(v);
        }
      }
      linkChecks.add(new LinkCheck(field, target + "." + targetField, keys.size(), new ArrayList<>(missing)));
    }
  }
}
