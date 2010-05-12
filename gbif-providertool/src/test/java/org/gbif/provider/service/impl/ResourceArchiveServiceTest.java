/*
 * Copyright 2010 Global Biodiversity Informatics Facility.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gbif.provider.service.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.gbif.dwc.text.UnsupportedArchiveException;
import org.gbif.file.FileUtils;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.service.ResourceArchiveManager;
import org.gbif.provider.util.ResourceTestBase;

import java.io.IOException;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class can be used for unit testing {@link ResourceArchiveManager}
 * implementations.
 * 
 */
public class ResourceArchiveServiceTest extends ResourceTestBase {

  @Autowired
  private ResourceArchiveManager ras;

  @Test
  public final void testBind() throws IOException, UnsupportedArchiveException {
    OccurrenceResource resource = getResourceMock();
    resource.setId(null);
    ResourceArchive archive = doOpenArchive("dwc-archives/zip/archive-dwc.zip");
    resource = ras.bind(resource, archive);
    assertNotNull(resource.getCoreMapping());
    assertNotNull(resource.getCoreMapping().getSource());
    assertNotNull(resource.getExtensionMappings());
    for (ExtensionMapping m : resource.getExtensionMappings()) {
      assertNotNull(m.getSource());
    }
    assertFalse(resource.getExtensionMappings().isEmpty());
  }

  /**
   * Test method for
   * {@link ResourceArchiveManagerImpl#createArchive(org.gbif.provider.model.Resource)}
   * 
   */
  @Test
  public final void testCreateArchive() {
    // TODO
  }

  /**
   * Test method for
   * {@link ResourceArchiveManagerImpl#createResource(org.gbif.provider.service.impl.ResourceArchive)}
   * 
   * @throws UnsupportedArchiveException
   * @throws IOException
   * 
   */
  @Test
  public final void testCreateResource() throws IOException,
      UnsupportedArchiveException {

    // EML only:
    ResourceArchive a = doOpenArchive("dwc-archives/eml/eml.xml");
    assertNotNull(a.getEml());
    Resource resource = ras.createResource(a);
    assertNotNull(resource);
    assertNotNull(resource.getId());

    // EML only:
    a = doOpenArchive("dwc-archives/zip/archive-dwc.zip");
    assertNotNull(a.getEml());
    DataResource dr = ras.createResource(a);
    assertNotNull(dr);
    assertNotNull(dr.getId());
    assertNotNull(dr.getExtensionMappingsMap());
  }

  /**
   * Test method for
   * {@link ResourceArchiveManagerImpl#openArchive(java.io.File, boolean)}
   * 
   * @throws UnsupportedArchiveException
   * @throws IOException
   */
  @Test
  public final void testOpenArchive() throws IOException,
      UnsupportedArchiveException {
    ResourceArchive a;

    // ZIP archive with eml, meta, and data file
    a = doOpenArchive("dwc-archives/zip/archive-dwc.zip");
    assertNotNull(a.getEml());
    assertNotNull(a.getCoreSourceFile());
    assertFalse(a.getExtensionSourceFiles().isEmpty());

    // GZIP archive with eml, meta, and data file
    a = doOpenArchive("dwc-archives/gzip/archive-dwc.tar.gz");
    assertNotNull(a.getEml());
    assertNotNull(a.getCoreSourceFile());
    assertFalse(a.getExtensionSourceFiles().isEmpty());

    // Directory with eml, meta, and data file
    a = doOpenArchive("dwc-archives/archive-dwc");
    assertNotNull(a.getEml());
    assertNotNull(a.getCoreSourceFile());
    assertFalse(a.getExtensionSourceFiles().isEmpty());

    // Directory with data file
    a = doOpenArchive("dwc-archives/dwca");
    assertNull(a.getEml());
    assertNotNull(a.getCoreSourceFile());
    assertTrue(a.getExtensionSourceFiles().isEmpty());

    // Single meta file in a directory containing eml, meta, and data file
    a = doOpenArchive("dwc-archives/archive-dwc/meta.xml");
    assertNotNull(a.getEml());
    assertNotNull(a.getCoreSourceFile());
    assertFalse(a.getExtensionSourceFiles().isEmpty());

    // Single data file in a directory containing eml and meta file
    a = doOpenArchive("dwc-archives/archive-dwc/DarwinCore.txt");
    assertNotNull(a.getEml());
    assertNotNull(a.getCoreSourceFile());
    // FIXME(duplicate H2 issue):
    // assertFalse(a.getSourceFilesForExtensions().isEmpty());

    // Single data file
    a = doOpenArchive("dwc-archives/DarwinCore-mini.txt");
    assertNull(a.getEml());
    assertNotNull(a.getCoreSourceFile());
    // FIXME(duplicate H2 issue):
    // assertFalse(a.getSourceFilesForExtensions().isEmpty());

  }

  private <T extends ResourceArchive> T doOpenArchive(String location)
      throws IOException, UnsupportedArchiveException {
    return ras.openArchive(FileUtils.getClasspathFile(location), true);
  }
}
