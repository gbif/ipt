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

import org.gbif.dwc.text.UnsupportedArchiveException;
import org.gbif.file.FileUtils;
import org.gbif.provider.service.ResourceArchiveService;
import org.gbif.provider.util.ResourceTestBase;

import java.io.IOException;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class can be used for unit testing {@link ResourceArchiveService}
 * implementations.
 * 
 */
public class ResourceArchiveServiceTest extends ResourceTestBase {

  @Autowired
  private ResourceArchiveService dam;

  /**
   * Test method for
   * {@link IptResourceArchiveService#createArchive(org.gbif.provider.model.Resource)}
   * 
   */
  @Test
  public final void testCreateArchive() {
    // TODO
  }

  /**
   * Test method for
   * {@link IptResourceArchiveService#createResource(org.gbif.provider.service.impl.ResourceArchive)}
   * 
   */
  @Test
  public final void testCreateResource() {
    // TODO
  }

  /**
   * Test method for
   * {@link IptResourceArchiveService#openArchive(java.io.File, boolean)}
   * 
   * @throws UnsupportedArchiveException
   * @throws IOException
   */
  @Test
  public final void testOpenArchive() throws IOException,
      UnsupportedArchiveException {
    ResourceArchive a;

    // ZIP archive with eml, meta, and data files
    a = doOpenArchive("dwc-archives/zip/archive-dwc.zip");
    assertNotNull(a.getEml());
    assertFalse(a.getSourceFiles().isEmpty());
    assertFalse(a.getExtensions().isEmpty());

    // GZIP archive with eml, meta, and data files
    a = doOpenArchive("dwc-archives/gzip/archive-dwc.tar.gz");
    assertNotNull(a.getEml());
    assertFalse(a.getSourceFiles().isEmpty());
    assertFalse(a.getExtensions().isEmpty());

    // Directory with eml, meta, and data files
    a = doOpenArchive("dwc-archives/archive-dwc");
    assertNotNull(a.getEml());
    assertFalse(a.getSourceFiles().isEmpty());
    assertFalse(a.getExtensions().isEmpty());

    // Directory with data file
    a = doOpenArchive("dwc-archives/dwca");
    assertNull(a.getEml());
    assertFalse(a.getSourceFiles().isEmpty());
    assertFalse(a.getExtensions().isEmpty());

    // Single meta file in a directory containing eml, meta, and data files
    a = doOpenArchive("dwc-archives/archive-dwc/meta.xml");
    assertNotNull(a.getEml());
    assertFalse(a.getSourceFiles().isEmpty());
    assertFalse(a.getExtensions().isEmpty());

    // Single data file in a directory containing eml and meta files
    a = doOpenArchive("dwc-archives/archive-dwc/DarwinCore.txt");
    assertNotNull(a.getEml());
    assertFalse(a.getSourceFiles().isEmpty());
    assertFalse(a.getExtensions().isEmpty());

    // Single data file
    a = doOpenArchive("dwc-archives/DarwinCore-mini.txt");
    assertNull(a.getEml());
    assertFalse(a.getSourceFiles().isEmpty());
    assertFalse(a.getExtensions().isEmpty());
  }

  private <T extends ResourceArchive> T doOpenArchive(String location)
      throws IOException, UnsupportedArchiveException {
    return dam.openArchive(FileUtils.getClasspathFile(location), true);
  }
}
