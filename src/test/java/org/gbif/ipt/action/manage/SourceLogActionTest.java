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
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.util.HashMap;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SourceLogActionTest {

  private static SourceLogAction sourceLogAction(String r, String s) {
    Resource resource = new Resource();
    resource.setShortname("res");
    ResourceManager resourceManager = mock(ResourceManager.class);
    when(resourceManager.get("res")).thenReturn(resource);

    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getParameter(Constants.REQ_PARAM_RESOURCE)).thenReturn(r);
    when(req.getParameter(Constants.REQ_PARAM_SOURCE)).thenReturn(s);
    when(req.getMethod()).thenReturn("GET");

    SourceLogAction action = new SourceLogAction(new SimpleTextProvider(), mock(AppConfig.class),
        mock(RegistrationManager.class), resourceManager, mock(SourceManager.class), mock(DataDir.class));
    action.setServletRequest(req);
    action.setSession(new HashMap<>());
    action.prepare();
    return action;
  }

  /**
   * The source log without r, or with a missing or unknown s, is a 404, as a page and as a download.
   */
  @Test
  public void testSourceLogNotFound() {
    assertEquals(BaseAction.NOT_FOUND, sourceLogAction(null, null).execute());
    assertEquals(BaseAction.NOT_FOUND, sourceLogAction(null, "src").execute());
    assertEquals(BaseAction.NOT_FOUND, sourceLogAction("res", null).execute());
    assertEquals(BaseAction.NOT_FOUND, sourceLogAction("res", "unknown").execute());
    assertEquals(BaseAction.NOT_FOUND, sourceLogAction("res", "unknown").download());
  }
}
