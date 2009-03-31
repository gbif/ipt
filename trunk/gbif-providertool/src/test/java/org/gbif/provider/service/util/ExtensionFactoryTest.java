package org.gbif.provider.service.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Set;

import org.gbif.provider.model.Extension;
import org.gbif.provider.model.ExtensionProperty;
import org.gbif.provider.model.factory.ExtensionFactory;
import org.gbif.provider.service.ExtensionPropertyManager;
import org.junit.Test;

public class ExtensionFactoryTest {

	@Test
	public void testBuild() {
		try {
			Extension e = ExtensionFactory.build(ExtensionFactoryTest.class.getResourceAsStream("/extensions/vernacularName.xml"), null);
			assertEquals("Vernacular Name", e.getTitle());
			assertEquals("VernacularName", e.getName());
			assertEquals("http://rs.gbif.org/ecat/class/", e.getNamespace());
			assertNull(e.getLink());
			
			assertNotNull(e.getProperties());
			assertEquals(5, e.getProperties().size());
			for (ExtensionProperty p : e.getProperties()){
				if (p.getName().equalsIgnoreCase("language")){
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
