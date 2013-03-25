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
package org.gbif.provider.service.util;

import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.factory.ExtensionFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.util.Set;

/**
 * TODO: Documentation.
 * 
 */
public class ExtensionFactoryTest {

  @Test
  public void testBuild() {
    try {
      Extension e = ExtensionFactory.build(
          ExtensionFactoryTest.class.getResourceAsStream("/extensions/vernacularName.xml"),
          null);
      assertEquals("Vernacular Name", e.getTitle());
      assertEquals("VernacularName", e.getName());
      assertEquals("http://rs.gbif.org/ecat/class/", e.getNamespace());
      assertNull(e.getLink());

      assertNotNull(e.getProperties());
      assertEquals(5, e.getProperties().size());
      for (ExtensionProperty p : e.getProperties()) {
        if (p.getName().equalsIgnoreCase("language")) {
          assertEquals("http://purl.org/dc/terms/", p.getNamespace());
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void testBuildFromServer() {
    try {
      ExtensionFactory ef = new ExtensionFactory();
      ef.setThesaurusManager(new MockThesaurusManager());
      Extension e = ef.build("http://gbrds.gbif.org/resources/extensions/vernacularName.xml");

      // no assertions as it relies on external sources...
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testThesaurusURLExtraction() {
    try {
      Set<String> urls = ExtensionFactory.thesaurusURLs(ExtensionFactoryTest.class.getResourceAsStream("/extensions/vernacularName.xml"));
      assertNotNull(urls);
      assertEquals(2, urls.size());
      assertTrue(urls.contains("http://gbrds.gbif.org/resources/thesauri/lang.xml"));
      assertTrue(urls.contains("http://gbrds.gbif.org/resources/thesauri/area.xml"));

    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

}
