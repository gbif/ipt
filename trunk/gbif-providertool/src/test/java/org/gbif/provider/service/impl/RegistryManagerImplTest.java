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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.gbif.provider.service.RegistryManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.ResourceTestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class can be used for unit testing {@link RegistryManagerImpl}.
 * 
 */
public class RegistryManagerImplTest extends ResourceTestBase {

  @Autowired
  protected AppConfig cfg;

  @Autowired
  private RegistryManager registryManager;

  @Test
  public void createGbifResource() {
  }

  @Test
  public void testAppConfig() {
    String uddiId = cfg.getOrg().getUddiID();
    String existingOrgKey = "3780d048-8e18-4c0c-afcd-cb6389df56de";
    String existingResourceKey = "3f138d32-eb85-430c-8d5d-115c2f03429e";

    // Valid Organisation key:
    try {
      cfg.getOrg().setUddiID(existingOrgKey);
      assertTrue(cfg.isOrgRegistered());
    } finally {
      cfg.getOrg().setUddiID(uddiId);
    }

    // Invalid Organisation key:
    try {
      cfg.getOrg().setUddiID("INVALID_KEY");
      assertFalse(cfg.isOrgRegistered());
    } finally {
      cfg.getOrg().setUddiID(uddiId);
    }

    // Null Organisation key:
    try {
      cfg.getOrg().setUddiID(null);
      assertFalse(cfg.isOrgRegistered());
    } finally {
      cfg.getOrg().setUddiID(uddiId);
    }

    // Empty string Organisation key:
    try {
      cfg.getOrg().setUddiID("      ");
      assertFalse(cfg.isOrgRegistered());
    } finally {
      cfg.getOrg().setUddiID(uddiId);
    }

    // Valid Resource key:
    try {
      cfg.getIpt().setUddiID(existingResourceKey);
      assertTrue(cfg.isIptRegistered());
    } finally {
      cfg.getIpt().setUddiID(uddiId);
    }

    // Invalid Resource key:
    try {
      cfg.getIpt().setUddiID("INVALID_KEY");
      assertFalse(cfg.isIptRegistered());
    } finally {
      cfg.getIpt().setUddiID(uddiId);
    }

    // Null Resource key:
    try {
      cfg.getIpt().setUddiID(null);
      assertFalse(cfg.isIptRegistered());
    } finally {
      cfg.getIpt().setUddiID(uddiId);
    }

    // Empty string Resource key:
    try {
      cfg.getIpt().setUddiID("     ");
      assertFalse(cfg.isIptRegistered());
    } finally {
      cfg.getIpt().setUddiID(uddiId);
    }
  }

  @Test
  public void testReadGbifOrganisation() {
    String key = "3780d048-8e18-4c0c-afcd-cb6389df56de";
    try {
      registryManager.readGbrdsOrganisation(key);
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }

    key = "INVALID_KEY";
    try {
      assertNull(registryManager.readGbrdsResource(key).getResult());
    } catch (Exception e) {
      fail();
    }

    key = null;
    try {
      registryManager.readGbrdsOrganisation(key);
      fail();
    } catch (Exception e) {
      assertTrue(e instanceof NullPointerException);
    }

    key = "";
    try {
      registryManager.readGbrdsOrganisation(key);
      fail();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

  }

  @Test
  public void testReadGbifResource() {
    String key = "3f138d32-eb85-430c-8d5d-115c2f03429e";
    try {
      assertNotNull(registryManager.readGbrdsResource(key).getResult());
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }

    key = "INVALID_KEY";
    try {
      assertNull(registryManager.readGbrdsResource(key).getResult());
    } catch (Exception e) {
      fail();
    }

    key = null;
    try {
      registryManager.readGbrdsResource(key);
      fail();
    } catch (Exception e) {
      assertTrue(e instanceof NullPointerException);
    }

    key = "";
    try {
      registryManager.readGbrdsResource(key);
      fail();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
  }
}
