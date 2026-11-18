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

import org.gbif.ipt.model.DataPackageFieldMapping;
import org.gbif.ipt.model.DataPackageMapping;
import org.gbif.ipt.model.DataPackageRowMode;
import org.gbif.ipt.model.DataPackageTableSchema;
import org.gbif.ipt.model.DataPackageTableSchemaForeignKey;
import org.gbif.ipt.model.DataPackageValueKind;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * Makes the rows of a data package mapping's table from one source row: one row, or one per list item, with the
 * generated values ({@link DataPackageValueKind}) filled in. Used when publishing, for the mapping page's preview and
 * by "Split into tables" (DataPackageTablesAction), so all three make the same rows.
 * <p>
 * Keys made from a value are name-based UUIDs (version 5) of the normalized value in a namespace per table, so the same
 * name gives the same key in every publication, and a foreign key made with the same rule matches its primary key.
 */
public class DataPackageRowBuilder {

  public static final String DEFAULT_SEPARATOR = "|";

  // RFC 4122 URL namespace
  private static final UUID URL_NAMESPACE = UUID.fromString("6ba7b811-9dad-11d1-80b4-00c04fd430c8");
  private static final String NAMESPACE_PREFIX = "https://ipt.gbif.org/data-package/";
  private static final Pattern WHITESPACE = Pattern.compile("\\s+");
  private static final Pattern PLACEHOLDER = Pattern.compile("\\{([^{}]+)}");
  static final String ITEM_PLACEHOLDER = "item";
  static final String POSITION_PLACEHOLDER = "position";

  /**
   * Something that keeps the mapping from making its rows as set up. The key is a text key, the parameters fill it in.
   */
  public record Problem(String key, String... params) {
    @Override
    public String toString() {
      return key + (params.length > 0 ? " " + String.join(", ", params) : "");
    }
  }

  /** a field mapping and the table it belongs to, which names the namespace of the keys it makes */
  private record Rule(DataPackageFieldMapping mapping, String table) {}

  private record Item(String value, int position) {}

  private final DataPackageMapping mapping;
  private final String table;
  private final List<DataPackageFieldMapping> usedMappings;
  private final Map<String, Integer> sourceColumnIndexes = new HashMap<>();
  private final Map<String, Rule> links = new HashMap<>();
  private final List<Problem> problems = new ArrayList<>();
  private final Pattern separator;
  private final int mergeColumn;

  /**
   * @param mapping the mapping whose rows are made
   * @param columns the output columns (field names), in order
   * @param allMappings all data package mappings of the resource, for linked keys
   * @param sourceColumns the source's column names, for templates
   */
  public DataPackageRowBuilder(DataPackageMapping mapping, List<String> columns, List<DataPackageMapping> allMappings,
                               List<String> sourceColumns) {
    this.mapping = mapping;
    this.table = mapping.getDataPackageTableSchemaName() != null ? mapping.getDataPackageTableSchemaName().getName() : "";

    Map<String, DataPackageFieldMapping> byName = new HashMap<>();
    for (DataPackageFieldMapping fm : mapping.getFields()) {
      if (fm.getField() != null && fm.isMapped()) {
        byName.put(fm.getField().getName(), fm);
      }
    }
    this.usedMappings = columns.stream().map(byName::get).toList();

    if (sourceColumns != null) {
      for (int i = 0; i < sourceColumns.size(); i++) {
        sourceColumnIndexes.putIfAbsent(sourceColumns.get(i), i);
      }
    }

    String sep = StringUtils.defaultIfEmpty(mapping.getSeparator(), DEFAULT_SEPARATOR);
    this.separator = Pattern.compile(Pattern.quote(sep));

    DataPackageRowMode mode = mapping.getRowModeOrDefault();
    String mergeKey = mapping.getEffectiveMergeKey();
    this.mergeColumn = mergeKey == null ? -1 : columns.indexOf(mergeKey);
    if (mode == DataPackageRowMode.DISTINCT && mergeKey == null) {
      problems.add(new Problem("manage.mapping.rows.problem.noKey"));
    } else if (mergeKey != null && mergeColumn < 0) {
      problems.add(new Problem("manage.mapping.rows.problem.keyNotMapped", mergeKey));
    }
    if (mode == DataPackageRowMode.ITEMS && (mapping.getSplitColumns() == null || mapping.getSplitColumns().isEmpty())) {
      problems.add(new Problem("manage.mapping.rows.problem.noLists"));
    }

    for (DataPackageFieldMapping fm : usedMappings) {
      if (fm == null || fm.getValueKind() == null) {
        continue;
      }
      String name = fm.getField().getName();
      switch (fm.getValueKind()) {
        case ITEM, POSITION -> {
          if (mode != DataPackageRowMode.ITEMS) {
            problems.add(new Problem("manage.mapping.rows.problem.itemWithoutList", name));
          }
        }
        case ID -> {
          if (fm.getIndex() == null && mode != DataPackageRowMode.ITEMS) {
            problems.add(new Problem("manage.mapping.rows.problem.itemWithoutList", name));
          }
        }
        case LINK -> resolveLink(name, allMappings);
        default -> { }
      }
    }
  }

  /**
   * A linked key takes the rule of the primary key of the table it refers to, from that table's mapping of the same
   * source: a rule reading a column only means something for rows of that source.
   */
  private void resolveLink(String field, List<DataPackageMapping> allMappings) {
    DataPackageTableSchema tableSchema = mapping.getTableSchema();
    String target = null;
    String targetField = null;
    if (tableSchema != null && tableSchema.getForeignKeys() != null) {
      for (DataPackageTableSchemaForeignKey fk : tableSchema.getForeignKeys()) {
        if (fk.getFields() != null && field.equals(fk.getFields().trim()) && fk.getReference() != null) {
          target = StringUtils.defaultIfBlank(fk.getReference().getResource(), table);
          targetField = StringUtils.trimToNull(fk.getReference().getFields());
          break;
        }
      }
    }
    if (target == null || targetField == null) {
      problems.add(new Problem("manage.mapping.rows.problem.notForeignKey", field));
      return;
    }

    String sourceName = mapping.getSource() != null ? mapping.getSource().getName() : null;
    for (DataPackageMapping other : allMappings) {
      if (other.getDataPackageTableSchemaName() == null || !target.equals(other.getDataPackageTableSchemaName().getName())
          || other.getSource() == null || !Objects.equals(sourceName, other.getSource().getName())) {
        continue;
      }
      DataPackageFieldMapping key = other.getField(targetField);
      if (key != null && key.isMapped() && key.getValueKind() != DataPackageValueKind.LINK) {
        links.put(field, new Rule(key, target));
        return;
      }
    }
    problems.add(new Problem("manage.mapping.rows.problem.noLinkedMapping", field, target + "." + targetField,
        StringUtils.defaultString(sourceName)));
  }

  /**
   * @return what keeps the mapping from making its rows as set up, empty if nothing
   */
  public List<Problem> getProblems() {
    return problems;
  }

  /**
   * @return the output column rows merge by, -1 if they don't merge
   */
  public int getMergeColumn() {
    return mergeColumn;
  }

  /**
   * @param in the source row, translations already applied
   * @return the rows it makes: one, one per list item, or none when the list columns are empty
   */
  public List<String[]> build(String[] in) {
    List<String[]> rows = new ArrayList<>();
    if (mapping.getRowModeOrDefault() == DataPackageRowMode.ITEMS) {
      for (Item item : items(in)) {
        rows.add(row(in, item));
      }
    } else {
      rows.add(row(in, null));
    }
    return rows;
  }

  private List<Item> items(String[] in) {
    List<Item> items = new ArrayList<>();
    if (mapping.getSplitColumns() == null) {
      return items;
    }
    for (Integer column : mapping.getSplitColumns()) {
      if (column == null || column < 0 || column >= in.length || in[column] == null) {
        continue;
      }
      int position = 0;
      for (String part : separator.split(in[column])) {
        String value = normalize(part);
        if (!value.isEmpty()) {
          items.add(new Item(value, ++position));
        }
      }
    }
    return items;
  }

  private String[] row(String[] in, Item item) {
    String[] out = new String[usedMappings.size()];
    for (int i = 0; i < out.length; i++) {
      DataPackageFieldMapping fm = usedMappings.get(i);
      if (fm != null) {
        out[i] = value(new Rule(fm, table), in, item);
      }
    }
    return out;
  }

  private String value(Rule rule, String[] in, Item item) {
    DataPackageFieldMapping fm = rule.mapping();
    if (fm.getValueKind() == null) {
      String value = column(in, fm.getIndex());
      return value == null ? fm.getDefaultValue() : value;
    }
    return switch (fm.getValueKind()) {
      case ITEM -> item == null ? null : item.value();
      case POSITION -> item == null ? null : String.valueOf(item.position());
      case ID -> {
        String from = fm.getIndex() != null ? column(in, fm.getIndex()) : (item == null ? null : item.value());
        yield StringUtils.isBlank(from) ? null : key(rule.table(), from);
      }
      case TEMPLATE -> template(fm.getTemplate(), in, item);
      case LINK -> {
        Rule target = links.get(fm.getField().getName());
        yield target == null ? null : value(target, in, item);
      }
    };
  }

  private static String column(String[] in, Integer index) {
    return index != null && index >= 0 && index < in.length ? in[index] : null;
  }

  private String template(String template, String[] in, Item item) {
    if (StringUtils.isEmpty(template)) {
      return null;
    }
    Matcher m = PLACEHOLDER.matcher(template);
    StringBuilder sb = new StringBuilder();
    while (m.find()) {
      String name = m.group(1);
      String value;
      if (ITEM_PLACEHOLDER.equals(name)) {
        value = item == null ? "" : item.value();
      } else if (POSITION_PLACEHOLDER.equals(name)) {
        value = item == null ? "" : String.valueOf(item.position());
      } else {
        Integer index = sourceColumnIndexes.get(name);
        value = index == null ? m.group() : StringUtils.defaultString(column(in, index));
      }
      m.appendReplacement(sb, Matcher.quoteReplacement(value));
    }
    m.appendTail(sb);
    return sb.toString();
  }

  /**
   * @return the value trimmed, with runs of whitespace made one space
   */
  public static String normalize(String value) {
    return value == null ? "" : WHITESPACE.matcher(value).replaceAll(" ").trim();
  }

  /**
   * @return the key made from a value for a table: a version 5 UUID of the normalized value
   */
  public static String key(String table, String value) {
    UUID namespace = uuid5(URL_NAMESPACE, NAMESPACE_PREFIX + table);
    return uuid5(namespace, normalize(value)).toString();
  }

  static UUID uuid5(UUID namespace, String name) {
    MessageDigest sha1;
    try {
      sha1 = MessageDigest.getInstance("SHA-1");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-1 not available", e);
    }
    ByteBuffer ns = ByteBuffer.allocate(16);
    ns.putLong(namespace.getMostSignificantBits());
    ns.putLong(namespace.getLeastSignificantBits());
    sha1.update(ns.array());
    byte[] hash = sha1.digest(name.getBytes(StandardCharsets.UTF_8));
    hash[6] = (byte) ((hash[6] & 0x0f) | 0x50);
    hash[8] = (byte) ((hash[8] & 0x3f) | 0x80);
    ByteBuffer bb = ByteBuffer.wrap(hash, 0, 16);
    return new UUID(bb.getLong(), bb.getLong());
  }

  /**
   * A row merged into the first one with the same key, though some values differ.
   *
   * @param key the merge key value
   * @param field the field whose values differ
   * @param first the value of the first row
   * @param firstLine the source line of the first row
   * @param other the value of the other row
   * @param otherLine the source line of the other row
   */
  public record Conflict(String key, String field, String first, long firstLine, String other, long otherLine) {}

  /**
   * @return the fields whose values differ between two rows with the same key
   */
  public static List<Conflict> compare(List<String> columns, String key, String[] first, long firstLine,
                                       String[] other, long otherLine) {
    List<Conflict> conflicts = new ArrayList<>();
    for (int i = 0; i < first.length; i++) {
      if (!Objects.equals(StringUtils.defaultString(first[i]), StringUtils.defaultString(other[i]))) {
        conflicts.add(new Conflict(key, columns.get(i), first[i], firstLine, other[i], otherLine));
      }
    }
    return conflicts;
  }
}
