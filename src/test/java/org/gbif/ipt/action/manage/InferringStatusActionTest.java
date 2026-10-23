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
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.InferredMetadataManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.struts2.ActionSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InferringStatusActionTest {

  private static final String SHORTNAME = "res";

  private InferredMetadataManager manager;
  private HttpServletRequest req;
  private ResourceManager resourceManager;

  @BeforeEach
  public void setUp() {
    manager = mock(InferredMetadataManager.class);
    resourceManager = mock(ResourceManager.class);
    Resource resource = new Resource();
    resource.setShortname(SHORTNAME);
    when(resourceManager.get(SHORTNAME)).thenReturn(resource);
    req = mock(HttpServletRequest.class);
  }

  @DisplayName("Whether the metadata of the resource is being inferred in the background")
  @Test
  public void testStatus() {
    when(req.getParameter(Constants.REQ_PARAM_RESOURCE)).thenReturn(SHORTNAME);

    when(manager.isInferring(SHORTNAME)).thenReturn(true);
    assertEquals(Map.of("inferring", true), status());

    when(manager.isInferring(SHORTNAME)).thenReturn(false);
    assertEquals(Map.of("inferring", false), status());
  }

  @DisplayName("Unknown resource: not inferring")
  @Test
  public void testUnknownResource() {
    when(req.getParameter(Constants.REQ_PARAM_RESOURCE)).thenReturn("unknown");

    assertEquals(Map.of("inferring", false), status());
  }

  private Map<String, Object> status() {
    InferringStatusAction action = new InferringStatusAction(mock(SimpleTextProvider.class), mock(AppConfig.class),
        mock(RegistrationManager.class), resourceManager, manager);
    action.withServletRequest(req);
    Map<String, Object> session = new HashMap<>();
    session.put(Constants.SESSION_USER, new User());
    action.withSession(session);
    action.prepare();

    assertEquals(ActionSupport.SUCCESS, action.execute());
    return action.getStatus();
  }
}
