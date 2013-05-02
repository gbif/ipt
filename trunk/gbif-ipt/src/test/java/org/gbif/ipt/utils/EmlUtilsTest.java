package org.gbif.ipt.utils;

import org.gbif.ipt.config.Constants;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.KeywordSet;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EmlUtilsTest {

  private Eml eml;

  @Before
  public void setup() {
    eml = new Eml();
  }

  @Test
  public void testAddOrUpdateKeywordSet() {
    // start with empty List
    List<KeywordSet> keywords = eml.getKeywords();
    // add KeywordSet for dataset type Occurrence
    EmlUtils.addOrUpdateKeywordSet(keywords, "occurrence", Constants.THESAURUS_DATASET_TYPE);
    assertEquals(1, keywords.size());
    assertEquals("Occurrence", keywords.get(0).getKeywordsString());
    assertEquals(Constants.THESAURUS_DATASET_TYPE, keywords.get(0).getKeywordThesaurus());
    // ensure calling same method, doesn't add duplicate
    EmlUtils.addOrUpdateKeywordSet(keywords, "Occurrence", Constants.THESAURUS_DATASET_TYPE);
    assertEquals(1, keywords.size());
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
