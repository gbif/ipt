/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.ipt.action.admin;

import org.gbif.api.model.common.DOI;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.voc.DOIRegistrationAgency;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.validation.OrganisationSupport;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Saving an organisation with the DataCite account turned off (#3353).
 */
public class OrganisationsActionTest {

  private static final UUID KEY = UUID.fromString("f9b67ad0-9c9b-11d9-b9db-b8a03c50a862");

  private RegistrationManager registrationManager;
  private ResourceManager resourceManager;
  private OrganisationsAction action;

  @BeforeEach
  public void setup() {
    registrationManager = mock(RegistrationManager.class);
    resourceManager = mock(ResourceManager.class);
    OrganisationSupport support = mock(OrganisationSupport.class);
    when(support.validate(any(), any(), anyBoolean())).thenReturn(true);
    action = new OrganisationsAction(mock(SimpleTextProvider.class), mock(AppConfig.class), registrationManager,
        support, resourceManager, mock(RegistryManager.class));
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getMethod()).thenReturn("POST");
    action.withServletRequest(req);
    action.setLinkedOrganisations(new ArrayList<>());

    Organisation stored = new Organisation();
    stored.setKey(KEY.toString());
    stored.setDoiRegistrationAgency(DOIRegistrationAgency.DATACITE);
    stored.setDoiPrefix("10.21373");
    stored.setAgencyAccountUsername("GBIF.TEST");
    stored.setAgencyAccountPassword("secret");
    stored.setAgencyAccountPrimary(true);
    when(registrationManager.getFromDisk(KEY.toString())).thenAnswer(i -> new Organisation(stored));

    // the switch is off: the account fields aren't posted
    Organisation posted = new Organisation();
    posted.setKey(KEY.toString());
    posted.setPassword("token");
    action.id = KEY.toString();
    action.setOrganisation(posted);
  }

  private static Resource resource(String shortname, UUID doiOrganisation) {
    Resource r = new Resource();
    r.setShortname(shortname);
    r.setDoi(new DOI("10.21373/abc-" + shortname));
    r.setDoiOrganisationKey(doiOrganisation);
    return r;
  }

  @Test
  public void removingAccountWithDoisIsRefused() {
    when(resourceManager.list()).thenReturn(List.of(resource("ants", KEY), resource("bees", UUID.randomUUID())));

    action.validate();

    assertTrue(action.getFieldErrors().containsKey("organisation.doiAccount"));
    // the page is shown again with the stored account
    Organisation shown = action.getOrganisation();
    assertEquals(DOIRegistrationAgency.DATACITE, shown.getDoiRegistrationAgency());
    assertEquals("10.21373", shown.getDoiPrefix());
    assertEquals("GBIF.TEST", shown.getAgencyAccountUsername());
    assertEquals("secret", shown.getAgencyAccountPassword());
    assertTrue(shown.isAgencyAccountPrimary());
  }

  @Test
  public void removingAccountWithoutDoisIsAllowed() {
    when(resourceManager.list()).thenReturn(List.of(resource("bees", UUID.randomUUID())));

    action.validate();

    assertFalse(action.hasFieldErrors());
    assertNull(action.getOrganisation().getDoiRegistrationAgency());
  }
}
