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
import org.gbif.ipt.model.DataPackageTableSchemaRequirement;
import org.gbif.ipt.service.admin.DataPackageSchemaManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("HttpUrlsUsage")
public class DataPackageActionTest {

  private static final String IDENTIFIER = "http://rs.gbif.org/data-packages/coldp";

  private DataPackageSchema schema;
  private DataPackageAction action;

  @BeforeEach
  public void setup() {
    DataPackageTableSchemaRequirement rule = new DataPackageTableSchemaRequirement();
    rule.setDescription("At least one required schema present: name, taxon");
    schema = DataPackageSchema.builder().identifier(IDENTIFIER).name("coldp").version("1.1")
        .tableSchemasRequirements(rule).build();

    DataPackageSchemaManager schemaManager = mock(DataPackageSchemaManager.class);
    when(schemaManager.get(IDENTIFIER)).thenReturn(schema);
    when(schemaManager.getRawData("coldp")).thenReturn("{\"name\": \"coldp\"}");

    action = new DataPackageAction(mock(SimpleTextProvider.class), mock(AppConfig.class),
        mock(RegistrationManager.class), schemaManager);
  }

  /**
   * An installed schema is shown with its source and table rules.
   */
  @Test
  public void testInstalledSchema() throws Exception {
    action.id = IDENTIFIER;

    assertEquals(DataPackageAction.SUCCESS, action.execute());
    assertSame(schema, action.getDataPackageSchema());
    assertEquals("{\"name\": \"coldp\"}", action.getDataPackageSchemaRawData());
    assertEquals(List.of("At least one required schema present: name, taxon"), action.getTableRules());
  }

  /**
   * A schema that isn't installed, or no id, is not found.
   */
  @Test
  public void testNotFound() throws Exception {
    action.id = "http://example.org/unknown";
    assertEquals(DataPackageAction.NOT_FOUND, action.execute());
    assertNull(action.getDataPackageSchema());

    DataPackageAction noId = new DataPackageAction(mock(SimpleTextProvider.class), mock(AppConfig.class),
        mock(RegistrationManager.class), mock(DataPackageSchemaManager.class));
    assertEquals(DataPackageAction.NOT_FOUND, noId.execute());
    assertTrue(noId.getTableRules().isEmpty());
  }
}
