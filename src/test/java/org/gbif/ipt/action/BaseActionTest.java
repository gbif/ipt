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

import org.gbif.ipt.IptBaseTest;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.User;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.struts2.CsrfInterceptor;
import org.gbif.ipt.struts2.SimpleTextProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.apache.struts2.ActionContext;
import org.apache.struts2.inject.Container;
import org.apache.struts2.locale.LocaleProvider;
import org.apache.struts2.locale.LocaleProviderFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BaseActionTest extends IptBaseTest {

  /**
   * Test getLocale on none Struts action context.
   */
  @Test
  public void testGetLocaleOnNoneActionContext() {
    BaseAction action = new BaseAction(mock(SimpleTextProvider.class), mock(AppConfig.class), mock(RegistrationManager.class));
    assertNull(action.getLocale());
  }

  /**
   * Test getLocale on Struts action context.
   */
  @Test
  public void testGetLocaleOnActionContext() {
    Map<String, Object> contextMap = new HashMap<>();

    Container container = mock(Container.class);

    LocaleProviderFactory localeProviderFactory = mock(LocaleProviderFactory.class);
    LocaleProvider localeProvider = mock(LocaleProvider.class);

    when(localeProvider.getLocale()).thenReturn(Locale.JAPANESE);
    when(localeProviderFactory.createLocaleProvider()).thenReturn(localeProvider);
    when(container.getInstance(LocaleProviderFactory.class))
        .thenReturn(localeProviderFactory);

    ActionContext actionContext = ActionContext.of(contextMap)
        .withContainer(container);

    ActionContext.bind(actionContext);

    try {
      BaseAction action = new BaseAction(
          mock(SimpleTextProvider.class),
          mock(AppConfig.class),
          mock(RegistrationManager.class)
      );

      Locale locale = action.getLocale();

      assertEquals(Locale.JAPANESE, locale);
    } finally {
      ActionContext.clear();
    }
  }

  private BaseAction newAction(HttpServletRequest request, Map<String, Object> session) {
    BaseAction action = new BaseAction(mock(SimpleTextProvider.class), mock(AppConfig.class), mock(RegistrationManager.class));
    action.withServletRequest(request);
    action.withSession(session);
    return action;
  }

  /**
   * Login changes the session ID (against session fixation), renews the CSRF token and stores the user.
   */
  @Test
  public void logInChangesSessionIdAndRenewsCsrfToken() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getSession(false)).thenReturn(mock(HttpSession.class));
    Map<String, Object> session = new HashMap<>();
    session.put(Constants.SESSION_CSRF_TOKEN, "old-token");
    User user = new User();

    newAction(request, session).logIn(user);

    verify(request).changeSessionId();
    assertSame(user, session.get(Constants.SESSION_USER));
    assertNotNull(session.get(Constants.SESSION_CSRF_TOKEN));
    assertNotEquals("old-token", session.get(Constants.SESSION_CSRF_TOKEN));
  }

  /**
   * Without an HTTP session there's no ID to change; login still works.
   */
  @Test
  public void logInWithoutHttpSessionDoesNotChangeSessionId() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    Map<String, Object> session = new HashMap<>();
    User user = new User();

    newAction(request, session).logIn(user);

    verify(request, never()).changeSessionId();
    assertSame(user, session.get(Constants.SESSION_USER));
    assertNotNull(session.get(Constants.SESSION_CSRF_TOKEN));
  }

  /**
   * A page rendered by the login request itself (e.g. the last setup step) must carry the new token, not the one the
   * CsrfInterceptor exposed before the action ran.
   */
  @Test
  public void logInExposesTheNewTokenToThePage() {
    ActionContext context = ActionContext.of(new HashMap<>());
    context.put(CsrfInterceptor.TOKEN_CONTEXT_KEY, "old-token");
    ActionContext.bind(context);
    try {
      Map<String, Object> session = new HashMap<>();
      newAction(mock(HttpServletRequest.class), session).logIn(new User());

      assertEquals(session.get(Constants.SESSION_CSRF_TOKEN), context.get(CsrfInterceptor.TOKEN_CONTEXT_KEY));
      assertNotEquals("old-token", context.get(CsrfInterceptor.TOKEN_CONTEXT_KEY));
    } finally {
      ActionContext.clear();
    }
  }
}
