/*
 * Copyright 2009 GBIF.
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
package org.gbif.provider.model;

import org.gbif.provider.model.factory.ResourceFactory;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.service.OccResourceManager;
import org.gbif.provider.util.Constants;

import org.appfuse.dao.BaseDaoTestCase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * TODO: Documentation.
 * 
 */
public class ExtensionTest extends BaseDaoTestCase {
  @Autowired
  @Qualifier("extensionManager")
  private GenericManager<Extension> extensionManager;
  @Autowired
  private OccResourceManager occResourceManager;
  @Autowired
  private ResourceFactory resourceFactory;

  public void setResourceFactory(ResourceFactory resourceFactory) {
    this.resourceFactory = resourceFactory;
  }

  @Test
  public void testExtensionMap() {
    OccurrenceResource occRes = resourceFactory.newOccurrenceResourceInstance();

    Extension ext1 = new Extension();
    ext1.setName("testExtensionMap 1");
    ext1 = extensionManager.save(ext1);
    ExtensionMapping map1 = new ExtensionMapping();
    map1.setExtension(ext1);
    occRes.addExtensionMapping(map1);

    Extension ext2 = new Extension();
    ext2.setName("testExtensionMap 2");
    ext2 = extensionManager.save(ext2);
    ExtensionMapping map2 = new ExtensionMapping();
    map2.setExtension(ext2);
    occRes.addExtensionMapping(map2);

    Long occId = occResourceManager.save(occRes).getId();

    // check retrieved data. what about the hibernate cache?
    DataResource res = occResourceManager.get(occId);
    Collection<ExtensionMapping> allMappings = res.getAllMappings();

    assertTrue(res.getAllMappings().size() == 3);
    assertTrue(res.getExtensionMappings().size() == 2);
    assertTrue(res.getCoreMapping().getExtension().getId().equals(
        Constants.DARWIN_CORE_EXTENSION_ID));
    // the core mapping should not be in the extension mappings map
    assertFalse(res.getExtensionMappings().contains(res.getCoreMapping()));
    // but in all mappings it should:
    assertTrue(res.getAllMappings().contains(res.getCoreMapping()));

    for (Long i : res.getExtensionMappingsMap().keySet()) {
      Extension e = res.getExtensionMappingsMap().get(i).getExtension();
      Long i2 = e.getId();
      assertTrue(i.equals(i2));
    }
  }

  @Test
  public void testExtensionPropertyList() throws Exception {
    Extension extension = new Extension();
    extension.setName("testExtensionPropertyList");
    ExtensionProperty propMap = new ExtensionProperty();
    extension.addProperty(propMap);
    extension = extensionManager.save(extension);
    // check dwc, checklist and inserted extensions
    for (Long extId : Arrays.asList(Constants.DARWIN_CORE_EXTENSION_ID,
        extension.getId())) {
      Extension ext = extensionManager.get(extId);
      List<ExtensionProperty> props = ext.getProperties();
      assertFalse(props.isEmpty());
    }
  }

}
