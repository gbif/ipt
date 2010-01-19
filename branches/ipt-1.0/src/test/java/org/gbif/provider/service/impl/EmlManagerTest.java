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
package org.gbif.provider.service.impl;

import org.gbif.provider.model.BBox;
import org.gbif.provider.model.eml.Eml;
import org.gbif.provider.service.EmlManager;
import org.gbif.provider.util.ResourceTestBase;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * TODO: Documentation.
 * 
 */
public class EmlManagerTest extends ResourceTestBase {
  @Autowired
  private EmlManager emlManager;

  @Test
  public void testLoadResource() {
    resource = this.getResourceMock();

    Eml eml = emlManager.load(resource);
    eml.addKeyword("Italia");
    eml.addKeyword("Romans");
    eml.addKeyword("River");
    eml.addKeyword("Climate change");
    BBox bbox = new BBox(-3.0, -123.0, 12.0, 32.0);
    eml.getGeographicCoverage().setBoundingCoordinates(bbox);
    eml.getGeographicCoverage().setDescription(
        "ick weiss auch nicht welche Ecke der Welt das sein soll...");

    emlManager.save(eml);
  }

  @Test
  public void testPublishResource() {
    setupOccResource();
    try {
      emlManager.publishNewEmlVersion(resource);
    } catch (IOException e) {
      fail();
      e.printStackTrace();
    }
  }

  @Test
  public void testSaveResource() {
    resource = this.getResourceMock();
    Eml eml = new Eml();
    eml.setResource(resource);
    eml.addKeyword("Italia");
    eml.addKeyword("Romans");
    eml.addKeyword("River");
    eml.addKeyword("Climate change");
    eml.addKeyword("Mötörhead");
    BBox bbox = new BBox(-3.0, -123.0, 12.0, 32.0);
    eml.getGeographicCoverage().setBoundingCoordinates(bbox);
    eml.getGeographicCoverage().setDescription(
        "ick weiss auch nicht welche Ecke der Welt däs sein soll...");

    emlManager.save(eml);
  }

}
