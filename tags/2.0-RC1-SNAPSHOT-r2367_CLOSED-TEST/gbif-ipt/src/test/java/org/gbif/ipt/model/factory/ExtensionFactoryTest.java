/*
 * Copyright 2009 GBIF. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.gbif.ipt.model.factory;

import org.gbif.ipt.config.IPTModule;
import org.gbif.ipt.config.InjectingTestClassRunner;
import org.gbif.ipt.mock.MockVocabularyManager;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionProperty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

/**
 */
@RunWith(InjectingTestClassRunner.class)
public class ExtensionFactoryTest {

  private ExtensionFactory getFactory() throws ParserConfigurationException, SAXException {
    IPTModule mod = new IPTModule();
    SAXParserFactory sax = mod.provideNsAwareSaxParserFactory();
    ExtensionFactory factory = new ExtensionFactory(new ThesaurusHandlingRule(new MockVocabularyManager()), sax);
    return factory;
  }

  @Test
  public void testBuild() {
    try {
      ExtensionFactory factory = getFactory();
      Extension e = factory.build(ExtensionFactoryTest.class.getResourceAsStream("/extensions/dwc-core-extension.xml"));

      /*
       * dc:title="Darwin Core Taxon" name="Taxon" namespace="http://rs.tdwg.org/dwc/terms/"
       * rowType="http://rs.tdwg.org/dwc/terms/Taxon" dc:relation="http://rs.tdwg.org/dwc/terms/index.htm#Taxon"
       * dc:description
       * ="The category of information pertaining to taxonomic names, taxon name usages, or taxon concepts.">
       */

      assertEquals("Darwin Core Taxon", e.getTitle());
      assertEquals("Taxon", e.getName());
      assertEquals("http://rs.tdwg.org/dwc/terms/", e.getNamespace());
      assertEquals("http://rs.tdwg.org/dwc/terms/Taxon", e.getRowType());
      assertEquals("The category of information pertaining to taxonomic names, taxon name usages, or taxon concepts.",
          e.getDescription());
      assertEquals("http://rs.tdwg.org/dwc/terms/index.htm#Taxon", e.getLink().toString());

      assertNotNull(e.getProperties());
      assertEquals(47, e.getProperties().size());
      for (ExtensionProperty p : e.getProperties()) {
        if (p.getName().equalsIgnoreCase("kingdom")) {
          assertEquals("http://rs.tdwg.org/dwc/terms/kingdom", p.getQualname());
          assertEquals("http://rs.tdwg.org/dwc/terms/", p.getNamespace());
          assertEquals("Taxon", p.getGroup());
          assertEquals("Kingdom examples", p.getExamples());
          assertEquals("Kingdom description", p.getDescription());
          assertEquals("http://rs.tdwg.org/dwc/terms/index.htm#kingdom", p.getLink());
        }

        if (p.getName().equalsIgnoreCase("nomenclaturalCode")) {
          assertEquals("http://rs.tdwg.org/dwc/terms/nomenclaturalCode", p.getQualname());
          assertEquals("http://rs.tdwg.org/dwc/terms/", p.getNamespace());
          assertEquals("Taxon", p.getGroup());
          assertNotNull(p.getVocabulary());
          // dont assert these as we use a mock vocab manager !
//        assertEquals("Nomenclatural Codes", p.getVocabulary().getTitle());
//        assertEquals(6, p.getVocabulary().getConcepts().size());
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
      ExtensionFactory factory = getFactory();
      Extension e = factory.build("http://rs.gbif.org/core/dwc_taxon.xml");

      // no assertions as it relies on external sources...
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void testPrefixBuild() {
    try {
      ExtensionFactory factory = getFactory();
      Extension e = factory.build(ExtensionFactoryTest.class.getResourceAsStream("/extensions/dwc-core-extension_prefixed.xml"));

      /*
       * dc:title="Darwin Core Taxon" name="Taxon" namespace="http://rs.tdwg.org/dwc/terms/"
       * rowType="http://rs.tdwg.org/dwc/terms/Taxon" dc:relation="http://rs.tdwg.org/dwc/terms/index.htm#Taxon"
       * dc:description
       * ="The category of information pertaining to taxonomic names, taxon name usages, or taxon concepts.">
       */

      assertEquals("Darwin Core Taxon", e.getTitle());
      assertEquals("Taxon", e.getName());
      assertEquals("http://rs.tdwg.org/dwc/terms/", e.getNamespace());
      assertEquals("http://rs.tdwg.org/dwc/terms/Taxon", e.getRowType());
      assertEquals("The category of information pertaining to taxonomic names, taxon name usages, or taxon concepts.",
          e.getDescription());
      assertEquals("http://rs.tdwg.org/dwc/terms/index.htm#Taxon", e.getLink().toString());

      assertNotNull(e.getProperties());
      assertEquals(47, e.getProperties().size());
      for (ExtensionProperty p : e.getProperties()) {
        if (p.getName().equalsIgnoreCase("kingdom")) {
          assertEquals("http://rs.tdwg.org/dwc/terms/kingdom", p.getQualname());
          assertEquals("http://rs.tdwg.org/dwc/terms/", p.getNamespace());
          assertEquals("Taxon", p.getGroup());
          assertEquals("Kingdom examples", p.getExamples());
          assertEquals("Kingdom description", p.getDescription());
          assertEquals("http://rs.tdwg.org/dwc/terms/index.htm#kingdom", p.getLink());
        }

        if (p.getName().equalsIgnoreCase("nomenclaturalCode")) {
          assertEquals("http://rs.tdwg.org/dwc/terms/nomenclaturalCode", p.getQualname());
          assertEquals("http://rs.tdwg.org/dwc/terms/", p.getNamespace());
          assertEquals("Taxon", p.getGroup());
          assertNotNull(p.getVocabulary());
          // dont assert these as we use a mock vocab manager !
//          assertEquals("Nomenclatural Codes", p.getVocabulary().getTitle());
//          assertEquals(6, p.getVocabulary().getConcepts().size());
        }

      }

    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

}
