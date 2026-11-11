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

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.math.BigDecimal;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VersionHistoryActionTest {

  private static VersionHistoryAction prepare(String v, String method) {
    Resource resource = new Resource();
    resource.setShortname("res");
    resource.addVersionHistory(new VersionHistory(new BigDecimal("1.0"), PublicationStatus.PUBLIC));
    ResourceManager resourceManager = mock(ResourceManager.class);
    when(resourceManager.get("res")).thenReturn(resource);

    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getParameter(Constants.REQ_PARAM_RESOURCE)).thenReturn("res");
    when(req.getParameter(Constants.REQ_PARAM_VERSION)).thenReturn(v);
    when(req.getMethod()).thenReturn(method);

    VersionHistoryAction action = new VersionHistoryAction(mock(SimpleTextProvider.class), mock(AppConfig.class),
        mock(RegistrationManager.class), resourceManager);
    action.withServletRequest(req);
    action.prepare();
    return action;
  }

  /**
   * A missing, invalid or unknown version is a 404, for the form and for saving it.
   */
  @Test
  public void testUnknownVersion() throws Exception {
    for (String method : new String[] {"GET", "POST"}) {
      assertEquals(BaseAction.NOT_FOUND, prepare(null, method).execute());
      assertEquals(BaseAction.NOT_FOUND, prepare("abc", method).execute());
      assertEquals(BaseAction.NOT_FOUND, prepare("2.0", method).execute());
    }
  }

  @Test
  public void testKnownVersion() throws Exception {
    assertEquals(BaseAction.INPUT, prepare("1.0", "GET").execute());
  }
}
