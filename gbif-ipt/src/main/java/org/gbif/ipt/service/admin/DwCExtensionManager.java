/**
 * 
 */
package org.gbif.ipt.service.admin;

import org.gbif.ipt.model.Extension;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.admin.impl.DwCExtensionManagerImpl;

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
@ImplementedBy(DwCExtensionManagerImpl.class)
public interface DwCExtensionManager {

  /**
   * Remove an installed extension by its unique rowType
   * 
   * @param rowType
   */
  public void delete(String rowType);

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
   * Load all installed extensions from the data dir
   * 
   * @return number of extensions that have been loaded successfully
   */
  public int load();

}
