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
package org.gbif.ipt.action.admin;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.model.DataPackageMapping;
import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.admin.DataPackageSchemaManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataPackageSchemaActionTest {

  private static final String IDENTIFIER = "http://example.org/dp";

  private DataPackageSchemaManager schemaManager;
  private ResourceManager resourceManager;
  private DataPackageSchemaAction action;

  @BeforeEach
  public void setUp() {
    DataPackageSchema schema = DataPackageSchema.builder().identifier(IDENTIFIER).name("dp").build();
    schemaManager = mock(DataPackageSchemaManager.class);
    when(schemaManager.get(IDENTIFIER)).thenReturn(schema);
    when(schemaManager.get("https://example.org/dp")).thenReturn(schema);

    resourceManager = mock(ResourceManager.class);
    action = new DataPackageSchemaAction(mock(SimpleTextProvider.class), mock(AppConfig.class),
        mock(RegistrationManager.class), schemaManager, mock(RegistryManager.class), new ConfigWarnings(),
        resourceManager);
    action.id = IDENTIFIER;
  }

  /**
   * A schema mapped in a resource isn't uninstalled, whichever form of its identifier the resource has.
   */
  @Test
  public void testDeleteMappedSchema() throws Exception {
    when(resourceManager.list()).thenReturn(List.of(resource("https://example.org/dp", true)));

    action.delete();

    verify(schemaManager, never()).uninstallSafely(any());
  }

  @Test
  public void testDeleteUnmappedSchema() throws Exception {
    when(resourceManager.list()).thenReturn(List.of(resource(IDENTIFIER, false), resource("http://example.org/other", true)));

    action.delete();

    verify(schemaManager).uninstallSafely(IDENTIFIER);
  }

  @Test
  public void testDeleteUnknownSchema() throws Exception {
    action.id = "http://example.org/unknown";

    action.delete();

    verify(schemaManager, never()).uninstallSafely(any());
  }

  private static Resource resource(String dataPackageIdentifier, boolean mapped) {
    Resource resource = new Resource();
    resource.setShortname("res-" + mapped);
    resource.setDataPackageIdentifier(dataPackageIdentifier);
    if (mapped) {
      DataPackageMapping mapping = new DataPackageMapping();
      mapping.setDataPackageSchema(DataPackageSchema.builder().identifier(dataPackageIdentifier).build());
      resource.addDataPackageMapping(mapping);
    }
    return resource;
  }
}
