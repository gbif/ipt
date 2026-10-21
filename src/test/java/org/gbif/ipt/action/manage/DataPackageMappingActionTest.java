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

import org.gbif.ipt.model.DataPackageField;
import org.gbif.ipt.model.DataPackageFieldMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DataPackageMappingActionTest {

  /**
   * Field names with underscores, hyphens or digits are automapped from columns with the same name.
   */
  @Test
  public void testAutomapFieldNamesWithSpecialCharacters() {
    List<DataPackageFieldMapping> fields = fieldMappings(
        "assembly_name", "_16s_recover", "sample-rate", "ratioOfAbsorbance260_230", "ratioOfAbsorbance260_280",
        "molecularProtocol_pk");
    List<String> columns = List.of(
        "molecularProtocol_pk", "ratioOfAbsorbance260_280", "ratioOfAbsorbance260_230", "sample-rate", "_16s_recover",
        "assembly_name");

    assertEquals(6, DataPackageMappingAction.automap(fields, columns));
    assertEquals(List.of(5, 4, 3, 2, 1, 0), indices(fields));
  }

  @Test
  public void testAutomapNormalizedNames() {
    List<DataPackageFieldMapping> fields = fieldMappings("eventID", "assembly_name", "event_pk", "locality");
    List<String> columns = List.of("dwc:eventID", "Assembly Name", "EVENT-PK", "unrelated");

    assertEquals(3, DataPackageMappingAction.automap(fields, columns));
    assertEquals(0, fields.get(0).getIndex());
    assertEquals(1, fields.get(1).getIndex());
    assertEquals(2, fields.get(2).getIndex());
    assertNull(fields.get(3).getIndex());
  }

  /**
   * A column with the field's exact name wins over one that only matches when normalized.
   */
  @Test
  public void testAutomapPrefersExactName() {
    List<DataPackageFieldMapping> fields = fieldMappings("_id");
    List<String> columns = List.of("id", "_id");

    DataPackageMappingAction.automap(fields, columns);

    assertEquals(1, fields.get(0).getIndex());
  }

  /**
   * A field mapping of a field not in the installed schema is kept, discarded, or moved to an unmapped field.
   */
  @Test
  public void testApplyUnresolvedFieldAction() {
    List<DataPackageFieldMapping> fields = fieldMappings("eventDate", "locality");
    DataPackageFieldMapping unresolved = fieldMappings("eventDateOld").get(0);
    unresolved.setIndex(3);
    unresolved.setDefaultValue("2020");
    unresolved.setTranslation(Map.of("a", "b"));

    assertEquals(DataPackageMappingAction.UnresolvedFieldResult.KEPT,
        DataPackageMappingAction.applyUnresolvedFieldAction(unresolved, null, fields));
    assertEquals(DataPackageMappingAction.UnresolvedFieldResult.KEPT,
        DataPackageMappingAction.applyUnresolvedFieldAction(unresolved, "field:unknown", fields));
    assertEquals(DataPackageMappingAction.UnresolvedFieldResult.DISCARDED,
        DataPackageMappingAction.applyUnresolvedFieldAction(unresolved, "discard", fields));

    assertEquals(DataPackageMappingAction.UnresolvedFieldResult.MOVED,
        DataPackageMappingAction.applyUnresolvedFieldAction(unresolved, "field:eventDate", fields));
    DataPackageFieldMapping eventDate = fields.get(0);
    assertEquals(3, eventDate.getIndex());
    assertEquals("2020", eventDate.getDefaultValue());
    assertEquals(Map.of("a", "b"), eventDate.getTranslation());
  }

  /**
   * A field mapping isn't moved to a field mapped already, which is left as it is.
   */
  @Test
  public void testApplyUnresolvedFieldActionToMappedField() {
    List<DataPackageFieldMapping> fields = fieldMappings("eventDate");
    fields.get(0).setIndex(0);
    DataPackageFieldMapping unresolved = fieldMappings("eventDateOld").get(0);
    unresolved.setIndex(3);

    assertEquals(DataPackageMappingAction.UnresolvedFieldResult.TARGET_MAPPED,
        DataPackageMappingAction.applyUnresolvedFieldAction(unresolved, "field:eventDate", fields));
    assertEquals(0, fields.get(0).getIndex());
  }

  private static List<DataPackageFieldMapping> fieldMappings(String... names) {
    return Stream.of(names).map(name -> {
      DataPackageField field = new DataPackageField();
      field.setName(name);
      DataPackageFieldMapping mapping = new DataPackageFieldMapping();
      mapping.setField(field);
      return mapping;
    }).collect(Collectors.toList());
  }

  private static List<Integer> indices(List<DataPackageFieldMapping> fields) {
    return fields.stream().map(DataPackageFieldMapping::getIndex).collect(Collectors.toList());
  }
}
