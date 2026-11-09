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
package org.gbif.ipt.action.portal;

import org.gbif.ipt.IptBaseTest;
import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.utils.HttpClient;
import org.gbif.utils.HttpUtil;

import java.io.File;
import java.net.URL;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResourceFileActionTest extends IptBaseTest {

  /**
   * Manual test to tryout the conditional get archive download
   */
  @Test
  @Disabled
  public void testDwca() throws Exception {
    HttpClient client = HttpUtil.newMultithreadedClient(1000, 1, 1);
    File down = new File("/Users/markus/Desktop/dwca.zip");
    URL url = new URL("http://localhost:7001/ipt/archive.do?r=condiget");
    boolean status = client.downloadIfChanged(url, down);
    System.out.println("Download success=" + status);
  }

  private static ResourceFileAction sourceLogAction(String r, String s) {
    Resource resource = new Resource();
    resource.setShortname("res");
    ResourceManager resourceManager = mock(ResourceManager.class);
    when(resourceManager.get("res")).thenReturn(resource);

    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getParameter(Constants.REQ_PARAM_RESOURCE)).thenReturn(r);
    when(req.getParameter(Constants.REQ_PARAM_SOURCE)).thenReturn(s);

    ResourceFileAction action = new ResourceFileAction(new SimpleTextProvider(), mock(AppConfig.class),
        mock(RegistrationManager.class), mock(DataDir.class), resourceManager);
    action.setServletRequest(req);
    action.prepare();
    return action;
  }

  /**
   * The source log without r, or with a missing or unknown s, is a 404.
   */
  @Test
  public void testSourceLogNotFound() {
    assertEquals(BaseAction.NOT_FOUND, sourceLogAction(null, null).sourceLog());
    assertEquals(BaseAction.NOT_FOUND, sourceLogAction(null, "src").sourceLog());
    assertEquals(BaseAction.NOT_FOUND, sourceLogAction("res", null).sourceLog());
    assertEquals(BaseAction.NOT_FOUND, sourceLogAction("res", "unknown").sourceLog());
  }
}
