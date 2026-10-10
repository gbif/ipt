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
import org.gbif.ipt.model.PhylogeneticTreeFile;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.InvalidFilenameException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PhylogeneticTreeManagerImplTest {

  private static final String TREE = "(Fabales_Fabaceae_Paramacrolobium_coeruleum:0.1,(B:0.2,C:0.3):0.4);";

  private static final String META = """
    <archive xmlns="http://rs.tdwg.org/dwc/text/" metadata="eml.xml">
      <core encoding="UTF-8" fieldsTerminatedBy="\\t" linesTerminatedBy="\\n" fieldsEnclosedBy="" ignoreHeaderLines="1" rowType="http://rs.tdwg.org/dwc/terms/Occurrence">
        <files><location>occurrence.txt</location></files>
        <id index="0"/>
        <field index="1" term="http://rs.tdwg.org/dwc/terms/occurrenceID"/>
        <field index="2" term="http://rs.tdwg.org/dwc/terms/catalogNumber"/>
        <field index="3" term="http://rs.tdwg.org/dwc/terms/scientificName"/>
      </core>
      <extension encoding="UTF-8" fieldsTerminatedBy="\\t" linesTerminatedBy="\\n" fieldsEnclosedBy="" ignoreHeaderLines="1" rowType="http://rs.gbif.org/terms/1.0/PhylogeneticMaterialCitation">
        <files><location>phylogeneticmaterialcitation.txt</location></files>
        <coreid index="0"/>
        <field index="1" term="http://rs.gbif.org/terms/1.0/phylogeneticTreeTipLabel"/>
        <field index="2" term="http://rs.gbif.org/terms/1.0/phylogeneticTreeFileName"/>
      </extension>
    </archive>
    """;

  private static final String OCCURRENCES = """
    id\toccurrenceID\tcatalogNumber\tscientificName
    o1\to1\tK-001\tParamacrolobium coeruleum
    o2\to2\tK-002\tParamacrolobium coeruleum
    o3\to3\tK-003\tB-us
    o4\to4\tK-004\tNot cited
    """;

  private static final String CITATIONS = """
    coreid\tphylogeneticTreeTipLabel\tphylogeneticTreeFileName
    o1\tFabales_Fabaceae_Paramacrolobium_coeruleum\tglobal_tree.nwk
    o2\tFabales_Fabaceae_Paramacrolobium_coeruleum\tglobal_tree.nwk
    o3\tB\tglobal_tree.nwk
    o4\tC\tanother_tree.nwk
    """;

  @TempDir
  Path tmp;

  private DataDir dataDir;
  private PhylogeneticTreeManagerImpl manager;
  private Resource resource;

  @BeforeEach
  public void setup() {
    dataDir = DataDir.buildFromString(tmp.toString());
    manager = new PhylogeneticTreeManagerImpl(dataDir);
    resource = new Resource();
    resource.setShortname("res");
  }

  @Test
  public void testAllowedFileNames() {
    assertTrue(manager.isAllowedFileName("global_tree.nex"));
    assertTrue(manager.isAllowedFileName("tree-1.NWK"));
    assertTrue(manager.isAllowedFileName("tree.phyloxml"));
    assertFalse(manager.isAllowedFileName("../tree.nwk"));
    assertFalse(manager.isAllowedFileName("my tree.nwk"));
    assertFalse(manager.isAllowedFileName("tree.txt"));
    assertFalse(manager.isAllowedFileName("eml.xml"));
    assertFalse(manager.isAllowedFileName("META.xml"));
    assertFalse(manager.isAllowedFileName(null));
  }

  @Test
  public void testAddListDelete() throws Exception {
    File upload = tmp.resolve("upload.tmp").toFile();
    Files.writeString(upload.toPath(), TREE);

    assertThrows(InvalidFilenameException.class, () -> manager.add(resource, upload, "../evil.nwk"));

    manager.add(resource, upload, "global_tree.nwk");
    List<PhylogeneticTreeFile> trees = manager.list(resource);
    assertEquals(1, trees.size());
    assertEquals("global_tree.nwk", trees.get(0).getName());
    assertEquals(TREE.length(), trees.get(0).getSize());
    assertNotNull(manager.get(resource, "global_tree.nwk"));
    assertNull(manager.get(resource, "missing.nwk"));

    assertTrue(manager.delete(resource, "global_tree.nwk"));
    assertFalse(manager.delete(resource, "global_tree.nwk"));
    assertTrue(manager.list(resource).isEmpty());
  }

  @Test
  public void testPublished() throws Exception {
    BigDecimal version = new BigDecimal("1.0");
    writeDwca(version, Map.of(
      "meta.xml", META,
      "eml.xml", "<eml/>",
      "occurrence.txt", OCCURRENCES,
      "phylogeneticmaterialcitation.txt", CITATIONS,
      "global_tree.nwk", TREE));

    assertEquals(List.of("global_tree.nwk"), manager.listPublished(resource, version));
    assertArrayEquals(TREE.getBytes(StandardCharsets.UTF_8), manager.readPublished(resource, version, "global_tree.nwk"));
    assertNull(manager.readPublished(resource, version, "eml.xml"));
    assertTrue(manager.listPublished(resource, new BigDecimal("2.0")).isEmpty());

    String table = manager.tipMetadataTable(resource, version, "global_tree.nwk");
    assertNotNull(table);
    List<String> rows = table.lines().toList();
    assertEquals("tip\tLinked specimen\toccurrenceID\tcatalogNumber\tscientificName", rows.get(0));
    // two specimens cite the same tip
    assertTrue(rows.contains(
      "Fabales_Fabaceae_Paramacrolobium_coeruleum\tyes\to1 | o2\tK-001 | K-002\tParamacrolobium coeruleum"));
    assertTrue(rows.contains("B\tyes\to3\tK-003\tB-us"));
    // o4 cites another tree
    assertEquals(3, rows.size());

    assertNull(manager.tipMetadataTable(resource, version, "uncited.nwk"));
  }

  private void writeDwca(BigDecimal version, Map<String, String> entries) throws IOException {
    File dwca = dataDir.resourceDwcaFile(resource.getShortname(), version);
    dwca.getParentFile().mkdirs();
    try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(dwca))) {
      for (Map.Entry<String, String> entry : entries.entrySet()) {
        zip.putNextEntry(new ZipEntry(entry.getKey()));
        zip.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
      }
    }
  }
}
