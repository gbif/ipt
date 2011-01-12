/*
 * Copyright 2010 GBIF.
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

import org.apache.commons.httpclient.HttpStatus;
import org.gbif.provider.service.RegistryManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.ContextAwareTestBase;
import org.gbif.registry.api.client.GbrdsOrganisation;
import org.gbif.registry.api.client.GbrdsResource;
import org.gbif.registry.api.client.Gbrds.BadCredentialsException;
import org.gbif.registry.api.client.Gbrds.CreateOrgResponse;
import org.gbif.registry.api.client.Gbrds.CreateResourceResponse;
import org.gbif.registry.api.client.Gbrds.OrgCredentials;
import org.gbif.registry.api.client.Gbrds.ReadOrgResponse;
import org.gbif.registry.api.client.Gbrds.UpdateOrgResponse;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
public class RegistryManagerTest extends ContextAwareTestBase {

  @Autowired
  RegistryManager registry;

  @Autowired
  AppConfig appConfig;

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#createOrg(org.gbif.registry.api.client.GbrdsOrganisation)}
   * .
   */
  @Test
  public final void testCreateOrg() {
    // Valid organisations:
    String name = "Name";
    String type = "technical";
    String email = "eightysteele@gmail.com";
    String nodeKey = "us";
    GbrdsOrganisation.Builder b = GbrdsOrganisation.builder().name(name).primaryContactType(
        type).primaryContactEmail(email).nodeKey(nodeKey);
    CreateOrgResponse cor = registry.createOrg(b.build());
    assertTrue(cor.getStatus() == HttpStatus.SC_CREATED);
    OrgCredentials creds = cor.getResult();
    assertNotNull(creds);
    assertTrue(registry.validateCreds(creds).getResult());

    GbrdsOrganisation.Builder go = GbrdsOrganisation.builder().description("d").descriptionLanguage(
        "es").homepageURL("hu").name("n").nameLanguage("es").nodeKey("sp2000").nodeName(
        "Species 2000").primaryContactAddress("pca").primaryContactDescription(
        "pcd").primaryContactName("pcn").primaryContactPhone("pcp").primaryContactType(
        "administrative").primaryContactEmail("eightysteele@gmail.com");
    cor = registry.createOrg(go.build());
    OrgCredentials oc = cor.getResult();
    go.key(oc.getKey());

    ReadOrgResponse ror = registry.readOrg(go.getKey());
    assertEquals(go.build(), ror.getResult());

    // Invalid organisations:
    try {
      registry.createOrg(null);
      fail();
    } catch (Exception e) {
      System.out.println(e);
    }
    try {
      registry.createOrg(GbrdsOrganisation.builder().build());
      fail();
    } catch (Exception e) {
      System.out.println(e);
    }

    // Invalid organisation names:
    try {
      registry.createOrg(b.name(null).build());
      fail();
    } catch (Exception e) {
      System.out.println(e);
      b.name(name);
    }
    try {
      registry.createOrg(b.name("").build());
      fail();
    } catch (Exception e) {
      System.out.println(e);
      b.name(name);
    }
    try {
      registry.createOrg(b.name("   ").build());
      fail();
    } catch (Exception e) {
      System.out.println(e);
      b.name(name);
    }

    // Invalid organisation contact type:
    try {
      registry.createOrg(b.primaryContactType(null).build());
      fail();
    } catch (Exception e) {
      System.out.println(e);
      b.primaryContactType(type);
    }
    try {
      registry.createOrg(b.primaryContactType("").build());
      fail();
    } catch (Exception e) {
      System.out.println(e);
      b.primaryContactType(type);
    }
    try {
      registry.createOrg(b.primaryContactType("   ").build());
      fail();
    } catch (Exception e) {
      System.out.println(e);
      b.primaryContactType(type);
    }
    try {
      registry.createOrg(b.primaryContactType("InvalidType").build());
      fail();
    } catch (Exception e) {
      System.out.println(e);
      b.primaryContactType(type);
    }

    // Invalid organisation contact email:
    try {
      registry.createOrg(b.primaryContactEmail(null).build());
      fail();
    } catch (Exception e) {
      System.out.println(e);
      b.primaryContactEmail(email);
    }
    try {
      registry.createOrg(b.primaryContactEmail("").build());
      fail();
    } catch (Exception e) {
      System.out.println(e);
      b.primaryContactEmail(email);
    }
    try {
      registry.createOrg(b.primaryContactEmail("   ").build());
      fail();
    } catch (Exception e) {
      System.out.println(e);
      b.primaryContactEmail(email);
    }

    // Invalid organisation node key:
    try {
      registry.createOrg(b.nodeKey(null).build());
      fail();
    } catch (Exception e) {
      System.out.println(e);
      b.primaryContactEmail(email);
    }
    try {
      registry.createOrg(b.nodeKey("").build());
      fail();
    } catch (Exception e) {
      System.out.println(e);
      b.nodeKey(nodeKey);
    }
    try {
      registry.createOrg(b.nodeKey("   ").build());
      fail();
    } catch (Exception e) {
      System.out.println(e);
      b.nodeKey(nodeKey);
    }
    cor = registry.createOrg(b.nodeKey("UnknownKey").build());
    assertFalse(HttpStatus.SC_OK == cor.getStatus());
    assertNull(cor.getResult());
  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#createResource(org.gbif.registry.api.client.GbrdsResource, org.gbif.registry.api.client.Gbrds.OrgCredentials)}
   * .
   */
  @Test
  public final void testCreateResource() {
    OrgCredentials creds = registry.createOrg(
        GbrdsOrganisation.builder().name("n").primaryContactType("technical").primaryContactEmail(
            "eightysteele@gmail.com").nodeKey("us").build()).getResult();

    // Valid resource with valid creds:
    GbrdsResource.Builder r = GbrdsResource.builder().name("name").primaryContactType(
        "technical").primaryContactEmail("eightysteele@gmail.com").organisationKey(
        creds.getKey());
    try {
      CreateResourceResponse crr = registry.createResource(r.build(), creds);
      assertTrue(crr.getStatus() == HttpStatus.SC_CREATED);
      assertNotNull(crr.getResult());
      assertNotNull(crr.getResult().getKey());
    } catch (BadCredentialsException e) {
      fail();
    }

    // Valid resource with invalid creds:
    try {
      registry.createResource(r.build(),
          OrgCredentials.with("invalid", "creds"));
      fail();
    } catch (BadCredentialsException e) {
      System.out.println(e.toString());
    }

    // Invalid organisation key:
    try {
      registry.createResource(r.organisationKey(null).build(), creds);
      fail();
    } catch (Exception e) {
      System.out.println(e.toString());
    }
    try {
      registry.createResource(r.organisationKey("").build(), creds);
      fail();
    } catch (Exception e) {
      System.out.println(e.toString());
    }
    try {
      registry.createResource(r.organisationKey("   ").build(), creds);
      fail();
    } catch (Exception e) {
      System.out.println(e.toString());
    }
    try {
      CreateResourceResponse crr = registry.createResource(r.organisationKey(
          "unknown").build(), creds);
      assertFalse(crr.getStatus() == HttpStatus.SC_CREATED);
      System.out.println(crr.getBody());
    } catch (Exception e) {
      System.out.println(e.toString());
    }

    // Invalid email:
    try {
      registry.createResource(r.primaryContactEmail(null).build(), creds);
      fail();
    } catch (Exception e) {
      System.out.println(e.toString());
    }
    try {
      registry.createResource(r.primaryContactEmail("").build(), creds);
      fail();
    } catch (Exception e) {
      System.out.println(e.toString());
    }
    try {
      registry.createResource(r.primaryContactEmail("   ").build(), creds);
      fail();
    } catch (Exception e) {
      System.out.println(e.toString());
    }

    // Invalid contact type:
    try {
      registry.createResource(r.primaryContactType(null).build(), creds);
      fail();
    } catch (Exception e) {
      System.out.println(e.toString());
    }
    try {
      registry.createResource(r.primaryContactType("").build(), creds);
      fail();
    } catch (Exception e) {
      System.out.println(e.toString());
    }
    try {
      registry.createResource(r.primaryContactType("   ").build(), creds);
      fail();
    } catch (Exception e) {
      System.out.println(e.toString());
    }
    try {
      registry.createResource(r.primaryContactType("unknown").build(), creds);
      fail();
    } catch (Exception e) {
      System.out.println(e.toString());
    }

    // Invalid name:
    try {
      registry.createResource(r.name(null).build(), creds);
      fail();
    } catch (Exception e) {
      System.out.println(e.toString());
    }
    try {
      registry.createResource(r.name("").build(), creds);
      fail();
    } catch (Exception e) {
      System.out.println(e.toString());
    }
    try {
      registry.createResource(r.name("   ").build(), creds);
      fail();
    } catch (Exception e) {
      System.out.println(e.toString());
    }
  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#createService(org.gbif.registry.api.client.GbrdsService, org.gbif.registry.api.client.Gbrds.OrgCredentials)}
   * .
   */
  @Test
  public final void testCreateService() {

  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#deleteResource(java.lang.String, org.gbif.registry.api.client.Gbrds.OrgCredentials)}
   * .
   */
  @Test
  public final void testDeleteResource() {

  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#deleteService(java.lang.String, org.gbif.registry.api.client.Gbrds.OrgCredentials)}
   * .
   */
  @Test
  public final void testDeleteService() {

  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#getCreds(java.lang.String, java.lang.String)}
   * .
   */
  @Test
  public final void testGetCreds() {

  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#getMeta(org.gbif.registry.api.client.GbrdsOrganisation)}
   * .
   */
  @Test
  public final void testGetMetaGbrdsOrganisation() {

  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#getMeta(org.gbif.registry.api.client.GbrdsResource)}
   * .
   */
  @Test
  public final void testGetMetaGbrdsResource() {

  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#getOrgBuilder(org.gbif.provider.model.ResourceMetadata)}
   * .
   */
  @Test
  public final void testGetOrgBuilder() {

  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#getResourceBuilder(org.gbif.provider.model.ResourceMetadata)}
   * .
   */
  @Test
  public final void testGetResourceBuilder() {

  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#getServiceUrl(org.gbif.provider.model.voc.ServiceType, org.gbif.provider.model.Resource)}
   * .
   */
  @Test
  public final void testGetServiceUrl() {

  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#isLocalhost(java.lang.String)}
   * .
   */
  @Test
  public final void testIsLocalhost() {

  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#listAllExtensions()}
   * .
   */
  @Test
  public final void testListAllExtensions() {

  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#listAllThesauri()}
   * .
   */
  @Test
  public final void testListAllThesauri() {

  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#listServices(java.lang.String)}
   * .
   */
  @Test
  public final void testListServices() {

  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#orgExists(java.lang.String)}
   * .
   */
  @Test
  public final void testOrgExists() {

  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#readGbrdsResource(java.lang.String)}
   * .
   */
  @Test
  public final void testReadGbrdsResource() {

  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#readOrg(java.lang.String)}
   * .
   */
  @Test
  public final void testReadOrg() {

  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#resourceExists(java.lang.String)}
   * .
   */
  @Test
  public final void testResourceExists() {

  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#updateIptRssServiceUrl(java.lang.String, java.lang.String, java.lang.String)}
   * .
   */
  @Test
  public final void testUpdateIptRssServiceUrl() {

  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#updateResource(org.gbif.registry.api.client.GbrdsResource, org.gbif.registry.api.client.Gbrds.OrgCredentials)}
   * .
   */
  @Test
  public final void testUpdateResource() {

  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#updateService(org.gbif.registry.api.client.GbrdsService, org.gbif.registry.api.client.Gbrds.OrgCredentials)}
   * .
   */
  @Test
  public final void testUpdateService() {

  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#updateServiceUrls(java.util.List)}
   * .
   */
  @Test
  public final void testUpdateServiceUrls() {

  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#validateCreds(org.gbif.registry.api.client.Gbrds.OrgCredentials)}
   * .
   */
  @Test
  public final void testValidateCreds() {

  }

  /**
   * Test method for
   * {@link org.gbif.provider.service.impl.RegistryManagerImpl#updateOrg(org.gbif.registry.api.client.GbrdsOrganisation, org.gbif.registry.api.client.Gbrds.OrgCredentials)}
   * .
   */
  // @Test
  public final void UpdateOrg() {
    // Creates a new organisation for testing:
    String name = "Name";
    String type = "technical";
    String email = "eightysteele@gmail.com";
    String nodeKey = "us";
    GbrdsOrganisation.Builder b = GbrdsOrganisation.builder().name(name).primaryContactType(
        type).primaryContactEmail(email).nodeKey(nodeKey);
    CreateOrgResponse cor = registry.createOrg(b.build());
    OrgCredentials creds = cor.getResult();
    b.key(creds.getKey());

    UpdateOrgResponse uor;
    String val = "updated";

    // Updates name:
    try {
      GbrdsOrganisation go = b.description("d").descriptionLanguage("es").homepageURL(
          "hu").name("n").nameLanguage("es").nodeKey("sp2000").nodeName(
          "Species 2000").primaryContactAddress("pca").primaryContactDescription(
          "pcd").primaryContactName("pcn").primaryContactPhone("pcp").primaryContactType(
          "administrative").primaryContactEmail("gtuco.btuco@gmail.com").build();
      System.out.println(go);
      uor = registry.updateOrg(go, creds);
      assertTrue(uor.getStatus() == HttpStatus.SC_OK);
      System.out.println(creds);
      ReadOrgResponse ror = registry.readOrg(creds.getKey());
      assertEquals(go, registry.readOrg(creds.getKey()).getResult());
    } catch (BadCredentialsException e) {
      fail(e.toString());
    }
  }

}
