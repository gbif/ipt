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

import static org.junit.Assert.assertNotNull;

import org.apache.commons.httpclient.HttpStatus;
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

import java.util.List;

/**
 *
 */
public class ConfigActionTest {

  /**
   * Test method for
   * {@link org.gbif.provider.webapp.action.admin.ConfigAction.Helper#checkDataDir(java.lang.String)}
   * .
   */
  @Test
  public final void testCheckDataDir() {
    // TODO
  }

  /**
   * Test method for
   * {@link org.gbif.provider.webapp.action.admin.ConfigAction.Helper#checkGeoServerCreds(java.lang.String, java.lang.String, java.lang.String, org.gbif.provider.service.GeoserverManager)}
   * .
   */
  @Test
  public final void testCheckGeoServerCreds() {
    // TODO
  }

  /**
   * Test method for
   * {@link org.gbif.provider.webapp.action.admin.ConfigAction.Helper#checkMapsKey(java.lang.String)}
   * .
   */
  @Test
  public final void testCheckMapsKey() {
    // TODO
  }

  /**
   * Test method for
   * {@link org.gbif.provider.webapp.action.admin.ConfigAction.Helper#updateIptRssService(java.lang.String, java.lang.String, java.lang.String, org.gbif.provider.service.RegistryManager)}
   * .
   */
  @Test
  public final void testUpdateIptRssService() {
    RegistryManagerMock rm;
    String msg;

    // Tests a resource key that doesn't exist in the GBRDS:
    rm = new RegistryManagerMock() {
      @Override
      public boolean resourceExists(String key) {
        return false;
      }
    };
    msg = Helper.updateIptRssService("key", "password", "rssUrl", rm);
    assertNotNull(msg);
    System.out.println(msg);

    // Tests invalid resource credentials:
    rm = new RegistryManagerMock() {
      @Override
      public OrgCredentials getCreds(String key, String pass) {
        return null;
      }

      @Override
      public boolean resourceExists(String key) {
        return true;
      }
    };
    msg = Helper.updateIptRssService("key", "password", "rssUrl", rm);
    assertNotNull(msg);
    System.out.println(msg);

    // Tests RSS URL that is localhost:
    rm = new RegistryManagerMock() {
      @Override
      public OrgCredentials getCreds(String key, String pass) {
        return OrgCredentials.with(key, pass);
      }

      @Override
      public boolean isLocalhost(String url) {
        return true;
      }

      @Override
      public boolean resourceExists(String key) {
        return true;
      }
    };
    msg = Helper.updateIptRssService("key", "password", "rssUrl", rm);
    assertNotNull(msg);
    System.out.println(msg);

    // Tests error listing services for resource:
    rm = new RegistryManagerMock() {
      @Override
      public OrgCredentials getCreds(String key, String pass) {
        return OrgCredentials.with(key, pass);
      }

      @Override
      public boolean isLocalhost(String url) {
        return false;
      }

      @Override
      public ListServicesResponse listServices(String key) {
        return new ListServicesResponseMock() {
          @Override
          public int getStatus() {
            return HttpStatus.SC_BAD_REQUEST;
          }
        };
      }

      @Override
      public boolean resourceExists(String key) {
        return true;
      }
    };
    msg = Helper.updateIptRssService("key", "password", "rssUrl", rm);
    assertNotNull(msg);
    System.out.println(msg);

    // Tests no RSS service found:
    rm = new RegistryManagerMock() {
      @Override
      public OrgCredentials getCreds(String key, String pass) {
        return OrgCredentials.with(key, pass);
      }

      @Override
      public boolean isLocalhost(String url) {
        return false;
      }

      @Override
      public ListServicesResponse listServices(String key) {
        return new ListServicesResponseMock() {
          @Override
          public List<GbrdsService> getResult() {
            return Lists.newArrayList(GbrdsService.builder().type(
                ServiceType.WMS.code).build());
          }

          @Override
          public int getStatus() {
            return HttpStatus.SC_OK;
          }
        };
      }

      @Override
      public boolean resourceExists(String key) {
        return true;
      }

      @Override
      public UpdateServiceResponse updateService(GbrdsService s,
          OrgCredentials creds) throws BadCredentialsException {
        throw new BadCredentialsException();
      }
    };
    msg = Helper.updateIptRssService("key", "password", "rssUrl", rm);
    assertNotNull(msg);
    System.out.println(msg);

    // Tests error updating service:
    rm = new RegistryManagerMock() {
      @Override
      public OrgCredentials getCreds(String key, String pass) {
        return OrgCredentials.with(key, pass);
      }

      @Override
      public boolean isLocalhost(String url) {
        return false;
      }

      @Override
      public ListServicesResponse listServices(String key) {
        return new ListServicesResponseMock() {
          @Override
          public List<GbrdsService> getResult() {
            return Lists.newArrayList(GbrdsService.builder().type(
                ServiceType.RSS.code).build());
          }

          @Override
          public int getStatus() {
            return HttpStatus.SC_OK;
          }
        };
      }

      @Override
      public boolean resourceExists(String key) {
        return true;
      }

      @Override
      public UpdateServiceResponse updateService(GbrdsService s,
          OrgCredentials creds) throws BadCredentialsException {
        throw new BadCredentialsException();
      }
    };
    msg = Helper.updateIptRssService("key", "password", "rssUrl", rm);
    assertNotNull(msg);
    System.out.println(msg);

    // Tests bad status code from updating service:
    rm = new RegistryManagerMock() {
      @Override
      public OrgCredentials getCreds(String key, String pass) {
        return OrgCredentials.with(key, pass);
      }

      @Override
      public boolean isLocalhost(String url) {
        return false;
      }

      @Override
      public ListServicesResponse listServices(String key) {
        return new ListServicesResponseMock() {
          @Override
          public List<GbrdsService> getResult() {
            return Lists.newArrayList(GbrdsService.builder().type(
                ServiceType.RSS.code).build());
          }

          @Override
          public int getStatus() {
            return HttpStatus.SC_OK;
          }
        };
      }

      @Override
      public boolean resourceExists(String key) {
        return true;
      }

      @Override
      public UpdateServiceResponse updateService(GbrdsService s,
          OrgCredentials creds) throws BadCredentialsException {
        return new UpdateServiceResponseMock() {
          @Override
          public int getStatus() {
            return HttpStatus.SC_BAD_REQUEST;
          }
        };
      }
    };
    msg = Helper.updateIptRssService("key", "password", "rssUrl", rm);
    assertNotNull(msg);
    System.out.println(msg);
  }

  /**
   * Test method for
   * {@link org.gbif.provider.webapp.action.admin.ConfigAction.Helper#updateServices(java.util.List, org.gbif.provider.service.RegistryManager)}
   * .
   */
  @Test
  public final void testUpdateServices() {
    // TODO
  }

}
