package org.gbif.provider.service.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.gbif.provider.model.ThesaurusVocabulary;
import org.junit.Test;

public class ThesaurusFactoryTest {

	@Test
	public void testBuild() {
		try {
			ThesaurusVocabulary tv = ThesaurusFactory.build(ThesaurusFactoryTest.class.getResourceAsStream("/thesauri/lang.xml"));
			assertEquals("Language Vocabulary", tv.getTitle());
			assertEquals("http://purl.org/dc/terms/ISO639-3", tv.getUri());
			
			assertNotNull(tv.getConcepts());
			assertEquals(7694, tv.getConcepts().size());
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
