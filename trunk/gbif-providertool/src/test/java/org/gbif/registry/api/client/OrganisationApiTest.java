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
package org.gbif.registry.api.client;

import junit.framework.Assert;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.httpclient.HttpStatus;
import org.gbif.registry.api.client.GbrdsRegistry.CreateOrgRequest;
import org.gbif.registry.api.client.GbrdsRegistry.CreateOrgResponse;
import org.gbif.registry.api.client.GbrdsRegistry.DeleteOrgRequest;
import org.gbif.registry.api.client.GbrdsRegistry.DeleteOrgResponse;
import org.gbif.registry.api.client.GbrdsRegistry.ListOrgRequest;
import org.gbif.registry.api.client.GbrdsRegistry.ReadOrgRequest;
import org.gbif.registry.api.client.GbrdsRegistry.ReadOrgResponse;
import org.gbif.registry.api.client.GbrdsRegistry.UpdateOrgRequest;
import org.gbif.registry.api.client.GbrdsRegistry.UpdateOrgResponse;
import org.gbif.registry.api.client.GbrdsRegistry.ValidateOrgCredentialsResponse;
import org.gbif.registry.api.client.Gbrds.Credentials;
import org.gbif.registry.api.client.Gbrds.OrganisationApi;
import org.junit.Test;

import java.util.List;

/**
 * Unit testing coverage for {@link OrganisationApi}.
 */
public class OrganisationApiTest {

  private static Gbrds gbif = GbrdsRegistry.init("http://gbrdsdev.gbif.org");
  private static OrganisationApi api = gbif.getOrganisationApi();

  private static final GbrdsOrganisation org = GbrdsOrganisation.builder().name(
      "Organisation Test").primaryContactEmail("eightysteele@gmail.com").primaryContactType(
      "technical").nodeKey("us").build();

  /**
   * Test method for
   * {@link org.gbif.registry.api.client.Gbrds.OrganisationApi#create(org.gbif.registry.api.client.GbrdsOrganisation)}
   * .
   */
  @Test
  public final void testCreate() {
    GbrdsOrganisation result = null;
    try {
      CreateOrgRequest request = gbif.getOrganisationApi().create(org);
      CreateOrgResponse response = request.execute();
      result = response.getResult();
      Assert.assertNotNull(result);
      Assert.assertNotNull(result.getKey());
      Assert.assertNotNull(result.getPassword());
    } finally {
      if (result != null) {
        Assert.assertTrue(gbif.getOrganisationApi().delete(result).execute().getResult());
      }
    }
  }

  /**
   * Test method for
   * {@link org.gbif.registry.api.client.Gbrds.OrganisationApi#delete(org.gbif.registry.api.client.GbrdsOrganisation)}
   * .
   */
  @Test
  public final void testDelete() {
    DeleteOrgRequest request = gbif.getOrganisationApi().delete(
        GbrdsOrganisation.builder().password("DaSAWqmvQ0z").key(
            "c3513d8e-bf68-42ec-b125-2574cf022e99").primaryContactType(
            "technical").build());
    DeleteOrgResponse response = request.execute();
    Assert.assertNotNull(response.getResult());
    Assert.assertTrue(response.getResult());
  }

  /**
   * Test method for
   * {@link org.gbif.registry.api.client.Gbrds.OrganisationApi#list()} .
   */
  @Test
  public final void testList() {
    ListOrgRequest request = gbif.getOrganisationApi().list();
    List<GbrdsOrganisation> list = request.execute().getResult();
    Assert.assertNotNull(list);
    System.out.println(list);

  }

  /**
   * Test method for
   * {@link org.gbif.registry.api.client.Gbrds.OrganisationApi#read(java.lang.String)}
   * .
   */
  @Test
  public final void testRead() {
    String orgKey = "c3513d8e-bf68-42ec-b125-2574cf022e99";
    ReadOrgRequest request = gbif.getOrganisationApi().read(orgKey);
    ReadOrgResponse response = request.execute();
    GbrdsOrganisation org = response.getResult();
    Assert.assertNotNull(org);
    Assert.assertEquals(orgKey, org.getKey());
  }

  /**
   * Test method for
   * {@link org.gbif.registry.api.client.Gbrds.OrganisationApi#update(org.gbif.registry.api.client.GbrdsOrganisation)}
   * .
   */
  @Test
  public final void testUpdate() {
    UpdateOrgRequest request = gbif.getOrganisationApi().update(
        GbrdsOrganisation.builder().password("password").key(
            "3780d048-8e18-4c0c-afcd-cb6389df56de").primaryContactType(
            "technical").description("Test Update").build());
    UpdateOrgResponse response = request.execute();
    Assert.assertNotNull(response);
    GbrdsOrganisation org = response.getResult();
    Assert.assertEquals("Test Updated", org.getDescription());
  }

  @Test
  public final void testValidateCredentials() {
    String orgKey = "3780d048-8e18-4c0c-afcd-cb6389df56de";
    Credentials creds = Credentials.with(orgKey, "password");
    ValidateOrgCredentialsResponse r;

    r = api.validateCredentials(orgKey, creds).execute();
    assertTrue(r.getStatus() == HttpStatus.SC_OK);
    assertTrue(api.validateCredentials(orgKey, creds).execute().getResult());

    creds = Credentials.with("INVALID_ID", "INVALID_PASSWORD");
    r = api.validateCredentials(orgKey, creds).execute();
    assertTrue(r.getStatus() == HttpStatus.SC_UNAUTHORIZED
        || r.getStatus() == HttpStatus.SC_NOT_FOUND);
    assertFalse(r.getResult());

  }
}
