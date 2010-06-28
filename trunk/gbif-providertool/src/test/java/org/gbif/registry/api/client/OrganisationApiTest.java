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

import static org.junit.Assert.fail;

import org.gbif.registry.api.client.GbifRegistry.OrganisationApi;
import org.gbif.registry.api.client.Registry.RpcRequest;
import org.gbif.registry.api.client.Registry.RpcResponse;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit testing coverage for {@link OrganisationApi}.
 */
public class OrganisationApiTest {

  private static Registry registry;
  private static final GbifOrganisation org = GbifOrganisation.builder().name(
      "Name").primaryContactEmail("eightysteele@gmail.com").primaryContactType(
      "technical").nodeKey("us").build();

  /**
   * @throws java.lang.Exception void
   */
  @Before
  public void setUp() throws Exception {
    registry = GbifRegistry.init("http://gbrds.gbif.org");
  }

  /**
   * Test method for
   * {@link org.gbif.registry.api.client.GbifRegistry.OrganisationApi#create(org.gbif.registry.api.client.GbifOrganisation)}
   * .
   */
  @Test
  public final void testCreate() {
    try {
      registry.execute(OrganisationApi.create(GbifOrganisation.builder().build()));
      fail();
    } catch (Exception e) {
    }

    GbifOrganisation result = null;
    try {
      Assert.assertNull(org.getKey());
      RpcRequest request = OrganisationApi.create(org);
      RpcResponse<GbifOrganisation> response = registry.execute(request);
      result = response.getResult();
      Assert.assertNotNull(result.getKey());
      Assert.assertNotNull(result.getPassword());
    } finally {
      if (result != null) {
        registry.execute(OrganisationApi.delete(result));
      }
    }
  }

  /**
   * Test method for
   * {@link org.gbif.registry.api.client.GbifRegistry.OrganisationApi#delete(org.gbif.registry.api.client.GbifOrganisation)}
   * .
   */
  @Test
  public final void testDelete() {
    RpcResponse<GbifOrganisation> response = registry.execute(OrganisationApi.create(org));
    registry.execute(OrganisationApi.delete(response.getResult()));
  }

  /**
   * Test method for
   * {@link org.gbif.registry.api.client.GbifRegistry.OrganisationApi#list()}.
   */
  @Test
  public final void testList() {
    registry.execute(OrganisationApi.list());
  }

  /**
   * Test method for
   * {@link org.gbif.registry.api.client.GbifRegistry.OrganisationApi#read(java.lang.String)}
   * .
   */
  @Test
  public final void testRead() {
    RpcResponse<GbifOrganisation> response = null;
    try {
      response = registry.execute(OrganisationApi.create(org));
      RpcResponse<GbifOrganisation> read = registry.execute(OrganisationApi.read(response.getResult().getKey()));
      Assert.assertEquals(response.getResult().getKey(),
          read.getResult().getKey());
    } finally {
      if (response != null) {
        registry.execute(OrganisationApi.delete(response.getResult()));
      }
    }
  }

  /**
   * Test method for
   * {@link org.gbif.registry.api.client.GbifRegistry.OrganisationApi#update(org.gbif.registry.api.client.GbifOrganisation)}
   * .
   */
  @Test
  public final void testUpdate() {
    GbifOrganisation updated = null;
    try {
      RpcResponse<GbifOrganisation> response = registry.execute(OrganisationApi.create(org));
      String passwd = response.getResult().getPassword();
      String key = response.getResult().getKey();

      response = registry.execute(OrganisationApi.read(response.getResult().getKey()));
      GbifOrganisation existing = response.getResult();

      updated = GbifOrganisation.builder().key(key).password(passwd).primaryContactType(
          existing.getPrimaryContactType()).name("UPDATE").build();
      registry.execute(OrganisationApi.update(updated));

      response = registry.execute(OrganisationApi.read(key));

      GbifOrganisation test = response.getResult();
      Assert.assertEquals("UPDATE", test.getName());
    } finally {
      if (updated != null) {
        registry.execute(OrganisationApi.delete(updated));
      }
    }
  }
}
