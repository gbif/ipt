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

import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.datapackage.metadata.DataPackageMetadata;
import org.gbif.ipt.model.datapackage.metadata.FrictionlessMetadata;
import org.gbif.ipt.model.datapackage.metadata.camtrap.CamtrapMetadata;
import org.gbif.ipt.model.datapackage.metadata.col.ColMetadata;
import org.gbif.ipt.service.manage.MetadataReader;
import org.gbif.metadata.eml.ipt.model.Eml;
import org.gbif.utils.file.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.gbif.ipt.config.Constants.CAMTRAP_DP;
import static org.gbif.ipt.config.Constants.COL_DP;
import static org.gbif.ipt.config.Constants.DWC_DP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ResourceMetadataLoaderImplTest {

  private static final String SHORTNAME = "bees";

  @TempDir
  Path tempDir;

  private DataDir dataDir;
  private MetadataReader metadataReader;
  private ResourceMetadataLoaderImpl loader;
  private File emlFile;
  private File datapackageFile;

  @BeforeEach
  public void setUp() {
    emlFile = tempDir.resolve("eml.xml").toFile();
    datapackageFile = tempDir.resolve("datapackage.json").toFile();
    dataDir = mock(DataDir.class);
    when(dataDir.resourceEmlFile(SHORTNAME)).thenReturn(emlFile);
    when(dataDir.resourceDatapackageMetadataFile(anyString(), any())).thenReturn(datapackageFile);
    metadataReader = mock(MetadataReader.class);
    loader = new ResourceMetadataLoaderImpl(dataDir, metadataReader);
  }

  @Test
  public void testLoadEml() throws Exception {
    Files.copy(FileUtils.getClasspathFile("data/eml.xml").toPath(), emlFile.toPath());
    Resource resource = resource(null, null);

    loader.loadEml(resource);

    assertEquals("WII Herbarium Dataset", resource.getEml().getTitle());
  }

  @Test
  public void testLoadMissingEml() {
    Resource resource = resource(null, null);
    Eml before = resource.getEml();

    loader.loadEml(resource);

    assertNotSame(before, resource.getEml());
    assertNull(resource.getEml().getTitle());
  }

  @Test
  public void testLoadMetadataOfDarwinCoreArchiveResource() throws Exception {
    Files.copy(FileUtils.getClasspathFile("data/eml.xml").toPath(), emlFile.toPath());
    Resource resource = resource("occurrence", null);
    DataPackageMetadata before = resource.getDataPackageMetadata();

    loader.loadMetadata(resource);

    assertEquals("WII Herbarium Dataset", resource.getEml().getTitle());
    // data package metadata is not touched
    assertSame(before, resource.getDataPackageMetadata());
    verify(metadataReader, never()).readValue(any(), any());
  }

  @Test
  public void testLoadMetadataOfCamtrapResource() throws Exception {
    Files.createFile(datapackageFile.toPath());
    CamtrapMetadata metadata = new CamtrapMetadata();
    when(metadataReader.readValue(datapackageFile, CamtrapMetadata.class)).thenReturn(metadata);
    Resource resource = resource(CAMTRAP_DP, "https://rs.gbif.org/sandbox/data-packages/camtrap-dp");
    Eml before = resource.getEml();

    loader.loadMetadata(resource);

    assertSame(metadata, resource.getDataPackageMetadata());
    // EML is not loaded for non DwC-DP data packages
    assertSame(before, resource.getEml());
  }

  @Test
  public void testLoadMetadataOfColDpResource() throws Exception {
    Files.createFile(datapackageFile.toPath());
    ColMetadata metadata = new ColMetadata();
    when(metadataReader.readValue(datapackageFile, ColMetadata.class)).thenReturn(metadata);
    Resource resource = resource(COL_DP, "https://rs.gbif.org/data-packages/coldp");

    loader.loadMetadata(resource);

    assertSame(metadata, resource.getDataPackageMetadata());
  }

  @Test
  public void testLoadMetadataOfDwcDpResourceLoadsBothMetadataFiles() throws Exception {
    Files.createFile(datapackageFile.toPath());
    Files.copy(FileUtils.getClasspathFile("data/eml.xml").toPath(), emlFile.toPath());
    FrictionlessMetadata<?, ?, ?> metadata = new FrictionlessMetadata<>();
    when(metadataReader.readValue(datapackageFile, FrictionlessMetadata.class)).thenReturn(metadata);
    Resource resource = resource(DWC_DP, "http://rs.tdwg.org/dwc/dwc-dp");

    loader.loadMetadata(resource);

    assertSame(metadata, resource.getDataPackageMetadata());
    assertEquals("WII Herbarium Dataset", resource.getEml().getTitle());
  }

  @Test
  public void testLoadMissingDatapackageMetadataCreatesDefaultForType() throws Exception {
    Resource camtrap = resource(CAMTRAP_DP, "https://rs.gbif.org/sandbox/data-packages/camtrap-dp");
    loader.loadDatapackageMetadata(camtrap);
    assertInstanceOf(CamtrapMetadata.class, camtrap.getDataPackageMetadata());

    Resource colDp = resource(COL_DP, "https://rs.gbif.org/data-packages/coldp");
    loader.loadDatapackageMetadata(colDp);
    assertInstanceOf(ColMetadata.class, colDp.getDataPackageMetadata());

    Resource dwcDp = resource(DWC_DP, "http://rs.tdwg.org/dwc/dwc-dp");
    loader.loadDatapackageMetadata(dwcDp);
    FrictionlessMetadata<?, ?, ?> dwcDpMetadata =
        assertInstanceOf(FrictionlessMetadata.class, dwcDp.getDataPackageMetadata());
    // name is the shortname
    assertEquals(SHORTNAME, dwcDpMetadata.getName());

    verify(metadataReader, never()).readValue(any(), any());
  }

  @Test
  public void testLoadDatapackageMetadataIgnoresDirectory() throws Exception {
    Files.createDirectory(datapackageFile.toPath());
    Resource resource = resource(DWC_DP, "http://rs.tdwg.org/dwc/dwc-dp");

    loader.loadDatapackageMetadata(resource);

    assertInstanceOf(FrictionlessMetadata.class, resource.getDataPackageMetadata());
    verify(metadataReader, never()).readValue(any(), any());
  }

  @Test
  public void testLoadUnreadableDatapackageMetadataFails() throws Exception {
    Files.createFile(datapackageFile.toPath());
    when(metadataReader.readValue(datapackageFile, CamtrapMetadata.class)).thenThrow(new IOException("broken"));
    Resource resource = resource(CAMTRAP_DP, "https://rs.gbif.org/sandbox/data-packages/camtrap-dp");

    RuntimeException e = assertThrows(RuntimeException.class, () -> loader.loadDatapackageMetadata(resource));
    assertInstanceOf(IOException.class, e.getCause());
  }

  private static Resource resource(String coreType, String dataPackageIdentifier) {
    Resource resource = new Resource();
    resource.setShortname(SHORTNAME);
    resource.setCoreType(coreType);
    resource.setDataPackageIdentifier(dataPackageIdentifier);
    return resource;
  }
}
