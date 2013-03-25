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

import org.gbif.provider.model.Resource;

import java.util.Collection;

/**
 * TODO: Documentation.
 * 
 */
public interface RegistryManager {
  void deleteResource(Resource resource) throws RegistryException;

  /**
   * Calls the central registry to receive a list of the Extensions that are
   * available
   * 
   * @return The (supposedly) publicly accessible URLs of the extensions
   */
  Collection<String> listAllExtensions();

  /**
   * Calls the central registry to receive a list of the ThesaurusVocabularies
   * that are available
   * 
   * @return The (supposedly) publicly accessible URLs of the thesauri
   */
  Collection<String> listAllThesauri();

  String registerIPT() throws RegistryException;

  String registerOrg() throws RegistryException;

  String registerResource(Resource resource) throws RegistryException;

  boolean testLogin();

  void updateIPT() throws RegistryException;

  void updateOrg() throws RegistryException;

  void updateResource(Resource resource) throws RegistryException;
}
