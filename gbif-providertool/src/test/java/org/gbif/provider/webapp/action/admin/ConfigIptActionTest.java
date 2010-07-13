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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.gbif.mock.CreateResourceResponseMock;
import org.gbif.mock.CreateServiceResponseMock;
import org.gbif.mock.ReadOrgResponseMock;
import org.gbif.mock.ReadResourceResponseMock;
import org.gbif.mock.RegistryManagerMock;
import org.gbif.mock.ValidateOrgCredentialsResponseMock;
import org.gbif.provider.model.voc.ServiceType;
import org.gbif.provider.webapp.action.admin.ConfigIptAction.Helper;
import org.gbif.registry.api.client.GbrdsOrganisation;
import org.gbif.registry.api.client.GbrdsResource;
import org.gbif.registry.api.client.GbrdsService;
import org.gbif.registry.api.client.Gbrds.OrgCredentials;
import org.gbif.registry.api.client.GbrdsRegistry.CreateResourceResponse;
import org.gbif.registry.api.client.GbrdsRegistry.CreateServiceResponse;
import org.gbif.registry.api.client.GbrdsRegistry.ReadOrgResponse;
import org.gbif.registry.api.client.GbrdsRegistry.ReadResourceResponse;
import org.gbif.registry.api.client.GbrdsRegistry.ValidateOrgCredentialsResponse;
import org.junit.Test;

/**
 * Test coverage for {@link ConfigIptAction}.
 * 
 */
public class ConfigIptActionTest {

  /**
   * Test method for
   * {@link org.gbif.provider.webapp.action.admin.ConfigIptAction.Helper#checkLocalhostUrl(java.lang.String)}
   * .
   */
  @Test
  public final void testCheckLocalhostUrl() {
    assertFalse(Helper.checkLocalhostUrl("localhost"));
    assertFalse(Helper.checkLocalhostUrl("127.0.0.1"));
    assertFalse(Helper.checkLocalhostUrl(null));
    assertFalse(Helper.checkLocalhostUrl("http://localhost.com"));
    assertFalse(Helper.checkLocalhostUrl("http://127.0.0.1"));
    assertTrue(Helper.checkLocalhostUrl("http://foo.com"));
  }

  /**
   * Test method for
   * {@link org.gbif.provider.webapp.action.admin.ConfigIptAction.Helper#createResource(org.gbif.registry.api.client.GbrdsResource, org.gbif.registry.api.client.Gbrds.OrgCredentials, org.gbif.provider.service.RegistryManager)}
   * .
   */
  @Test
  public final void testCreateResource() {
    GbrdsResource gr = GbrdsResource.builder().build();
    OrgCredentials creds = OrgCredentials.with("key", "passwd");
    RegistryManagerMock rm = new RegistryManagerMock() {
    };

    // Null input testing:
    try {
      Helper.createResource(null, null, null);
      fail();
    } catch (Exception e) {
    }
    try {
      Helper.createResource(gr, null, null);
      fail();
    } catch (Exception e) {
    }
    try {
      Helper.createResource(null, creds, null);
      fail();
    } catch (Exception e) {
    }
    try {
      Helper.createResource(null, null, rm);
      fail();
    } catch (Exception e) {
    }
    try {
      Helper.createResource(gr, creds, null);
      fail();
    } catch (Exception e) {
    }
    try {
      Helper.createResource(null, creds, rm);
      fail();
    } catch (Exception e) {
    }
    try {
      Helper.createResource(gr, null, rm);
      fail();
    } catch (Exception e) {
    }

    // Test invalid resource:
    try {
      Helper.createResource(gr, creds, rm);
      fail();
    } catch (Exception e) {
    }

    // Test valid resource with invalid credentials:
    gr = GbrdsResource.builder().name("name").primaryContactType("type").primaryContactEmail(
        "email").organisationKey("key").build();
    rm = new RegistryManagerMock() {
      @Override
      public ValidateOrgCredentialsResponse validateCredentials(
          OrgCredentials creds) {
        return new ValidateOrgCredentialsResponseMock() {
          @Override
          public Boolean getResult() {
            return false;
          }
        };
      }
    };
    try {
      Helper.createResource(gr, creds, rm);
      fail();
    } catch (Exception e) {
    }

    // Test valid resource with valid credentials:
    gr = GbrdsResource.builder().name("name").primaryContactType("type").primaryContactEmail(
        "email").organisationKey("key").build();
    rm = new RegistryManagerMock() {
      @Override
      public CreateResourceResponse createGbrdsResource(final GbrdsResource gr,
          OrgCredentials creds) {
        return new CreateResourceResponseMock() {
          @Override
          public GbrdsResource getResult() {
            return gr;
          }
        };
      }

      @Override
      public ValidateOrgCredentialsResponse validateCredentials(
          OrgCredentials creds) {
        return new ValidateOrgCredentialsResponseMock() {
          @Override
          public Boolean getResult() {
            return true;
          }
        };
      }
    };
    assertEquals(gr, Helper.createResource(gr, creds, rm));
  }

  /**
   * Test method for
   * {@link org.gbif.provider.webapp.action.admin.ConfigIptAction.Helper#createRssService(java.lang.String, org.gbif.registry.api.client.Gbrds.OrgCredentials, java.lang.String, org.gbif.provider.service.RegistryManager)}
   * .
   */
  @Test
  public final void testCreateRssService() {
    OrgCredentials creds = OrgCredentials.with("k", "p");
    RegistryManagerMock rm = new RegistryManagerMock() {
    };

    try {
      Helper.createRssService(null, null, null, null);
      fail();
    } catch (Exception e) {
    }
    try {
      Helper.createRssService("", null, null, null);
      fail();
    } catch (Exception e) {
    }
    try {
      Helper.createRssService(" ", null, null, null);
      fail();
    } catch (Exception e) {
    }
    try {
      Helper.createRssService(null, null, "", null);
      fail();
    } catch (Exception e) {
    }
    try {
      Helper.createRssService(null, null, " ", null);
      fail();
    } catch (Exception e) {
    }
    try {
      Helper.createRssService(null, creds, "uri", rm);
      fail();
    } catch (Exception e) {
    }
    try {
      Helper.createRssService("", creds, "uri", rm);
      fail();
    } catch (Exception e) {
    }
    try {
      Helper.createRssService(" ", creds, "uri", rm);
      fail();
    } catch (Exception e) {
    }
    try {
      Helper.createRssService("key", null, "uri", rm);
      fail();
    } catch (Exception e) {
    }
    try {
      Helper.createRssService("key", creds, null, rm);
      fail();
    } catch (Exception e) {
    }
    try {
      Helper.createRssService("key", creds, "", rm);
      fail();
    } catch (Exception e) {
    }
    try {
      Helper.createRssService("key", creds, " ", rm);
      fail();
    } catch (Exception e) {
    }
    try {
      Helper.createRssService("key", creds, "uri", null);
      fail();
    } catch (Exception e) {
    }

    // Test resource key that doesn't exist:
    rm = new RegistryManagerMock() {
      @Override
      public ReadResourceResponse readGbrdsResource(final String key) {
        return new ReadResourceResponseMock() {
          @Override
          public GbrdsResource getResult() {
            return null;
          }
        };
      }

      @Override
      public ValidateOrgCredentialsResponse validateCredentials(
          OrgCredentials creds) {
        return new ValidateOrgCredentialsResponseMock() {
          @Override
          public Boolean getResult() {
            return true;
          }
        };
      }
    };
    try {
      Helper.createRssService("key", creds, "uri", rm);
      fail();
    } catch (Exception e) {
    }

    // Test invalid credentials:
    rm = new RegistryManagerMock() {
      @Override
      public ReadResourceResponse readGbrdsResource(final String key) {
        return new ReadResourceResponseMock() {
          @Override
          public GbrdsResource getResult() {
            return GbrdsResource.builder().key(key).build();
          }
        };
      }

      @Override
      public ValidateOrgCredentialsResponse validateCredentials(
          OrgCredentials creds) {
        return new ValidateOrgCredentialsResponseMock() {
          @Override
          public Boolean getResult() {
            return false;
          }
        };
      }
    };
    try {
      Helper.createRssService("key", creds, "uri", rm);
      fail();
    } catch (Exception e) {
    }

    // Test valid inputs with results:
    rm = new RegistryManagerMock() {
      @Override
      public CreateServiceResponse createGbrdsService(final GbrdsService gs,
          OrgCredentials creds) {
        return new CreateServiceResponseMock() {
          @Override
          public GbrdsService getResult() {
            return gs;
          }
        };
      }

      @Override
      public ReadResourceResponse readGbrdsResource(final String key) {
        return new ReadResourceResponseMock() {
          @Override
          public GbrdsResource getResult() {
            return GbrdsResource.builder().key(key).build();
          }
        };
      }

      @Override
      public ValidateOrgCredentialsResponse validateCredentials(
          OrgCredentials creds) {
        return new ValidateOrgCredentialsResponseMock() {
          @Override
          public Boolean getResult() {
            return true;
          }
        };
      }
    };
    GbrdsService gs = GbrdsService.builder().type(ServiceType.RSS.name()).resourceKey(
        "key").accessPointURL("uri").build();
    assertEquals(gs, Helper.createRssService("key", creds, "uri", rm));

    // Test valid inputs with no results:
    rm = new RegistryManagerMock() {
      @Override
      public CreateServiceResponse createGbrdsService(final GbrdsService gs,
          OrgCredentials creds) {
        return new CreateServiceResponseMock() {
          @Override
          public GbrdsService getResult() {
            return null;
          }
        };
      }

      @Override
      public ReadResourceResponse readGbrdsResource(final String key) {
        return new ReadResourceResponseMock() {
          @Override
          public GbrdsResource getResult() {
            return GbrdsResource.builder().key(key).build();
          }
        };
      }

      @Override
      public ValidateOrgCredentialsResponse validateCredentials(
          OrgCredentials creds) {
        return new ValidateOrgCredentialsResponseMock() {
          @Override
          public Boolean getResult() {
            return true;
          }
        };
      }
    };
    assertNull(Helper.createRssService("key", creds, "uri", rm));
  }

  /**
   * Test method for
   * {@link org.gbif.provider.webapp.action.admin.ConfigIptAction.Helper#getCreds(java.lang.String, java.lang.String)}
   * .
   */
  @Test
  public final void testGetCreds() {
    // Null input testing:
    assertNull(Helper.getCreds(null, null));
    assertNull(Helper.getCreds("key", null));
    assertNull(Helper.getCreds(null, "password"));

    // Empty string input testing:
    assertNull(Helper.getCreds("", ""));
    assertNull(Helper.getCreds(" ", " "));
    assertNull(Helper.getCreds(" ", ""));
    assertNull(Helper.getCreds("", " "));
    assertNull(Helper.getCreds("key", ""));
    assertNull(Helper.getCreds("", "password"));

    // Null with empty string input testing:
    assertNull(Helper.getCreds(null, ""));
    assertNull(Helper.getCreds("", null));
    assertNull(Helper.getCreds(null, " "));
    assertNull(Helper.getCreds(" ", null));

    // Valid credentials testing:
    OrgCredentials creds = Helper.getCreds("key", "password");
    assertNotNull(creds);
    assertEquals("key", creds.getKey());
    assertEquals("password", creds.getPassword());
  }

  /**
   * Test method for
   * {@link org.gbif.provider.webapp.action.admin.ConfigIptAction.Helper#getResourceBuilder(org.gbif.provider.model.ResourceMetadata)}
   * .
   */
  @Test
  public final void testGetResourceBuilder() {
    // TODO
  }

  /**
   * Test method for
   * {@link org.gbif.provider.webapp.action.admin.ConfigIptAction.Helper#getResourceMetadata(org.gbif.registry.api.client.GbrdsResource)}
   * .
   */
  @Test
  public final void testGetResourceMetadata() {
    // TODO
  }

  /**
   * Test method for
   * {@link org.gbif.provider.webapp.action.admin.ConfigIptAction.Helper#nullOrEmpty(java.lang.String)}
   * .
   */
  @Test
  public final void testNullOrEmpty() {
    assertTrue(Helper.nullOrEmpty(""));
    assertTrue(Helper.nullOrEmpty(" "));
    assertTrue(Helper.nullOrEmpty(null));
    assertFalse(Helper.nullOrEmpty("value"));
    assertFalse(Helper.nullOrEmpty(" value "));
  }

  /**
   * Test method for
   * {@link org.gbif.provider.webapp.action.admin.ConfigIptAction.Helper#orgExists(java.lang.String, org.gbif.provider.service.RegistryManager)}
   * .
   */
  @Test
  public final void testOrgExists() {
    // Null input testing:
    try {
      Helper.orgExists("key", null);
      fail();
    } catch (Exception e) {
    }

    // Invalid key testing:
    assertFalse(Helper.orgExists("", new RegistryManagerMock() {
    }));
    assertFalse(Helper.orgExists(" ", new RegistryManagerMock() {
    }));
    assertFalse(Helper.orgExists(null, new RegistryManagerMock() {
    }));

    // Valid input testing with existing org:
    RegistryManagerMock rm = new RegistryManagerMock() {
      @Override
      public ReadOrgResponse readGbrdsOrganisation(final String key) {
        return new ReadOrgResponseMock() {
          @Override
          public GbrdsOrganisation getResult() {
            return GbrdsOrganisation.builder().key(key).build();
          }
        };
      }
    };
    String key = "key";
    assertTrue(Helper.orgExists(key, rm));

    // Valid input testing with non-existing org:
    rm = new RegistryManagerMock() {
      @Override
      public ReadOrgResponse readGbrdsOrganisation(final String key) {
        return new ReadOrgResponseMock() {
          @Override
          public GbrdsOrganisation getResult() {
            return null;
          }
        };
      }
    };
    assertFalse(Helper.orgExists(key, rm));
  }

  /**
   * Test method for
   * {@link org.gbif.provider.webapp.action.admin.ConfigIptAction.Helper#resourceExists(java.lang.String, org.gbif.provider.service.RegistryManager)}
   * .
   */
  @Test
  public final void testResourceExists() {
    // Null input testing:
    try {
      Helper.resourceExists("key", null);
      fail();
    } catch (Exception e) {
    }

    // Invalid key testing:
    assertFalse(Helper.resourceExists("", new RegistryManagerMock() {
    }));
    assertFalse(Helper.resourceExists(" ", new RegistryManagerMock() {
    }));
    assertFalse(Helper.resourceExists(null, new RegistryManagerMock() {
    }));

    // Valid input testing with existing resource:
    RegistryManagerMock rm = new RegistryManagerMock() {
      @Override
      public ReadResourceResponse readGbrdsResource(final String key) {
        return new ReadResourceResponseMock() {
          @Override
          public GbrdsResource getResult() {
            return GbrdsResource.builder().key(key).build();
          }
        };
      }
    };
    String key = "key";
    assertTrue(Helper.resourceExists(key, rm));

    // Valid input testing with non-existing org:
    rm = new RegistryManagerMock() {
      @Override
      public ReadResourceResponse readGbrdsResource(String key) {
        return new ReadResourceResponseMock() {
          @Override
          public GbrdsResource getResult() {
            return null;
          }
        };
      }
    };
    assertFalse(Helper.resourceExists(key, rm));
  }

  /**
   * Test method for
   * {@link org.gbif.provider.webapp.action.admin.ConfigIptAction.Helper#updateResource(org.gbif.registry.api.client.GbrdsResource, org.gbif.registry.api.client.Gbrds.OrgCredentials, org.gbif.provider.service.RegistryManager)}
   * .
   */
  @Test
  public final void testUpdateResource() {
    // TODO
  }

  /**
   * Test method for
   * {@link org.gbif.provider.webapp.action.admin.ConfigIptAction.Helper#validateCreds(org.gbif.registry.api.client.Gbrds.OrgCredentials, org.gbif.provider.service.RegistryManager)}
   * .
   */
  @Test
  public final void testValidateCreds() {
    OrgCredentials creds = OrgCredentials.with("k", "p");

    // Null input testing:
    try {
      Helper.validateCreds(creds, null);
      fail();
    } catch (Exception e) {
    }

    // Valid creds test:
    assertTrue(Helper.validateCreds(creds, new RegistryManagerMock() {
      @Override
      public ValidateOrgCredentialsResponse validateCredentials(
          OrgCredentials creds) {
        return new ValidateOrgCredentialsResponseMock() {
          @Override
          public Boolean getResult() {
            return true;
          }
        };
      }
    }));

    // Invalid creds test:
    assertFalse(Helper.validateCreds(creds, new RegistryManagerMock() {
      @Override
      public ValidateOrgCredentialsResponse validateCredentials(
          OrgCredentials creds) {
        return new ValidateOrgCredentialsResponseMock() {
          @Override
          public Boolean getResult() {
            return false;
          }
        };
      }
    }));
  }

  /**
   * Test method for
   * {@link org.gbif.provider.webapp.action.admin.ConfigIptAction.Helper#validateResource(org.gbif.registry.api.client.GbrdsResource)}
   * .
   */
  @Test
  public final void testValidateResource() {
    GbrdsResource resource = GbrdsResource.builder().name("name").primaryContactType(
        "type").primaryContactEmail("email").organisationKey("Key").build();

    // Null testing:
    try {
      Helper.validateResource(null);
      fail();
    } catch (Exception e) {
    }

    // Valid organisation testing:
    assertTrue(Helper.validateResource(resource).isEmpty());

    // Invalid organisation testing:
    assertFalse(Helper.validateResource(GbrdsResource.builder().build()).isEmpty());
  }

}
