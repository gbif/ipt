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
package org.gbif.ipt.service.admin.impl;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.model.DataPackageTableSchema;
import org.gbif.ipt.model.factory.DataSchemaFactory;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.utils.HttpClient;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.http.HttpVersion;
import org.apache.http.message.BasicStatusLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DataPackageSchemaManagerImplTest {

  private static final String BASE_URL = "http://example.org/data-packages/";
  private static final String IDENTIFIER = "http://example.org/dp";
  private static final String OTHER_IDENTIFIER = "http://example.org/other-dp";

  @TempDir
  private File dataDirFolder;

  // "remote" files served by the fake downloader, by URL
  private final Map<String, String> remoteFiles = new HashMap<>();
  private DataDir dataDir;
  private HttpClient downloader;
  private RegistryManager registryManager;
  private ConfigWarnings warnings;
  private AppConfig cfg;
  private DataPackageSchemaManagerImpl manager;

  @BeforeEach
  public void setUp() throws Exception {
    dataDir = DataDir.buildFromString(dataDirFolder.getAbsolutePath());

    downloader = mock(HttpClient.class);
    when(downloader.download(any(URL.class), any(File.class))).thenAnswer(invocation -> {
      String content = remoteFiles.get(invocation.getArgument(0, URL.class).toString());
      if (content == null) {
        return new BasicStatusLine(HttpVersion.HTTP_1_1, 404, "Not Found");
      }
      Files.writeString(invocation.getArgument(1, File.class).toPath(), content);
      return new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK");
    });

    registryManager = mock(RegistryManager.class);
    cfg = mock(AppConfig.class);
    manager = newManager();
  }

  /**
   * A new manager on the same data dir, like after a restart.
   */
  private DataPackageSchemaManagerImpl newManager() {
    warnings = new ConfigWarnings();
    return new DataPackageSchemaManagerImpl(cfg, dataDir, warnings, new DataSchemaFactory(),
        mock(SimpleTextProvider.class), mock(RegistrationManager.class), registryManager, downloader);
  }

  @Test
  public void testInstall() {
    manager.install(schema(IDENTIFIER, "dp", "1.0", "2025-01-01", "event", "occurrence"));

    DataPackageSchema installed = manager.get(IDENTIFIER);
    assertNotNull(installed);
    assertEquals("1.0", installed.getVersion());
    assertEquals(Set.of("event", "occurrence"), tableSchemaNames(installed));
    assertTrue(manager.getTableSchemaFile(installed, "event").exists());
    assertTrue(manager.getTableSchemaFile(installed, "occurrence").exists());
    assertTrue(manager.getRawData("dp").contains("\"version\": \"1.0\""), manager.getRawData("dp"));
  }

  /**
   * A table schema that can't be downloaded fails the installation, leaving neither files nor an installed schema.
   */
  @Test
  public void testFailedInstallLeavesNothing() {
    DataPackageSchema schema = schema(IDENTIFIER, "dp", "1.0", "2025-01-01", "event", "occurrence");
    remoteFiles.remove(tableSchemaUrl("dp", "1.0", "occurrence"));

    assertThrows(InvalidConfigException.class, () -> manager.install(schema));

    assertNull(manager.get(IDENTIFIER));
    assertTrue(schemaFiles("dp").isEmpty(), schemaFiles("dp").toString());
  }

  @Test
  public void testReinstall() {
    manager.install(schema(IDENTIFIER, "dp", "1.0", "2025-01-01", "event"));
    manager.install(schema(IDENTIFIER, "dp", "1.0", "2025-01-01", "event"));

    assertEquals(1, manager.list().size());
    assertNotNull(manager.get(IDENTIFIER));
  }

  @Test
  public void testUpdate() throws Exception {
    manager.install(schema(IDENTIFIER, "dp", "1.0", "2025-01-01", "event"));
    DataPackageSchema newer = schema(IDENTIFIER, "dp", "2.0", "2025-06-01", "event", "occurrence");
    registryHasLatest(newer);

    assertEquals("2.0", manager.update(IDENTIFIER).getVersion());

    DataPackageSchema installed = manager.get(IDENTIFIER);
    assertEquals("2.0", installed.getVersion());
    assertEquals(Set.of("event", "occurrence"), tableSchemaNames(installed));
    assertEquals(1, manager.list().size());
    assertTrue(manager.getRawData("dp").contains("\"version\": \"2.0\""));
  }

  @Test
  public void testUpdateNotNewer() throws Exception {
    manager.install(schema(IDENTIFIER, "dp", "1.0", "2025-01-01", "event"));
    registryHasLatest(schema(IDENTIFIER, "dp", "2.0", "2025-01-01", "event"));

    assertNull(manager.update(IDENTIFIER));
    assertEquals("1.0", manager.get(IDENTIFIER).getVersion());
  }

  /**
   * The update is read with its table schemas, but not installed.
   */
  @Test
  public void testGetUpdate() throws Exception {
    manager.install(schema(IDENTIFIER, "dp", "1.0", "2025-01-01", "event"));
    registryHasLatest(schema(IDENTIFIER, "dp", "2.0", "2025-06-01", "event", "occurrence"));

    DataPackageSchema update = manager.getUpdate(IDENTIFIER);

    assertEquals("2.0", update.getVersion());
    assertTableSchemaLoaded(update, "event");
    assertTableSchemaLoaded(update, "occurrence");
    assertEquals("1.0", manager.get(IDENTIFIER).getVersion());
    assertFalse(manager.getTableSchemaFile(manager.get(IDENTIFIER), "occurrence").exists());
    assertTrue(manager.getRawData("dp").contains("\"version\": \"1.0\""));
    assertTrue(tmpFiles().isEmpty(), tmpFiles().toString());
  }

  /**
   * An update that can't be read fails, leaving the installed version as it was.
   */
  @Test
  public void testFailedGetUpdateKeepsInstalledVersion() {
    manager.install(schema(IDENTIFIER, "dp", "1.0", "2025-01-01", "event"));
    registryHasLatest(schema(IDENTIFIER, "dp", "2.0", "2025-06-01", "event", "occurrence"));
    remoteFiles.remove(tableSchemaUrl("dp", "2.0", "occurrence"));

    assertThrows(IOException.class, () -> manager.getUpdate(IDENTIFIER));

    DataPackageSchema installed = manager.get(IDENTIFIER);
    assertEquals("1.0", installed.getVersion());
    assertTrue(manager.getTableSchemaFile(installed, "event").exists());
    assertTrue(tmpFiles().isEmpty(), tmpFiles().toString());
  }

  @Test
  public void testGetUpdateNotNewer() throws Exception {
    manager.install(schema(IDENTIFIER, "dp", "1.0", "2025-01-01", "event"));
    registryHasLatest(schema(IDENTIFIER, "dp", "2.0", "2025-01-01", "event"));

    assertNull(manager.getUpdate(IDENTIFIER));
    assertNull(manager.getUpdate(OTHER_IDENTIFIER));
  }

  /**
   * An update that fails keeps the installed version, on disk and in memory.
   */
  @Test
  public void testFailedUpdateKeepsInstalledVersion() {
    manager.install(schema(IDENTIFIER, "dp", "1.0", "2025-01-01", "event"));
    DataPackageSchema newer = schema(IDENTIFIER, "dp", "2.0", "2025-06-01", "event", "occurrence");
    remoteFiles.remove(tableSchemaUrl("dp", "2.0", "occurrence"));
    registryHasLatest(newer);

    assertThrows(InvalidConfigException.class, () -> manager.update(IDENTIFIER));

    DataPackageSchema installed = manager.get(IDENTIFIER);
    assertNotNull(installed);
    assertEquals("1.0", installed.getVersion());
    assertTrue(manager.getTableSchemaFile(installed, "event").exists());
    assertTrue(manager.getRawData("dp").contains("\"version\": \"1.0\""));
  }

  @Test
  public void testInstallOrUpdateDefaults() {
    manager.install(schema(IDENTIFIER, "dp", "1.0", "2025-01-01", "event"));
    when(registryManager.getSupportedDataSchemas()).thenReturn(List.of(
        schema(IDENTIFIER, "dp", "2.0", "2025-06-01", "event"),
        schema(OTHER_IDENTIFIER, "other-dp", "1.0", "2025-01-01", "taxon")));

    manager.installOrUpdateDefaults();

    assertEquals("2.0", manager.get(IDENTIFIER).getVersion());
    assertEquals("1.0", manager.get(OTHER_IDENTIFIER).getVersion());
    assertEquals(2, manager.list().size());
  }

  /**
   * A default schema that can't be checked or installed doesn't stop the others, and keeps its installed version.
   */
  @Test
  public void testInstallOrUpdateDefaultsContinuesAfterFailure() {
    // installed without issued date
    manager.install(schema(IDENTIFIER, "dp", "1.0", null, "event"));
    DataPackageSchema failing = schema(OTHER_IDENTIFIER, "other-dp", "1.0", "2025-01-01", "taxon");
    remoteFiles.remove(tableSchemaUrl("other-dp", "1.0", "taxon"));
    when(registryManager.getSupportedDataSchemas()).thenReturn(List.of(
        failing,
        schema(IDENTIFIER, "dp", "2.0", "2025-06-01", "event")));

    manager.installOrUpdateDefaults();

    assertNull(manager.get(OTHER_IDENTIFIER));
    assertEquals("2.0", manager.get(IDENTIFIER).getVersion());
  }

  @Test
  public void testFailedDefaultUpdateKeepsInstalledVersion() {
    manager.install(schema(IDENTIFIER, "dp", "1.0", "2025-01-01", "event"));
    DataPackageSchema newer = schema(IDENTIFIER, "dp", "2.0", "2025-06-01", "event", "occurrence");
    remoteFiles.remove(tableSchemaUrl("dp", "2.0", "occurrence"));
    when(registryManager.getSupportedDataSchemas()).thenReturn(List.of(newer));

    manager.installOrUpdateDefaults();

    DataPackageSchema installed = manager.get(IDENTIFIER);
    assertNotNull(installed);
    assertEquals("1.0", installed.getVersion());
    assertTrue(manager.getTableSchemaFile(installed, "event").exists());
  }

  @Test
  public void testUpdatedSchemaLoadsAfterRestart() throws Exception {
    manager.install(schema(IDENTIFIER, "dp", "1.0", "2025-01-01", "event"));
    registryHasLatest(schema(IDENTIFIER, "dp", "2.0", "2025-06-01", "event", "occurrence"));
    manager.update(IDENTIFIER);

    manager.load();

    DataPackageSchema loaded = manager.get(IDENTIFIER);
    assertEquals("2.0", loaded.getVersion());
    assertEquals(Set.of("event", "occurrence"), tableSchemaNames(loaded));
    assertEquals(1, manager.list().size());
  }

  @Test
  public void testLoad() {
    manager.install(schema(IDENTIFIER, "dp", "1.0", "2025-01-01", "event", "occurrence"));
    manager.install(schema(OTHER_IDENTIFIER, "other-dp", "1.0", "2025-01-01", "taxon"));

    DataPackageSchemaManagerImpl restarted = newManager();

    assertEquals(2, restarted.list().size());
    assertEquals(Set.of("event", "occurrence"), tableSchemaNames(restarted.get(IDENTIFIER)));
    assertTableSchemaLoaded(restarted.get(IDENTIFIER), "occurrence");
    assertTableSchemaLoaded(restarted.get(OTHER_IDENTIFIER), "taxon");
    assertTrue(restarted.getRawData("dp").contains("\"version\": \"1.0\""));
  }

  /**
   * Schemas that can't be loaded are skipped with a startup warning and don't prevent loading the others.
   */
  @Test
  public void testLoadSkipsBrokenSchemas() throws Exception {
    manager.install(schema(IDENTIFIER, "dp", "1.0", "2025-01-01", "event"));
    manager.install(schema(OTHER_IDENTIFIER, "other-dp", "1.0", "2025-01-01", "taxon"));
    // broken schemas, loaded before the valid ones (folders are loaded in name order)
    DataPackageSchema missingTable = schema("http://example.org/missing-table", "a-missing-table", "1.0", "2025-01-01",
        "event", "occurrence");
    manager.install(missingTable);
    Files.delete(manager.getTableSchemaFile(missingTable, "occurrence").toPath());
    Files.createDirectories(schemaFolder("a-no-main-file").toPath());
    Files.writeString(new File(schemaFolder("a-no-main-file"), "table.json").toPath(), "{}");
    Files.createDirectories(schemaFolder("a-malformed").toPath());
    Files.writeString(new File(schemaFolder("a-malformed"), "_malformed.json").toPath(), "{\"identifier\": ");
    Files.createDirectories(schemaFolder("a-empty").toPath());
    Files.writeString(new File(schemaFolder("a-empty"), "_empty.json").toPath(), "");

    DataPackageSchemaManagerImpl restarted = newManager();

    assertEquals(2, restarted.load());
    assertEquals(Set.of(IDENTIFIER, OTHER_IDENTIFIER),
        restarted.list().stream().map(DataPackageSchema::getIdentifier).collect(Collectors.toSet()));
    assertEquals(4, warnings.getStartupErrors().size(), warnings.getStartupErrors().toString());
  }

  /**
   * A schema folder with more than one main schema file, or not named after its schema, isn't loaded: which schema is
   * installed, or where its files are, is unknown.
   */
  @Test
  public void testLoadSkipsAmbiguousSchemaFolders() throws Exception {
    manager.install(schema(IDENTIFIER, "dp", "1.0", "2025-01-01", "event"));
    manager.install(schema(OTHER_IDENTIFIER, "other-dp", "1.0", "2025-01-01", "taxon"));
    Files.copy(new File(schemaFolder("dp"), "_http_example_org_dp.json").toPath(),
        new File(schemaFolder("dp"), "_http_example_org_dp_copy.json").toPath());
    Files.move(schemaFolder("other-dp").toPath(), schemaFolder("renamed").toPath());

    DataPackageSchemaManagerImpl restarted = newManager();

    assertTrue(restarted.list().isEmpty());
    assertEquals(2, warnings.getStartupErrors().size(), warnings.getStartupErrors().toString());
    assertTrue(warnings.getStartupErrors().stream().anyMatch(e -> e.contains("More than one main schema file")));
    assertTrue(warnings.getStartupErrors().stream().anyMatch(e -> e.contains("installed in folder renamed")));
  }

  /**
   * Table schema files are found by the same names they are installed with, whatever the table schema identifiers.
   */
  @Test
  public void testLoadTableSchemaIdentifiersUnlikeSchemaIdentifier() {
    DataPackageSchema schema = schema(IDENTIFIER, "dp", "1.0", "2025-01-01", "event");
    schema.getTableSchemas().forEach(t -> t.setIdentifier("http://example.org/tables/" + t.getName()));
    manager.install(schema);

    assertTableSchemaLoaded(newManager().get(IDENTIFIER), "event");
  }

  /**
   * Table schema files named after the table schema identifier, by older IPT versions, still load.
   */
  @Test
  public void testLoadOldTableSchemaFileNames() throws Exception {
    DataPackageSchema schema = schema(IDENTIFIER, "dp", "1.0", "2025-01-01", "event");
    schema.getTableSchemas().forEach(t -> t.setIdentifier("http://example.org/tables/" + t.getName()));
    manager.install(schema);
    File installedFile = manager.getTableSchemaFile(schema, "event");
    Files.move(installedFile.toPath(), new File(installedFile.getParentFile(), "http_example_org_tables_event.json").toPath());

    assertTableSchemaLoaded(newManager().get(IDENTIFIER), "event");
  }

  @Test
  public void testLoadWithoutSchemas() throws Exception {
    assertEquals(0, manager.load());
    assertTrue(manager.list().isEmpty());
    assertNull(manager.get(IDENTIFIER));

    Files.createDirectories(new File(dataDirFolder, "config/.dataPackages").toPath());
    assertEquals(0, manager.load());
    assertTrue(manager.list().isEmpty());
  }

  @Test
  public void testGet() {
    manager.install(schema(IDENTIFIER, "dp", "1.0", "2025-01-01", "event"));

    assertEquals(IDENTIFIER, manager.get(IDENTIFIER).getIdentifier());
    assertEquals(IDENTIFIER, manager.get("https://example.org/dp").getIdentifier());
    assertEquals(IDENTIFIER, manager.get("dp").getIdentifier());
    assertEquals("1.0", manager.getVersion("dp"));
    assertNull(manager.get("http://example.org/unknown"));
    assertNull(manager.get(null));
    assertNull(manager.getVersion(null));
    assertNull(manager.getRawData(null));
    assertTrue(manager.isSchemaInstalled("dp"));
    assertFalse(manager.isSchemaInstalled("unknown"));
  }

  /**
   * The installed schemas returned are a snapshot, not changed by later installs.
   */
  @Test
  public void testListIsSnapshot() {
    manager.install(schema(IDENTIFIER, "dp", "1.0", "2025-01-01", "event"));
    List<DataPackageSchema> listed = manager.list();

    manager.install(schema(OTHER_IDENTIFIER, "other-dp", "1.0", "2025-01-01", "taxon"));

    assertEquals(1, listed.size());
    assertEquals(2, manager.list().size());
  }

  /**
   * Lookups while schemas are being reloaded always find the installed schema.
   */
  @Test
  public void testGetDuringLoad() throws Exception {
    manager.install(schema(IDENTIFIER, "dp", "1.0", "2025-01-01", "event"));
    Thread loader = new Thread(() -> {
      for (int i = 0; i < 200; i++) {
        manager.load();
      }
    });

    loader.start();
    int lookups = 0;
    while (loader.isAlive()) {
      assertNotNull(manager.get(IDENTIFIER));
      lookups++;
    }
    loader.join();

    assertTrue(lookups > 0);
  }

  @Test
  public void testUninstall() {
    manager.install(schema(IDENTIFIER, "dp", "1.0", "2025-01-01", "event"));
    manager.install(schema(OTHER_IDENTIFIER, "other-dp", "1.0", "2025-01-01", "taxon"));

    manager.uninstall(IDENTIFIER);

    assertNull(manager.get(IDENTIFIER));
    assertNull(manager.getRawData("dp"));
    assertFalse(schemaFolder("dp").exists());
    assertNotNull(manager.get(OTHER_IDENTIFIER));
    assertEquals(List.of(OTHER_IDENTIFIER),
        newManager().list().stream().map(DataPackageSchema::getIdentifier).collect(Collectors.toList()));
  }

  /**
   * A schema is uninstalled by any form of its identifier, or its name.
   */
  @Test
  public void testUninstallByOtherIdentifierForms() {
    manager.install(schema(IDENTIFIER, "dp", "1.0", "2025-01-01", "event"));
    manager.uninstall("https://example.org/dp");
    assertNull(manager.get(IDENTIFIER));
    assertFalse(schemaFolder("dp").exists());

    manager.install(schema(IDENTIFIER, "dp", "1.0", "2025-01-01", "event"));
    manager.uninstall("dp");
    assertNull(manager.get(IDENTIFIER));
  }

  @Test
  public void testUninstallUnknown() {
    manager.install(schema(IDENTIFIER, "dp", "1.0", "2025-01-01", "event"));

    manager.uninstall("http://example.org/unknown");
    manager.uninstall(null);

    assertNotNull(manager.get(IDENTIFIER));
    assertTrue(schemaFolder("dp").exists());
  }

  /**
   * The schema name is its folder name, so it can't point anywhere else.
   */
  @Test
  public void testInstallRejectsInvalidName() {
    DataPackageSchema schema = schema(IDENTIFIER, "../dp", "1.0", "2025-01-01", "event");

    assertThrows(InvalidConfigException.class, () -> manager.install(schema));

    assertNull(manager.get(IDENTIFIER));
    assertFalse(new File(dataDirFolder, "config/dp").exists());
  }

  /**
   * Installs the schemas supported for the registry type, e.g. without the ones for development on production.
   */
  @Test
  public void testInstallSupportedDataPackageSchemas() throws Exception {
    when(cfg.getSupportedDataSchemaNamesWithVersions()).thenReturn(Map.of("dp", "1.0", "other-dp", "1.0"));
    when(registryManager.getSupportedDataSchemas()).thenReturn(List.of(
        schema(IDENTIFIER, "dp", "1.0", "2025-01-01", "event"),
        schema(OTHER_IDENTIFIER, "other-dp", "1.0", "2025-01-01", "taxon")));

    manager.installSupportedDataPackageSchemas();

    assertEquals(2, manager.list().size());
  }

  @Test
  public void testInstallSupportedDataPackageSchemasContinuesAfterFailure() throws Exception {
    DataPackageSchema failing = schema(IDENTIFIER, "dp", "1.0", "2025-01-01", "event");
    remoteFiles.remove(tableSchemaUrl("dp", "1.0", "event"));
    when(registryManager.getSupportedDataSchemas()).thenReturn(List.of(
        failing,
        schema(OTHER_IDENTIFIER, "other-dp", "1.0", "2025-01-01", "taxon")));

    assertThrows(InvalidConfigException.class, () -> manager.installSupportedDataPackageSchemas());

    assertNull(manager.get(IDENTIFIER));
    assertNotNull(manager.get(OTHER_IDENTIFIER));
  }

  /**
   * A supported schema the registry doesn't return is reported like a failed one, after installing the others.
   */
  @Test
  public void testInstallSupportedDataPackageSchemasWithMissingSchema() throws Exception {
    when(cfg.getSupportedDataSchemaNamesWithVersions()).thenReturn(Map.of("dp", "1.0", "missing-dp", "1.0"));
    when(registryManager.getSupportedDataSchemas()).thenReturn(List.of(
        schema(IDENTIFIER, "dp", "1.0", "2025-01-01", "event")));

    InvalidConfigException e = assertThrows(InvalidConfigException.class,
        () -> manager.installSupportedDataPackageSchemas());

    assertTrue(e.getMessage().contains("missing-dp"), e.getMessage());
    assertNotNull(manager.get(IDENTIFIER));
  }

  @Test
  public void testInstallSupportedDataPackageSchemasWithoutAny() throws Exception {
    when(registryManager.getSupportedDataSchemas()).thenReturn(List.of());

    assertThrows(InvalidConfigException.class, () -> manager.installSupportedDataPackageSchemas());
  }

  /**
   * Builds a schema as the registry describes it, and serves its table schemas from the fake downloader.
   */
  private DataPackageSchema schema(String identifier, String name, String version, String issued,
                                   String... tableSchemaNames) {
    Set<DataPackageTableSchema> tableSchemas = new LinkedHashSet<>();
    for (String tableSchemaName : tableSchemaNames) {
      String url = tableSchemaUrl(name, version, tableSchemaName);
      remoteFiles.put(url, "{"
          + "\"identifier\": \"" + identifier + "/" + tableSchemaName + "\","
          + "\"url\": \"" + url + "\","
          + "\"name\": \"" + tableSchemaName + "\","
          + "\"fields\": [{\"name\": \"" + tableSchemaName + "ID\", \"type\": \"string\"}]"
          + "}");
      DataPackageTableSchema tableSchema = new DataPackageTableSchema();
      tableSchema.setIdentifier(identifier + "/" + tableSchemaName);
      tableSchema.setName(tableSchemaName);
      tableSchema.setUrl(url(url));
      tableSchemas.add(tableSchema);
    }

    return DataPackageSchema.builder()
        .identifier(identifier)
        .name(name)
        .title(name)
        .version(version)
        .url(url(BASE_URL + name + "/" + version + "/index.json"))
        .issued(issued == null ? null : date(issued))
        .tableSchemas(tableSchemas)
        .build();
  }

  private void registryHasLatest(DataPackageSchema latest) {
    when(registryManager.getLatestCompatibleSchemaVersion(any(), any())).thenReturn(latest.getVersion());
    when(registryManager.getSchema(latest.getName(), latest.getVersion())).thenReturn(latest);
  }

  private static String tableSchemaUrl(String name, String version, String tableSchemaName) {
    return BASE_URL + name + "/" + version + "/table-schemas/" + tableSchemaName + ".json";
  }

  private File schemaFolder(String name) {
    return new File(dataDirFolder, "config/.dataPackages/" + name);
  }

  /**
   * @return files left in the temp folder
   */
  private List<String> tmpFiles() {
    String[] files = dataDir.tmpFile("").list();
    return files == null ? List.of() : List.of(files);
  }

  private List<String> schemaFiles(String name) {
    File folder = new File(dataDirFolder, "config/.dataPackages/" + name);
    String[] files = folder.list();
    return files == null ? List.of() : List.of(files);
  }

  /**
   * Asserts the table schema was loaded from its file, with fields, not just listed in the main schema file.
   */
  private static void assertTableSchemaLoaded(DataPackageSchema schema, String tableSchemaName) {
    DataPackageTableSchema tableSchema = schema.tableSchemaByName(tableSchemaName);
    assertNotNull(tableSchema, tableSchemaName);
    assertNotNull(tableSchema.getFields(), tableSchemaName + " fields");
    assertEquals(tableSchemaName + "ID", tableSchema.getFields().get(0).getName());
  }

  private static Set<String> tableSchemaNames(DataPackageSchema schema) {
    return schema.getTableSchemas().stream().map(DataPackageTableSchema::getName).collect(Collectors.toSet());
  }

  private static URL url(String url) {
    try {
      return new URL(url);
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }

  private static Date date(String date) {
    try {
      return new SimpleDateFormat("yyyy-MM-dd").parse(date);
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }
}
