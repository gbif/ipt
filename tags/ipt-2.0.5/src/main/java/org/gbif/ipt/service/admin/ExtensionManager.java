package org.gbif.ipt.service.admin;

import org.gbif.ipt.model.Extension;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.admin.impl.ExtensionManagerImpl;

import java.net.URL;
import java.util.List;

import com.google.inject.ImplementedBy;

/**
 * This interface details ALL methods associated with the DwC extensions.
 */
@ImplementedBy(ExtensionManagerImpl.class)
public interface ExtensionManager {

  /**
   * Remove an installed extension by its unique rowType.
   */
  void delete(String rowType) throws DeletionNotAllowedException;

  /**
   * Get a locally installed extension by its rowType.
   *
   * @return extension for that rowtype or null if not installed
   */
  Extension get(String rowType);

  /**
   * Downloads an extension to the local cache and installs it for mapping. If the file is already locally existing
   * overwrite the older copy.
   *
   * @param url the url that returns the xml based extension definition
   */
  Extension install(URL url) throws InvalidConfigException;

  /**
   * List all installed extensions.
   *
   * @return list of installed IPT extensions
   */
  List<Extension> list();

  /**
   * List all available extensions available for the given core.
   *
   * @param coreRowType extension
   */
  List<Extension> list(String coreRowType);

  /**
   * List only the available core extensions.
   *
   * @return list including only core extensions, or an empty list if no core extensions have been installed yet
   */
  List<Extension> listCore();

  /**
   * Load all installed extensions from the data dir.
   *
   * @return number of extensions that have been loaded successfully
   */
  int load();

  /**
   * Install all core type extensions.
   */
  void installCoreTypes() throws InvalidConfigException;
}
