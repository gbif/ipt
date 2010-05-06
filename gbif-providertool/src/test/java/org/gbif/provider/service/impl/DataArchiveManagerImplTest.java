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

import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.ArchiveFile;
import org.gbif.provider.service.DataArchiveManager;
import org.gbif.provider.util.ResourceTestBase;

import java.io.File;
import java.util.Set;

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
  public void archive() {
    this.setupOccResource();
    try {
      File location = dam.createArchive(resource);
      assertNotNull(location);
      assertTrue(location.isFile());
      assertTrue(location.canRead());
      Archive archive = dam.openArchive(location);
      assertNotNull(archive);
      ArchiveFile core = archive.getCore();
      assertNotNull(core);
      Set<ArchiveFile> extensions = archive.getExtensions();
      assertTrue(extensions.isEmpty());
    } catch (Exception e) {
      fail();
    }
  }
}