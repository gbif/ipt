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

import org.gbif.registry.api.client.Gbrds.BadCredentialsException;
import org.gbif.registry.api.client.Gbrds.CreateResourceResponse;
import org.gbif.registry.api.client.Gbrds.ListResourceRequest;
import org.gbif.registry.api.client.Gbrds.OrgCredentials;
import org.gbif.registry.api.client.Gbrds.ReadResourceResponse;
import org.gbif.registry.api.client.Gbrds.ResourceApi;
import org.junit.Test;

import java.util.List;

/**
 * Unit testing coverage for {@link ResourceApi}.
 */
public class ResourceApiTest {

  private static Gbrds gbif = GbrdsImpl.init("http://gbrdsdev.gbif.org");
  private static final ResourceApi api = gbif.getResourceApi();

  private static final String resourceKey = "3f138d32-eb85-430c-8d5d-115c2f03429e";
  private static final String orgKey = "3780d048-8e18-4c0c-afcd-cb6389df56de";
  private static final String serviceKey = "e2522a8c-d66c-40ec-9f10-623cc16c6d6c";

  private static final GbrdsResource resource = GbrdsResource.builder().name(
      "Name").description("Description").primaryContactEmail(
      "eightysteele@gmail.com").primaryContactType("technical").organisationKey(
      orgKey).build();

  private static final OrgCredentials creds = OrgCredentials.with(orgKey,
      "password");

  @Test
  public final void testCreateAndDelete() throws BadCredentialsException {
    GbrdsResource result = null;
    try {
      CreateResourceResponse response = api.create(resource).execute(creds);
      Assert.assertNotNull(response);
      result = response.getResult();
      Assert.assertNotNull(result);
      Assert.assertNotNull(result.getKey());
    } finally {
      if (result != null) {
        Assert.assertTrue(api.delete(result.getKey()).execute(creds).getResult());
      }
    }
  }

  @Test
  public final void testList() {
    ListResourceRequest request = api.list(orgKey);
    List<GbrdsResource> list = request.execute().getResult();
    Assert.assertNotNull(list);
    System.out.println(list);
  }

  @Test
  public final void testRead() {
    ReadResourceResponse response = api.read(resourceKey).execute();
    GbrdsResource gr = response.getResult();
    Assert.assertNotNull(gr);
    Assert.assertEquals(resourceKey, gr.getKey());
    System.out.println(gr);
  }

  @Test
  public final void testUpdate() throws BadCredentialsException {
    String name = System.currentTimeMillis() + "-name";
    api.update(
        GbrdsResource.builder().key(resourceKey).name(name).primaryContactType(
            "technical").build()).execute(creds);
    GbrdsResource gr = api.read(resourceKey).execute().getResult();
    Assert.assertEquals(name, gr.getName());
  }
}
