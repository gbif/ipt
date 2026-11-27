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
package org.gbif.ipt.service.admin.impl;

import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OrganisationRegistryUpdaterTest {

  private static final String KEY_A = "00000000-0000-0000-0000-00000000000a";
  private static final String KEY_B = "00000000-0000-0000-0000-00000000000b";
  private static final String KEY_C = "00000000-0000-0000-0000-00000000000c";

  private DataDir dataDir;
  private RegistrationManager registrationManager;
  private ResourceManager resourceManager;
  private OrganisationRegistryUpdater updater;

  @BeforeEach
  public void setup() {
    dataDir = mock(DataDir.class);
    when(dataDir.isConfigured()).thenReturn(true);
    registrationManager = mock(RegistrationManager.class);
    resourceManager = mock(ResourceManager.class);
    Organisation none = organisation(Constants.DEFAULT_ORG_KEY.toString());
    Organisation a = organisation(KEY_A);
    Organisation b = organisation(KEY_B);
    Organisation c = organisation(KEY_C);
    when(registrationManager.listAll()).thenReturn(List.of(a, b, c, none));
    when(registrationManager.get(KEY_A)).thenReturn(a);
    when(registrationManager.get(KEY_B)).thenReturn(b);
    when(registrationManager.get(KEY_C)).thenReturn(c);
    updater = new OrganisationRegistryUpdater(dataDir, registrationManager, resourceManager);
  }

  private static Organisation organisation(String key) {
    Organisation o = new Organisation();
    o.setKey(key);
    o.setName("Organisation " + key);
    return o;
  }

  @Test
  public void updatesEveryOrganisationButTheDefaultOne() throws Exception {
    when(registrationManager.updateOrganisationFromRegistry(anyString())).thenReturn(true);

    assertTrue(updater.refresh());

    verify(registrationManager).updateOrganisationFromRegistry(KEY_A);
    verify(registrationManager).updateOrganisationFromRegistry(KEY_B);
    verify(registrationManager).updateOrganisationFromRegistry(KEY_C);
    verify(registrationManager, never()).updateOrganisationFromRegistry(Constants.DEFAULT_ORG_KEY.toString());
    verify(resourceManager, times(3)).updateOrganisationNameForResources(any(Organisation.class));
  }

  @Test
  public void notFoundOrBadResponseSkipsOnlyThatOrganisation() throws Exception {
    when(registrationManager.updateOrganisationFromRegistry(KEY_A)).thenReturn(false);
    when(registrationManager.updateOrganisationFromRegistry(KEY_B))
        .thenThrow(new RegistryException(RegistryException.Type.BAD_RESPONSE, "url", "bad JSON"));
    when(registrationManager.updateOrganisationFromRegistry(KEY_C)).thenReturn(true);

    assertTrue(updater.refresh());

    verify(registrationManager).updateOrganisationFromRegistry(KEY_C);
    verify(resourceManager, times(1)).updateOrganisationNameForResources(any(Organisation.class));
  }

  @Test
  public void unreachableRegistryStopsTheRound() throws Exception {
    when(registrationManager.updateOrganisationFromRegistry(KEY_A))
        .thenThrow(new RegistryException(RegistryException.Type.PROXY, "url", "timed out"));

    assertFalse(updater.refresh());

    verify(registrationManager, never()).updateOrganisationFromRegistry(KEY_B);
    verify(registrationManager, never()).updateOrganisationFromRegistry(KEY_C);
    verify(resourceManager, never()).updateOrganisationNameForResources(any(Organisation.class));
  }

  @Test
  public void removedDuringTheRoundIsSkipped() throws Exception {
    when(registrationManager.updateOrganisationFromRegistry(KEY_A)).thenThrow(new IllegalArgumentException("gone"));
    when(registrationManager.updateOrganisationFromRegistry(KEY_B)).thenReturn(true);
    when(registrationManager.updateOrganisationFromRegistry(KEY_C)).thenReturn(true);

    assertTrue(updater.refresh());

    verify(resourceManager, times(2)).updateOrganisationNameForResources(any(Organisation.class));
  }

  @Test
  public void notSetUpCallsNothing() throws Exception {
    when(dataDir.isConfigured()).thenReturn(false);

    assertFalse(updater.refresh());

    verify(registrationManager, never()).listAll();
    verify(registrationManager, never()).updateOrganisationFromRegistry(anyString());
  }
}
