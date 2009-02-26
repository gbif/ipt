package org.gbif.provider.service.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.gbif.provider.model.Extension;
import org.junit.Test;

public class ExtensionFactoryTest {

	@Test
	public void testBuild() {
		try {
			Extension e = ExtensionFactory.build(ExtensionFactoryTest.class.getResourceAsStream("/extensions/vernacularName.xml"));
			assertEquals("Vernacular Name", e.getTitle());
			assertEquals("VernacularName", e.getName());
			assertEquals("http://rs.gbif.org/ecat/class/", e.getNamespace());
			assertNull(e.getLink());
			
			assertNotNull(e.getProperties());
			assertEquals(5, e.getProperties().size());
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
