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
import org.gbif.ipt.model.DataPackageField;
import org.gbif.ipt.model.DataPackageFieldMapping;
import org.gbif.ipt.model.DataPackageMapping;
import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.model.DataPackageSchemaUpdateImpact.ResourceImpact;
import org.gbif.ipt.model.DataPackageTableSchema;
import org.gbif.ipt.model.DataPackageTableSchemaName;
import org.gbif.ipt.model.DataPackageTableSchemaRequirement;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapMetadata;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.DataPackageSchemaManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
   * A new version with another identifier becomes the identifier of the resources using the schema, so their mappings
   * are still used.
   */
  @Test
  public void testUpdateWithChangedIdentifierChangesResourceIdentifier() throws Exception {
    DataPackageSchema updated = DataPackageSchema.builder().identifier("http://example.org/dp/2.0").name("dp")
        .version("2.0").build();
    when(schemaManager.update(IDENTIFIER)).thenReturn(updated);
    // the installed schema is found by its previous identifier
    when(schemaManager.get(IDENTIFIER)).thenReturn(updated);
    Resource usingSchema = resource(IDENTIFIER, true);
    when(resourceManager.list()).thenReturn(List.of(usingSchema));

    action.update();

    assertEquals("http://example.org/dp/2.0", usingSchema.getDataPackageIdentifier());
    assertEquals(1, usingSchema.getDataPackageMappings().size());
    assertSame(updated, usingSchema.getDataPackageMappings().get(0).getDataPackageSchema());
  }

  /**
   * Versions of a schema are recognised by name, as the identifier may change between versions.
   */
  @Test
  public void testGetLatestVersionsByName() {
    DataPackageSchema older = DataPackageSchema.builder().identifier(IDENTIFIER).name("dp").version("1.0")
        .issued(new Date(1000)).build();
    DataPackageSchema newer = DataPackageSchema.builder().identifier("http://example.org/dp/2.0").name("dp")
        .version("2.0").issued(new Date(2000)).build();

    assertEquals(List.of(newer), action.getLatestVersions(List.of(older, newer)));
  }

  /**
   * Mappings the update doesn't affect are marked as made against the new version, the others keep the version they
   * were made against, to be reviewed. The resource is saved.
   */
  @Test
  public void testUpdateMarksUnaffectedMappingsAsUpToDate() throws Exception {
    DataPackageSchema installed = DataPackageSchema.builder().identifier(IDENTIFIER).name("dp").version("1.0")
        .tableSchemas(new LinkedHashSet<>(List.of(
            table("event", "eventID", "locality"), table("occurrence", "occurrenceID")))).build();
    DataPackageSchema updated = DataPackageSchema.builder().identifier(IDENTIFIER).name("dp").version("2.0")
        .tableSchemas(new LinkedHashSet<>(List.of(
            table("event", "eventID"), table("occurrence", "occurrenceID")))).build();
    when(schemaManager.get(IDENTIFIER)).thenReturn(installed);
    when(schemaManager.update(IDENTIFIER)).thenReturn(updated);

    Resource resource = new Resource();
    resource.setShortname("res");
    resource.setDataPackageIdentifier(IDENTIFIER);
    DataPackageMapping unaffected = mapping(installed, "1.0", "occurrence", "occurrenceID");
    // mapped field removed by the update
    DataPackageMapping affected = mapping(installed, "1.0", "event", "eventID", "locality");
    // made against an older version than the replaced one: still to be reviewed
    DataPackageMapping older = mapping(installed, "0.9", "occurrence", "occurrenceID");
    // made against an unknown version
    DataPackageMapping withoutVersion = mapping(installed, null, "occurrence", "occurrenceID");
    List.of(unaffected, affected, older, withoutVersion).forEach(resource::addDataPackageMapping);
    when(resourceManager.list()).thenReturn(List.of(resource));

    action.update();

    assertEquals("2.0", unaffected.getVersion());
    assertEquals("1.0", affected.getVersion());
    assertEquals("0.9", older.getVersion());
    assertNull(withoutVersion.getVersion());
    List.of(unaffected, affected, older, withoutVersion).forEach(m -> assertSame(updated, m.getDataPackageSchema()));
    verify(resourceManager).save(resource);
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
   * The new profile of a Camtrap DP resource using the schema is saved with its metadata, before the resource, as
   * saving the metadata sets its modified date.
   */
  @Test
  public void testUpdateSavesNewCamtrapProfile() throws Exception {
    when(schemaManager.update(IDENTIFIER)).thenReturn(DataPackageSchema.builder().identifier(IDENTIFIER).name("dp")
        .version("2.0").profile("http://example.org/profile-2").build());
    Resource camtrap = camtrapResource("http://example.org/profile-1");
    when(resourceManager.list()).thenReturn(List.of(camtrap));

    action.update();

    assertEquals("http://example.org/profile-2", ((CamtrapMetadata) camtrap.getDataPackageMetadata()).getProfile());
    InOrder inOrder = inOrder(resourceManager);
    inOrder.verify(resourceManager).saveDatapackageMetadata(camtrap);
    inOrder.verify(resourceManager).save(camtrap);
  }

  /**
   * The metadata isn't saved if the profile doesn't change, so the resource isn't marked as modified.
   */
  @Test
  public void testUpdateWithSameCamtrapProfileDoesNotSaveMetadata() throws Exception {
    when(schemaManager.update(IDENTIFIER)).thenReturn(DataPackageSchema.builder().identifier(IDENTIFIER).name("dp")
        .version("2.0").profile("http://example.org/profile-1").build());
    Resource camtrap = camtrapResource("http://example.org/profile-1");
    when(resourceManager.list()).thenReturn(List.of(camtrap));

    action.update();

    verify(resourceManager, never()).saveDatapackageMetadata(any());
    verify(resourceManager).save(camtrap);
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

  private static Resource camtrapResource(String profile) {
    Resource resource = resource(IDENTIFIER, true);
    resource.setCoreType(Constants.CAMTRAP_DP);
    CamtrapMetadata metadata = new CamtrapMetadata();
    metadata.setProfile(profile);
    resource.setDataPackageMetadata(metadata);
    return resource;
  }

  /**
   * The list page shows the resources using each installed schema, whichever form of its identifier they have.
   */
  @Test
  public void testListUsedBy() {
    DataPackageSchema schema = schemaManager.get(IDENTIFIER);
    when(schemaManager.list()).thenReturn(new ArrayList<>(List.of(schema)));
    Resource https = resource("https://example.org/dp", false);
    https.setShortname("https");
    Resource http = resource(IDENTIFIER, true);
    Resource other = resource("http://example.org/other", true);
    other.setShortname("other");
    when(resourceManager.list()).thenReturn(List.of(other, https, http));
    action.setLatestDataSchemasVersions(new ArrayList<>());

    action.list();

    assertEquals(List.of("https", "res-true"),
        action.getUsedBy().get(IDENTIFIER).stream().map(Resource::getShortname).toList());
  }

  /**
   * When the Registry can't be reached, the page says so, and it isn't asked again for the versions in the same request.
   */
  @Test
  public void testListWithRegistryDown() {
    RegistryManager registryManager = mock(RegistryManager.class);
    when(registryManager.getLatestDataPackageSchemas())
        .thenThrow(new RegistryException("http://registry.example.org", new RuntimeException("down")));
    DataPackageSchemaAction offline = new DataPackageSchemaAction(mock(SimpleTextProvider.class),
        mock(AppConfig.class), mock(RegistrationManager.class), schemaManager, registryManager, new ConfigWarnings(),
        resourceManager);
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getMethod()).thenReturn("GET");
    offline.setServletRequest(req);
    DataPackageSchema schema = schemaManager.get(IDENTIFIER);
    when(schemaManager.list()).thenReturn(new ArrayList<>(List.of(schema)));
    when(resourceManager.list()).thenReturn(List.of());

    offline.prepare();
    offline.list();

    assertTrue(offline.isRegistryUnreachable());
    assertTrue(offline.getNewSchemas().isEmpty());
    assertNull(offline.latestOf(schema));
    verify(registryManager, times(1)).getLatestDataPackageSchemas();
  }

  /**
   * The schema page lists the resources using the schema, with the tables and fields each maps, in schema order and
   * by title; resources using another schema and unmapped fields are left out.
   */
  @Test
  public void testSchemaPageUsage() throws Exception {
    DataPackageSchema schema = DataPackageSchema.builder().identifier(IDENTIFIER).name("dp").version("1.0")
        .tableSchemas(new LinkedHashSet<>(List.of(
            table("event", "eventID", "locality"), table("occurrence", "occurrenceID")))).build();
    when(schemaManager.get(IDENTIFIER)).thenReturn(schema);

    Resource b = new Resource();
    b.setShortname("b");
    b.setTitle("Beta");
    b.setDataPackageIdentifier(IDENTIFIER);
    // mapped in reverse table order, and a field without a column
    b.addDataPackageMapping(mapping(schema, "1.0", "occurrence", "occurrenceID"));
    DataPackageMapping event = mapping(schema, "1.0", "event", "eventID");
    event.getFields().add(new DataPackageFieldMapping(null, field("locality")));
    b.addDataPackageMapping(event);
    Resource a = new Resource();
    a.setShortname("a");
    a.setTitle("Alpha");
    a.setDataPackageIdentifier(IDENTIFIER);
    a.addDataPackageMapping(mapping(schema, "1.0", "event", "eventID", "locality"));
    Resource other = new Resource();
    other.setShortname("other");
    other.setDataPackageIdentifier("http://example.org/other");
    when(resourceManager.list()).thenReturn(List.of(b, other, a));

    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getParameter("id")).thenReturn(IDENTIFIER);
    when(req.getMethod()).thenReturn("GET");
    action.setServletRequest(req);
    action.prepare();
    action.execute();

    assertFalse(action.isRegistryUnreachable());
    assertEquals(List.of(a, b), action.getSchemaUsage().stream().map(DataPackageSchemaAction.SchemaUsage::getResource).toList());
    DataPackageSchemaAction.SchemaUsage usageB = action.getSchemaUsage().get(1);
    assertEquals(List.of("event", "occurrence"), usageB.getTables().stream().map(DataPackageTableSchema::getName).toList());
    assertEquals(2, usageB.getFieldsMapped());
    assertEquals(List.of(a, b), action.getTableUsedBy().get("event"));
    assertEquals(List.of(a, b), action.getFieldUsedBy().get("event/eventID"));
    assertEquals(List.of(a), action.getFieldUsedBy().get("event/locality"));
    assertEquals(List.of(b), action.getFieldUsedBy().get("occurrence/occurrenceID"));
  }

  /**
   * Table rules are the descriptions in the requirements, the top level first, then the nested ones.
   */
  @Test
  public void testTableRules() {
    assertTrue(DataPackageSchemaAction.tableRulesOf(null).isEmpty());
    assertTrue(DataPackageSchemaAction.tableRulesOf(new DataPackageTableSchemaRequirement()).isEmpty());

    DataPackageTableSchemaRequirement top = new DataPackageTableSchemaRequirement();
    top.setDescription(" Top ");
    DataPackageTableSchemaRequirement first = new DataPackageTableSchemaRequirement();
    first.setDescription("First");
    DataPackageTableSchemaRequirement nested = new DataPackageTableSchemaRequirement();
    nested.setDescription("Nested");
    first.setAnyOf(List.of(nested));
    DataPackageTableSchemaRequirement blank = new DataPackageTableSchemaRequirement();
    blank.setDescription(" ");
    top.setAllOf(List.of(first, blank));
    assertEquals(List.of("Top", "First", "Nested"), DataPackageSchemaAction.tableRulesOf(top));
  }

  private static DataPackageTableSchema table(String name, String... fieldNames) {
    DataPackageTableSchema table = new DataPackageTableSchema();
    table.setName(name);
    table.setFields(Arrays.stream(fieldNames).map(DataPackageSchemaActionTest::field).toList());
    return table;
  }

  private static DataPackageField field(String name) {
    DataPackageField field = new DataPackageField();
    field.setName(name);
    return field;
  }

  /**
   * Mapping of a table, made against the given version, with the given fields mapped to columns.
   */
  private static DataPackageMapping mapping(DataPackageSchema schema, String version, String tableName,
                                            String... mappedFields) {
    DataPackageMapping mapping = new DataPackageMapping();
    mapping.setDataPackageSchema(schema);
    mapping.setVersion(version);
    mapping.setDataPackageTableSchemaName(new DataPackageTableSchemaName(tableName));
    List<DataPackageFieldMapping> fields = new ArrayList<>();
    for (int i = 0; i < mappedFields.length; i++) {
      fields.add(new DataPackageFieldMapping(i, field(mappedFields[i])));
    }
    mapping.setFields(fields);
    return mapping;
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
