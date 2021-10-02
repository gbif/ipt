/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.ipt.utils;

import org.gbif.ipt.config.Constants;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.KeywordSet;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EmlUtilsTest {

  private Eml eml;

  @BeforeEach
  public void setup() {
    eml = new Eml();
  }

  @Test
  public void testAddOrUpdateKeywordSet() {
    // start with empty List
    List<KeywordSet> keywords = eml.getKeywords();
    // add KeywordSet for dataset type Occurrence
    EmlUtils.addOrUpdateKeywordSet(keywords, "Occurrencia", Constants.THESAURUS_DATASET_TYPE);
    assertEquals(1, keywords.size());
    assertEquals("Occurrencia", keywords.get(0).getKeywordsString());
    assertEquals(Constants.THESAURUS_DATASET_TYPE, keywords.get(0).getKeywordThesaurus());
    // ensure calling same method, doesn't replace keywordString
    EmlUtils.addOrUpdateKeywordSet(keywords, "Occurrence", Constants.THESAURUS_DATASET_TYPE);
    assertEquals(1, keywords.size());
    assertEquals("Occurrencia", keywords.get(0).getKeywordsString());
  }

  @Test
  public void testAddOrUpdateKeywordSetFails() {
    // start with empty List
    List<KeywordSet> keywords = eml.getKeywords();
    // add KeywordSet for empty dataset type - should fail
    EmlUtils.addOrUpdateKeywordSet(keywords, "", Constants.THESAURUS_DATASET_TYPE);
    assertEquals(0, keywords.size());
  }

  @Test
  public void testRemoveKeywordSet() {
    // start with pre-populated List
    List<KeywordSet> keywords = new ArrayList<KeywordSet>();

    KeywordSet ks1 = new KeywordSet();
    ks1.setKeywordsString("Occurrence");
    ks1.setKeywordThesaurus(Constants.THESAURUS_DATASET_TYPE);
    keywords.add(ks1);

    KeywordSet ks2 = new KeywordSet();
    ks2.setKeywordsString("Birds of prey");
    ks2.setKeywordThesaurus("Bird Types Thesaurus");
    keywords.add(ks2);

    KeywordSet ks3 = new KeywordSet();
    ks3.setKeywordsString("Checklist");
    ks3.setKeywordThesaurus(Constants.THESAURUS_DATASET_TYPE);
    keywords.add(ks3);

    assertEquals(3, keywords.size());
    EmlUtils.removeKeywordSet(keywords, Constants.THESAURUS_DATASET_TYPE);
    // should be left with 1
    assertEquals(1, keywords.size());
  }

}
