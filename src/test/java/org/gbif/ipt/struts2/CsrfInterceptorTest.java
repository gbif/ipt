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
import org.gbif.ipt.action.LoginAction;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import org.apache.struts2.ActionContext;
import org.apache.struts2.ActionInvocation;
import org.apache.struts2.ActionProxy;
import org.apache.struts2.dispatcher.HttpParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CsrfInterceptorTest {

  /**
   * Stands in for an action with a state-changing method and a read-only one.
   */
  public static class TestAction {
    @RequirePost
    public String delete() {
      return BaseAction.SUCCESS;
    }

    public String list() {
      return BaseAction.SUCCESS;
    }
  }

  /**
   * The annotation is inherited by overriding methods.
   */
  public static class SubTestAction extends TestAction {
    @Override
    public String delete() {
      return BaseAction.SUCCESS;
    }
  }

  private CsrfInterceptor interceptor;
  private ActionInvocation invocation;
  private ActionProxy proxy;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private Map<String, Object> session;
  private Map<String, Object> params;
  private ActionContext context;
  private Object action;

  @BeforeEach
  void setUp() throws Exception {
    interceptor = new CsrfInterceptor();
    session = new HashMap<>();
    params = new HashMap<>();
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
    proxy = mock(ActionProxy.class);
    invocation = mock(ActionInvocation.class);
    when(invocation.invoke()).thenReturn(BaseAction.SUCCESS);
    when(invocation.getProxy()).thenReturn(proxy);
    when(proxy.getNamespace()).thenReturn("/manage");
    when(proxy.getActionName()).thenReturn("test");
    action = new TestAction();
    request("GET", "list");
  }

  private void request(String httpMethod, String actionMethod) {
    when(request.getMethod()).thenReturn(httpMethod);
    when(proxy.getMethod()).thenReturn(actionMethod);
    when(invocation.getAction()).thenReturn(action);
    context = ActionContext.of(new HashMap<>())
        .withSession(session)
        .withParameters(HttpParameters.create(params).build())
        .withServletRequest(request)
        .withServletResponse(response);
    when(invocation.getInvocationContext()).thenReturn(context);
  }

  private String login() {
    User user = new User();
    user.setEmail("manager@example.org");
    user.setRole(User.Role.Manager);
    session.put(Constants.SESSION_USER, user);
    return CsrfInterceptor.getOrCreateToken(session);
  }

  @Test
  public void anonymousRequestsAreNotChecked() throws Exception {
    request("POST", "list");
    assertEquals(BaseAction.SUCCESS, interceptor.intercept(invocation));
    assertNull(session.get(Constants.SESSION_CSRF_TOKEN));
    assertNull(context.get(CsrfInterceptor.TOKEN_CONTEXT_KEY));
  }

  @Test
  public void loggedInUserGetsTokenAndFramingHeaders() throws Exception {
    session.put(Constants.SESSION_USER, new User());
    assertEquals(BaseAction.SUCCESS, interceptor.intercept(invocation));

    String token = (String) session.get(Constants.SESSION_CSRF_TOKEN);
    assertNotNull(token);
    assertEquals(token, context.get(CsrfInterceptor.TOKEN_CONTEXT_KEY));
    verify(response).setHeader("X-Frame-Options", "SAMEORIGIN");
    verify(response).setHeader("Content-Security-Policy", "frame-ancestors 'self'");

    // the token stays the same for the session
    assertEquals(token, CsrfInterceptor.getOrCreateToken(session));
  }

  @ParameterizedTest
  @ValueSource(strings = {"POST", "PUT", "DELETE", "PATCH"})
  public void postWithoutTokenIsRejected(String httpMethod) throws Exception {
    login();
    request(httpMethod, "list");
    assertEquals(CsrfInterceptor.INVALID_TOKEN, interceptor.intercept(invocation));
    verify(invocation, never()).invoke();
  }

  @Test
  public void postWithWrongTokenIsRejected() throws Exception {
    login();
    params.put(CsrfInterceptor.TOKEN_PARAM, "forged");
    request("POST", "list");
    assertEquals(CsrfInterceptor.INVALID_TOKEN, interceptor.intercept(invocation));
    verify(invocation, never()).invoke();
  }

  @Test
  public void postWithTokenParameterIsAccepted() throws Exception {
    params.put(CsrfInterceptor.TOKEN_PARAM, login());
    request("POST", "list");
    assertEquals(BaseAction.SUCCESS, interceptor.intercept(invocation));
  }

  @Test
  public void postWithTokenHeaderIsAccepted() throws Exception {
    String token = login();
    when(request.getHeader(CsrfInterceptor.TOKEN_HEADER)).thenReturn(token);
    request("POST", "list");
    assertEquals(BaseAction.SUCCESS, interceptor.intercept(invocation));
  }

  @Test
  public void staleLoginFormIsNotRejected() throws Exception {
    login();
    action = mock(LoginAction.class);
    request("POST", "login");
    assertEquals(BaseAction.SUCCESS, interceptor.intercept(invocation));
  }

  @Test
  public void logoutWithoutTokenIsRejected() throws Exception {
    login();
    action = mock(LoginAction.class);
    request("POST", "logout");
    assertEquals(CsrfInterceptor.INVALID_TOKEN, interceptor.intercept(invocation));
  }

  @Test
  public void logoutWithTokenIsAccepted() throws Exception {
    params.put(CsrfInterceptor.TOKEN_PARAM, login());
    action = mock(LoginAction.class);
    request("POST", "logout");
    assertEquals(BaseAction.SUCCESS, interceptor.intercept(invocation));
  }

  @Test
  public void logoutByGetIsRejected() throws Exception {
    login();
    action = mock(LoginAction.class);
    request("GET", "logout");
    assertEquals(CsrfInterceptor.METHOD_NOT_ALLOWED, interceptor.intercept(invocation));
  }

  @ParameterizedTest
  @ValueSource(strings = {"GET", "HEAD"})
  public void requirePostMethodRejectsOtherMethods(String httpMethod) throws Exception {
    login();
    request(httpMethod, "delete");
    assertEquals(CsrfInterceptor.METHOD_NOT_ALLOWED, interceptor.intercept(invocation));
    verify(invocation, never()).invoke();
    verify(response).setHeader("Allow", "POST");
  }

  @Test
  public void requirePostIsInherited() throws Exception {
    login();
    action = new SubTestAction();
    request("GET", "delete");
    assertEquals(CsrfInterceptor.METHOD_NOT_ALLOWED, interceptor.intercept(invocation));
  }

  @Test
  public void requirePostMethodRejectsAnonymousGet() throws Exception {
    request("GET", "delete");
    assertEquals(CsrfInterceptor.METHOD_NOT_ALLOWED, interceptor.intercept(invocation));
  }

  @Test
  public void requirePostMethodAcceptsPostWithToken() throws Exception {
    params.put(CsrfInterceptor.TOKEN_PARAM, login());
    request("POST", "delete");
    assertEquals(BaseAction.SUCCESS, interceptor.intercept(invocation));
  }

  @Test
  public void requirePostMethodRejectsPostWithoutToken() throws Exception {
    login();
    request("POST", "delete");
    assertEquals(CsrfInterceptor.INVALID_TOKEN, interceptor.intercept(invocation));
  }

  @Test
  public void resetTokenReplacesTheSessionToken() throws Exception {
    String oldToken = login();
    String newToken = CsrfInterceptor.resetToken(session);

    assertNotEquals(oldToken, newToken);
    assertEquals(newToken, session.get(Constants.SESSION_CSRF_TOKEN));
    assertEquals(newToken, CsrfInterceptor.getOrCreateToken(session));

    // a form rendered before the new login is rejected
    params.put(CsrfInterceptor.TOKEN_PARAM, oldToken);
    request("POST", "delete");
    assertEquals(CsrfInterceptor.INVALID_TOKEN, interceptor.intercept(invocation));
  }
}
