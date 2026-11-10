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
package org.gbif.ipt.struts2;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.manage.ResourceManager;

import java.util.HashMap;
import java.util.Map;

import org.apache.struts2.ActionContext;
import org.apache.struts2.ActionInvocation;
import org.apache.struts2.dispatcher.HttpParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PrivateDeletedResourceInterceptorTest {

  private static final String PRIVATE = "private1";
  private static final String PUBLIC = "public1";

  private PrivateDeletedResourceInterceptor interceptor;
  private ActionInvocation invocation;
  private Map<String, Object> session;
  private Map<String, Object> params;
  private User owner;

  @BeforeEach
  void setUp() throws Exception {
    owner = new User();
    owner.setEmail("owner@example.org");
    owner.setRole(User.Role.Manager);

    ResourceManager resourceManager = mock(ResourceManager.class);
    when(resourceManager.get(PRIVATE)).thenReturn(resource(PRIVATE, PublicationStatus.PRIVATE));
    when(resourceManager.get(PUBLIC)).thenReturn(resource(PUBLIC, PublicationStatus.PUBLIC));

    interceptor = new PrivateDeletedResourceInterceptor();
    interceptor.setResourceManager(resourceManager);

    session = new HashMap<>();
    params = new HashMap<>();
    invocation = mock(ActionInvocation.class);
    when(invocation.invoke()).thenReturn(BaseAction.SUCCESS);
  }

  private Resource resource(String shortname, PublicationStatus status) {
    Resource r = new Resource();
    r.setShortname(shortname);
    r.setStatus(status);
    r.setCreator(owner);
    return r;
  }

  private String intercept() throws Exception {
    ActionContext context = ActionContext.of(new HashMap<>())
        .withSession(session)
        .withParameters(HttpParameters.create(params).build());
    when(invocation.getInvocationContext()).thenReturn(context);
    return interceptor.intercept(invocation);
  }

  @Test
  public void privateResourceParamIsRejectedForAnonymous() throws Exception {
    params.put(Constants.REQ_PARAM_RESOURCE, PRIVATE);
    assertEquals(BaseAction.NOT_ALLOWED, intercept());
    verify(invocation, never()).invoke();
  }
}
