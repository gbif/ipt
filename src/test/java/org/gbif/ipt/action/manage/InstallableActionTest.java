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
package org.gbif.ipt.action.manage;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.DataPackageSchema;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InstallableActionTest {

  private static final String MULTIMEDIA = "http://rs.gbif.org/terms/1.0/Multimedia";
  private static final String UNKNOWN_ROW_TYPE = "http://example.org/terms/Unknown";
  private static final String CAMTRAP_DP = "http://rs.gbif.org/data-packages/camtrap-dp";
  private static final String UNKNOWN_DP = "http://example.org/unknown-dp";

  private RegistryManager registryManager;
  private InstallableAction action;

  @BeforeEach
  public void setUp() {
    registryManager = mock(RegistryManager.class);
    action = new InstallableAction(mock(SimpleTextProvider.class), mock(AppConfig.class),
        mock(RegistrationManager.class), registryManager);
  }

  @Test
  public void testOnlyRegisteredTypesAreInstallable() {
    Extension multimedia = new Extension();
    multimedia.setRowType(MULTIMEDIA);
    when(registryManager.getExtensions()).thenReturn(List.of(multimedia));
    when(registryManager.getLatestDataPackageSchemas())
        .thenReturn(List.of(DataPackageSchema.builder().identifier(CAMTRAP_DP).name("camtrap-dp").build()));
    action.setRowType(List.of(MULTIMEDIA, UNKNOWN_ROW_TYPE));
    action.setDataPackageType(List.of(CAMTRAP_DP.replace("http://", "https://"), UNKNOWN_DP));

    action.execute();

    assertEquals(Map.of(MULTIMEDIA, true, UNKNOWN_ROW_TYPE, false), action.getInstallable().get("rowTypes"));
    assertEquals(Map.of(CAMTRAP_DP.replace("http://", "https://"), true, UNKNOWN_DP, false),
        action.getInstallable().get("dataPackageTypes"));
  }

  @Test
  public void testRegistryOnlyAskedForWhatIsMissing() {
    action.setRowType(List.of(MULTIMEDIA));

    action.execute();

    verify(registryManager, never()).getLatestDataPackageSchemas();
  }

  @Test
  public void testUnknownWhenRegistryUnreachable() {
    when(registryManager.getExtensions())
        .thenThrow(new RegistryException("http://registry.example.org", new IOException("down")));
    action.setRowType(List.of(MULTIMEDIA));

    action.execute();

    assertTrue(action.getInstallable().isEmpty());
  }
}
