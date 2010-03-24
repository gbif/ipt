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
package org.gbif.provider.tapir;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Documentation.
 * 
 */
public class TemplateFactoryTest {
  Log log = LogFactory.getLog(this.getClass());

  @Test
  public void testInventory() {
    try {
      URL templateUrl = TemplateFactoryTest.class.getResource("/tapir/inventoryTemplate.xml");
      Map<String, String> params = new HashMap<String, String>();
      params.put("land", "DE");
      params.put("name", "Markus doeringus var. robertsonino");
      params.put("sammler", "Dave Martin");
      Template template = TemplateFactory.buildTemplate(templateUrl, params);

      String expected = "Filter: AND: http://rs.tdwg.org/dwc/terms/Country = 'DE' and http://rs.tdwg.org/dwc/terms/ScientificName like 'Markus doeringus var. robertsonino' and http://rs.tdwg.org/dwc/terms/Collector like 'Dave Martin'";
      log.debug("Expected: " + expected);
      log.debug("Received: " + template.getFilter().toString());
      assertEquals(expected, template.getFilter().toString());

      assertEquals(TapirOperation.inventory, template.getOperation());
      assertEquals(3, template.getConcepts().size());
      assertTrue(template.getConcepts().keySet().contains(
          "http://rs.tdwg.org/dwc/terms/Country"));
      assertTrue(template.getConcepts().keySet().contains(
          "http://rs.tdwg.org/dwc/terms/ScientificName"));
      assertTrue(template.getConcepts().keySet().contains(
          "http://rs.tdwg.org/dwc/terms/Collector"));
      assertEquals("land", template.getConcepts().get(
          "http://rs.tdwg.org/dwc/terms/Country"));
      assertEquals("name", template.getConcepts().get(
          "http://rs.tdwg.org/dwc/terms/ScientificName"));
      assertEquals("sammler", template.getConcepts().get(
          "http://rs.tdwg.org/dwc/terms/Collector"));

      assertNull(template.getModel());
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      fail(e.getMessage());
    }
  }

  @Test
  public void testSearch() {
    try {
      URL templateUrl = TemplateFactoryTest.class.getResource("/tapir/searchTemplate.xml");
      Map<String, String> params = new HashMap<String, String>();
      params.put("sciname", "Markus doeringus var. robertsonino");
      Template template = TemplateFactory.buildTemplate(templateUrl, params);

      String expected = "Filter: not (AND: http://rs.tdwg.org/dwc/terms/Country is null and http://rs.tdwg.org/dwc/terms/ScientificName like 'Markus doeringus var. robertsonino')";
      log.debug("Expected: " + expected);
      log.debug("Received: " + template.getFilter().toString());
      assertEquals(expected, template.getFilter().toString());

      assertEquals(TapirOperation.search, template.getOperation());
      assertEquals(2, template.getOrderBy().size());
      assertTrue(template.getOrderBy().keySet().contains(
          "http://rs.tdwg.org/dwc/terms/Family"));
      assertTrue(template.getOrderBy().keySet().contains(
          "http://rs.tdwg.org/dwc/terms/ScientificName"));
      assertEquals(true, template.getOrderBy().get(
          "http://rs.tdwg.org/dwc/terms/Family"));
      assertEquals(false, template.getOrderBy().get(
          "http://rs.tdwg.org/dwc/terms/ScientificName"));

      assertEquals("http://rs.tdwg.org/tapir/cs/dwc/dwcstar.xml",
          template.getModel());
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      fail(e.getMessage());
    }
  }

  @Test
  // template contains an inline output model with a link to an external schema.
  // so operation should be search, but model location NULL:
  public void testSearch2() {
    try {
      URL templateUrl = TemplateFactoryTest.class.getResource("/tapir/searchTemplate2.xml");
      Map<String, String> params = new HashMap<String, String>();
      params.put("sciname", "Markus doeringus var. robertsonino");
      Template template = TemplateFactory.buildTemplate(templateUrl, params);

      String expected = "Filter: not (AND: http://rs.tdwg.org/dwc/terms/Country is null and http://rs.tdwg.org/dwc/terms/ScientificName like 'Markus doeringus var. robertsonino')";
      log.debug("Expected: " + expected);
      log.debug("Received: " + template.getFilter().toString());
      assertEquals(expected, template.getFilter().toString());

      assertEquals(TapirOperation.search, template.getOperation());
      assertEquals(2, template.getOrderBy().size());
      assertTrue(template.getOrderBy().keySet().contains(
          "http://rs.tdwg.org/dwc/terms/Family"));
      assertTrue(template.getOrderBy().keySet().contains(
          "http://rs.tdwg.org/dwc/terms/ScientificName"));
      assertEquals(true, template.getOrderBy().get(
          "http://rs.tdwg.org/dwc/terms/Family"));
      assertEquals(false, template.getOrderBy().get(
          "http://rs.tdwg.org/dwc/terms/ScientificName"));

      assertNull(template.getModel());
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      fail(e.getMessage());
    }
  }

}
