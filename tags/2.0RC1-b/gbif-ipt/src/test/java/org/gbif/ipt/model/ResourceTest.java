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
import org.gbif.ipt.model.Source.FileSource;
import org.gbif.ipt.service.AlreadyExistingException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author markus
 * 
 */
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

    assertTrue(res.getMappings().size() == 4);
    assertTrue(res.getCoreMappings().size() == 2);

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

    assertTrue(res.getMappings().size() == 5);
    assertTrue(res.getCoreMappings().size() == 2);

    // delete first core
    assertTrue(res.deleteMapping(em1));
    assertTrue(res.getMappings().size() == 4);
    assertTrue(res.getCoreMappings().size() == 1);

    // try againt to remove the same ext - should not work
    assertFalse(res.deleteMapping(em1));
    assertTrue(res.getMappings().size() == 4);
    assertTrue(res.getCoreMappings().size() == 1);

    // remove an extension
    assertTrue(res.deleteMapping(em5));
    assertTrue(res.getMappings().size() == 3);
    assertTrue(res.getCoreMappings().size() == 1);

    // remove the last core, should remove all
    assertTrue(res.deleteMapping(em2));
    assertFalse(res.hasCore());
    assertTrue(res.getMappings().size() == 0);
    assertTrue(res.getCoreMappings().size() == 0);
  }

  @Test
  public void testDeleteSource() throws AlreadyExistingException {
    Resource res = getResource();

    Source src1 = new FileSource();
    src1.setName("Peter");
    res.addSource(src1, false);

    Source src2 = new FileSource();
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

    assertTrue(res.getMappings().size() == 3);
    assertTrue(res.getCoreMappings().size() == 1);

    // delete source mapped only to 1 extension
    assertTrue(res.deleteSource(src2));
    assertTrue(res.getMappings().size() == 2);
    assertTrue(res.getCoreMappings().size() == 1);

    // delete other source
    assertTrue(res.deleteSource(src1));
    assertTrue(res.getMappings().size() == 0);
    assertTrue(res.getCoreMappings().size() == 0);
  }

}
