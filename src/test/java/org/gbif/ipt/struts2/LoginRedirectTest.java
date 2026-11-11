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

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LoginRedirectTest {

  private static HttpServletRequest request(String method, String path, String query, boolean ajax) {
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getMethod()).thenReturn(method);
    when(req.getServletPath()).thenReturn(path);
    when(req.getQueryString()).thenReturn(query);
    if (ajax) {
      when(req.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");
    }
    return req;
  }

  /**
   * A page opened by GET is remembered, to return to it after login.
   */
  @Test
  public void getIsRemembered() {
    Map<String, Object> session = new HashMap<>();
    assertEquals(BaseAction.LOGIN,
        LoginRedirect.loginRequired(request("GET", "/manage/resource.do", "r=res1", false), session));
    assertEquals("/manage/resource.do?r=res1", session.get(Constants.SESSION_REFERER));
  }

  /**
   * The URL of a POST can't be opened after login (405), so it isn't remembered.
   */
  @Test
  public void postIsNotRemembered() {
    Map<String, Object> session = new HashMap<>();
    assertEquals(BaseAction.LOGIN,
        LoginRedirect.loginRequired(request("POST", "/manage/cancel.do", "r=res1", false), session));
    assertFalse(session.containsKey(Constants.SESSION_REFERER));
  }

  /**
   * AJAX requests, e.g. the polled publication report, get a 401 instead of the login page, and aren't remembered.
   */
  @Test
  public void ajaxIsUnauthorized() {
    Map<String, Object> session = new HashMap<>();
    assertEquals(BaseAction.NOT_ALLOWED,
        LoginRedirect.loginRequired(request("GET", "/manage/report.do", "r=res1", true), session));
    assertFalse(session.containsKey(Constants.SESSION_REFERER));
  }
}
