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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.gbif.provider.model.voc.ServiceType;
import org.gbif.registry.api.client.Gbrds.BadCredentialsException;
import org.gbif.registry.api.client.Gbrds.CreateServiceResponse;
import org.gbif.registry.api.client.Gbrds.ListServiceResponse;
import org.gbif.registry.api.client.Gbrds.OrgCredentials;
import org.gbif.registry.api.client.Gbrds.ReadServiceResponse;
import org.gbif.registry.api.client.Gbrds.ServiceApi;
import org.junit.Test;

import java.util.List;

/**
 * Unit testing coverage for {@link ServiceApi}.
 */
public class ServiceApiTest {

  private static Gbrds gbif = GbrdsImpl.init("http://gbrdsdev.gbif.org");
  private static final ServiceApi api = gbif.getServiceApi();

  private static final String resourceKey = "3f138d32-eb85-430c-8d5d-115c2f03429e";
  private static final String orgKey = "3780d048-8e18-4c0c-afcd-cb6389df56de";
  private static final String serviceKey = "e2522a8c-d66c-40ec-9f10-623cc16c6d6c";

  private static final GbrdsService service = GbrdsService.builder().resourceKey(
      resourceKey).type(ServiceType.DWC_ARCHIVE.getCode()).accessPointURL(
      "http://foo.com").build();

  private static final OrgCredentials creds = OrgCredentials.with(orgKey,
      "password");

  @Test
  public final void testCreateAndDelete() throws BadCredentialsException {

    // Tests executing with null credentials:
    try {
      api.create(service).execute(null);
      fail();
    } catch (NullPointerException e) {
      System.out.println(e);
    }

    // Tests executing with bad credentials:
    try {
      api.create(service).execute(OrgCredentials.with("bad", "creds"));
      fail();
    } catch (BadCredentialsException e) {
      System.out.println(e);
    }

    // Tests creating and deleting a valid service:
    GbrdsService createdService = null;
    try {
      CreateServiceResponse response = api.create(service).execute(creds);
      Assert.assertNotNull(response);
      createdService = response.getResult();
      Assert.assertNotNull(createdService);
      System.out.println(createdService);
      Assert.assertNotNull(createdService.getKey());
    } finally {
      if (createdService != null) {
        Assert.assertTrue(api.delete(createdService.getKey()).execute(creds).getResult());
      }
    }
  }

  @Test
  public final void testList() {
    ListServiceResponse r = api.list(resourceKey).execute();
    List<GbrdsService> list = r.getResult();
    assertNotNull(list);
    System.out.println(list);
  }

  @Test
  public final void testRead() {
    ReadServiceResponse response = api.read(serviceKey).execute();
    GbrdsService res = response.getResult();
    Assert.assertNotNull(res);
    Assert.assertEquals(serviceKey, res.getKey());
    System.out.println(res);
  }

  @Test
  public final void testUpdate() throws BadCredentialsException {
    GbrdsService s;
    s = GbrdsService.builder(service).key(serviceKey).accessPointURL("foo").build();
    api.update(s).execute(creds).getResult();
    GbrdsService su = api.read(serviceKey).execute().getResult();
    assertEquals(su.getAccessPointURL(), "foo");
  }
}
