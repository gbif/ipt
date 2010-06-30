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
import org.gbif.registry.api.client.GbifService;
import org.gbif.registry.api.client.Gbrds;
import org.gbif.registry.api.client.GbifRegistry.CreateServiceResponse;
import org.gbif.registry.api.client.GbifRegistry.ListOrgRequest;
import org.gbif.registry.api.client.GbifRegistry.ReadServiceResponse;
import org.gbif.registry.api.client.GbifRegistry.UpdateServiceResponse;
import org.gbif.registry.api.client.Gbrds.ServiceApi;
import org.junit.Test;

import java.util.List;

/**
 * Unit testing coverage for {@link ServiceApi}.
 */
public class ServiceApiTest {

  private static Gbrds gbif = GbifRegistry.init("http://gbrdsdev.gbif.org");
  private static final ServiceApi api = gbif.getServiceApi();

  private static final GbifService service = GbifService.builder().resourceKey(
      "3f138d32-eb85-430c-8d5d-115c2f03429e").type("WMS").accessPointURL(
      "http://foo.com").organisationKey("3780d048-8e18-4c0c-afcd-cb6389df56de").resourcePassword(
      "password").build();

  @Test
  public final void testCreateAndDelete() {
    GbifService result = null;
    try {
      CreateServiceResponse response = api.create(service).execute();
      Assert.assertNotNull(response);
      result = response.getResult();
      Assert.assertNotNull(result);
      Assert.assertNotNull(result.getKey());
    } finally {
      if (result != null) {
        Assert.assertTrue(api.delete(
            GbifService.builder().key(result.getKey()).resourceKey(
                service.getResourceKey()).organisationKey(
                service.getOrganisationKey()).resourcePassword(
                service.getResourcePassword()).build()).execute().getResult());
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
    String serviceKey = "e2522a8c-d66c-40ec-9f10-623cc16c6d6c";
    ReadServiceResponse response = api.read(serviceKey).execute();
    GbifService res = response.getResult();
    Assert.assertNotNull(res);
    Assert.assertEquals(serviceKey, res.getKey());
    System.out.println(res);
  }

  @Test
  public final void testUpdate() {
    UpdateServiceResponse response = api.update(
        GbifService.builder().resourceKey(
            "3f138d32-eb85-430c-8d5d-115c2f03429e").key(
            "e2522a8c-d66c-40ec-9f10-623cc16c6d6c").type("WMS").accessPointURL(
            "http://bar.com").organisationKey(
            "3780d048-8e18-4c0c-afcd-cb6389df56de").resourcePassword("password").build()).execute();
    Assert.assertNotNull(response);
    Assert.assertEquals("http://bar.com",
        response.getResult().getAccessPointURL());
  }
}
