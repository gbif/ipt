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

import static org.junit.Assert.fail;

import org.gbif.provider.model.eml.Agent;
import org.gbif.provider.model.eml.Eml;
import org.gbif.provider.model.eml.Role;
import org.gbif.provider.service.EmlManager;
import org.gbif.provider.util.ResourceTestBase;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
    Agent a = new Agent();
    a.setFirstName("Aaron");
    a.setRole(Role.ASSOCIATED_PARTY);
    eml.addAssociatedParty(a);
    emlManager.save(eml);
    Eml loadedEml = emlManager.load(resource);
    Assert.assertTrue(loadedEml.getAssociatedParties().size() == 1);
    Assert.assertEquals(loadedEml.getAssociatedParties().get(0), a);
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
    emlManager.save(eml);
  }

}
