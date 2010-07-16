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
package org.gbif.ipt.model.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Set;

import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionProperty;
import org.junit.Test;

/**
 * TODO: Documentation.
 * 
 */
public class ExtensionFactoryTest {

  @Test
  public void testBuild() {
    try {
      Extension e = ExtensionFactory.build(ExtensionFactoryTest.class.getResourceAsStream("/extensions/dwc-core-extension.xml"), null);
      
      /*
      dc:title="Darwin Core Taxon" 
    	    name="Taxon" namespace="http://rs.tdwg.org/dwc/terms/" rowType="http://rs.tdwg.org/dwc/terms/Taxon"
    	    dc:relation="http://rs.tdwg.org/dwc/terms/index.htm#Taxon"
    	    dc:description="The category of information pertaining to taxonomic names, taxon name usages, or taxon concepts.">
       */
      
      assertEquals("Darwin Core Taxon", e.getTitle());
      assertEquals("Taxon", e.getName());
      assertEquals("http://rs.tdwg.org/dwc/terms/", e.getNamespace());
      assertEquals("http://rs.tdwg.org/dwc/terms/Taxon", e.getRowType());
      assertEquals("The category of information pertaining to taxonomic names, taxon name usages, or taxon concepts.", e.getDescription());
      assertEquals("http://rs.tdwg.org/dwc/terms/index.htm#Taxon", e.getLink().toString());

      assertNotNull(e.getProperties());
      assertEquals(47, e.getProperties().size());
      for (ExtensionProperty p : e.getProperties()) {
        if (p.getName().equalsIgnoreCase("year")) {
          assertEquals("http://rs.tdwg.org/dwc/terms/year", p.getQualname());
          assertEquals("http://rs.tdwg.org/dwc/terms/", p.getNamespace());
          assertEquals("Events", p.getGroup());
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
      ExtensionFactory ef = new ExtensionFactory(new MockVocabularyManager());
      Extension e = ef.build("http://rs.gbif.org/core/dwc_taxon.xml");

      // no assertions as it relies on external sources...
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testThesaurusURLExtraction() {
    try {
      Set<String> urls = ExtensionFactory.thesaurusURLs(ExtensionFactoryTest.class.getResourceAsStream("/extensions/dwc-core-extension.xml"));
      assertNotNull(urls);
      assertEquals(3, urls.size());
      assertTrue(urls.contains("http://rs.gbif.org/vocabulary/dcterms/type.xml"));
      assertTrue(urls.contains("http://rs.gbif.org/vocabulary/dwc/basis_of_record.xml"));
      assertTrue(urls.contains("http://rs.gbif.org/vocabulary/gbif/nomenclatural_code.xml"));

    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

}
