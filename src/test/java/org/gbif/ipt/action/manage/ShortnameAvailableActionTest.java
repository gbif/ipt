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
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ShortnameAvailableActionTest {

  private ShortnameAvailableAction action;

  @BeforeEach
  public void setUp() {
    ResourceManager resourceManager = mock(ResourceManager.class);
    when(resourceManager.get("taken")).thenReturn(new Resource());
    action = new ShortnameAvailableAction(mock(SimpleTextProvider.class), mock(AppConfig.class),
        mock(RegistrationManager.class), resourceManager);
  }

  @Test
  public void testFreeShortname() {
    action.setShortname("free");
    action.execute();
    assertEquals(true, action.getStatus().get("available"));
  }

  @Test
  public void testTakenShortname() {
    action.setShortname(" taken ");
    action.execute();
    assertEquals(false, action.getStatus().get("available"));
  }

  @Test
  public void testNoShortname() {
    action.setShortname("  ");
    action.execute();
    assertEquals(false, action.getStatus().get("available"));
  }
}
