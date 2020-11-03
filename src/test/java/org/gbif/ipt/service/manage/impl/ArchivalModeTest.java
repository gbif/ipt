/***************************************************************************
 * Copyright 2011 Global Biodiversity Information Facility Secretariat
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.ipt.service.manage.impl;

import org.gbif.ipt.config.*;
import org.gbif.ipt.mock.MockAppConfig;
import org.gbif.ipt.model.*;
import org.gbif.ipt.model.converter.*;
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
import org.junit.Before;
import org.junit.Test;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArchivalModeTest {

    private DataDir mockDataDir = mock(DataDir.class);

    @Before
    public void setup() throws IOException {
        File tmpDataDir = FileUtils.createTempDir();
        when(mockDataDir.resourceEmlFile(anyString(), anyObject())).thenReturn(tmpDataDir);
        when(mockDataDir.resourceRtfFile(anyString(), anyObject())).thenReturn(tmpDataDir);
        when(mockDataDir.resourceDwcaFile(anyString(), anyObject())).thenReturn(tmpDataDir);
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
                mock(PasswordConverter.class),
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
    }

    @Test
    public void testArchiveModeOffWithoutLimit() {
        AppConfig mockAppConfig = MockAppConfig.rebuildMock();
        when(mockAppConfig.isArchivalMode()).thenReturn(false);
        ResourceManagerImpl resourceManager = getResourceManagerImpl(mockAppConfig);

        Resource resource = getResource("testArchiveModeOffWithoutLimit");
        resourceManager.cleanArchiveVersions(resource);

        assertEquals(resource.getVersionHistory().size(), 6);
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
    }

    @Test
    public void testArchiveLimitLower() {
        AppConfig mockAppConfig = MockAppConfig.rebuildMock();
        when(mockAppConfig.isArchivalMode()).thenReturn(true);
        when(mockAppConfig.getArchivalLimit()).thenReturn(2);
        ResourceManagerImpl resourceManager = getResourceManagerImpl(mockAppConfig);

        Resource resource = getResource("testArchiveLimitLower");
        resourceManager.cleanArchiveVersions(resource);

        assertEquals(resource.getVersionHistory().size(), 2);
        assertEquals(resource.getVersionHistory().get(0).getVersion(), "2.5");
        assertEquals(resource.getVersionHistory().get(1).getVersion(), "2.4");
    }
}