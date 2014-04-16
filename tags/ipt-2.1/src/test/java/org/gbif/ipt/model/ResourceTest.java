/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
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
 ***************************************************************************/

package org.gbif.ipt.model;

import org.gbif.ipt.config.Constants;
import org.gbif.ipt.service.AlreadyExistingException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ResourceTest {

  private final Extension OCC;
  private final Extension EXT;

  public ResourceTest() {
    OCC = new Extension();
    OCC.setInstalled(true);
    OCC.setName("Occurrence Core");
    OCC.setTitle("Occurrence Core");
    OCC.setRowType(Constants.DWC_ROWTYPE_OCCURRENCE);

    EXT = new Extension();
    EXT.setInstalled(true);
    EXT.setName("Occurrence Extension");
    EXT.setTitle("Occurrence Extension");
    EXT.setRowType("http://rs.gbif.org/my/extension/test");
  }

  private ExtensionMapping getExtExtensionMapping() {
    ExtensionMapping mapping = new ExtensionMapping();
    mapping.setExtension(EXT);
    return mapping;
  }

  private ExtensionMapping getOccExtensionMapping() {
    ExtensionMapping mapping = new ExtensionMapping();
    mapping.setExtension(OCC);
    return mapping;
  }

  private Resource getResource() {
    Resource res = new Resource();
    res.setTitle("Test Resource");
    res.setShortname("test");
    return res;
  }

  @Test
  public void testAddMapping() {
    Resource res = getResource();
    boolean failed = false;
    try {
      res.addMapping(getExtExtensionMapping());
    } catch (IllegalArgumentException e) {
      failed = true;
    }
    // cant add an extension without having a core
    assertTrue(failed);

    res.addMapping(getOccExtensionMapping());
    res.addMapping(getExtExtensionMapping());
    res.addMapping(getOccExtensionMapping());
    res.addMapping(getExtExtensionMapping());

    assertEquals(4, res.getMappings().size());
    assertEquals(2, res.getCoreMappings().size());

  }

  @Test
  public void testDeleteMapping() {
    Resource res = getResource();
    ExtensionMapping em1 = getOccExtensionMapping();
    ExtensionMapping em2 = getOccExtensionMapping();
    ExtensionMapping em3 = getExtExtensionMapping();
    ExtensionMapping em4 = getExtExtensionMapping();
    ExtensionMapping em5 = getExtExtensionMapping();

    res.addMapping(em1);
    res.addMapping(em2);
    res.addMapping(em3);
    res.addMapping(em4);
    res.addMapping(em5);

    assertEquals(5, res.getMappings().size());
    assertEquals(2, res.getCoreMappings().size());

    // delete first core
    assertTrue(res.deleteMapping(em1));
    assertEquals(4, res.getMappings().size());
    assertEquals(1, res.getCoreMappings().size());

    // try againt to remove the same ext - should not work
    assertFalse(res.deleteMapping(em1));
    assertEquals(4, res.getMappings().size());
    assertEquals(1, res.getCoreMappings().size());

    // remove an extension
    assertTrue(res.deleteMapping(em5));
    assertEquals(3, res.getMappings().size());
    assertEquals(1, res.getCoreMappings().size());

    // remove the last core, should remove all
    assertTrue(res.deleteMapping(em2));
    assertFalse(res.hasCore());
    assertEquals(0, res.getMappings().size());
    assertEquals(0, res.getCoreMappings().size());
  }

  @Test
  public void testDeleteSource() throws AlreadyExistingException {
    Resource res = getResource();

    Source src1 = new TextFileSource();
    src1.setName("Peter");
    res.addSource(src1, false);

    Source src2 = new TextFileSource();
    src2.setName("Carla");
    res.addSource(src2, false);

    // add 3 mappings
    ExtensionMapping emOcc = getOccExtensionMapping();
    emOcc.setSource(src1);
    ExtensionMapping emE1 = getExtExtensionMapping();
    emE1.setSource(src1);
    ExtensionMapping emE2 = getExtExtensionMapping();
    emE2.setSource(src2);

    res.addMapping(emOcc);
    res.addMapping(emE1);
    res.addMapping(emE2);

    assertEquals(3, res.getMappings().size());
    assertEquals(1, res.getCoreMappings().size());

    // delete source mapped only to 1 extension
    assertTrue(res.deleteSource(src2));
    assertEquals(2, res.getMappings().size());
    assertEquals(1, res.getCoreMappings().size());

    // delete other source
    assertTrue(res.deleteSource(src1));
    assertEquals(0, res.getMappings().size());
    assertEquals(0, res.getCoreMappings().size());
  }

  @Test
  public void testCoreRowTypeSet() {
    // create test resource
    Resource resource = new Resource();
    // add mapping to taxon core
    ExtensionMapping mapping = new ExtensionMapping();
    Extension ext = new Extension();
    ext.setRowType(Constants.DWC_ROWTYPE_TAXON);
    mapping.setExtension(ext);
    resource.addMapping(mapping);
    // assert correct core row type has been determined from core mapping
    assertEquals(Constants.DWC_ROWTYPE_TAXON, resource.getCoreRowType());
  }

}
