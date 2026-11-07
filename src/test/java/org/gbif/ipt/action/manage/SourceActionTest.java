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

import org.gbif.ipt.IptBaseTest;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.config.JdbcSupport;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Source;
import org.gbif.ipt.model.TextFileSource;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.manage.SourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SourceActionTest extends IptBaseTest {

  private SourceAction action;
  private ResourceManager resourceManager;

  @BeforeEach
  public void setup() throws IOException {
    // mock action
    resourceManager = mock(ResourceManager.class);
    action = new SourceAction(mock(SimpleTextProvider.class), mock(AppConfig.class), mock(RegistrationManager.class),
      resourceManager, mock(SourceManager.class), mock(JdbcSupport.class), mock(DataDir.class));
  }

  @Test
  public void testAlertColumnNumberChange() {
    Source src = new TextFileSource();
    src.setName("images");
    action.setSource(src);

    assertFalse(action.alertColumnNumberChange(false, 1, 2));
    assertFalse(action.alertColumnNumberChange(true, 0, 0));
    assertFalse(action.alertColumnNumberChange(true, 10, 10));
    assertTrue(action.alertColumnNumberChange(true, 1, 2));
    assertTrue(action.alertColumnNumberChange(true, 2, 1));
  }

  /**
   * No resource parameter and no resource in the session: must return 404 instead of throwing an NPE.
   */
  @Test
  public void testNoResourceSelectedReturnsNotFound() throws Exception {
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getParameter("id")).thenReturn("occurrence");
    when(req.getMethod()).thenReturn("GET");
    action.setServletRequest(req);
    action.setSession(new HashMap<>());

    action.prepare();
    assertEquals(SourceAction.NOT_FOUND, action.peek());
    assertEquals(SourceAction.NOT_FOUND, action.execute());

    // POST: validation must not touch the missing resource
    when(req.getMethod()).thenReturn("POST");
    action.validate();
    assertEquals(SourceAction.NOT_FOUND, action.execute());
  }

  /**
   * The resource exists but has no source with the requested id: must return 404 instead of rendering the source
   * form without a source.
   */
  @Test
  public void testUnknownSourceReturnsNotFound() throws Exception {
    Resource resource = new Resource();
    resource.setShortname("res1");
    when(resourceManager.get("res1")).thenReturn(resource);

    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getParameter("r")).thenReturn("res1");
    when(req.getParameter("id")).thenReturn("occurrences");
    when(req.getMethod()).thenReturn("GET");
    action.setServletRequest(req);
    action.setSession(new HashMap<>());

    action.prepare();
    // an action error would make the workflow interceptor return the input form before execute()
    assertFalse(action.hasErrors());
    assertEquals(SourceAction.NOT_FOUND, action.execute());
    assertEquals(SourceAction.NOT_FOUND, action.peek());
    assertEquals(SourceAction.NOT_FOUND, action.delete());
  }

  /**
   * No resource parameter: a resource stored in the session, e.g. by IPT versions up to 3.3.7, must not be used instead.
   */
  @Test
  public void testSessionResourceIsNotUsed() throws Exception {
    Resource resource = new Resource();
    resource.setShortname("res1");
    when(resourceManager.get("res1")).thenReturn(resource);

    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getParameter("id")).thenReturn("occurrences");
    when(req.getMethod()).thenReturn("GET");
    action.setServletRequest(req);
    Map<String, Object> session = new HashMap<>();
    session.put("curr_resource", "res1");
    action.setSession(session);

    action.prepare();
    assertNull(action.getResource());
    assertEquals(SourceAction.NOT_FOUND, action.execute());
  }
}
