/*
 * Copyright 2010 Regents of the University of California, University of Kansas.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.gbif.provider.service.RegistryManager;
import org.gbif.registry.api.client.Gbrds.OrgCredentials;
import org.junit.Test;

/**
 *
 */
public class RegistryManagerTest {

  private final static RegistryManager rm = new RegistryManagerImpl();

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#buildGbrdsOrganisation(org.gbif.provider.model.ResourceMetadata)}
   * .
   */
  @Test
  public final void testBuildGbrdsOrganisation() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#buildGbrdsResource(org.gbif.provider.model.ResourceMetadata)}
   * .
   */
  @Test
  public final void testBuildGbrdsResource() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#createOrg(org.gbif.registry.api.client.GbrdsOrganisation)}
   * .
   */
  @Test
  public final void testCreateGbrdsOrganisation() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#createResource(org.gbif.registry.api.client.GbrdsResource, org.gbif.registry.api.client.Gbrds.OrgCredentials)}
   * .
   */
  @Test
  public final void testCreateGbrdsResource() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#createService(org.gbif.registry.api.client.GbrdsService, org.gbif.registry.api.client.Gbrds.OrgCredentials)}
   * .
   */
  @Test
  public final void testCreateGbrdsService() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#deleteResource(java.lang.String, org.gbif.registry.api.client.Gbrds.OrgCredentials)}
   * .
   */
  @Test
  public final void testDeleteGbrdsResource() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#deleteService(java.lang.String, org.gbif.registry.api.client.Gbrds.OrgCredentials)}
   * .
   */
  @Test
  public final void testDeleteGbrdsService() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#getCreds(java.lang.String, java.lang.String)}
   * .
   */
  @Test
  public final void testGetCreds() {
    // Tests valid credentials:
    String key = "3780d048-8e18-4c0c-afcd-cb6389df56de";
    String pass = "password";
    OrgCredentials creds = rm.getCreds(key, pass);
    assertNotNull(creds);
    assertEquals(key, creds.getKey());
    assertEquals(pass, creds.getPassword());

    // Tests invalid credentials:
    key = "foo";
    pass = "bar";
    assertNull(rm.getCreds(key, pass));

    // Tests invalid inputs:
    assertNull(rm.getCreds(null, null));
    assertNull(rm.getCreds(null, ""));
    assertNull(rm.getCreds(null, " "));
    assertNull(rm.getCreds(null, "x"));
    assertNull(rm.getCreds("", null));
    assertNull(rm.getCreds(" ", null));
    assertNull(rm.getCreds("x", null));
    assertNull(rm.getCreds("", ""));
    assertNull(rm.getCreds(" ", ""));
    assertNull(rm.getCreds("", " "));
    assertNull(rm.getCreds(" ", " "));
  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#listAllExtensions()}
   * .
   */
  @Test
  public final void testListAllExtensions() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#listAllThesauri()}
   * .
   */
  @Test
  public final void testListAllThesauri() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#listServices(java.lang.String)}
   * .
   */
  @Test
  public final void testListGbrdsServices() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#readOrg(java.lang.String)}
   * .
   */
  @Test
  public final void testReadGbrdsOrganisation() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#readGbrdsResource(java.lang.String)}
   * .
   */
  @Test
  public final void testReadGbrdsResource() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#updateOrg(org.gbif.registry.api.client.GbrdsOrganisation, org.gbif.registry.api.client.Gbrds.OrgCredentials)}
   * .
   */
  @Test
  public final void testUpdateGbrdsOrganisation() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#updateResource(org.gbif.registry.api.client.GbrdsResource, org.gbif.registry.api.client.Gbrds.OrgCredentials)}
   * .
   */
  @Test
  public final void testUpdateGbrdsResource() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#updateService(org.gbif.registry.api.client.GbrdsService, org.gbif.registry.api.client.Gbrds.OrgCredentials)}
   * .
   */
  @Test
  public final void testUpdateGbrdsService() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#validateCreds(org.gbif.registry.api.client.Gbrds.OrgCredentials)}
   * .
   */
  @Test
  public final void testValidateCredentials() {
    fail("Not yet implemented"); // TODO
  }

}
