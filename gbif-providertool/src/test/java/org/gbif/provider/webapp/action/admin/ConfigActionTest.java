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
package org.gbif.provider.webapp.action.admin;

import com.google.common.collect.Lists;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.gbif.mock.ListServicesResponseMock;
import org.gbif.mock.RegistryManagerMock;
import org.gbif.mock.UpdateServiceResponseMock;
import org.gbif.provider.model.voc.ServiceType;
import org.gbif.provider.webapp.action.admin.ConfigAction.Helper;
import org.gbif.registry.api.client.GbrdsService;
import org.gbif.registry.api.client.Gbrds.BadCredentialsException;
import org.gbif.registry.api.client.Gbrds.OrgCredentials;
import org.gbif.registry.api.client.GbrdsRegistry.ListServicesResponse;
import org.gbif.registry.api.client.GbrdsRegistry.UpdateServiceResponse;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Test coverage for {@link ConfigAction}.
 * 
 */
public class ConfigActionTest {

  /**
   * Creates and returns a temp directory.
   */
  static File createTempDir() throws IOException {
    final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
    File newTempDir;
    final int maxAttempts = 9;
    int attemptCount = 0;
    do {
      attemptCount++;
      if (attemptCount > maxAttempts) {
        throw new IOException("Unable to create dir after " + maxAttempts
            + " attempts.");
      }
      String dirName = UUID.randomUUID().toString();
      newTempDir = new File(sysTempDir, dirName);
    } while (newTempDir.exists());

    if (newTempDir.mkdirs()) {
      return newTempDir;
    } else {
      throw new IOException("Failed to create temp dir named "
          + newTempDir.getAbsolutePath());
    }
  }

  @Test
  public void testHelper() throws IOException {
    // Base URL tests:
    assertFalse(Helper.checkBaseUrl("localhost"));
    assertFalse(Helper.checkBaseUrl(null));
    assertFalse(Helper.checkBaseUrl("http://localhost.com"));
    assertTrue(Helper.checkBaseUrl("http://foo.com"));

    // Data dir tests:
    assertFalse(Helper.checkDataDir(null));
    assertFalse(Helper.checkDataDir(""));
    assertFalse(Helper.checkDataDir("  "));
    File dir = createTempDir();
    assertTrue(Helper.checkDataDir(dir.getPath()));
    dir.setReadOnly();
    assertFalse(Helper.checkDataDir(dir.getPath()));
    dir = File.createTempFile("foo", "bar");
    assertFalse(Helper.checkDataDir(dir.getPath()));

    // Geoserver URL tests:
    String url = null;
    assertFalse(Helper.checkGeoServerUrl(url));
    url = "";
    assertFalse(Helper.checkGeoServerUrl(url));
    url = "foo.com";
    assertFalse(Helper.checkGeoServerUrl(url));
    url = "http://foo.com";
    assertTrue(Helper.checkGeoServerUrl(url));

    // Maps key test:
    String key = "ABQIAAAAaLS3GE1JVrq3TRuXuQ68wBT2yXp_ZAY8_ufC3CFXhHIE1NvwkxQY-Unm8BwXJu9YioYorDsQkvdK0Q";
    assertFalse(Helper.checkMapsKey(key));
    key = null;
    assertFalse(Helper.checkMapsKey(key));
    key = "";
    assertFalse(Helper.checkMapsKey(key));
    key = " ";
    assertFalse(Helper.checkMapsKey(key));
    key = "foo";
    assertTrue(Helper.checkMapsKey(key));

    // Credentials test:
    assertNull(Helper.getCreds(null, null));
    assertNull(Helper.getCreds("", null));
    assertNull(Helper.getCreds(null, ""));
    assertNull(Helper.getCreds("", ""));
    assertNull(Helper.getCreds("key", null));
    assertNull(Helper.getCreds(null, "pass"));
    assertNotNull(Helper.getCreds("key", "pass"));
  }

  @Test
  public void testUpdateServices() {
    RegistryManagerMock mockRegistry = new RegistryManagerMock() {

      @Override
      public ListServicesResponse listGbrdsServices(String resourceKey) {
        return new ListServicesResponseMock() {
          @Override
          public List<GbrdsService> getResult() {
            return Lists.newArrayList(GbrdsService.builder().type(
                ServiceType.RSS.getCode()).build());
          }
        };
      }

      @Override
      public UpdateServiceResponse updateGbrdsService(GbrdsService service,
          OrgCredentials creds) {
        this.service = service;
        return new UpdateServiceResponseMock() {
          @Override
          public Boolean getResult() {
            return true;
          }
        };
      }
    };

    OrgCredentials creds = OrgCredentials.with("key", "password");
    UpdateServiceResponse r;

    r = Helper.udpateIptRssService(creds, "rkey", "serviceUrl", mockRegistry);
    assertNotNull(r);
    assertTrue(r.getResult());
    GbrdsService service = mockRegistry.service;
    assertEquals(service.getResourceKey(), "rkey");
    assertEquals(service.getAccessPointURL(), "serviceUrl");
    assertEquals(service.getType(), ServiceType.RSS.getCode());

    // Null testing:
    r = Helper.udpateIptRssService(null, "rkey", "serviceUrl", mockRegistry);
    assertNull(r);
    r = Helper.udpateIptRssService(creds, null, "serviceUrl", mockRegistry);
    assertNull(r);
    r = Helper.udpateIptRssService(creds, "rkey", null, mockRegistry);
    assertNull(r);
    r = Helper.udpateIptRssService(creds, "rkey", "serviceUrl", null);
    assertNull(r);

    // Empty string testing:
    r = Helper.udpateIptRssService(creds, "", "serviceUrl", mockRegistry);
    assertNull(r);
    r = Helper.udpateIptRssService(creds, "rkey", "", mockRegistry);
    assertNull(r);

    // Test for no services associated with the resource key:
    mockRegistry = new RegistryManagerMock() {
      @Override
      public ListServicesResponse listGbrdsServices(String resourceKey) {
        return new ListServicesResponseMock() {
          @Override
          public List<GbrdsService> getResult() {
            return Lists.newArrayList();
          }
        };
      }
    };
    r = Helper.udpateIptRssService(creds, "rkey", "serviceUrl", mockRegistry);
    assertNull(r);

    // Test for bad credentials:
    mockRegistry = new RegistryManagerMock() {

      @Override
      public ListServicesResponse listGbrdsServices(String resourceKey) {
        return new ListServicesResponseMock() {
          @Override
          public List<GbrdsService> getResult() {
            return Lists.newArrayList(GbrdsService.builder().type(
                ServiceType.RSS.getCode()).build());
          }
        };
      }

      @Override
      public UpdateServiceResponse updateGbrdsService(GbrdsService service,
          OrgCredentials creds) {
        throw new BadCredentialsException("Unauthorized: " + creds);
      }
    };
    r = Helper.udpateIptRssService(creds, "rkey", "serviceUrl", mockRegistry);
    assertNull(r);
  }
}
