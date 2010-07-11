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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import static org.gbif.provider.model.voc.ServiceType.DWC_ARCHIVE;
import static org.gbif.provider.model.voc.ServiceType.EML;
import static org.gbif.provider.model.voc.ServiceType.TAPIR;
import static org.gbif.provider.model.voc.ServiceType.TCS_RDF;
import static org.gbif.provider.model.voc.ServiceType.WFS;
import static org.gbif.provider.model.voc.ServiceType.WMS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.gbif.mock.ListServicesResponseMock;
import org.gbif.mock.RegistryManagerMock;
import org.gbif.mock.UpdateServiceResponseMock;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.voc.ServiceType;
import org.gbif.provider.webapp.action.admin.ConfigAction.Helper;
import org.gbif.provider.webapp.action.admin.ConfigAction.Helper.UrlProvider;
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
  public void testUpdateResourceServices() {
    // Setting up inputs:
    Resource r = new Resource();
    r.getMeta().setUddiID("resourceKey");
    OrgCredentials creds = OrgCredentials.with("key", "password");
    ImmutableMap<Resource, OrgCredentials> map = ImmutableMap.of(r, creds);
    final List<ServiceType> types = Lists.newArrayList(EML, DWC_ARCHIVE, TAPIR,
        WFS, WMS, TCS_RDF);
    UrlProvider up = new UrlProvider() {
      public String getUrl(ServiceType type, Resource resource) {
        return "http://mockurl.com";
      }
    };
    RegistryManagerMock mockRegistry = new RegistryManagerMock() {
      @Override
      public ListServicesResponse listGbrdsServices(String resourceKey) {
        return new ListServicesResponseMock() {
          @Override
          public List<GbrdsService> getResult() {
            List<GbrdsService> list = Lists.newArrayList();
            for (ServiceType t : ServiceType.values()) {
              if (types.contains(t)) {
                list.add(GbrdsService.builder().type(t.getCode()).build());
              }
            }
            return list;
          }
        };
      }

      @Override
      public UpdateServiceResponse updateGbrdsService(GbrdsService service,
          OrgCredentials creds) {
        serviceList.add(service);
        return new UpdateServiceResponseMock() {
          @Override
          public Boolean getResult() {
            return true;
          }
        };
      }
    };

    // Test with valid inputs:
    assertTrue(Helper.updateResourceServices(map, up, mockRegistry));
    List<GbrdsService> list = mockRegistry.serviceList;
    assertEquals(list.size(), types.size());
    for (GbrdsService s : list) {
      assertEquals("http://mockurl.com", s.getAccessPointURL());
    }

    // Test with an empty resource credentials mapping:
    mockRegistry.serviceList.clear();
    ImmutableMap<Resource, OrgCredentials> m = ImmutableMap.of();
    assertTrue(Helper.updateResourceServices(m, up, mockRegistry));
    list = mockRegistry.serviceList;
    assertEquals(list.size(), 0);

    // Test with no services associated with resources:
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

      @Override
      public UpdateServiceResponse updateGbrdsService(GbrdsService service,
          OrgCredentials creds) {
        serviceList.add(service);
        return new UpdateServiceResponseMock() {
          @Override
          public Boolean getResult() {
            return true;
          }
        };
      }
    };
    mockRegistry.serviceList.clear();
    assertTrue(Helper.updateResourceServices(map, up, mockRegistry));
    list = mockRegistry.serviceList;
    assertEquals(list.size(), 0);

    // Test with invalid resource credentials:
    mockRegistry = new RegistryManagerMock() {
      @Override
      public ListServicesResponse listGbrdsServices(String resourceKey) {
        return new ListServicesResponseMock() {
          @Override
          public List<GbrdsService> getResult() {
            List<GbrdsService> list = Lists.newArrayList();
            for (ServiceType t : ServiceType.values()) {
              if (types.contains(t)) {
                list.add(GbrdsService.builder().type(t.getCode()).build());
              }
            }
            return list;
          }
        };
      }

      @Override
      public UpdateServiceResponse updateGbrdsService(GbrdsService service,
          OrgCredentials creds) {
        throw new BadCredentialsException("Unauthorized: " + creds);
      }
    };
    mockRegistry.serviceList.clear();
    assertFalse(Helper.updateResourceServices(map, up, mockRegistry));
    list = mockRegistry.serviceList;
    assertEquals(list.size(), 0);

    // Test with url provider returning localhost URLs:
    up = new UrlProvider() {
      public String getUrl(ServiceType type, Resource resource) {
        return "http://localhost";
      }
    };
    try {
      Helper.updateResourceServices(map, up, mockRegistry);
      fail();
    } catch (Exception e) {
    }

    // Null testing:
    try {
      Helper.updateResourceServices(null, null, null);
      fail();
    } catch (Exception e) {
    }
    try {
      Helper.updateResourceServices(map, null, null);
      fail();
    } catch (Exception e) {
    }
    try {
      Helper.updateResourceServices(null, up, null);
      fail();
    } catch (Exception e) {
    }
    try {
      Helper.updateResourceServices(null, null, mockRegistry);
      fail();
    } catch (Exception e) {
    }
    try {
      Helper.updateResourceServices(null, up, mockRegistry);
      fail();
    } catch (Exception e) {
    }
    try {
      Helper.updateResourceServices(map, null, mockRegistry);
      fail();
    } catch (Exception e) {
    }
    try {
      Helper.updateResourceServices(map, up, null);
      fail();
    } catch (Exception e) {
    }

  }

  @Test
  public void testUpdateRssService() {
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
    try {
      Helper.udpateIptRssService(null, "rkey", "serviceUrl", mockRegistry);
      fail();
    } catch (Exception e) {
    }
    try {
      Helper.udpateIptRssService(creds, null, "serviceUrl", mockRegistry);
      fail();
    } catch (Exception e) {
    }
    try {
      Helper.udpateIptRssService(creds, "rkey", null, mockRegistry);
      fail();
    } catch (Exception e) {
    }
    try {
      Helper.udpateIptRssService(creds, "rkey", "serviceUrl", null);
      fail();
    } catch (Exception e) {
    }

    // Empty string testing:
    try {
      Helper.udpateIptRssService(creds, "", "serviceUrl", mockRegistry);
      fail();
    } catch (Exception e) {
    }
    try {
      Helper.udpateIptRssService(creds, "rkey", "", mockRegistry);
      fail();
    } catch (Exception e) {
    }
    try {
      Helper.udpateIptRssService(creds, "", "", mockRegistry);
      fail();
    } catch (Exception e) {
    }

    // Test localhost RSS URL:
    try {
      Helper.udpateIptRssService(creds, "rkey", "localhost", mockRegistry);
      fail();
    } catch (Exception e) {
    }

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
