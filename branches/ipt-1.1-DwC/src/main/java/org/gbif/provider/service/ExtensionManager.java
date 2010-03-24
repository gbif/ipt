/*
 * Copyright 2009 GBIF.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gbif.provider.service;

import org.gbif.provider.model.Extension;

import java.sql.SQLException;
import java.util.List;

/**
 * TODO: Documentation.
 * 
 */
public interface ExtensionManager extends GenericManager<Extension> {

  Extension getCore() throws SQLException;

  Extension getExtensionByUri(String uri);

  /**
   * Get all installed extensions for a certain core entity extension type (i.e.
   * currently occurrence/checklist)
   * 
   * @param type
   * @return
   */
  List<Extension> getInstalledExtensions();

  /**
   * Install a new extension, i.e. saving the extension entity and creating a
   * matching, new and empty extension table
   * 
   * @param extension
   * @throws SQLException
   */
  void installExtension(Extension extension);

  /**
   * Remove an extension, i.e. deleteing the extension instance and dropping the
   * matching extension table with all its records
   * 
   * @param extension
   * @throws SQLException
   */
  void removeExtension(Extension extension);

  /**
   * This will communicate with the central registry of extensions and update
   * the database extension tables When a thesaurus is found referenced by an
   * extension, it will be automatically sychronised.
   */
  void synchroniseExtensionsWithRepository();

}
