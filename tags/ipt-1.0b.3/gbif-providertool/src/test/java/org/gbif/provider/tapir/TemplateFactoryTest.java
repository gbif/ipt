package org.gbif.provider.tapir;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertNull;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

public class TemplateFactoryTest {
	Log log = LogFactory.getLog(this.getClass());
	
	@Test
	public void testSearch() {
		try {
			URL templateUrl = TemplateFactoryTest.class.getResource("/tapir/searchTemplate.xml");
			Map<String, String> params = new HashMap<String, String>();
			params.put("sciname", "Markus doeringus var. robertsonino");
			Template template = TemplateFactory.buildTemplate(templateUrl, params);

			String expected = "Filter: not (AND: http://rs.tdwg.org/dwc/terms/country is null and http://rs.tdwg.org/dwc/terms/scientificName like 'Markus doeringus var. robertsonino')";
			log.debug("Expected: " + expected);
			log.debug("Received: " + template.getFilter().toString());
			assertEquals(expected, template.getFilter().toString());
			
			assertEquals(TapirOperation.search, template.getOperation());
			assertEquals(2, template.getOrderBy().size());
			assertTrue(template.getOrderBy().keySet().contains("http://rs.tdwg.org/dwc/terms/family"));
			assertTrue(template.getOrderBy().keySet().contains("http://rs.tdwg.org/dwc/terms/scientificName"));
			assertEquals(true, template.getOrderBy().get("http://rs.tdwg.org/dwc/terms/family"));
			assertEquals(false, template.getOrderBy().get("http://rs.tdwg.org/dwc/terms/scientificName"));
			
			assertEquals("http://rs.tdwg.org/tapir/cs/dwc/dwcstar.xml", template.getModel());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail(e.getMessage());
		}
	}

	@Test
	public void testInventory() {
		try {
			URL templateUrl = TemplateFactoryTest.class.getResource("/tapir/inventoryTemplate.xml");
			Map<String, String> params = new HashMap<String, String>();
			params.put("land", "DE");
			params.put("name", "Markus doeringus var. robertsonino");
			params.put("sammler", "Dave Martin");
			Template template = TemplateFactory.buildTemplate(templateUrl, params);

			String expected = "Filter: AND: http://rs.tdwg.org/dwc/terms/country = 'DE' and http://rs.tdwg.org/dwc/terms/scientificName like 'Markus doeringus var. robertsonino' and http://rs.tdwg.org/dwc/terms/collector like 'Dave Martin'";
			log.debug("Expected: " + expected);
			log.debug("Received: " + template.getFilter().toString());
			assertEquals(expected, template.getFilter().toString());
			
			assertEquals(TapirOperation.inventory, template.getOperation());
			assertEquals(3, template.getConcepts().size());
			assertTrue(template.getConcepts().keySet().contains("http://rs.tdwg.org/dwc/terms/country"));
			assertTrue(template.getConcepts().keySet().contains("http://rs.tdwg.org/dwc/terms/scientificName"));
			assertTrue(template.getConcepts().keySet().contains("http://rs.tdwg.org/dwc/terms/collector"));
			assertEquals("land", template.getConcepts().get("http://rs.tdwg.org/dwc/terms/country"));
			assertEquals("name", template.getConcepts().get("http://rs.tdwg.org/dwc/terms/scientificName"));
			assertEquals("sammler", template.getConcepts().get("http://rs.tdwg.org/dwc/terms/collector"));
			
			assertNull(template.getModel());
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

			String expected = "Filter: not (AND: http://rs.tdwg.org/dwc/terms/country is null and http://rs.tdwg.org/dwc/terms/scientificName like 'Markus doeringus var. robertsonino')";
			log.debug("Expected: " + expected);
			log.debug("Received: " + template.getFilter().toString());
			assertEquals(expected, template.getFilter().toString());
			
			assertEquals(TapirOperation.search, template.getOperation());
			assertEquals(2, template.getOrderBy().size());
			assertTrue(template.getOrderBy().keySet().contains("http://rs.tdwg.org/dwc/terms/family"));
			assertTrue(template.getOrderBy().keySet().contains("http://rs.tdwg.org/dwc/terms/scientificName"));
			assertEquals(true, template.getOrderBy().get("http://rs.tdwg.org/dwc/terms/family"));
			assertEquals(false, template.getOrderBy().get("http://rs.tdwg.org/dwc/terms/scientificName"));
			
			assertNull(template.getModel());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			fail(e.getMessage());
		}
	}	

}
