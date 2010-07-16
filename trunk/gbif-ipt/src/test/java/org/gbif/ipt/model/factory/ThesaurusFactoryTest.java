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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.model.VocabularyConcept;
import org.gbif.ipt.model.VocabularyTerm;
/**
 * TODO: Documentation.
 * 
 */
public class ThesaurusFactoryTest {

  public void testBuild() {
    try {
      Vocabulary tv = VocabularyFactory.build(ThesaurusFactoryTest.class.getResourceAsStream("/thesauri/type-vocabulary.xml"));
      assertEquals("Language Vocabulary", tv.getTitle());
      assertEquals("http://purl.org/dc/terms/ISO639-3", tv.getUri());

      assertNotNull(tv.getConcepts());
      assertEquals(7694, tv.getConcepts().size());

      VocabularyConcept tc = tv.getConcepts().get(0);
      assertEquals("aaa", tc.getIdentifier());
      assertEquals("http://vocabularies.gbif.org/lang/aaa", tc.getLink());
      assertEquals("http://www.sil.org/iso639-3/documentation.asp?id=aaa",
          tc.getUri());
      assertEquals(tv, tc.getVocabulary());

      assertNotNull(tc.getTerms());
      assertEquals(1, tc.getTerms().size());
      VocabularyTerm tt = tc.getTerms().iterator().next();
      assertEquals("en", tt.getLang());
      assertEquals("Ghotuo", tt.getTitle());

    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }
}
