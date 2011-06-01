/**
 * 
 */
package org.gbif.ipt.service.admin;

import org.gbif.ipt.model.Extension;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.admin.impl.ExtensionManagerImpl;

import com.google.inject.ImplementedBy;

import java.net.URL;
import java.util.List;

/**
 * This interface details ALL methods associated with the DwC extensions.
 * 
 * @author tim
 */
/**
 * @author markus
 */
@ImplementedBy(ExtensionManagerImpl.class)
public interface ExtensionManager {

  /**
   * Remove an installed extension by its unique rowType
   * 
   * @param rowType
   * @throws DeletionNotAllowedException
   */
  public void delete(String rowType) throws DeletionNotAllowedException;

  /**
   * Get a locally installed extension by its rowType
   * 
   * @param rowType
   * @return extension for that rowtype or null if not installed
   */
  public Extension get(String rowType);

  /**
   * Downloads an extension to the local cache and installs it for mapping. If the file is already locally existing
   * overwrite the older copy.
   * 
   * @param url the url that returns the xml based extension definition
   * @throws InvalidConfigException
   */
  public Extension install(URL url) throws InvalidConfigException;

  /**
   * List all installed extensions
   * 
   * @return list of installed IPT extensions
   */
  public List<Extension> list();

  /**
   * List all available extensions available for the given core
   * 
   * @param core extension
   * @return
   */
  public List<Extension> list(String coreRowType);

  /**
   * List only the available core extensions
   * 
   * @return
   */
  public List<Extension> listCore();

  /**
   * Load all installed extensions from the data dir
   * 
   * @return number of extensions that have been loaded successfully
   */
  public int load();

  /**
   * List all available extensions matching a registered keyword
   * 
   * @param keyword to filter by, e.g. dwc:Taxon for all taxonomic extensions
   * @return
   */
  public List<Extension> search(String keyword);

  public void installCoreTypes();

}
