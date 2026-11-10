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

import java.io.File;
import java.math.BigDecimal;
import java.util.Date;

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
