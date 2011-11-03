package org.gbif.ipt.service.admin;

import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.admin.impl.VocabulariesManagerImpl;
import org.gbif.ipt.service.admin.impl.VocabulariesManagerImpl.UpdateResult;

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
   * removes a local vocabulary copy
   *
   * @param uri unique URI identifying the vocabulary as given in the vocabulary definition
   */
  void delete(String uri) throws DeletionNotAllowedException;

  /**
   * Retrieve vocabulary by its unique global URI identifier from installed vocabularies.
   *
   * @param uri unique URI identifying the vocabulary as given in the vocabulary definition
   *
   * @return the installed vocabulary or null if not found
   */
  Vocabulary get(String uri);

  /**
   * Returns the parsed vocabulary located at the given URL. If downloaded already it will return the cached copy or
   * otherwise download it from the URL.
   *
   * @param url the resolvable URL that locates the xml vocabulary definition
   */
  Vocabulary get(URL url);

  /**
   * Returns a regular map than can be used to populate html select drop downs with
   * keys=vocabulary concept identifiers and values=preferred term for the given language.
   * Defaults to english if no term for the requested language exists.
   *
   * @param uri                the identifier for the vocabulary
   * @param lang               a 2 character iso language code, e.g. DE
   * @param sortAlphabetically if true sort map values alphabetically, otherwise use native ordering
   */
  Map<String, String> getI18nVocab(String uri, String lang, boolean sortAlphabetically);

  /**
   * Lists all locally known vocabularies.
   */
  List<Vocabulary> list();

  /**
   * Load all known vocabularies from the data dir.
   *
   * @return number of vocabularies that have been loaded successfully
   */
  int load();

  /**
   * Downloads the latest version for the locally known vocabuarlies by looking up the latest registry entry
   * for their URI. Updates all related concepts & terms.
   */
  UpdateResult updateAll();

}
