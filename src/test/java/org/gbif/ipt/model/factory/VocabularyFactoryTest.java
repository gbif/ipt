/*
 * Copyright 2009 GBIF. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.gbif.ipt.model.factory;

import org.gbif.ipt.config.IPTModule;
import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.model.VocabularyConcept;
import org.gbif.ipt.model.VocabularyTerm;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test XML vocabulary parsed correctly.
 */
public class VocabularyFactoryTest {

  private VocabularyFactory getFactory() throws ParserConfigurationException, SAXException {
    IPTModule mod = new IPTModule();
    SAXParserFactory sax = mod.provideNsAwareSaxParserFactory();
    return new VocabularyFactory(sax);
  }

  @Test
  public void testBuild() {
    try {
      Vocabulary tv =
        getFactory().build(VocabularyFactoryTest.class.getResourceAsStream("/thesauri/type-vocabulary.xml"));
      assertEquals("Dublin Core Type Vocabulary", tv.getTitle());
      assertEquals("http://dublincore.org/documents/dcmi-type-vocabulary/", tv.getUriString());
      assertEquals(
        "The DCMI Type Vocabulary provides a general, cross-domain list of approved terms that may be used as values for the Resource Type element to identify the genre of a resource. The terms documented here are also included in the more comprehensive document \"DCMI Metadata Terms\" at http://dublincore.org/documents/dcmi-terms/.",
        tv.getDescription());
      assertEquals("http://dublincore.org/documents/dcmi-type-vocabulary/", tv.getLink().toString());

      assertNotNull(tv.getConcepts());
      assertEquals(12, tv.getConcepts().size());

      VocabularyConcept tc = tv.getConcepts().get(0);
      assertEquals("Collection", tc.getIdentifier());
      assertNull(tc.getLink());
      assertEquals("http://purl.org/dc/dcmitype/Collection", tc.getUri());
      assertEquals(tv, tc.getVocabulary());
      assertEquals("Collection", tc.getPreferredTerm("en").getTitle());
      assertEquals("Sammlung", tc.getPreferredTerm("de").getTitle());

      assertNotNull(tc.getTerms());
      assertNotNull(tc.getPreferredTerms());
      assertEquals(2, tc.getPreferredTerms().size());
      assertEquals(0, tc.getAlternativeTerms().size());
      assertEquals(2, tc.getTerms().size());

      // previously there was an assertion that caused IPT to fail when built with Java 5
      // Java 5 - term that comes off iterator 1st is de
      // Java 6 - term that comes off iterator 1st is en
      VocabularyTerm tt = tc.getTerms().iterator().next();
      if (tt.getLang().equals("en")) {
        assertEquals("Collection", tt.getTitle());
      } else {
        assertEquals("de", tt.getLang());
        assertEquals("Sammlung", tt.getTitle());
      }

    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Test building Quantity Type vocabulary that has issued date (used to version the vocabulary).
   */
  @Test
  public void testBuildVersionedVocabulary() {
    try {
      Vocabulary v =
        getFactory().build(VocabularyFactoryTest.class.getResourceAsStream("/thesauri/quantity_type-2015-05-04.xml"));

      assertEquals("Quantity Type Vocabulary", v.getTitle());
      assertEquals("http://rs.gbif.org/vocabulary/gbif/quantityType", v.getUriString());
      assertNull(v.getLink());
      assertEquals("The quantityType Vocabulary is a recommended set of values to use for the Darwin Core quantityType property.", v.getDescription());

      // issued date parsed correctly?
      DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
      String issued = "2015-05-04";
      Date result = df.parse(issued);
      assertEquals(result.toString(), v.getIssued().toString());

      assertNotNull(v.getConcepts());
      assertEquals(12, v.getConcepts().size());

      VocabularyConcept tc = v.getConcepts().get(0);
      assertEquals("percentageOfSpecies", tc.getIdentifier());
      assertNull(tc.getLink());
      assertEquals("http://rs.gbif.org/vocabulary/gbif/quantityType/percentageOfSpecies", tc.getUri());
      assertEquals(v, tc.getVocabulary());
      assertEquals("percentage of species", tc.getPreferredTerm("en").getTitle());

      assertNotNull(tc.getTerms());
      assertEquals(1, tc.getTerms().size());
      assertNotNull(tc.getPreferredTerms());
      assertEquals(1, tc.getPreferredTerms().size());
      assertEquals(0, tc.getAlternativeTerms().size());
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }
}
