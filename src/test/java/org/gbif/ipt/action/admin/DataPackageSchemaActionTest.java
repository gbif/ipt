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

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.DataPackageMapping;
import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.model.DataPackageSchemaUpdateImpact.ResourceImpact;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapMetadata;
import org.gbif.ipt.service.admin.DataPackageSchemaManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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

    verify(schemaManager, never()).uninstall(any());
  }

  @Test
  public void testDeleteUnmappedSchema() throws Exception {
    when(resourceManager.list()).thenReturn(List.of(resource(IDENTIFIER, false), resource("http://example.org/other", true)));

    action.delete();

    verify(schemaManager).uninstall(IDENTIFIER);
  }

  @Test
  public void testDeleteUnknownSchema() throws Exception {
    action.id = "http://example.org/unknown";

    action.delete();

    verify(schemaManager, never()).uninstall(any());
  }

  /**
   * Updating a schema changes the mappings of the resources using it, not of resources using another schema.
   */
  @Test
  public void testUpdateOnlyChangesResourcesUsingTheSchema() throws Exception {
    DataPackageSchema updated = DataPackageSchema.builder().identifier(IDENTIFIER).name("dp").version("2.0").build();
    when(schemaManager.update(IDENTIFIER)).thenReturn(updated);
    Resource usingSchema = resource(IDENTIFIER, true);
    Resource usingOtherSchema = resource("http://example.org/other", true);
    when(resourceManager.list()).thenReturn(List.of(usingSchema, usingOtherSchema));

    action.update();

    assertSame(updated, usingSchema.getDataPackageMappings().get(0).getDataPackageSchema());
    // mappings pointing to another schema than the resource's are hidden
    assertEquals(1, usingOtherSchema.getDataPackageMappings().size());
  }

  /**
   * The Camtrap DP profile of a resource isn't replaced by the profile of another schema being updated.
   */
  @Test
  public void testUpdateKeepsCamtrapProfileOfResourcesUsingAnotherSchema() throws Exception {
    when(schemaManager.update(IDENTIFIER)).thenReturn(
        DataPackageSchema.builder().identifier(IDENTIFIER).name("dp").version("2.0").profile("http://example.org/dp-profile").build());
    Resource camtrap = resource("http://example.org/camtrap-dp", true);
    camtrap.setCoreType(Constants.CAMTRAP_DP);
    CamtrapMetadata metadata = new CamtrapMetadata();
    metadata.setProfile("http://example.org/camtrap-profile");
    camtrap.setDataPackageMetadata(metadata);
    when(resourceManager.list()).thenReturn(List.of(camtrap));

    action.update();

    assertEquals("http://example.org/camtrap-profile", metadata.getProfile());
  }

  /**
   * The impact of an update covers the resources using the schema, whichever form of its identifier they have.
   */
  @Test
  public void testUpdateImpact() throws Exception {
    when(schemaManager.getUpdate(IDENTIFIER))
        .thenReturn(DataPackageSchema.builder().identifier(IDENTIFIER).name("dp").version("2.0").build());
    Resource usingSchema = resource("https://example.org/dp", true);
    when(resourceManager.list()).thenReturn(List.of(usingSchema, resource("http://example.org/other", true)));

    assertEquals(BaseAction.SUCCESS, action.updateImpact());

    assertEquals("2.0", action.getUpdateImpact().getUpdate().getVersion());
    assertEquals(List.of(usingSchema),
        action.getUpdateImpact().getResources().stream().map(ResourceImpact::getResource).toList());
    verify(schemaManager, never()).update(any());
  }

  @Test
  public void testUpdateImpactWithoutUpdate() throws Exception {
    assertEquals(BaseAction.INPUT, action.updateImpact());
    assertNull(action.getUpdateImpact());

    action.id = "http://example.org/unknown";
    assertEquals(BaseAction.NOT_FOUND, action.updateImpact());
  }

  /**
   * An update that can't be read is reported, and nothing is shown or updated.
   */
  @Test
  public void testUpdateImpactFailure() throws Exception {
    when(schemaManager.getUpdate(IDENTIFIER)).thenThrow(new IOException("unreachable"));

    assertEquals(BaseAction.INPUT, action.updateImpact());

    assertNull(action.getUpdateImpact());
    assertFalse(action.getActionWarnings().isEmpty());
    verify(schemaManager, never()).update(any());
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
