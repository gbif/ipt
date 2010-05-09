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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.UnsupportedArchiveException;
import org.gbif.file.FileUtils;
import org.gbif.provider.service.DataArchiveManager;
import org.gbif.provider.util.ResourceTestBase;

import java.io.IOException;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class can be used for unit testing {@link DataArchiveManager}
 * implementations.
 * 
 */
public class DataArchiveManagerTest extends ResourceTestBase {

  @Autowired
  private DataArchiveManager dam;

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.DataArchiveManagerImpl#createArchive(org.gbif.provider.model.DataResource)}
   * 
   * @throws IOException
   * @throws UnsupportedArchiveException
   */
  @Test
  public final void createArchive() throws IOException,
      UnsupportedArchiveException {
    setupOccResource();
    dam.openArchive(dam.createArchive(resource), true);
  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.DataArchiveManagerImpl#getCoreSourceFile(org.gbif.dwc.text.Archive)}
   * 
   * @throws UnsupportedArchiveException
   * @throws IOException
   */
  @Test
  public final void getCoreSourceFile() throws IOException,
      UnsupportedArchiveException {
    // ZIP archive with eml, meta, and data files
    testSourceFile(doOpenArchive("dwc-archives/zip/archive-dwc.zip"));
    // GZIP archive with eml, meta, and data files
    testSourceFile(doOpenArchive("dwc-archives/gzip/archive-dwc.tar.gz"));
    // Directory with eml, meta, and data files
    testSourceFile(doOpenArchive("dwc-archives/archive-dwc"));
    // Directory with data file
    testSourceFile(doOpenArchive("dwc-archives/dwca"));
    // Single meta file
    testSourceFile(doOpenArchive("dwc-archives/archive-dwc/meta.xml"));
    // Single data file
    testSourceFile(doOpenArchive("dwc-archives/archive-dwc/DarwinCore.txt"));
    // Single data file
    testSourceFile(doOpenArchive("dwc-archives/DarwinCore-mini.txt"));
  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.DataArchiveManagerImpl#getExtensions(org.gbif.dwc.text.Archive)}
   * 
   * @throws UnsupportedArchiveException
   * @throws IOException
   */
  @Test
  public final void getExtensions() throws IOException,
      UnsupportedArchiveException {
    // ZIP archive with eml, meta, and data files
    testExtensions(doOpenArchive("dwc-archives/zip/archive-dwc.zip"), 1);
    // GZIP archive with eml, meta, and data files
    testExtensions(doOpenArchive("dwc-archives/gzip/archive-dwc.tar.gz"), 1);
    // Directory with eml, meta, and data files
    testExtensions(doOpenArchive("dwc-archives/archive-dwc"), 1);
    // Directory with data file
    testExtensions(doOpenArchive("dwc-archives/dwca"), 0);
    // Single meta file
    testExtensions(doOpenArchive("dwc-archives/archive-dwc/meta.xml"), 1);
    // Single data file
    testExtensions(doOpenArchive("dwc-archives/archive-dwc/DarwinCore.txt"), 0);
    // Single data file
    testExtensions(doOpenArchive("dwc-archives/DarwinCore-mini.txt"), 0);
  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.DataArchiveManagerImpl#openArchive(java.io.File, boolean)}
   * 
   * @throws UnsupportedArchiveException
   * @throws IOException
   */
  @Test
  public final void openArchive() throws IOException,
      UnsupportedArchiveException {
    // ZIP archive with eml, meta, and data files
    doOpenArchive("dwc-archives/zip/archive-dwc.zip");
    // GZIP archive with eml, meta, and data files
    doOpenArchive("dwc-archives/gzip/archive-dwc.tar.gz");
    // Directory with eml, meta, and data files
    doOpenArchive("dwc-archives/archive-dwc");
    // Directory with data file
    doOpenArchive("dwc-archives/dwca");
    // Single meta file
    doOpenArchive("dwc-archives/archive-dwc/meta.xml");
    // Single data file
    doOpenArchive("dwc-archives/archive-dwc/DarwinCore.txt");
    // Single data file
    doOpenArchive("dwc-archives/DarwinCore-mini.txt");
  }

  private Archive doOpenArchive(String location) throws IOException,
      UnsupportedArchiveException {
    Archive a = dam.openArchive(FileUtils.getClasspathFile(location), true);
    assertTrue(a.getCore() != null);
    assertTrue(a.getCore().getId() != null);
    assertTrue(a.getCore().hasTerm(DwcTerm.scientificName));
    return a;
  }

  private void testExtensions(Archive archive, int extensionCount)
      throws IOException {
    assertEquals(archive.getExtensions().size(), extensionCount);
    assertEquals(dam.getExtensions(archive).size(), extensionCount);
  }

  private void testSourceFile(Archive archive) throws IOException {
    assertEquals(dam.getCoreSourceFile(archive).getFilename(),
        archive.getCore().getTitle());
  }
}
