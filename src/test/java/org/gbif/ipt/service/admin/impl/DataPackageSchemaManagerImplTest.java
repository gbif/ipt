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
  private RegistryManager registryManager;
  private DataPackageSchemaManagerImpl manager;

  @BeforeEach
  public void setUp() throws Exception {
    DataDir dataDir = DataDir.buildFromString(dataDirFolder.getAbsolutePath());

    HttpClient downloader = mock(HttpClient.class);
    when(downloader.download(any(URL.class), any(File.class))).thenAnswer(invocation -> {
      String content = remoteFiles.get(invocation.getArgument(0, URL.class).toString());
      if (content == null) {
        return new BasicStatusLine(HttpVersion.HTTP_1_1, 404, "Not Found");
      }
      Files.writeString(invocation.getArgument(1, File.class).toPath(), content);
      return new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK");
    });

    registryManager = mock(RegistryManager.class);

    manager = new DataPackageSchemaManagerImpl(mock(AppConfig.class), dataDir, new ConfigWarnings(),
        new DataSchemaFactory(), mock(SimpleTextProvider.class), mock(RegistrationManager.class), registryManager,
        downloader);
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

  private List<String> schemaFiles(String name) {
    File folder = new File(dataDirFolder, "config/.dataPackages/" + name);
    String[] files = folder.list();
    return files == null ? List.of() : List.of(files);
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
