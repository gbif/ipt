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

import org.gbif.ipt.model.Extension;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.impl.ExtensionManagerImpl;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import com.google.inject.ImplementedBy;

/**
 * This interface details ALL methods associated with the DwC extensions.
 */
@ImplementedBy(ExtensionManagerImpl.class)
public interface ExtensionManager {

  /**
   * Safely remove an installed extension by its unique rowType, making sure no mappings to this extension exist.
   *
   * @param rowType of installed extension to remove
   *
   * @throws DeletionNotAllowedException if at least one mapping to this extension exists preventing deletion
   */
  void uninstallSafely(String rowType) throws DeletionNotAllowedException;

  /**
   * Update an installed extension to the latest version, identified by its rowType.
   */
  void update(String rowType) throws IOException;

  /**
   * Update extension if it changed since last time it was updated.
   *
   * @param rowType the rowType of the extension
   *
   * @return true if the update happened, false otherwise
   */
  boolean updateIfChanged(String rowType) throws IOException, RegistryException;

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
   * List all available extensions available for the given core. If no core is specified, list all installed
   * extensions.
   * </br>
   * Extensions available for a given core, will be determined by the extension's keywords list. For example, all
   * extensions having keyword dwc:Taxon are available for the Taxon core. An extension with an empty
   * keywords list will make the extension available for all cores.
   * </br>
   * Please note that extensions having an empty keywords list qualify for use by any core.
   *
   * @param coreRowType core row type, e.g. http://rs.tdwg.org/dwc/terms/Occurrence
   *
   * @return list including extensions available for the given core, all extensions installed if no core is specified,
   * or an empty list if no core extensions have been installed yet
   */
  List<Extension> list(String coreRowType);

  /**
   * List only the available core extensions.
   *
   * @return list including core extensions available, or an empty list if no core extensions have been installed yet
   */
  List<Extension> listCore();

  /**
   * List only the available core extensions available for the given core. If no core is specified, list all installed
   * core extensions.
   * </br>
   * Core extensions available for a given core, will be determined by the extension's keywords list. For example,
   * all core extensions having keyword dwc:Event are available for the Event core. An extension with an empty
   * keywords list will NOT make the extension available for all cores.
   *
   * @param coreRowType core row type, e.g. http://rs.tdwg.org/dwc/terms/Occurrence
   *
   * @return list including core extensions available for the given core, all core extensions installed if no core is
   * installed, or an empty list if no core extensions have been installed yet
   */
  List<Extension> listCore(String coreRowType);

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

  /**
   * Get a list of all (term) groups/classes in an extension that are redundant, excluding Record-Level terms.
   * A redundant group/class, is a group/class that is already included in the core extension.
   *
   * @return list of redundant groups in an extension
   */
  List<String> getRedundantGroups(Extension extension, Extension core);
}
