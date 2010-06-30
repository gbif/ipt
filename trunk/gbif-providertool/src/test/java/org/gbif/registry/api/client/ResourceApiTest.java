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
import org.gbif.registry.api.client.GbifResource;
import org.gbif.registry.api.client.Gbrds;
import org.gbif.registry.api.client.GbifRegistry.CreateResourceResponse;
import org.gbif.registry.api.client.GbifRegistry.ListOrgRequest;
import org.gbif.registry.api.client.GbifRegistry.ReadResourceResponse;
import org.gbif.registry.api.client.GbifRegistry.UpdateResourceResponse;
import org.gbif.registry.api.client.Gbrds.ResourceApi;
import org.junit.Test;

import java.util.List;

/**
 * Unit testing coverage for {@link ResourceApi}.
 */
public class ResourceApiTest {

  private static Gbrds gbif = GbifRegistry.init("http://gbrdsdev.gbif.org");
  private static final ResourceApi api = gbif.getResourceApi();

  private static final GbifResource resource = GbifResource.builder().name(
      "Test Resource").description("Test Description").primaryContactEmail(
      "eightysteele@gmail.com").primaryContactType("technical").organisationKey(
      "3780d048-8e18-4c0c-afcd-cb6389df56de").organisationPassword("password").build();

  @Test
  public final void testCreateAndDelete() {
    GbifResource result = null;
    try {
      CreateResourceResponse response = api.create(resource).execute();
      Assert.assertNotNull(response);
      result = response.getResult();
      Assert.assertNotNull(result);
      Assert.assertNotNull(result.getKey());
    } finally {
      if (result != null) {
        Assert.assertTrue(api.delete(
            GbifResource.builder().key(result.getKey()).organisationKey(
                resource.getOrganisationKey()).organisationPassword(
                resource.getOrganisationPassword()).build()).execute().getResult());
      }
    }
  }

  @Test
  public final void testList() {
    ListOrgRequest request = gbif.getOrganisationApi().list();
    List<GbifOrganisation> list = request.execute().getResult();
    Assert.assertNotNull(list);
    System.out.println(list);
  }

  @Test
  public final void testRead() {
    String resourceKey = "3f138d32-eb85-430c-8d5d-115c2f03429e";
    ReadResourceResponse response = api.read(resourceKey).execute();
    GbifResource res = response.getResult();
    Assert.assertNotNull(res);
    Assert.assertEquals(resourceKey, res.getKey());
    System.out.println(res);
  }

  @Test
  public final void testUpdate() {
    UpdateResourceResponse response = api.update(
        GbifResource.builder().key("3f138d32-eb85-430c-8d5d-115c2f03429e").name(
            "Test Resource").description("Test Description - Updated!").primaryContactEmail(
            "eightysteele@gmail.com").primaryContactType("technical").organisationKey(
            "3780d048-8e18-4c0c-afcd-cb6389df56de").organisationPassword(
            "password").build()).execute();
    Assert.assertNotNull(response);
    Assert.assertEquals("Test Description - Updated!",
        response.getResult().getDescription());
  }
}
