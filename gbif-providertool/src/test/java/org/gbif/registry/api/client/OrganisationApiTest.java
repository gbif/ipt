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

import com.google.gson.Gson;

import junit.framework.Assert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.gbif.provider.model.voc.ContactType;
import org.gbif.registry.api.client.Gbrds.BadCredentialsException;
import org.gbif.registry.api.client.Gbrds.CreateOrgResponse;
import org.gbif.registry.api.client.Gbrds.ListOrgResponse;
import org.gbif.registry.api.client.Gbrds.OrgCredentials;
import org.gbif.registry.api.client.Gbrds.OrganisationApi;
import org.gbif.registry.api.client.Gbrds.ReadOrgResponse;
import org.junit.Test;

import java.util.List;

/**
 * Unit testing coverage for {@link OrganisationApi}.
 */
public class OrganisationApiTest {

  private static Gbrds gbif = GbrdsImpl.init("http://gbrdsdev.gbif.org");
  private static OrganisationApi api = gbif.getOrganisationApi();

  private static final String resourceKey = "3f138d32-eb85-430c-8d5d-115c2f03429e";
  private static final String orgKey = "3780d048-8e18-4c0c-afcd-cb6389df56de";
  private static final String serviceKey = "e2522a8c-d66c-40ec-9f10-623cc16c6d6c";

  private static final OrgCredentials creds = OrgCredentials.with(orgKey,
      "password");

  private static final GbrdsOrganisation org = GbrdsOrganisation.builder().name(
      "Name").primaryContactEmail("eightysteele@gmail.com").primaryContactType(
      "technical").nodeKey("us").build();

  @Test
  public final void testCreate() throws BadCredentialsException {
    // Tests executing with null org:
    try {
      api.create(null).execute();
      fail();
    } catch (NullPointerException e) {
      System.out.println(e);
    }

    // Tests creating and deleting a valid service:
    OrgCredentials oc = null;
    try {
      CreateOrgResponse response = api.create(org).execute();
      Assert.assertNotNull(response);
      oc = response.getResult();
      Assert.assertNotNull(oc);
      System.out.println(oc);
    } finally {
      if (oc != null) {
        Assert.assertTrue(api.delete(oc.getKey()).execute(creds).getResult());
      }
    }
  }

  @Test
  public final void testGbrdsOrganisation() {
    GbrdsOrganisation.Builder go = GbrdsOrganisation.builder().description("d").descriptionLanguage(
        "es").homepageURL("hu").name("n").nameLanguage("es").nodeKey("sp2000").nodeName(
        "Species 2000").primaryContactAddress("pca").primaryContactDescription(
        "pcd").primaryContactName("pcn").primaryContactPhone("pcp").primaryContactType(
        "administrative").primaryContactEmail("eightysteele@gmail.com");
    String goJson = new Gson().toJson(go, GbrdsOrganisation.Builder.class);
    String json = "{\"primaryContactDescription\":\"pcd\",\"homepageURL\":\"hu\",\"nodeName\":\"Species 2000\",\"primaryContactPhone\":\"pcp\",\"nodeContactEmail\":\"\",\"primaryContactEmail\":\"eightysteele@gmail.com\",\"descriptionLanguage\":\"es\",\"nameLanguage\":\"es\",\"primaryContactAddress\":\"pca\",\"description\":\"d\",\"name\":\"n\",\"primaryContactType\":\"administrative\",\"key\":\"0f9065fb-df6d-46b8-96b3-044cd3b582e8\",\"primaryContactName\":\"pcn \",\"nodeKey\":\"sp2000\"}";
    GbrdsOrganisation org = new Gson().fromJson(goJson,
        GbrdsOrganisation.Builder.class).build();// OrgUtil.fromJson(goJson);
    System.out.println(go.build());
    System.out.println(org);

    assertEquals(go.build(), org);
  }

  @Test
  public final void testList() {
    ListOrgResponse r = api.list().execute();
    List<GbrdsOrganisation> list = r.getResult();
    assertNotNull(list);
    System.out.println(list);
  }

  @Test
  public final void testRead() {
    ReadOrgResponse response = api.read(orgKey).execute();
    GbrdsOrganisation go = response.getResult();
    Assert.assertNotNull(go);
    Assert.assertEquals(orgKey, go.getKey());
    System.out.println(go);
  }

  @Test
  public final void testUpdate() throws BadCredentialsException {
    String description = System.currentTimeMillis() + "";
    GbrdsOrganisation go;
    go = GbrdsOrganisation.builder().key(orgKey).primaryContactType(
        ContactType.technical.name()).description(description).name("Tim").build();
    api.update(go).execute(creds).getResult();
    go = api.read(orgKey).execute().getResult();
    assertEquals(go.getDescription(), description);
  }

  @Test
  public final void testValidateCredentials() {
    try {
      api.validateCredentials(null);
      fail();
    } catch (NullPointerException e) {
      System.out.println(e);
    }

    assertTrue(api.validateCredentials(creds).execute().getResult());

    assertFalse(api.validateCredentials(
        OrgCredentials.with("bad", "credentials")).execute().getResult());
  }
}
