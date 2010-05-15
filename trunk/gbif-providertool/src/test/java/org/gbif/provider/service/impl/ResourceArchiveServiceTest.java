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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.gbif.dwc.text.UnsupportedArchiveException;
import org.gbif.file.FileUtils;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionMapping;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.PropertyMapping;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.SourceFile;
import org.gbif.provider.service.ExtensionManager;
import org.gbif.provider.service.ResourceArchiveManager;
import org.gbif.provider.util.ResourceTestBase;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import java.io.File;
import java.io.IOException;
import java.util.List;

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

  @Autowired
  private ExtensionManager extensionManager;

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
    // FIXME: H2 doesn't have VernacularName extension
    // assertFalse(resource.getExtensionMappings().isEmpty());
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
   * 
   * @throws UnsupportedArchiveException
   * @throws IOException
   */
  @Test
  public final void testOpenArchive() throws IOException,
      UnsupportedArchiveException {
    ResourceArchive a;

    // ZIP archive with eml, meta, and data file.
    // Core: rowType="http://rs.tdwg.org/dwc/terms/DarwinCore"
    // Extension: rowType="http://rs.gbif.org/ipt/terms/1.0/VernacularName"
    // Missing extensions: http://rs.tdwg.org/dwc/terms/TaxonAccordingTo,
    // http://purl.org/dc/terms/source.
    a = doOpenArchive("dwc-archives/zip/archive-dwc.zip");
    assertNotNull(a.getEml());
    assertNotNull(a.getCoreSourceFile());
    assertNotNull(a.getExtensionMapping(a.getCoreSourceFile()));
    assertNotNull(a.getExtensionMapping(a.getCoreSourceFile()).getExtension().isCore());
    assertEquals(a.getExtensionSourceFiles().size(), 1);
    ImmutableMultimap.Builder<String, String> b = ImmutableMultimap.builder();
    ImmutableMultimap<String, String> coreColumns = b.put(
        "http://rs.tdwg.org/dwc/terms/DarwinCore", "taxonID".toLowerCase()).put(
        "http://rs.tdwg.org/dwc/terms/DarwinCore",
        "scientificName".toLowerCase()).put(
        "http://rs.tdwg.org/dwc/terms/DarwinCore",
        "higherTaxonID".toLowerCase()).put(
        "http://rs.tdwg.org/dwc/terms/DarwinCore", "taxonRank".toLowerCase()).put(
        "http://rs.tdwg.org/dwc/terms/DarwinCore",
        "nomenclaturalCode".toLowerCase()).put(
        "http://rs.tdwg.org/dwc/terms/DarwinCore", "basionymID".toLowerCase()).put(
        "http://rs.tdwg.org/dwc/terms/DarwinCore",
        "taxonomicStatus".toLowerCase()).put(
        "http://rs.tdwg.org/dwc/terms/DarwinCore",
        "nomenclaturalStatus".toLowerCase()).put(
        "http://rs.tdwg.org/dwc/terms/DarwinCore",
        "acceptedTaxonID".toLowerCase()).put(
        "http://rs.tdwg.org/dwc/terms/DarwinCore",
        "namePublishedIn".toLowerCase()).build();
    ImmutableMultimap<String, String> extensionColumns = b.put(
        "http://rs.gbif.org/ipt/terms/1.0/VernacularName",
        "vernacularName".toLowerCase()).put(
        "http://rs.gbif.org/ipt/terms/1.0/VernacularName",
        "languageCode".toLowerCase()).build();
    checkCorePropertyMappings(a, coreColumns);
    checkExtensionPropertyMappings(a, extensionColumns);
    checkExtensions(a);

    if (true) {
      return;
    }

    // GZIP archive with eml, meta, and data file
    a = doOpenArchive("dwc-archives/gzip/archive-dwc.tar.gz");
    assertNotNull(a.getEml());
    assertNotNull(a.getCoreSourceFile());
    assertNotNull(a.getExtensionMapping(a.getCoreSourceFile()));
    assertNotNull(a.getExtensionMapping(a.getCoreSourceFile()).getExtension().isCore());
    assertEquals(a.getExtensionSourceFiles().size(), 1);
    checkExtensions(a);

    // Directory with eml, meta, and data file
    a = doOpenArchive("dwc-archives/archive-dwc");
    assertNotNull(a.getEml());
    assertNotNull(a.getCoreSourceFile());
    assertNotNull(a.getExtensionMapping(a.getCoreSourceFile()));
    assertNotNull(a.getExtensionMapping(a.getCoreSourceFile()).getExtension().isCore());
    assertFalse(a.getExtensionSourceFiles().isEmpty());

    // Directory with data file
    a = doOpenArchive("dwc-archives/dwca");
    assertNull(a.getEml());
    assertNotNull(a.getCoreSourceFile());
    assertNotNull(a.getExtensionMapping(a.getCoreSourceFile()));
    assertTrue(a.getExtensionSourceFiles().isEmpty());

    // Single meta file in a directory containing eml, meta, and data file
    a = doOpenArchive("dwc-archives/archive-dwc/meta.xml");
    assertNotNull(a.getEml());
    assertNotNull(a.getCoreSourceFile());
    assertNotNull(a.getExtensionMapping(a.getCoreSourceFile()));
    assertFalse(a.getExtensionSourceFiles().isEmpty());

    // Single data file in a directory containing eml and meta file
    a = doOpenArchive("dwc-archives/archive-dwc/DarwinCore.txt");
    assertNotNull(a.getEml());
    assertNotNull(a.getCoreSourceFile());
    assertNotNull(a.getExtensionMapping(a.getCoreSourceFile()));
    assertNotNull(a.getExtensionMapping(a.getCoreSourceFile()).getExtension().isCore());
    // FIXME: H2 doesn't have VernacularName extension
    // assertFalse(a.getExtensionSourceFiles().isEmpty());

    // Single data file
    a = doOpenArchive("dwc-archives/DarwinCore-mini.txt");
    assertNull(a.getEml());
    assertNotNull(a.getCoreSourceFile());
    assertNotNull(a.getExtensionMapping(a.getCoreSourceFile()));
    assertNotNull(a.getExtensionMapping(a.getCoreSourceFile()).getExtension().isCore());
    // FIXME: H2 doesn't have VernacularName extension
    assertFalse(a.getExtensionSourceFiles().isEmpty());

  }

  private void checkCorePropertyMappings(ResourceArchive a,
      Multimap<String, String> columns) {
    Extension e;
    String rowType;
    List<String> cols;
    SourceFile coreSource = a.getCoreSourceFile();
    e = a.getExtensionMapping(coreSource).getExtension();
    cols = Lists.newArrayList();
    for (PropertyMapping pm : a.getExtensionMapping(coreSource).getPropertyMappingsSorted()) {
      cols.add(pm.getColumn().toLowerCase());
    }
    rowType = e.getNamespace() + e.getName();
    assertTrue(cols.containsAll(columns.get(rowType)));
  }

  private void checkExtensionPropertyMappings(ResourceArchive a,
      Multimap<String, String> columns) {
    Extension e;
    String rowType;
    List<String> cols;
    for (SourceFile source : a.getExtensionSourceFiles()) {
      e = a.getExtensionMapping(source).getExtension();
      cols = Lists.newArrayList();
      for (PropertyMapping pm : a.getExtensionMapping(source).getPropertyMappingsSorted()) {
        cols.add(pm.getColumn().toLowerCase());
      }
      rowType = e.getNamespace() + e.getName();
      assertTrue(cols.containsAll(columns.get(rowType)));
    }
  }

  /**
   * @param a void
   */
  private void checkExtensions(ResourceArchive a) {
    Extension sourceExtension, h2Extension;
    String rowType;
    // Checks core extension:
    sourceExtension = a.getExtensionMapping(a.getCoreSourceFile()).getExtension();
    rowType = sourceExtension.getNamespace() + sourceExtension.getName();
    h2Extension = extensionManager.getExtensionByRowType(rowType);
    assertEquals(sourceExtension, h2Extension);
    // Checks extensions:
    for (SourceFile source : a.getExtensionSourceFiles()) {
      sourceExtension = a.getExtensionMapping(source).getExtension();
      rowType = sourceExtension.getNamespace() + sourceExtension.getName();
      h2Extension = extensionManager.getExtensionByRowType(rowType);
      assertEquals(sourceExtension, h2Extension);
    }
  }

  private <T extends ResourceArchive> T doOpenArchive(String location)
      throws IOException, UnsupportedArchiveException {
    File f = FileUtils.getClasspathFile(location);
    return ras.openArchive(f, true);
  }
}
