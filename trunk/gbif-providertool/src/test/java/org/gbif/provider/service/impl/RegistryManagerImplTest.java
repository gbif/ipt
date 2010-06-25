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

import org.gbif.provider.model.Organisation;
import org.gbif.provider.service.RegistryException;
import org.gbif.provider.service.RegistryManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.ContextAwareTestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class can be used for unit testing {@link RegistryManagerImpl}.
 * 
 */
public class RegistryManagerImplTest extends ContextAwareTestBase {

  @Autowired
  private RegistryManager registryManager;

  @Autowired
  protected AppConfig cfg;

  @Test
  public void testIsOrgRegistered() {

    Organisation org = Organisation.builder().organisationKey(
        "DB03E430-304B-11DF-A430-CB15FDE703B2").password("73QVMX8lE5R").build();
    assertTrue(registryManager.isOrganisationRegistered(org));
    System.out.println(String.format("Success=%s", org));
  }

  @Test
  public void testRegisterIptLocalhost() throws RegistryException {
    cfg.setBaseUrl("http://localhost:8080");
    registryManager.registerIPT();
  }

  /**
   * Tests registering an {@link Organisation} and then verifying it's
   * credentials.
   */
  @Test
  public void testRegisterOrganisation() {
    Organisation org = Organisation.builder().name(
        "IPT Test Organization Version 3.0").nodeKey("sp2000").primaryContactEmail(
        "tuco@berkeley.edu").build();
    try {
      org = registryManager.registerOrganisation(org);
      assertTrue(registryManager.isOrganisationRegistered(org));
      assertTrue(!registryManager.isOrganisationRegistered(Organisation.builder().build()));
      org.setDescription("Example description");
      registryManager.updateOrganisation(org);
      System.out.println(String.format("Success=%s", org));
    } catch (RegistryException e) {
      System.out.println(String.format("Error=%s, Org=%s", e.getMessage(), org));
      fail();
    }
  }
}
