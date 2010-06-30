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

import org.gbif.registry.api.client.GbifOrganisation;
import org.gbif.registry.api.client.GbifRegistry;
import org.gbif.registry.api.client.RegistryService;
import org.gbif.registry.api.client.GbifRegistry.CreateOrgRequest;
import org.gbif.registry.api.client.GbifRegistry.CreateOrgResponse;
import org.gbif.registry.api.client.GbifRegistry.DeleteOrgRequest;
import org.gbif.registry.api.client.GbifRegistry.DeleteOrgResponse;
import org.gbif.registry.api.client.GbifRegistry.ListOrgRequest;
import org.gbif.registry.api.client.GbifRegistry.ReadOrgRequest;
import org.gbif.registry.api.client.GbifRegistry.ReadOrgResponse;
import org.gbif.registry.api.client.GbifRegistry.UpdateOrgRequest;
import org.gbif.registry.api.client.GbifRegistry.UpdateOrgResponse;
import org.gbif.registry.api.client.RegistryService.OrganisationApi;
import org.junit.Test;

import java.util.List;

/**
 * Unit testing coverage for {@link OrganisationApi}.
 */
public class OrganisationApiTest {

  private static RegistryService gbif = GbifRegistry.init("http://gbrdsdev.gbif.org");;

  private static final GbifOrganisation org = GbifOrganisation.builder().name(
      "GBIF Production Registry Test").primaryContactEmail(
      "eightysteele@gmail.com").primaryContactType("technical").nodeKey("us").build();

  /**
   * Test method for
   * {@link org.gbif.registry.api.client.RegistryService.OrganisationApi#create(org.gbif.registry.api.client.GbifOrganisation)}
   * .
   */
  @Test
  public final void testCreate() {
    GbifOrganisation result = null;
    try {
      CreateOrgRequest request = gbif.getOrganisationApi().getCreateRequest(org);
      CreateOrgResponse response = request.execute();
      result = response.getResult();
      Assert.assertNotNull(result);
      Assert.assertNotNull(result.getKey());
      Assert.assertNotNull(result.getPassword());
    } finally {
      if (result != null) {
        Assert.assertTrue(gbif.getOrganisationApi().getDeleteRequest(result).execute().getResult());
      }
    }
  }

  /**
   * Test method for
   * {@link org.gbif.registry.api.client.RegistryService.OrganisationApi#delete(org.gbif.registry.api.client.GbifOrganisation)}
   * .
   */
  @Test
  public final void testDelete() {
    DeleteOrgRequest request = gbif.getOrganisationApi().getDeleteRequest(
        GbifOrganisation.builder().password("DaSAWqmvQ0z").key(
            "c3513d8e-bf68-42ec-b125-2574cf022e99").primaryContactType(
            "technical").build());
    DeleteOrgResponse response = request.execute();
    Assert.assertNotNull(response.getResult());
    Assert.assertTrue(response.getResult());
  }

  /**
   * Test method for
   * {@link org.gbif.registry.api.client.RegistryService.OrganisationApi#list()}
   * .
   */
  @Test
  public final void testList() {
    ListOrgRequest request = gbif.getOrganisationApi().getListRequest();
    List<GbifOrganisation> list = request.execute().getResult();
    Assert.assertNotNull(list);
    System.out.println(list);

  }

  /**
   * Test method for
   * {@link org.gbif.registry.api.client.RegistryService.OrganisationApi#read(java.lang.String)}
   * .
   */
  @Test
  public final void testRead() {
    String orgKey = "c3513d8e-bf68-42ec-b125-2574cf022e99";
    ReadOrgRequest request = gbif.getOrganisationApi().getReadRequest(orgKey);
    ReadOrgResponse response = request.execute();
    GbifOrganisation org = response.getResult();
    Assert.assertNotNull(org);
    Assert.assertEquals(orgKey, org.getKey());
  }

  /**
   * Test method for
   * {@link org.gbif.registry.api.client.RegistryService.OrganisationApi#update(org.gbif.registry.api.client.GbifOrganisation)}
   * .
   */
  @Test
  public final void testUpdate() {
    UpdateOrgRequest request = gbif.getOrganisationApi().getUpdateRequest(
        GbifOrganisation.builder().password("DaSAWqmvQ0z").key(
            "c3513d8e-bf68-42ec-b125-2574cf022e99").primaryContactType(
            "technical").description("Test Update").build());
    UpdateOrgResponse response = request.execute();
    Assert.assertNotNull(response);
    Assert.assertEquals("Test Update", response.getResult().getDescription());
  }
}
