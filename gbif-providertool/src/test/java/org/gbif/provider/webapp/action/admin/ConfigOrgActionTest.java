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
package org.gbif.provider.webapp.action.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.gbif.mock.CreateOrgResponseMock;
import org.gbif.mock.ReadOrgResponseMock;
import org.gbif.mock.ReadResourceResponseMock;
import org.gbif.mock.RegistryManagerMock;
import org.gbif.mock.UpdateOrgResponseMock;
import org.gbif.mock.ValidateOrgCredentialsResponseMock;
import org.gbif.provider.model.ResourceMetadata;
import org.gbif.provider.service.RegistryManager;
import org.gbif.provider.webapp.action.admin.ConfigOrgAction.Helper;
import org.gbif.registry.api.client.GbrdsOrganisation;
import org.gbif.registry.api.client.GbrdsResource;
import org.gbif.registry.api.client.Gbrds.OrgCredentials;
import org.gbif.registry.api.client.GbrdsRegistry.CreateOrgResponse;
import org.gbif.registry.api.client.GbrdsRegistry.ReadOrgResponse;
import org.gbif.registry.api.client.GbrdsRegistry.ReadResourceResponse;
import org.gbif.registry.api.client.GbrdsRegistry.UpdateOrgResponse;
import org.gbif.registry.api.client.GbrdsRegistry.ValidateOrgCredentialsResponse;
import org.junit.Test;

/**
 * Test coverage for {@link ConfigOrgAction.Helper}.
 * 
 */
public class ConfigOrgActionTest {

  /**
   * Test method for
   * {@link ConfigOrgAction.Helper#createOrg(GbrdsOrganisation, RegistryManager)}
   * .
   */
  @Test
  public final void testCreateOrg() {
    GbrdsOrganisation org = GbrdsOrganisation.builder().build();
    RegistryManagerMock rm;

    // Null input testing:
    try {
      Helper.createOrg(null, null);
      fail();
    } catch (Exception e) {
    }
    try {
      Helper.createOrg(null, new RegistryManagerMock() {
      });
      fail();
    } catch (Exception e) {
    }
    try {
      Helper.createOrg(GbrdsOrganisation.builder().build(), null);
      fail();
    } catch (Exception e) {
    }

    // Invalid organisation testing:
    try {
      Helper.createOrg(org, new RegistryManagerMock() {
      });
      fail();
    } catch (Exception e) {
    }

    // Valid organisation testing:
    final OrgCredentials mockCreds = OrgCredentials.with("key", "password");
    org = GbrdsOrganisation.builder().name("name").primaryContactType("type").primaryContactEmail(
        "email").nodeKey("key").build();
    rm = new RegistryManagerMock() {
      @Override
      public CreateOrgResponse createGbrdsOrganisation(GbrdsOrganisation org) {
        return new CreateOrgResponseMock() {
          @Override
          public OrgCredentials getResult() {
            return mockCreds;
          }
        };
      }
    };
    OrgCredentials creds = Helper.createOrg(org, rm);
    assertEquals(mockCreds, creds);
  }

  /**
   * Test method for
   * {@link org.gbif.provider.webapp.action.admin.ConfigOrgAction.Helper#getCreds(java.lang.String, java.lang.String)}
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
   * {@link org.gbif.provider.webapp.action.admin.ConfigOrgAction.Helper#getOrg(org.gbif.provider.model.ResourceMetadata)}
   * .
   */
  @Test
  public final void testGetOrg() {
    // Null input testing:
    try {
      Helper.getOrg(null);
      fail();
    } catch (Exception e) {
    }

    // Valid input testing:
    GbrdsOrganisation org = GbrdsOrganisation.builder().description(
        "description").primaryContactEmail("email").primaryContactName("name").name(
        "title").key("key").homepageURL("link").build();
    ResourceMetadata meta = Helper.getResourceMetadata(org);
    assertEquals(org, Helper.getOrg(meta));
  }

  /**
   * Test method for
   * {@link org.gbif.provider.webapp.action.admin.ConfigOrgAction.Helper#getResourceMetadata(org.gbif.registry.api.client.GbrdsOrganisation)}
   * .
   */
  @Test
  public final void testGetResourceMetadata() {
    // Null input testing:
    try {
      Helper.getResourceMetadata(null);
      fail();
    } catch (Exception e) {
    }

    // Valid meta testing:
    ResourceMetadata meta = new ResourceMetadata();
    meta.setDescription("description");
    meta.setContactEmail("email");
    meta.setContactName("name");
    meta.setTitle("title");
    meta.setUddiID("key");
    meta.setLink("link");
    GbrdsOrganisation org = Helper.getOrg(meta);
    assertEquals(meta, Helper.getResourceMetadata(org));
  }

  /**
   * Test method for
   * {@link org.gbif.provider.webapp.action.admin.ConfigOrgAction.Helper#nullOrEmpty(java.lang.String)}
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
   * {@link org.gbif.provider.webapp.action.admin.ConfigOrgAction.Helper#orgExists(java.lang.String, org.gbif.provider.service.RegistryManager)}
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
   * {@link org.gbif.provider.webapp.action.admin.ConfigOrgAction.Helper#resourceExists(java.lang.String, org.gbif.provider.service.RegistryManager)}
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
   * {@link org.gbif.provider.webapp.action.admin.ConfigOrgAction.Helper#updateOrg(org.gbif.registry.api.client.GbrdsOrganisation, org.gbif.provider.service.RegistryManager)}
   * .
   */
  @Test
  public final void testUpdateOrg() {
    GbrdsOrganisation org = GbrdsOrganisation.builder().name("name").primaryContactType(
        "type").primaryContactEmail("email").nodeKey("nodeKey").password("pass").key(
        "key").build();

    // Null input testing:
    try {
      Helper.updateOrg(null, null);
      fail();
    } catch (Exception e) {
    }
    try {
      Helper.updateOrg(org, null);
      fail();
    } catch (Exception e) {
    }
    try {
      Helper.updateOrg(null, new RegistryManagerMock() {
      });
      fail();
    } catch (Exception e) {
    }

    // Invalid organisation testing:
    try {
      Helper.updateOrg(GbrdsOrganisation.builder().build(),
          new RegistryManagerMock() {
          });
      fail();
    } catch (Exception e) {
    }

    // Valid organisation without password testing:
    GbrdsOrganisation o = GbrdsOrganisation.builder().name("name").primaryContactType(
        "type").primaryContactEmail("email").nodeKey("key").build();
    try {
      Helper.updateOrg(o, new RegistryManagerMock() {
      });
      fail();
    } catch (Exception e) {
    }

    // Valid organisation and credentials testing:
    assertTrue(Helper.updateOrg(org, new RegistryManagerMock() {
      @Override
      public UpdateOrgResponse updateGbrdsOrganisation(GbrdsOrganisation org,
          OrgCredentials creds) {
        return new UpdateOrgResponseMock() {
          @Override
          public Boolean getResult() {
            return true;
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
    }));

    // Valid organisation and credentials but update returns false:
    assertFalse(Helper.updateOrg(org, new RegistryManagerMock() {
      @Override
      public UpdateOrgResponse updateGbrdsOrganisation(GbrdsOrganisation org,
          OrgCredentials creds) {
        return new UpdateOrgResponseMock() {
          @Override
          public Boolean getResult() {
            return false;
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
    }));

    // Valid organisation but invalid credentials:
    try {
      Helper.updateOrg(org, new RegistryManagerMock() {
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
      });
      fail();
    } catch (Exception e) {
    }
  }

  /**
   * Test method for
   * {@link org.gbif.provider.webapp.action.admin.ConfigOrgAction.Helper#validateCreds(org.gbif.registry.api.client.Gbrds.OrgCredentials, org.gbif.provider.service.RegistryManager)}
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
   * {@link org.gbif.provider.webapp.action.admin.ConfigOrgAction.Helper#validateOrg(org.gbif.registry.api.client.GbrdsOrganisation)}
   * .
   */
  @Test
  public final void testValidateOrg() {
    GbrdsOrganisation org = GbrdsOrganisation.builder().name("name").primaryContactType(
        "type").primaryContactEmail("email").nodeKey("nodeKey").password("pass").key(
        "key").build();

    // Null testing:
    try {
      Helper.validateOrg(null);
      fail();
    } catch (Exception e) {
    }

    // Valid organisation testing:
    assertTrue(Helper.validateOrg(org).isEmpty());

    // Invalid organisation testing:
    assertFalse(Helper.validateOrg(GbrdsOrganisation.builder().build()).isEmpty());
  }
}