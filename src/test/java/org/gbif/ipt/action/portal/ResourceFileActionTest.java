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
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.utils.HttpClient;
import org.gbif.utils.HttpUtil;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Date;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

  /**
   * The RTF without v is the latest published version's, like the archive and the metadata. Never published: 404.
   */
  @Test
  public void testRtfWithoutVersion(@TempDir File tmp) throws Exception {
    Resource published = new Resource();
    published.setShortname("res");
    published.addVersionHistory(new VersionHistory(new BigDecimal("1.1"), new Date(), PublicationStatus.PUBLIC));
    Resource neverPublished = new Resource();
    neverPublished.setShortname("draft");

    ResourceManager resourceManager = mock(ResourceManager.class);
    when(resourceManager.get("res")).thenReturn(published);
    when(resourceManager.get("draft")).thenReturn(neverPublished);
    File rtf = new File(tmp, "res-1.1.rtf");
    assertTrue(rtf.createNewFile());
    DataDir dataDir = mock(DataDir.class);
    when(dataDir.resourceRtfFile("res", new BigDecimal("1.1"))).thenReturn(rtf);

    ResourceFileAction action = rtfAction("res", resourceManager, dataDir);
    assertEquals(BaseAction.SUCCESS, action.rtf());
    assertEquals("rtf-res-v1.1.rtf", action.getFilename());

    assertEquals(BaseAction.NOT_FOUND, rtfAction("draft", resourceManager, dataDir).rtf());
  }

  private static ResourceFileAction rtfAction(String r, ResourceManager resourceManager, DataDir dataDir) {
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getParameter(Constants.REQ_PARAM_RESOURCE)).thenReturn(r);
    ResourceFileAction action = new ResourceFileAction(new SimpleTextProvider(), mock(AppConfig.class),
        mock(RegistrationManager.class), dataDir, resourceManager);
    action.setServletRequest(req);
    action.withServletResponse(mock(HttpServletResponse.class));
    action.prepare();
    return action;
  }
}
