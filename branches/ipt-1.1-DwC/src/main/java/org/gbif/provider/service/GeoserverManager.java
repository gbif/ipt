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

import org.gbif.provider.model.OccurrenceResource;

import java.io.IOException;

/**
 * TODO: Documentation.
 * 
 */
public interface GeoserverManager {

  String buildFeatureTypeDescriptor(OccurrenceResource resource);

  boolean login(String username, String password, String geoserverURL);

  void reloadCatalog() throws IOException;

  void removeFeatureType(OccurrenceResource resource) throws IOException;

  void updateCatalog() throws IOException;

  void updateFeatureType(OccurrenceResource resource) throws IOException;

  void updateGeowebcache(OccurrenceResource resource);

}