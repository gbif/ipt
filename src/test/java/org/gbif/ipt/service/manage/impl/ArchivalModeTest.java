/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
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

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.mock.MockAppConfig;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.converter.ConceptTermConverter;
import org.gbif.ipt.model.converter.ExtensionRowTypeConverter;
import org.gbif.ipt.model.converter.JdbcInfoConverter;
import org.gbif.ipt.model.converter.OrganisationKeyConverter;
import org.gbif.ipt.model.converter.PasswordEncrypter;
import org.gbif.ipt.model.converter.UserEmailConverter;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.task.Eml2Rtf;
import org.gbif.ipt.task.GenerateDwcaFactory;
import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ArchivalModeTest {

  private DataDir mockDataDir = mock(DataDir.class);

  @BeforeEach
  public void setup() throws IOException {
    File tmpDataDir = FileUtils.createTempDir();
    when(mockDataDir.resourceEmlFile(anyString(), any())).thenReturn(tmpDataDir);
    when(mockDataDir.resourceRtfFile(anyString(), any())).thenReturn(tmpDataDir);
    when(mockDataDir.resourceDwcaFile(anyString(), any())).thenReturn(tmpDataDir);
  }

  public ResourceManagerImpl getResourceManagerImpl(AppConfig mockAppConfig) {
    return new ResourceManagerImpl(
      mockAppConfig,
      mockDataDir,
      mock(UserEmailConverter.class),
      mock(OrganisationKeyConverter.class),
      mock(ExtensionRowTypeConverter.class),
      mock(JdbcInfoConverter.class),
      mock(SourceManager.class),
      mock(ExtensionManager.class),
      mock(RegistryManager.class),
      mock(ConceptTermConverter.class),
      mock(GenerateDwcaFactory.class),
      mock(PasswordEncrypter.class),
      mock(Eml2Rtf.class),
      mock(VocabulariesManager.class),
      mock(SimpleTextProvider.class),
      mock(RegistrationManager.class));
  }

  public Resource getResource(String shortName) {
    List<VersionHistory> history = new ArrayList<>();
    history.add(new VersionHistory(BigDecimal.valueOf(2.5), PublicationStatus.PUBLIC));
    history.add(new VersionHistory(BigDecimal.valueOf(2.4), PublicationStatus.PUBLIC));
    history.add(new VersionHistory(BigDecimal.valueOf(2.3), PublicationStatus.PUBLIC));
    history.add(new VersionHistory(BigDecimal.valueOf(2.2), PublicationStatus.PUBLIC));
    history.add(new VersionHistory(BigDecimal.valueOf(2.1), PublicationStatus.PUBLIC));
    history.add(new VersionHistory(BigDecimal.valueOf(2.0), PublicationStatus.PUBLIC));
    Resource resource = new Resource();
    resource.setShortname(shortName);
    resource.setVersionHistory(history);
    return resource;
  }

  @Test
  public void testArchiveModeOnWithoutLimit() {
    AppConfig mockAppConfig = MockAppConfig.rebuildMock();
    when(mockAppConfig.isArchivalMode()).thenReturn(true);
    ResourceManagerImpl resourceManager = getResourceManagerImpl(mockAppConfig);

    Resource resource = getResource("testArchiveModeOnWithoutLimit");
    resourceManager.cleanArchiveVersions(resource);

    assertEquals(resource.getVersionHistory().size(), 6);
    verify(mockDataDir, times(0)).resourceDwcaFile(any(), any());
  }

  @Test
  public void testArchiveModeOffWithoutLimit() {
    AppConfig mockAppConfig = MockAppConfig.rebuildMock();
    when(mockAppConfig.isArchivalMode()).thenReturn(false);
    ResourceManagerImpl resourceManager = getResourceManagerImpl(mockAppConfig);

    Resource resource = getResource("testArchiveModeOffWithoutLimit");
    resourceManager.cleanArchiveVersions(resource);

    assertEquals(resource.getVersionHistory().size(), 6);
    verify(mockDataDir, times(0)).resourceDwcaFile(any(), any());
  }

  @Test
  public void testArchiveModeOnWithNullLimit() {
    AppConfig mockAppConfig = MockAppConfig.rebuildMock();
    when(mockAppConfig.isArchivalMode()).thenReturn(true);
    when(mockAppConfig.getArchivalLimit()).thenReturn(null);
    ResourceManagerImpl resourceManager = getResourceManagerImpl(mockAppConfig);

    Resource resource = getResource("testArchiveModeOnWithNullLimit");
    resourceManager.cleanArchiveVersions(resource);

    assertEquals(resource.getVersionHistory().size(), 6);
    verify(mockDataDir, times(0)).resourceDwcaFile(any(), any());
  }

  @Test
  public void testArchiveModeOnWithZeroLimit() {
    AppConfig mockAppConfig = MockAppConfig.rebuildMock();
    when(mockAppConfig.isArchivalMode()).thenReturn(true);
    when(mockAppConfig.getArchivalLimit()).thenReturn(0);
    ResourceManagerImpl resourceManager = getResourceManagerImpl(mockAppConfig);

    Resource resource = getResource("testArchiveModeOnWithZeroLimit");
    resourceManager.cleanArchiveVersions(resource);

    assertEquals(resource.getVersionHistory().size(), 6);
    verify(mockDataDir, times(0)).resourceDwcaFile(any(), any());
  }

  @Test
  public void testArchiveLimitHigher() {
    AppConfig mockAppConfig = MockAppConfig.rebuildMock();
    when(mockAppConfig.isArchivalMode()).thenReturn(true);
    when(mockAppConfig.getArchivalLimit()).thenReturn(10);
    ResourceManagerImpl resourceManager = getResourceManagerImpl(mockAppConfig);

    Resource resource = getResource("testArchiveLimitHigher");
    resourceManager.cleanArchiveVersions(resource);

    assertEquals(resource.getVersionHistory().size(), 6);
    verify(mockDataDir, times(0)).resourceDwcaFile(any(), any());
  }

  @Test
  public void testArchiveLimitLower() {
    String resourceName = "testArchiveLimitLower";
    AppConfig mockAppConfig = MockAppConfig.rebuildMock();
    when(mockAppConfig.isArchivalMode()).thenReturn(true);
    when(mockAppConfig.getArchivalLimit()).thenReturn(2);
    ResourceManagerImpl resourceManager = getResourceManagerImpl(mockAppConfig);

    Resource resource = getResource(resourceName);
    resourceManager.cleanArchiveVersions(resource);

    assertEquals(resource.getVersionHistory().size(), 6);
    verify(mockDataDir, times(4)).resourceDwcaFile(any(), any());
    verify(mockDataDir, times(1)).resourceDwcaFile(resourceName, BigDecimal.valueOf(2.3));
    verify(mockDataDir, times(1)).resourceDwcaFile(resourceName, BigDecimal.valueOf(2.2));
     verify(mockDataDir, times(1)).resourceDwcaFile(resourceName, BigDecimal.valueOf(2.1));
    verify(mockDataDir, times(1)).resourceDwcaFile(resourceName, BigDecimal.valueOf(2.0));
  }
}
