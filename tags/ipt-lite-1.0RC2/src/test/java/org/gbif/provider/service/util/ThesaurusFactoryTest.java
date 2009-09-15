package org.gbif.provider.service.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.gbif.provider.model.ThesaurusConcept;
import org.gbif.provider.model.ThesaurusTerm;
import org.gbif.provider.model.ThesaurusVocabulary;
import org.gbif.provider.model.factory.ThesaurusFactory;
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
			
			ThesaurusConcept tc = tv.getConcepts().get(0);
			assertEquals("aaa", tc.getIdentifier());
			assertEquals("http://vocabularies.gbif.org/lang/aaa", tc.getLink());
			assertEquals("http://www.sil.org/iso639-3/documentation.asp?id=aaa", tc.getUri());
			assertNotNull(tc.getIssued());
			assertEquals(tv, tc.getVocabulary());
			
			assertNotNull(tc.getTerms());
			assertEquals(1, tc.getTerms().size());
			ThesaurusTerm tt = tc.getTerms().iterator().next();
			assertEquals(tc, tt.getConcept());
			assertNotNull(tt.getCreated());
			assertNotNull(tt.getModified());
			assertEquals("en", tt.getLang());
			assertTrue(tt.isPreferred());
			assertEquals("Ghotuo", tt.getTitle());
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
