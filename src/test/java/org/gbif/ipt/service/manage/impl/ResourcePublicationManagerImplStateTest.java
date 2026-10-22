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
package org.gbif.ipt.service.manage.impl;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.voc.PublicationMode;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.ResourceMetadataInferringService;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.task.Eml2Rtf;
import org.gbif.ipt.task.GenerateDarwinCoreDataPackageFactory;
import org.gbif.ipt.task.GenerateDataPackageFactory;
import org.gbif.ipt.task.GenerateDwcaFactory;
import org.gbif.ipt.task.StatusReport;
import org.gbif.metadata.eml.ipt.model.MaintenanceUpdateFrequency;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link ResourcePublicationManagerImpl} state handling (locks, reports, failures), visibility and versions, with its
 * collaborators mocked.
 */
public class ResourcePublicationManagerImplStateTest {

  private static final String SHORTNAME = "bees";

  @TempDir
  Path tempDir;

  private File resourceDir;
  private AppConfig cfg;
  private ResourceManager resourceManager;
  private ResourcePublicationManagerImpl manager;

  @BeforeEach
  public void setUp() throws IOException {
    resourceDir = Files.createDirectory(tempDir.resolve(SHORTNAME)).toFile();
    cfg = mock(AppConfig.class);
    when(cfg.getMaxThreads()).thenReturn(3);
    DataDir dataDir = mock(DataDir.class);
    when(dataDir.resourceEmlFile(anyString(), any(BigDecimal.class))).thenAnswer(invocation ->
        versionedFile("eml-", invocation.getArgument(1, BigDecimal.class), ".xml"));
    when(dataDir.resourceDatapackageMetadataFile(anyString(), any(), any(BigDecimal.class))).thenAnswer(invocation ->
        versionedFile("datapackage-", invocation.getArgument(2, BigDecimal.class), ".json"));
    when(dataDir.resourceRtfFile(anyString(), any(BigDecimal.class))).thenAnswer(invocation ->
        versionedFile(SHORTNAME + "-", invocation.getArgument(1, BigDecimal.class), ".rtf"));
    when(dataDir.resourceDwcaFile(anyString(), any(BigDecimal.class))).thenAnswer(invocation ->
        versionedFile("dwca-", invocation.getArgument(1, BigDecimal.class), ".zip"));
    when(dataDir.resourceDataPackageFile(anyString(), any(BigDecimal.class))).thenAnswer(invocation ->
        versionedFile("datapackage-", invocation.getArgument(1, BigDecimal.class), ".zip"));
    resourceManager = mock(ResourceManager.class);

    manager = new ResourcePublicationManagerImpl(cfg, dataDir, resourceManager, mock(RegistryManager.class),
        mock(RegistrationManager.class), mock(Eml2Rtf.class), mock(GenerateDwcaFactory.class),
        mock(GenerateDataPackageFactory.class), mock(GenerateDarwinCoreDataPackageFactory.class),
        mock(SimpleTextProvider.class), mock(ResourceMetadataInferringService.class),
        new ResourceDoiServiceImpl(cfg, dataDir, mock(RegistrationManager.class)));
  }

  @AfterEach
  public void tearDown() {
    manager.getExecutor().shutdownNow();
  }

  @Test
  public void testExecutorUsesConfiguredThreads() {
    assertEquals(3, manager.getExecutor().getCorePoolSize());
    assertEquals(3, manager.getExecutor().getMaximumPoolSize());
  }

  @Test
  public void testNotLocked() {
    assertFalse(manager.isLocked(SHORTNAME));
    assertFalse(manager.isLocked(SHORTNAME, mock(BaseAction.class)));
  }

  @Test
  public void testLockedWhilePublishing() {
    when(resourceManager.get(SHORTNAME)).thenReturn(resource());
    manager.getProcessFutures().put(SHORTNAME, new CompletableFuture<>());

    assertTrue(manager.isLocked(SHORTNAME));
    assertTrue(manager.getProcessFutures().containsKey(SHORTNAME));
  }

  @Test
  public void testCancelPublishingWithoutJob() {
    assertFalse(manager.cancelPublishing(SHORTNAME));
  }

  @Test
  public void testCancelPublishing() {
    CompletableFuture<Map<String, Integer>> future = new CompletableFuture<>();
    manager.getProcessFutures().put(SHORTNAME, future);

    assertTrue(manager.cancelPublishing(SHORTNAME));

    assertTrue(future.isCancelled());
    assertFalse(manager.getProcessFutures().containsKey(SHORTNAME));
    assertFalse(manager.isLocked(SHORTNAME));
  }

  @Test
  public void testCancelFinishedPublishing() {
    manager.getProcessFutures().put(SHORTNAME, CompletableFuture.completedFuture(Map.of()));

    assertFalse(manager.cancelPublishing(SHORTNAME));

    // left for isLocked to finish the publication
    assertTrue(manager.getProcessFutures().containsKey(SHORTNAME));
  }

  @Test
  public void testCancelPublishingOfOtherResource() {
    CompletableFuture<Map<String, Integer>> future = new CompletableFuture<>();
    manager.getProcessFutures().put("ants", future);

    assertFalse(manager.cancelPublishing(SHORTNAME));

    assertFalse(future.isCancelled());
    assertTrue(manager.getProcessFutures().containsKey("ants"));
  }

  @Test
  public void testReportAndStatus() {
    assertNull(manager.status(SHORTNAME));

    StatusReport report = new StatusReport(true, "Done", List.of());
    manager.report(SHORTNAME, report);

    assertSame(report, manager.status(SHORTNAME));
    assertSame(report, manager.getProcessReports().get(SHORTNAME));
    assertNull(manager.status("ants"));
  }

  @Test
  public void testClearProcessReports() {
    manager.report(SHORTNAME, new StatusReport(true, "Done", List.of()));
    manager.report("ants", new StatusReport(false, "Failed", List.of()));

    manager.clearProcessReports();

    assertTrue(manager.getProcessReports().isEmpty());
    assertNull(manager.status(SHORTNAME));
  }

  @Test
  public void testHasMaxProcessFailures() {
    Resource resource = resource();
    assertFalse(manager.hasMaxProcessFailures(resource));

    addFailures(SHORTNAME, 2);
    assertFalse(manager.hasMaxProcessFailures(resource));
    assertFalse(manager.hasMaxProcessFailures(resource, false));

    addFailures(SHORTNAME, 1);
    assertTrue(manager.hasMaxProcessFailures(resource));
    // logged once a day, the result is the same
    assertTrue(manager.hasMaxProcessFailures(resource));
    assertTrue(manager.hasMaxProcessFailures(resource, false));

    manager.getProcessFailures().remove(SHORTNAME);
    assertFalse(manager.hasMaxProcessFailures(resource));
  }

  @Test
  public void testVisibilityToPrivate() {
    Resource resource = resource();
    resource.setDataPackageIdentifier("http://rs.tdwg.org/dwc/dwc-dp");
    resource.setStatus(PublicationStatus.PUBLIC);

    manager.visibilityToPrivate(resource, mock(BaseAction.class));

    assertEquals(PublicationStatus.PRIVATE, resource.getPendingStatus());
    // applies on the next publication
    assertEquals(PublicationStatus.PUBLIC, resource.getStatus());
    verify(resourceManager).save(resource);
  }

  @Test
  public void testVisibilityToPrivateOfPrivateResource() {
    Resource resource = resource();
    resource.setStatus(PublicationStatus.PRIVATE);

    manager.visibilityToPrivate(resource, mock(BaseAction.class));

    assertNull(resource.getPendingStatus());
    verify(resourceManager, never()).save(any());
  }

  @Test
  public void testVisibilityToPrivateOfRegisteredResource() {
    Resource resource = resource();
    resource.setStatus(PublicationStatus.REGISTERED);

    InvalidConfigException e = assertThrows(InvalidConfigException.class,
        () -> manager.visibilityToPrivate(resource, mock(BaseAction.class)));
    assertEquals(InvalidConfigException.TYPE.RESOURCE_ALREADY_REGISTERED, e.getType());
    verify(resourceManager, never()).save(any());
  }

  @Test
  public void testVisibilityToPublic() {
    Resource resource = resource();
    resource.setDataPackageIdentifier("http://rs.tdwg.org/dwc/dwc-dp");
    resource.setStatus(PublicationStatus.PRIVATE);
    resource.setMakePublicDate(new Date());

    manager.visibilityToPublic(resource, mock(BaseAction.class));

    assertEquals(PublicationStatus.PUBLIC, resource.getPendingStatus());
    assertEquals(PublicationStatus.PRIVATE, resource.getStatus());
    assertNull(resource.getMakePublicDate());
    verify(resourceManager).save(resource);
  }

  @Test
  public void testVisibilityToPublicOfPublicResource() {
    Resource resource = resource();
    resource.setStatus(PublicationStatus.PUBLIC);

    manager.visibilityToPublic(resource, mock(BaseAction.class));

    assertNull(resource.getPendingStatus());
    verify(resourceManager, never()).save(any());
  }

  @Test
  public void testVisibilityToPublicOfRegisteredResource() {
    Resource resource = resource();
    resource.setStatus(PublicationStatus.REGISTERED);

    assertThrows(InvalidConfigException.class, () -> manager.visibilityToPublic(resource, mock(BaseAction.class)));
    verify(resourceManager, never()).save(any());
  }

  @Test
  public void testUpdatePublicationModeOff() {
    Resource resource = resource();
    resource.setPublicationMode(PublicationMode.AUTO_PUBLISH_OFF);
    resource.setUpdateFrequency(MaintenanceUpdateFrequency.DAILY.getIdentifier());
    resource.setNextPublished(new Date());

    manager.updatePublicationMode(resource);

    assertNull(resource.getNextPublished());
  }

  @Test
  public void testUpdatePublicationModeOnWithoutFrequency() {
    Resource resource = resource();
    resource.setPublicationMode(PublicationMode.AUTO_PUBLISH_ON);
    resource.setNextPublished(new Date());

    manager.updatePublicationMode(resource);

    assertNull(resource.getNextPublished());
  }

  @Test
  public void testRemoveVersion() throws Exception {
    Resource resource = resource();
    resource.setMetadataVersion(new BigDecimal("2.0"));
    resource.addVersionHistory(new VersionHistory(new BigDecimal("1.0"), new Date(), PublicationStatus.PUBLIC));
    resource.addVersionHistory(new VersionHistory(new BigDecimal("2.0"), new Date(), PublicationStatus.PUBLIC));
    List<File> version1Files = createVersionFiles(new BigDecimal("1.0"));
    List<File> version2Files = createVersionFiles(new BigDecimal("2.0"));

    manager.removeVersion(resource, new BigDecimal("1.0"));

    version1Files.forEach(f -> assertFalse(f.exists(), f.getName()));
    version2Files.forEach(f -> assertTrue(f.exists(), f.getName()));
    assertEquals(1, resource.getVersionHistory().size());
    assertEquals("2.0", resource.getVersionHistory().get(0).getVersion());
    verify(resourceManager).save(resource);
  }

  @Test
  public void testRemoveCurrentVersionIsNotAllowed() throws Exception {
    Resource resource = resource();
    resource.setMetadataVersion(new BigDecimal("2.0"));
    resource.addVersionHistory(new VersionHistory(new BigDecimal("2.0"), new Date(), PublicationStatus.PUBLIC));
    List<File> files = createVersionFiles(new BigDecimal("2.0"));

    manager.removeVersion(resource, new BigDecimal("2.0"));
    manager.removeVersion(resource, null);

    files.forEach(f -> assertTrue(f.exists(), f.getName()));
    assertEquals(1, resource.getVersionHistory().size());
    verify(resourceManager, never()).save(any());
  }

  @Test
  public void testRemoveArchiveVersionKeepsMetadata() throws Exception {
    createVersionFiles(new BigDecimal("1.0"));

    manager.removeArchiveVersion(SHORTNAME, new BigDecimal("1.0"));

    assertFalse(versionedFile("dwca-", new BigDecimal("1.0"), ".zip").exists());
    assertFalse(versionedFile("datapackage-", new BigDecimal("1.0"), ".zip").exists());
    assertTrue(versionedFile("eml-", new BigDecimal("1.0"), ".xml").exists());
    assertTrue(versionedFile("datapackage-", new BigDecimal("1.0"), ".json").exists());
    assertTrue(versionedFile(SHORTNAME + "-", new BigDecimal("1.0"), ".rtf").exists());
  }

  @Test
  public void testRemoveMissingArchiveVersion() {
    manager.removeArchiveVersion(SHORTNAME, new BigDecimal("7.0"));
    assertFalse(versionedFile("dwca-", new BigDecimal("7.0"), ".zip").exists());
  }

  @Test
  public void testCleanArchiveVersionsKeepsLatestVersions() throws Exception {
    when(cfg.isArchivalMode()).thenReturn(true);
    when(cfg.getArchivalLimit()).thenReturn(2);
    Resource resource = resource();
    for (String version : List.of("1.0", "2.0", "3.0", "4.0")) {
      resource.addVersionHistory(new VersionHistory(new BigDecimal(version), new Date(), PublicationStatus.PUBLIC));
      createVersionFiles(new BigDecimal(version));
    }

    manager.cleanArchiveVersions(resource);

    assertFalse(versionedFile("dwca-", new BigDecimal("1.0"), ".zip").exists());
    assertFalse(versionedFile("dwca-", new BigDecimal("2.0"), ".zip").exists());
    assertTrue(versionedFile("dwca-", new BigDecimal("3.0"), ".zip").exists());
    assertTrue(versionedFile("dwca-", new BigDecimal("4.0"), ".zip").exists());
    // metadata and history are kept
    assertTrue(versionedFile("eml-", new BigDecimal("1.0"), ".xml").exists());
    assertEquals(4, resource.getVersionHistory().size());
  }

  @Test
  public void testCleanArchiveVersionsWithoutArchivalMode() throws Exception {
    when(cfg.isArchivalMode()).thenReturn(false);
    when(cfg.getArchivalLimit()).thenReturn(1);
    Resource resource = resource();
    for (String version : List.of("1.0", "2.0")) {
      resource.addVersionHistory(new VersionHistory(new BigDecimal(version), new Date(), PublicationStatus.PUBLIC));
      createVersionFiles(new BigDecimal(version));
    }

    manager.cleanArchiveVersions(resource);

    assertTrue(versionedFile("dwca-", new BigDecimal("1.0"), ".zip").exists());
  }

  @Test
  public void testCalculateChecksum() throws Exception {
    File file = Files.writeString(tempDir.resolve("abc.txt"), "abc", StandardCharsets.UTF_8).toFile();

    assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", manager.calculateChecksum(file));
  }

  @Test
  public void testCalculateArchiveChecksumIgnoresMetadata() throws Exception {
    File archive = zip("a.zip", Map.of("occurrence.txt", "id\n1\n", "eml.xml", "<eml>v1</eml>"));
    File changedEml = zip("b.zip", Map.of("occurrence.txt", "id\n1\n", "eml.xml", "<eml>v2</eml>"));
    File changedDatapackage = zip("c.zip", Map.of("occurrence.txt", "id\n1\n", "datapackage.json", "{}"));
    File changedData = zip("d.zip", Map.of("occurrence.txt", "id\n2\n", "eml.xml", "<eml>v1</eml>"));

    String checksum = manager.calculateArchiveChecksum(archive);

    assertEquals(checksum, manager.calculateArchiveChecksum(changedEml));
    assertEquals(checksum, manager.calculateArchiveChecksum(changedDatapackage));
    assertNotEquals(checksum, manager.calculateArchiveChecksum(changedData));
  }

  private Resource resource() {
    Resource resource = new Resource();
    resource.setShortname(SHORTNAME);
    return resource;
  }

  private void addFailures(String shortname, int count) {
    for (int i = 0; i < count; i++) {
      manager.getProcessFailures().computeIfAbsent(shortname, k -> new CopyOnWriteArrayList<>()).add(new Date());
    }
  }

  private File versionedFile(String prefix, BigDecimal version, String suffix) {
    return new File(resourceDir, prefix + version.toPlainString() + suffix);
  }

  private List<File> createVersionFiles(BigDecimal version) throws IOException {
    List<File> files = List.of(
        versionedFile("eml-", version, ".xml"),
        versionedFile("datapackage-", version, ".json"),
        versionedFile(SHORTNAME + "-", version, ".rtf"),
        versionedFile("dwca-", version, ".zip"),
        versionedFile("datapackage-", version, ".zip"));
    for (File f : files) {
      Files.writeString(f.toPath(), f.getName());
    }
    return files;
  }

  private File zip(String name, Map<String, String> entries) throws IOException {
    File file = tempDir.resolve(name).toFile();
    try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file))) {
      for (String entry : entries.keySet().stream().sorted().toList()) {
        zos.putNextEntry(new ZipEntry(entry));
        zos.write(entries.get(entry).getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
      }
    }
    return file;
  }
}
