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
package org.gbif.ipt.service.admin;

import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.impl.VocabulariesManagerImpl;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import com.google.inject.ImplementedBy;

/**
 * This interface details ALL methods associated with the vocabularies within the IPT.
 */
@ImplementedBy(VocabulariesManagerImpl.class)
public interface VocabulariesManager {

  /**
   * Retrieves an installed vocabulary by its unique URI.
   *
   * @param uri unique URI
   *
   * @return the installed vocabulary or null if not found
   */
  Vocabulary get(String uri);

  /**
   * Retrieves an installed vocabulary by its URL.
   *
   * @param url url to the xml vocabulary definition
   *
   * @return the installed vocabulary or null if not found
   */
  Vocabulary get(URL url);

  /**
   * Download and install a vocabulary into local file. The final filename is based on the vocabulary's identifier.
   *
   * @param url the URL of the XML based vocabulary definition
   *
   * @return the installed vocabulary
   *
   * @throws InvalidConfigException if Vocabulary failed to be installed
   */
  Vocabulary install(URL url);

  /**
   * Returns a regular map than can be used to populate html select drop downs with
   * keys=vocabulary concept identifiers and values=preferred term for the given language.
   * Defaults to english if no term for the requested language exists.
   *
   * @param uri                the identifier for the vocabulary
   * @param lang               a 2 character iso language code, e.g. DE
   * @param sortAlphabetically if true sort map values alphabetically, otherwise use native ordering
   *
   * @return return vocabulary map for given language sorted alphabetically, or an empty map if no vocabulary concepts
   * could be populated
   */
  Map<String, String> getI18nVocab(String uri, String lang, boolean sortAlphabetically);

  /**
   * Returns a regular map of dataset types than can be used to populate html select dropdowns with
   * keys=vocabulary concept identifiers and values=preferred term for the given language.
   * Defaults to english if no term for the requested language exists.
   *
   * @param lang               a 2 character iso language code, e.g. DE
   * @param sortAlphabetically if true sort map values alphabetically, otherwise use native ordering
   *
   * @return return dataset types vocabulary map for given language sorted alphabetically, or an empty map if no
   * vocabulary concepts could be populated
   */
  Map<String, String> getI18nDatasetTypesVocab(String lang, boolean sortAlphabetically);

  /**
   * Returns a regular map of dataset subtypes than can be used to populate html select dropdowns with
   * keys=vocabulary concept identifiers and values=preferred term for the given language.
   * Defaults to english if no term for the requested language exists.
   *
   * @param lang               a 2 character iso language code, e.g. DE
   * @param sortAlphabetically if true sort map values alphabetically, otherwise use native ordering
   *
   * @return return dataset subtypes vocabulary map for given language sorted alphabetically, or an empty map if no
   * vocabulary concepts could be populated
   */
  Map<String, String> getI18nDatasetSubtypesVocab(String lang, boolean sortAlphabetically);

  /**
   * Lists all installed vocabularies.
   *
   * @return all installed vocabularies
   */
  List<Vocabulary> list();

  /**
   * Load all known vocabularies from the data dir. It also ensures that all default vocabularies
   * get installed and loaded also.
   *
   * @return number of vocabularies that have been loaded successfully
   */
  int load();

  /**
   * Install or update latest version of all default vocabularies.
   */
  void installOrUpdateDefaults() throws InvalidConfigException;

  /**
   * Update vocabulary if it changed since last time it was updated.
   *
   * @param uri the identifier of the vocabulary
   *
   * @return true if the update happened, false otherwise
   */
  boolean updateIfChanged(String uri) throws IOException, RegistryException;
}
