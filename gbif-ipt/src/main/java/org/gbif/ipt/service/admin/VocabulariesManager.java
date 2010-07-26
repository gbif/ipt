/**
 * 
 */
package org.gbif.ipt.service.admin;

import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.service.admin.impl.VocabulariesManagerImpl;

import com.google.inject.ImplementedBy;

import java.net.URL;
import java.util.List;

/**
 * This interface details ALL methods associated with the vocabularies within the IPT.
 * 
 * @author tim
 */
@ImplementedBy(VocabulariesManagerImpl.class)
public interface VocabulariesManager {

  /**
   * removes a local vocabulary copy
   * 
   * @param uri unique URI identifying the vocabulary as given in the vocabulary definition
   */
  public void delete(String uri);

  /**
   * Retrieve vocabulary by its unique global URI identifier from installed vocabularies.
   * 
   * @param uri unique URI identifying the vocabulary as given in the vocabulary definition
   * @return the installed vocabulary or null if not found
   */
  public Vocabulary get(String uri);

  /**
   * Returns the parsed vocabulary located at the given URL. If downloaded already it will return the cached copy or
   * otherwise download it from the URL.
   * 
   * @param url the resolvable URL that locates the xml vocabulary definition
   * @return
   */
  public Vocabulary get(URL url);

  /**
   * Lists all locally known vocabularies
   * 
   * @return
   */
  public List<Vocabulary> list();

  /**
   * Load all known vocabularies from the data dir
   * 
   * @return number of vocabularies that have been loaded successfully
   */
  public int load();

  /**
   * Downloads the latest version for the locally known vocabuarlies by looking up the latest registry entry
   * for their URI. Udpates all related concepts & terms
   * 
   */
  public void updateAll();

}
