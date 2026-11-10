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
package org.gbif.ipt.action;

import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.User;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Login CSRF protection: the login form must send the CSRF token of the session.
 */
public class LoginActionTest {

  private static final String EMAIL = "admin@example.org";
  private static final String PASSWORD = "secret";

  private UserAccountManager userManager;
  private HttpServletRequest request;
  private Map<String, Object> session;
  private LoginAction action;
  private User user;

  @BeforeEach
  public void setUp() {
    userManager = mock(UserAccountManager.class);
    user = new User();
    user.setEmail(EMAIL);
    when(userManager.authenticate(EMAIL, PASSWORD)).thenReturn(user);

    request = mock(HttpServletRequest.class);
    HttpSession httpSession = mock(HttpSession.class);
    when(request.getSession()).thenReturn(httpSession);
    when(request.getSession(false)).thenReturn(httpSession);

    session = new HashMap<>();
    action = new LoginAction(mock(SimpleTextProvider.class), mock(AppConfig.class), mock(RegistrationManager.class),
        userManager);
    action.withServletRequest(request);
    action.withSession(session);
    action.setEmail(EMAIL);
    action.setPassword(PASSWORD);
  }

  @Test
  public void loginFormTokenIsCreatedOnceForTheSession() {
    String token = action.getLoginToken();
    assertNotNull(token);
    assertEquals(token, session.get(Constants.SESSION_CSRF_TOKEN));
    assertEquals(token, action.getLoginToken());
  }

  @Test
  public void loginWithSessionTokenSucceeds() throws Exception {
    String formToken = action.getLoginToken();
    action.setCsrfToken(formToken);

    assertEquals(BaseAction.SUCCESS, action.login());
    assertSame(user, session.get(Constants.SESSION_USER));
    verify(request).changeSessionId();
    // login replaces the token of the login form
    assertNotEquals(formToken, session.get(Constants.SESSION_CSRF_TOKEN));
  }

  @Test
  public void loginWithWrongTokenIsRejected() throws Exception {
    action.getLoginToken();
    action.setCsrfToken("forged");

    assertEquals(BaseAction.INPUT, action.login());
    assertNull(session.get(Constants.SESSION_USER));
    assertFalse(action.getActionErrors().isEmpty());
    verify(userManager, never()).authenticate(anyString(), anyString());
  }

  @Test
  public void loginWithoutTokenIsRejected() throws Exception {
    action.getLoginToken();

    assertEquals(BaseAction.INPUT, action.login());
    assertNull(session.get(Constants.SESSION_USER));
    verify(userManager, never()).authenticate(anyString(), anyString());
  }

  /**
   * E.g. the session expired while the login form was open, or a forged form posted from another site.
   */
  @Test
  public void loginWithoutSessionTokenIsRejected() throws Exception {
    action.setCsrfToken("some-token");

    assertEquals(BaseAction.INPUT, action.login());
    assertNull(session.get(Constants.SESSION_USER));
    verify(userManager, never()).authenticate(anyString(), anyString());
  }

  /**
   * A request without a password is a failed login, not an error.
   */
  @Test
  public void loginWithoutPasswordFails() throws Exception {
    action.setCsrfToken(action.getLoginToken());
    action.setPassword(null);

    assertEquals(BaseAction.INPUT, action.login());
    assertNull(session.get(Constants.SESSION_USER));
    assertFalse(action.getActionErrors().isEmpty());
    verify(userManager).authenticate(EMAIL, "");
  }
}
