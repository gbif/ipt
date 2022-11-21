/*
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
package org.gbif.ipt.mock;

import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.model.VocabularyConcept;
import org.gbif.ipt.model.VocabularyTerm;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.utils.IptMockBaseTest;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Mocked VocabulariesManager, for use in TestClasses.
 */
public class MockVocabulariesManager extends IptMockBaseTest implements VocabulariesManager {

  /**
   * Get one of the installed vocabularies by its unique identifier, populate its list of VocabularyConcept, and
   * return it.
   */
  @Override
  public Vocabulary get(String uri) {
    Vocabulary v = new Vocabulary();
    Map<String, String> vocabMap = getI18nVocab(uri, Locale.getDefault().getDisplayLanguage(), false);
    for (Map.Entry<String, String> stringStringEntry : vocabMap.entrySet()) {
      VocabularyConcept concept = new VocabularyConcept();
      concept.setIdentifier(stringStringEntry.getKey());
      VocabularyTerm term = new VocabularyTerm();
      term.setTitle(stringStringEntry.getValue());
      term.setLang("en");
      concept.addPreferredTerm(term);
      v.addConcept(concept);
    }
    return v;
  }

  @Override
  public Vocabulary get(URL url) {
    return null;
  }

  @Override
  public Vocabulary install(URL url) {
    return null;
  }

  public Vocabulary get(URI uri) {
    return new Vocabulary();
  }

  /**
   * Mock minimal copies of rank, country, language, and preservation method vocabularies being installed.
   */
  @Override
  public Map<String, String> getI18nVocab(String uri, String lang, boolean sort) {
    Map<String, String> vocabMap = new LinkedHashMap<>();
    if (uri.equals(Constants.VOCAB_URI_RANKS)) {
      vocabMap.put("domain", "domain");
      vocabMap.put("kingdom", "kingdom");
      vocabMap.put("phylum", "phylum");
      vocabMap.put("class", "class");
      vocabMap.put("order", "order");
      vocabMap.put("family", "family");
      vocabMap.put("tribe", "tribe");
      vocabMap.put("genus", "genus");
      vocabMap.put("section", "section");
      vocabMap.put("species", "species");
      vocabMap.put("variety", "variety");
      vocabMap.put("form", "form");
      vocabMap.put("cultivar", "cultivar");
    } else if (uri.equals(Constants.VOCAB_URI_COUNTRY)) {
      vocabMap.put("CO", "COLOMBIA");
      vocabMap.put("DK", "DENMARK");
      vocabMap.put("DE", "GERMANY");
      vocabMap.put("US", "UNITED STATES");
      vocabMap.put("BG", "BULGARIA");
      vocabMap.put("IN", "INDIA");
      vocabMap.put("NL", "NETHERLANDS");
    } else if (uri.equals(Constants.VOCAB_URI_LANGUAGE)) {
      vocabMap.put("eng", "English");
      vocabMap.put("spa", "Spanish");
      vocabMap.put("ger", "German");
      vocabMap.put("fre", "French");
      vocabMap.put("dut", "Dutch");
      vocabMap.put("est", "Estonian");
    } else if (uri.equals(Constants.VOCAB_URI_PRESERVATION_METHOD)) {
      vocabMap.put("noTreatment", "No treatment");
      vocabMap.put("alcohol", "Alcohol");
      vocabMap.put("deepFrozen", "Deep frozen");
      vocabMap.put("dried", "Dried");
      vocabMap.put("driedAndPressed", "Dried and pressed");
      vocabMap.put("formalin", "Formalin");
      vocabMap.put("refrigerated", "Refrigerated");
      vocabMap.put("freezeDried", "Freeze-dried");
      vocabMap.put("glycerin", "Glycerin");
      vocabMap.put("gumArabic", "Gum arabic");
      vocabMap.put("microscopicPreparation", "Microscopic preparation");
      vocabMap.put("mounted", "Mounted");
      vocabMap.put("pinned", "Pinned");
      vocabMap.put("other", "Other");
    }
    return vocabMap;
  }

  @Override
  public Map<String, String> getI18nDatasetTypesVocab(String lang, boolean sortAlphabetically) {
    return new HashMap<>();
  }

  @Override
  public Map<String, String> getI18nDatasetSubtypesVocab(String lang, boolean sortAlphabetically) {
    return new HashMap<>();
  }

  @Override
  public List<Vocabulary> list() {
    return new ArrayList<>();
  }

  @Override
  public int load() {
    return 0;
  }

  @Override
  public void installOrUpdateDefaults() throws InvalidConfigException {
  }


  @Override
  public boolean updateIfChanged(String uri) throws IOException, InvalidConfigException {
    return false;
  }
}
