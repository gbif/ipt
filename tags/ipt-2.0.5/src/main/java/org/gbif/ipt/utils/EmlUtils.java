package org.gbif.ipt.utils;

import org.gbif.metadata.eml.KeywordSet;

import java.util.Iterator;
import java.util.List;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;

public class EmlUtils {

  /*
   * Empty constructor.
   */
  private EmlUtils() {
  }

  /**
   * Add or update KeywordSet identified by thesaurus name. If the KeywordSet is found and already contains a
   * a non empty and not null keywordString, its value is not overwritten.
   *
   * @param keywords list of KeywordSet to add/update to
   * @param keyword keyword string
   * @param thesaurus thesaurus name
   */
  public static void addOrUpdateKeywordSet(List<KeywordSet> keywords, String keyword, String thesaurus) {
    if (!Strings.isNullOrEmpty(keyword) && !Strings.isNullOrEmpty(thesaurus)) {
      // capitalize incoming keyword, i.e., Occurrence, Specimen
      String capped = StringUtils.capitalize(keyword.toLowerCase());
      boolean found = false;
      for (KeywordSet ks : keywords) {
        String keywordThesaurus = ks.getKeywordThesaurus();
        if (!Strings.isNullOrEmpty(keywordThesaurus) && keywordThesaurus.equalsIgnoreCase(thesaurus)) {
          String keywordString = ks.getKeywordsString();
          // update keywordString, only if empty
          if (Strings.isNullOrEmpty(keywordString)) {
            ks.setKeywordsString(capped);
          }
          found = true;
          break;
        }
      }
      // if no match, add new KeywordSet
      if (!found) {
        KeywordSet ks = new KeywordSet();
        ks.setKeywordThesaurus(thesaurus);
        ks.setKeywordsString(capped);
        // add new KeywordSet
        keywords.add(ks);
      }
    }
  }

  /**
   * Remove all KeywordSet with specific thesaurus name.
   *
   * @param keywords list of KeywordSet to remove from
   * @param thesaurus thesaurus name
   */
  public static void removeKeywordSet(List<KeywordSet> keywords, String thesaurus) {
    if (!Strings.isNullOrEmpty(thesaurus)) {
      for (Iterator<KeywordSet> iterator = keywords.iterator(); iterator.hasNext();) {
        String keywordThesaurus = iterator.next().getKeywordThesaurus();
        if (!Strings.isNullOrEmpty(keywordThesaurus) && keywordThesaurus.equalsIgnoreCase(thesaurus)) {
          iterator.remove();
        }
      }
    }
  }
}
