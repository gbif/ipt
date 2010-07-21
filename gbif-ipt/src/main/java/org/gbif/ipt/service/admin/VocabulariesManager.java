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
   * @param url original location of the vocabulary
   */
  public void delete(URL url);

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
   * Downloads the latest version from the vocabulary URL and udpates all related concepts & terms
   * 
   * @param vocabulary the vocabulary to update
   */
  public void update(Vocabulary vocabulary);

}
