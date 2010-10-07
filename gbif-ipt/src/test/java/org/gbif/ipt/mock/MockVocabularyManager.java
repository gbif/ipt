/*
 * Copyright 2009 GBIF. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.gbif.ipt.mock;

import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.service.admin.impl.VocabulariesManagerImpl.UpdateResult;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TODO: Documentation.
 */
public class MockVocabularyManager implements VocabulariesManager {

  public void delete(String uri) {
  }

  public Vocabulary get(String uri) {
    return new Vocabulary();
  }

  public Vocabulary get(URL url) {
    return new Vocabulary();
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.admin.VocabulariesManager#getI18nVocab(java.lang.String, java.lang.String)
   */
  public Map<String, String> getI18nVocab(String uri, String lang, boolean sort) {
    return null;
  }

  public List<Vocabulary> list() {
    return new ArrayList<Vocabulary>();
  }

  public int load() {
    return 0;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.admin.VocabulariesManager#updateAll()
   */
  public UpdateResult updateAll() {
    return null;
  }

}
