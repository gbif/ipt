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
import org.gbif.ipt.mock.MockDataDir;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.voc.PublicationStatus;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import org.gbif.ipt.service.manage.ResourceVersioningService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResourceVersioningServiceImplTest {

  private final DataDir mockedDataDir = MockDataDir.buildMock();

  private final ResourceVersioningService resourceVersioningService = new ResourceVersioningServiceImpl(mockedDataDir);

  @Test
  public void testConvertVersion() throws Exception {
    Resource r = new Resource();
    r.setMetadataVersion(BigDecimal.valueOf(4));
    assertEquals(0, r.getEmlVersion().scale());
    assertEquals(4, r.getEmlVersion().intValueExact());
    // do conversion 4 -> 4.0
    BigDecimal converted = resourceVersioningService.convertVersion(r);
    assertEquals(new BigDecimal("4.0"), converted);
    // ensure conversions aren't repeated
    r.setMetadataVersion(converted);
    assertNull(resourceVersioningService.convertVersion(r));
  }

  @Test
  public void testConvertVersionZero() throws Exception {
    Resource r = new Resource();
    r.setMetadataVersion(BigDecimal.valueOf(0));
    assertEquals(0, r.getEmlVersion().scale());
    assertEquals(0, r.getEmlVersion().intValueExact());
    // do conversion 0 -> 1.0
    BigDecimal converted = resourceVersioningService.convertVersion(r);
    assertEquals(new BigDecimal("1.0"), converted);
    // ensure conversions aren't repeated
    r.setMetadataVersion(converted);
    assertNull(resourceVersioningService.convertVersion(r));
  }

  @Test
  public void testConstructVersionHistoryForLastPublishedVersion() throws Exception {
    Resource r = new Resource();
    r.setMetadataVersion(new BigDecimal("4.0"));
    r.setStatus(PublicationStatus.PUBLIC);
    r.setRecordsPublished(100);
    Date lastPublished = new Date();
    r.setLastPublished(lastPublished);

    VersionHistory history = resourceVersioningService.constructVersionHistoryForLastPublishedVersion(r);
    assertNotNull(history);
    assertEquals("4.0", history.getVersion());
    assertEquals(lastPublished, history.getReleased());
    assertEquals(PublicationStatus.PUBLIC, history.getPublicationStatus());
    assertEquals(100, history.getRecordsPublished());

    // properties aren't set
    assertNull(history.getDoi());
    assertNull(history.getStatus());
    assertNull(history.getChangeSummary());
    assertNull(history.getModifiedBy());

    // next version?
    assertEquals("4.1", r.getNextVersion().toPlainString());
  }

  @Test
  public void testRestoreDwcDpVersionedEmlFiles(@TempDir Path tempDir) throws Exception {
    DataDir dataDir = mock(DataDir.class);
    when(dataDir.resourceEmlFile(anyString(), any(BigDecimal.class))).thenAnswer(invocation ->
        tempDir.resolve("eml-" + invocation.getArgument(1, BigDecimal.class).toPlainString() + ".xml").toFile());
    ResourceVersioningService service = new ResourceVersioningServiceImpl(dataDir);

    Resource r = new Resource();
    r.setShortname("dwc-dp-resource");
    r.setDataPackageIdentifier("http://rs.tdwg.org/dwc/dwc-dp");
    r.addVersionHistory(new VersionHistory(new BigDecimal("8"), PublicationStatus.PUBLIC));
    r.addVersionHistory(new VersionHistory(new BigDecimal("9"), PublicationStatus.PUBLIC));

    // eml-8.xml is fine, eml-9.xml was wrongly converted to eml-9.0.xml
    File eml8 = Files.writeString(tempDir.resolve("eml-8.xml"), "v8").toFile();
    File eml9 = tempDir.resolve("eml-9.xml").toFile();
    File eml90 = Files.writeString(tempDir.resolve("eml-9.0.xml"), "v9").toFile();

    service.restoreDwcDpVersionedEmlFiles(r);

    assertTrue(eml8.exists());
    assertTrue(eml9.exists());
    assertEquals("v9", Files.readString(eml9.toPath()));
    assertFalse(eml90.exists());
  }

  @Test
  public void testRestoreDwcDpVersionedEmlFilesKeepsExistingFile(@TempDir Path tempDir) throws Exception {
    DataDir dataDir = mock(DataDir.class);
    when(dataDir.resourceEmlFile(anyString(), any(BigDecimal.class))).thenAnswer(invocation ->
        tempDir.resolve("eml-" + invocation.getArgument(1, BigDecimal.class).toPlainString() + ".xml").toFile());
    ResourceVersioningService service = new ResourceVersioningServiceImpl(dataDir);

    Resource r = new Resource();
    r.setShortname("dwc-dp-resource");
    r.setDataPackageIdentifier("http://rs.tdwg.org/dwc/dwc-dp");
    r.addVersionHistory(new VersionHistory(new BigDecimal("9"), PublicationStatus.PUBLIC));

    // both exist: don't overwrite eml-9.xml
    File eml9 = Files.writeString(tempDir.resolve("eml-9.xml"), "current").toFile();
    File eml90 = Files.writeString(tempDir.resolve("eml-9.0.xml"), "converted").toFile();

    service.restoreDwcDpVersionedEmlFiles(r);

    assertEquals("current", Files.readString(eml9.toPath()));
    assertTrue(eml90.exists());
  }
}
