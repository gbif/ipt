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
package org.gbif.provider.geoserver;

import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.factory.ResourceFactory;
import org.gbif.provider.service.GeoserverManager;
import org.gbif.provider.util.ResourceTestBase;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * TODO: Documentation.
 * 
 */
public class GeoserverUtilsTest extends ResourceTestBase {
  @Autowired
  private GeoserverManager utils;
  @Autowired
  private ResourceFactory resourceFactory;

  @Test
  public void testFeatureInfoGen() {
    setupOccResource();
    resource.setTitle("Walter Ulbrich");
    String feature = utils.buildFeatureTypeDescriptor((OccurrenceResource) resource);
    assertTrue(feature != null);
    assertTrue(feature.indexOf("Walter Ulbrich") > 0);
  }

  @Test
  public void testReload() throws IOException {
    utils.reloadCatalog();
  }
}
