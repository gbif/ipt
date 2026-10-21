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

import org.gbif.ipt.model.voc.PublicationMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.text.similarity.LevenshteinDistance;

import lombok.Getter;

/**
 * What updating a data schema to a newer version changes, and how it affects the resources using it. Computed before
 * the update, nothing is changed.
 */
@Getter
public class DataPackageSchemaUpdateImpact {

  /**
   * How an update affects a resource.
   */
  public enum Severity {
    // the resource can't be published until its mappings are fixed
    BLOCKS_PUBLICATION,
    // mapped fields are left out until reviewed
    NEEDS_REVIEW,
    NONE
  }

  private final DataPackageSchema installed;
  private final DataPackageSchema update;
  private final List<String> addedTables;
  private final List<String> removedTables;
  // changes of the tables in both versions, only the ones with changes
  private final List<TableChange> tableChanges;
  // resources using the data schema, sorted by severity: the ones that can't be published first
  private final List<ResourceImpact> resources;

  private DataPackageSchemaUpdateImpact(DataPackageSchema installed, DataPackageSchema update,
                                        List<String> addedTables, List<String> removedTables,
                                        List<TableChange> tableChanges, List<ResourceImpact> resources) {
    this.installed = installed;
    this.update = update;
    this.addedTables = addedTables;
    this.removedTables = removedTables;
    this.tableChanges = tableChanges;
    this.resources = resources;
  }

  /**
   * @param installed installed data schema
   * @param update newer version of the data schema, with its table schemas
   * @param resources resources using the data schema
   * @return impact of the update
   */
  public static DataPackageSchemaUpdateImpact of(DataPackageSchema installed, DataPackageSchema update,
                                                 Collection<Resource> resources) {
    Map<String, DataPackageTableSchema> installedTables = tablesByName(installed);
    Map<String, DataPackageTableSchema> updateTables = tablesByName(update);

    List<String> addedTables = updateTables.keySet().stream().filter(t -> !installedTables.containsKey(t)).toList();
    List<String> removedTables = installedTables.keySet().stream().filter(t -> !updateTables.containsKey(t)).toList();

    List<TableChange> tableChanges = new ArrayList<>();
    for (Map.Entry<String, DataPackageTableSchema> entry : updateTables.entrySet()) {
      DataPackageTableSchema installedTable = installedTables.get(entry.getKey());
      if (installedTable != null) {
        TableChange change = TableChange.of(installedTable, entry.getValue());
        if (change.hasChanges()) {
          tableChanges.add(change);
        }
      }
    }

    List<ResourceImpact> resourceImpacts = resources.stream()
        .map(resource -> ResourceImpact.of(resource, installed, update, installedTables, updateTables))
        .sorted(Comparator.comparing(ResourceImpact::getSeverity))
        .toList();

    return new DataPackageSchemaUpdateImpact(installed, update, addedTables, removedTables, tableChanges,
        resourceImpacts);
  }

  public int getAddedFieldsCount() {
    return tableChanges.stream().mapToInt(c -> c.getAddedFields().size()).sum();
  }

  public int getRemovedFieldsCount() {
    return tableChanges.stream().mapToInt(c -> c.getRemovedFields().size()).sum();
  }

  public int getNewlyRequiredFieldsCount() {
    return tableChanges.stream().mapToInt(c -> c.getNewlyRequiredFields().size()).sum();
  }

  public int getPossiblyRenamedFieldsCount() {
    return tableChanges.stream().mapToInt(c -> c.getPossiblyRenamedFields().size()).sum();
  }

  /**
   * @return true if anything changes in the tables or fields
   */
  public boolean isSchemaChanged() {
    return !addedTables.isEmpty() || !removedTables.isEmpty() || !tableChanges.isEmpty();
  }

  /**
   * @return the worst effect on a resource, NONE if no resource is affected
   */
  public Severity getSeverity() {
    return resources.stream().map(ResourceImpact::getSeverity).min(Enum::compareTo).orElse(Severity.NONE);
  }

  public long getBlockedResourcesCount() {
    return resources.stream().filter(r -> r.getSeverity() == Severity.BLOCKS_PUBLICATION).count();
  }

  public long getResourcesNeedingReviewCount() {
    return resources.stream().filter(r -> r.getSeverity() == Severity.NEEDS_REVIEW).count();
  }

  public List<ResourceImpact> getAffectedResources() {
    return resources.stream().filter(r -> r.getSeverity() != Severity.NONE).toList();
  }

  public List<ResourceImpact> getUnaffectedResources() {
    return resources.stream().filter(r -> r.getSeverity() == Severity.NONE).toList();
  }

  /**
   * @param mapping mapping made against the installed data schema
   * @param installed installed data schema
   * @param update newer version of the data schema, with its table schemas
   * @return true if the update affects the mapping, i.e. it's listed in the impact of the update and needs review
   */
  public static boolean affects(DataPackageMapping mapping, DataPackageSchema installed, DataPackageSchema update) {
    return MappingImpact.of(mapping, tablesByName(installed), tablesByName(update)).hasImpact();
  }

  private static Map<String, DataPackageTableSchema> tablesByName(DataPackageSchema schema) {
    if (schema.getTableSchemas() == null) {
      return Map.of();
    }
    return schema.getTableSchemas().stream()
        .filter(t -> t.getName() != null)
        .collect(Collectors.toMap(DataPackageTableSchema::getName, Function.identity(), (a, b) -> a,
            LinkedHashMap::new));
  }

  private static Map<String, DataPackageField> fieldsByName(DataPackageTableSchema table) {
    if (table == null || table.getFields() == null) {
      return Map.of();
    }
    return table.getFields().stream()
        .filter(f -> f.getName() != null)
        .collect(Collectors.toMap(DataPackageField::getName, Function.identity(), (a, b) -> a, LinkedHashMap::new));
  }

  /**
   * Changes of a table in both versions.
   */
  @Getter
  public static class TableChange {

    private static final LevenshteinDistance DISTANCE = new LevenshteinDistance(2);

    private final String name;
    private final List<String> addedFields;
    private final List<String> removedFields;
    // required in the new version, but not in the installed one (or not in it at all)
    private final List<String> newlyRequiredFields;
    // removed field -> added field with a similar name, a guess
    private final Map<String, String> possiblyRenamedFields;

    private TableChange(String name, List<String> addedFields, List<String> removedFields,
                        List<String> newlyRequiredFields, Map<String, String> possiblyRenamedFields) {
      this.name = name;
      this.addedFields = addedFields;
      this.removedFields = removedFields;
      this.newlyRequiredFields = newlyRequiredFields;
      this.possiblyRenamedFields = possiblyRenamedFields;
    }

    static TableChange of(DataPackageTableSchema installed, DataPackageTableSchema update) {
      Map<String, DataPackageField> installedFields = fieldsByName(installed);
      Map<String, DataPackageField> updateFields = fieldsByName(update);

      List<String> added = updateFields.keySet().stream().filter(f -> !installedFields.containsKey(f)).toList();
      List<String> removed = installedFields.keySet().stream().filter(f -> !updateFields.containsKey(f)).toList();
      List<String> newlyRequired = updateFields.values().stream()
          .filter(DataPackageField::isRequired)
          .map(DataPackageField::getName)
          .filter(f -> installedFields.get(f) == null || !installedFields.get(f).isRequired())
          .toList();

      // each removed field paired with the closest similar added field not paired yet
      Map<String, String> possiblyRenamed = new LinkedHashMap<>();
      for (String removedField : removed) {
        added.stream()
            .filter(addedField -> !possiblyRenamed.containsValue(addedField))
            .filter(addedField -> isSimilar(removedField, addedField))
            .min(Comparator.comparingInt(addedField -> distance(removedField, addedField)))
            .ifPresent(addedField -> possiblyRenamed.put(removedField, addedField));
      }

      return new TableChange(update.getName(), added, removed, newlyRequired, possiblyRenamed);
    }

    /**
     * @return true if the names only differ by case, separators or a couple of characters
     */
    static boolean isSimilar(String a, String b) {
      String normalizedA = normalize(a);
      String normalizedB = normalize(b);
      if (normalizedA.equals(normalizedB)) {
        return true;
      }
      // a couple of edits in a short name would make most names similar
      return Math.min(normalizedA.length(), normalizedB.length()) >= 6 && distance(a, b) != -1;
    }

    /**
     * @return number of edits between the names ignoring case and separators, -1 if more than a couple
     */
    private static int distance(String a, String b) {
      return DISTANCE.apply(normalize(a), normalize(b));
    }

    private static String normalize(String name) {
      return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    boolean hasChanges() {
      return !addedFields.isEmpty() || !removedFields.isEmpty() || !newlyRequiredFields.isEmpty();
    }
  }

  /**
   * How an update affects a resource.
   */
  @Getter
  public static class ResourceImpact {

    private final Resource resource;
    private final List<MappingImpact> mappings;
    // reason the mapped tables no longer meet the table requirements, null if they do or already didn't
    private final String tableRequirementsProblem;

    private ResourceImpact(Resource resource, List<MappingImpact> mappings, String tableRequirementsProblem) {
      this.resource = resource;
      this.mappings = mappings;
      this.tableRequirementsProblem = tableRequirementsProblem;
    }

    static ResourceImpact of(Resource resource, DataPackageSchema installed, DataPackageSchema update,
                             Map<String, DataPackageTableSchema> installedTables,
                             Map<String, DataPackageTableSchema> updateTables) {
      List<MappingImpact> mappingImpacts = resource.getDataPackageMappings().stream()
          .map(mapping -> MappingImpact.of(mapping, installedTables, updateTables))
          .filter(MappingImpact::hasImpact)
          .toList();

      Set<String> mappedTables = resource.getDataPackageMappings().stream()
          .map(DataPackageMapping::getDataPackageTableSchemaName)
          .filter(Objects::nonNull)
          .map(DataPackageTableSchemaName::getName)
          .collect(Collectors.toSet());
      String tableRequirementsProblem = null;
      if (!mappedTables.isEmpty() && validate(installed, mappedTables).isValid()) {
        DataPackageTableSchemaRequirement.ValidationResult result = validate(update, mappedTables);
        if (!result.isValid()) {
          tableRequirementsProblem = result.getReason();
        }
      }

      return new ResourceImpact(resource, mappingImpacts, tableRequirementsProblem);
    }

    private static DataPackageTableSchemaRequirement.ValidationResult validate(DataPackageSchema schema,
                                                                               Set<String> mappedTables) {
      DataPackageTableSchemaRequirement requirements = schema.getTableSchemasRequirements();
      return requirements == null
          ? new DataPackageTableSchemaRequirement.ValidationResult()
          : requirements.validate(mappedTables);
    }

    public int getPossiblyRenamedFieldsCount() {
      return mappings.stream().mapToInt(m -> m.getPossiblyRenamedFields().size()).sum();
    }

    public int getMissingRequiredFieldsCount() {
      return mappings.stream().mapToInt(m -> m.getMissingRequiredFields().size()).sum();
    }

    public int getRemovedFieldsCount() {
      return mappings.stream().mapToInt(m -> m.getRemovedFields().size()).sum();
    }

    public long getRemovedTablesCount() {
      return mappings.stream().filter(MappingImpact::isTableRemoved).count();
    }

    public Severity getSeverity() {
      if (tableRequirementsProblem != null || mappings.stream().anyMatch(MappingImpact::isBlocksPublication)) {
        return Severity.BLOCKS_PUBLICATION;
      }
      return mappings.isEmpty() ? Severity.NONE : Severity.NEEDS_REVIEW;
    }

    /**
     * @return true if the resource is published automatically, so its next publication would fail unnoticed
     */
    public boolean isAutoPublishing() {
      return resource.getPublicationMode() == PublicationMode.AUTO_PUBLISH_ON;
    }
  }

  /**
   * How an update affects a mapping, only what the update causes: problems the mapping already has are left out.
   */
  @Getter
  public static class MappingImpact {

    private final String label;
    private final boolean tableRemoved;
    // mapped fields of the installed table not in the new one, other than the possibly renamed ones
    private final List<String> removedFields;
    // required fields of the new table not mapped, that weren't required and unmapped already, other than the ones
    // possibly renamed from a mapped field
    private final List<String> missingRequiredFields;
    // mapped field of the installed table not in the new one -> unmapped field of the new table with a similar name
    private final Map<String, String> possiblyRenamedFields;
    // fields of possiblyRenamedFields that are required, and so block publication until mapped
    private final Set<String> requiredRenamedFields;

    private MappingImpact(String label, boolean tableRemoved, List<String> removedFields,
                          List<String> missingRequiredFields, Map<String, String> possiblyRenamedFields,
                          Set<String> requiredRenamedFields) {
      this.label = label;
      this.tableRemoved = tableRemoved;
      this.removedFields = removedFields;
      this.missingRequiredFields = missingRequiredFields;
      this.possiblyRenamedFields = possiblyRenamedFields;
      this.requiredRenamedFields = requiredRenamedFields;
    }

    static MappingImpact of(DataPackageMapping mapping, Map<String, DataPackageTableSchema> installedTables,
                            Map<String, DataPackageTableSchema> updateTables) {
      String tableName = mapping.getDataPackageTableSchemaName() == null
          ? null
          : mapping.getDataPackageTableSchemaName().getName();
      DataPackageTableSchema installedTable = tableName == null ? null : installedTables.get(tableName);
      DataPackageTableSchema updateTable = tableName == null ? null : updateTables.get(tableName);

      // a table already missing isn't caused by the update
      if (installedTable == null) {
        return new MappingImpact(mapping.getLabel(), false, List.of(), List.of(), Map.of(), Set.of());
      }
      if (updateTable == null) {
        return new MappingImpact(mapping.getLabel(), true, List.of(), List.of(), Map.of(), Set.of());
      }

      Map<String, DataPackageField> installedFields = fieldsByName(installedTable);
      Map<String, DataPackageField> updateFields = fieldsByName(updateTable);
      Set<String> mappedFields = mapping.getFields() == null
          ? Set.of()
          : mapping.getFields().stream()
              .filter(fm -> fm.getField() != null && fm.isMapped())
              .map(fm -> fm.getField().getName())
              .collect(Collectors.toSet());

      List<String> removedFields = installedFields.keySet().stream()
          .filter(mappedFields::contains)
          .filter(f -> !updateFields.containsKey(f))
          .toList();
      List<String> missingRequiredFields = updateFields.values().stream()
          .filter(DataPackageField::isRequired)
          .map(DataPackageField::getName)
          .filter(f -> !mappedFields.contains(f))
          .filter(f -> installedFields.get(f) == null || !installedFields.get(f).isRequired())
          .toList();

      // a removed mapped field and the unmapped field it was possibly renamed to are one change, not two
      Map<String, String> possiblyRenamedFields = new LinkedHashMap<>();
      TableChange.of(installedTable, updateTable).getPossiblyRenamedFields().forEach((removed, added) -> {
        if (removedFields.contains(removed) && !mappedFields.contains(added)) {
          possiblyRenamedFields.put(removed, added);
        }
      });
      Set<String> requiredRenamedFields = possiblyRenamedFields.values().stream()
          .filter(f -> updateFields.get(f).isRequired())
          .collect(Collectors.toSet());

      return new MappingImpact(mapping.getLabel(), false,
          removedFields.stream().filter(f -> !possiblyRenamedFields.containsKey(f)).toList(),
          missingRequiredFields.stream().filter(f -> !possiblyRenamedFields.containsValue(f)).toList(),
          possiblyRenamedFields, requiredRenamedFields);
    }

    public boolean isBlocksPublication() {
      return tableRemoved || !missingRequiredFields.isEmpty() || !requiredRenamedFields.isEmpty();
    }

    boolean hasImpact() {
      return tableRemoved || !removedFields.isEmpty() || !missingRequiredFields.isEmpty()
          || !possiblyRenamedFields.isEmpty();
    }
  }
}
