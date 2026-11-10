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
import org.gbif.ipt.action.manage.ManagerBaseAction;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.service.manage.ResourceManager;

import jakarta.servlet.http.HttpServletRequest;
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

public class RequireManagerInterceptorTest {

  private static final String SHORTNAME = "res1";

  private RequireManagerInterceptor interceptor;
  private ResourceManager resourceManager;
  private ActionInvocation invocation;
  private Map<String, Object> params;
  private Map<String, Object> session;
  private User owner;
  private User otherManager;

  @BeforeEach
  void setUp() throws Exception {
    owner = user("owner@example.org", User.Role.Manager);
    otherManager = user("other@example.org", User.Role.Manager);

    Resource resource = new Resource();
    resource.setShortname(SHORTNAME);
    resource.setCreator(owner);

    resourceManager = mock(ResourceManager.class);
    when(resourceManager.get(SHORTNAME)).thenReturn(resource);

    interceptor = new RequireManagerInterceptor();
    interceptor.setResourceManager(resourceManager);

    session = new HashMap<>();
    params = new HashMap<>();
    params.put(Constants.REQ_PARAM_RESOURCE, SHORTNAME);
    invocation = mock(ActionInvocation.class);
    when(invocation.invoke()).thenReturn(BaseAction.SUCCESS);
    rebuildContext();
  }

  private void rebuildContext() {
    ActionContext context = ActionContext.of(new HashMap<>())
        .withSession(session)
        .withParameters(HttpParameters.create(params).build())
        .withServletRequest(mock(HttpServletRequest.class));
    when(invocation.getInvocationContext()).thenReturn(context);
  }

  private static User user(String email, User.Role role) {
    User u = new User();
    u.setEmail(email);
    u.setRole(role);
    return u;
  }

  @Test
  public void anonymousUserIsRedirectedToLogin() throws Exception {
    assertEquals(BaseAction.LOGIN, interceptor.intercept(invocation));
    verify(invocation, never()).invoke();
  }


  @Test
  public void managerWithoutRightsOnResourceIsRejected() throws Exception {
    session.put(Constants.SESSION_USER, otherManager);
    assertEquals(BaseAction.NOT_ALLOWED, interceptor.intercept(invocation));
    verify(invocation, never()).invoke();
  }



  @Test
  public void missingResourceParamIsBadRequest() throws Exception {
    params.clear();
    rebuildContext();
    session.put(Constants.SESSION_USER, owner);
    ManagerBaseAction action = resourceAction(true);
    when(invocation.getAction()).thenReturn(action);
    assertEquals(BaseAction.BAD_REQUEST, interceptor.intercept(invocation));
    verify(invocation, never()).invoke();
  }

  @Test
  public void emptyResourceParamIsBadRequest() throws Exception {
    params.put(Constants.REQ_PARAM_RESOURCE, " ");
    rebuildContext();
    session.put(Constants.SESSION_USER, owner);
    ManagerBaseAction action = resourceAction(true);
    when(invocation.getAction()).thenReturn(action);
    assertEquals(BaseAction.BAD_REQUEST, interceptor.intercept(invocation));
    verify(invocation, never()).invoke();
  }

  @Test
  public void missingResourceParamIsAllowedForActionsWithoutResource() throws Exception {
    params.clear();
    rebuildContext();
    session.put(Constants.SESSION_USER, owner);
    ManagerBaseAction action = resourceAction(false);
    when(invocation.getAction()).thenReturn(action);
    assertEquals(BaseAction.SUCCESS, interceptor.intercept(invocation));
  }

  @Test
  public void unknownResourceIsNotFound() throws Exception {
    params.put(Constants.REQ_PARAM_RESOURCE, "unknown");
    rebuildContext();
    session.put(Constants.SESSION_USER, owner);
    ManagerBaseAction action = resourceAction(true);
    when(invocation.getAction()).thenReturn(action);
    assertEquals(BaseAction.NOT_FOUND, interceptor.intercept(invocation));
    verify(invocation, never()).invoke();
  }

  private static ManagerBaseAction resourceAction(boolean requiresResource) {
    ManagerBaseAction action = mock(ManagerBaseAction.class);
    when(action.requiresResource()).thenReturn(requiresResource);
    return action;
  }

  @Test
  public void lockedResourceIsNotCheckedHere() throws Exception {
    session.put(Constants.SESSION_USER, owner);
    // the lock is checked by ResourcePublicationManager, which this interceptor doesn't use (ResourceLockedInterceptor)
    assertEquals(BaseAction.SUCCESS, interceptor.intercept(invocation));
  }
}
