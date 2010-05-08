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

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.ArchiveFile;
import org.gbif.dwc.text.UnsupportedArchiveException;
import org.gbif.file.FileUtils;
import org.gbif.provider.service.DataArchiveManager;
import org.gbif.provider.util.ResourceTestBase;

import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.Map.Entry;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class can be used for unit testing {@link DataArchiveManagerImpl}.
 * 
 */
public class DataArchiveManagerImplTest extends ResourceTestBase {

  @Autowired
  private DataArchiveManager dam;

  /**
   * Tests creating and opening an archive without extensions containing a
   * single data file with a header row and a meta.xml file describing the data
   * file.
   */
  @Test
  public void createAndOpenArchive() {
    this.setupOccResource();
    try {
      File location = dam.createArchive(resource);
      assertNotNull(location);
      assertTrue(location.isFile());
      assertTrue(location.canRead());
      Archive archive = dam.openArchive(location, true);
      assertNotNull(archive);
      ArchiveFile core = archive.getCore();
      assertNotNull(core);
      Set<ArchiveFile> extensions = archive.getExtensions();
      assertTrue(extensions.isEmpty());
    } catch (Exception e) {
      fail();
    }
  }

  /**
   * Tests opening different kinds of archives.
   */
  @Test
  public void openArchive() {
    // Tests compressed ZIP and GZIP directory archives:
    open(ImmutableMap.of("zip/archive-dwc.zip", 1, "gzip/archive-dwc.tar.gz", 1));
    // Tests uncompressed directory archive:
    open(ImmutableMap.of("archive-dwc", 1, "dwca", 0));
    // Tests uncompressed metadata file archive:
    open(ImmutableMap.of("archive-dwc/meta.xml", 1));
    // Tests uncompressed data file archive:
    open(ImmutableMap.of("archive-dwc/DarwinCore.txt", 0,
        "DarwinCore-mini.txt", 0));
  }

  /**
   * Opens and tests each archive represented by a map entry where the key is
   * the archive location and the value is the number of extensions expected in
   * the opened archive.
   */
  private void open(ImmutableMap<String, Integer> map) {
    File location;
    Archive archive;
    for (Entry<String, Integer> entry : map.entrySet()) {
      location = FileUtils.getClasspathFile("dwc-archives/" + entry.getKey());
      System.out.println("Testing archive " + location);
      try {
        archive = dam.openArchive(location, true);
        assertTrue(archive.getCore() != null);
        assertTrue(archive.getCore().getId() != null);
        assertTrue(archive.getCore().hasTerm(DwcTerm.scientificName));
        assertTrue(archive.getExtensions().size() == entry.getValue());
      } catch (IOException e) {
        e.printStackTrace();
        System.err.println(location);
      } catch (UnsupportedArchiveException e) {
        e.printStackTrace();
        System.err.println(location);
      }
    }
  }
}