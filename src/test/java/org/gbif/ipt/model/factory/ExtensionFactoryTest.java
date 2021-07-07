/*
 * Copyright 2009 GBIF. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.gbif.ipt.model.factory;

import org.gbif.dwc.ArchiveField.DataType;
import org.gbif.dwc.terms.DcTerm;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.ipt.config.IPTModule;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionProperty;
import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.service.admin.VocabulariesManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExtensionFactoryTest {

  public static ExtensionFactory getFactory() throws ParserConfigurationException, SAXException, MalformedURLException {
    IPTModule mod = new IPTModule();
    SAXParserFactory sax = mod.provideNsAwareSaxParserFactory();
    DefaultHttpClient client = new DefaultHttpClient();

    VocabulariesManager vocabulariesManager = mock(VocabulariesManager.class);
    Vocabulary v = new Vocabulary();
    v.setUriString("http://rs.gbif.org/vocabulary/gbif/nomenclatural_code");
    v.setTitle("Nomenclatural Codes");
    when(vocabulariesManager.get(new URL("http://rs.gbif.org/vocabulary/gbif/nomenclatural_code.xml"))).thenReturn(v);

    ThesaurusHandlingRule thesaurusHandlingRule = new ThesaurusHandlingRule(vocabulariesManager);
    return new ExtensionFactory(thesaurusHandlingRule, sax, client);
  }

  @Test
  public void testBuild() {
    try {
      ExtensionFactory factory = getFactory();
      Extension e = factory.build(ExtensionFactoryTest.class.getResourceAsStream("/extensions/dwc_taxon.xml"));

      assertEquals("Darwin Core Taxon", e.getTitle());
      assertEquals("Taxon", e.getName());
      assertEquals("http://rs.tdwg.org/dwc/terms/", e.getNamespace());
      assertEquals("http://rs.tdwg.org/dwc/terms/Taxon", e.getRowType());
      assertEquals(
        "The category of information pertaining to taxonomic names, taxon name usages, or taxon concepts. Updated Nov 20011 with newly ratified terms.",
        e.getDescription());
      assertEquals("http://rs.tdwg.org/dwc/terms/index.htm#Taxon", e.getLink().toString());

      assertNotNull(e.getProperties());
      assertEquals(44, e.getProperties().size());
      for (ExtensionProperty p : e.getProperties()) {
        if (p.getName().equalsIgnoreCase("kingdom")) {
          assertEquals("http://rs.tdwg.org/dwc/terms/kingdom", p.getQualname());
          assertEquals("http://rs.tdwg.org/dwc/terms/", p.getNamespace());
          assertEquals("Taxon", p.getGroup());
          assertEquals("\"Animalia\", \"Plantae\"", p.getExamples());
          assertEquals("The full scientific name of the kingdom in which the taxon is classified.", p.getDescription());
          assertEquals("http://rs.tdwg.org/dwc/terms/index.htm#kingdom", p.getLink());
        }

        if (p.getName().equalsIgnoreCase("nomenclaturalCode")) {
          assertEquals("http://rs.tdwg.org/dwc/terms/nomenclaturalCode", p.getQualname());
          assertEquals("http://rs.tdwg.org/dwc/terms/", p.getNamespace());
          assertEquals("Taxon", p.getGroup());
          assertNotNull(p.getVocabulary());
          assertEquals("Nomenclatural Codes", p.getVocabulary().getTitle());
        }
      }

      // data types
      assertEquals(DataType.date, e.getProperty(DcTerm.modified).getType());
      assertEquals(DataType.string, e.getProperty(DwcTerm.scientificName).getType());
      assertEquals(DataType.uri, e.getProperty(DcTerm.source).getType());

    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test building Event core extension that has issued date (used to version the extension).
   */
  @Test
  public void testBuildVersionedExtension() {
    try {
      ExtensionFactory factory = getFactory();
      Extension e =
        factory.build(ExtensionFactoryTest.class.getResourceAsStream("/extensions/dwc_event_2015-04-24.xml"));

      assertEquals("Darwin Core Event", e.getTitle());
      assertEquals("Event", e.getName());
      assertEquals("http://rs.tdwg.org/dwc/terms/", e.getNamespace());
      assertEquals("http://rs.tdwg.org/dwc/terms/Event", e.getRowType());
      assertEquals("The category of information pertaining to a sampling event.", e.getDescription());
      assertEquals("http://rs.tdwg.org/dwc/terms/index.htm#Event", e.getLink().toString());

      // issued date parsed correctly?
      DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
      String issued = "2015-04-24";
      Date result = df.parse(issued);
      assertEquals(result.toString(), e.getIssued().toString());

      assertNotNull(e.getProperties());
      assertEquals(99, e.getProperties().size());

      // data types
      assertEquals(DataType.string, e.getProperty(DcTerm.license).getType());
      assertEquals(DataType.string, e.getProperty(DwcTerm.sampleSizeUnit).getType());
      assertEquals("Event", e.getProperty(DwcTerm.sampleSizeUnit).getGroup());
      assertEquals(DataType.string, e.getProperty(DwcTerm.sampleSizeValue).getType());
      assertEquals(DataType.string, e.getProperty(DwcTerm.parentEventID).getType());

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
      Extension e =
        factory.build(ExtensionFactoryTest.class.getResourceAsStream("/extensions/dwc-core-extension_prefixed.xml"));
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
          assertEquals("Nomenclatural Codes", p.getVocabulary().getTitle());
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

}
