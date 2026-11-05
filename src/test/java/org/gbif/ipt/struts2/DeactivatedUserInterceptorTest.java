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

import org.gbif.ipt.IptBaseTest;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.User;
import org.gbif.ipt.service.admin.UserAccountManager;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.apache.struts2.ActionContext;
import org.apache.struts2.ActionInvocation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DeactivatedUserInterceptorTest extends IptBaseTest {

  private final DeactivatedUserInterceptor interceptor = new DeactivatedUserInterceptor();

  @Mock
  private UserAccountManager userManagerMock;

  @Mock
  private ActionInvocation invocationMock;

  private Map<String, Object> session;
  private User sessionUser;

  @BeforeEach
  void setUp() throws Exception {
    interceptor.setUserManager(userManagerMock);

    sessionUser = new User();
    sessionUser.setEmail("manager@ipt.gbif.org");

    session = new HashMap<>();
    session.put(Constants.SESSION_USER, sessionUser);
    ActionContext actionContext = ActionContext.of(new HashMap<>()).withSession(session);
    when(invocationMock.getInvocationContext()).thenReturn(actionContext);
    when(invocationMock.invoke()).thenReturn("success");
  }

  @Test
  public void testActiveUserStaysLoggedIn() throws Exception {
    User stored = new User();
    stored.setEmail(sessionUser.getEmail());
    when(userManagerMock.get(sessionUser.getEmail())).thenReturn(stored);

    assertEquals("success", interceptor.intercept(invocationMock));
    assertEquals(sessionUser, session.get(Constants.SESSION_USER));
  }

  @Test
  public void testDeactivatedUserIsLoggedOut() throws Exception {
    // the session holds a different instance than the one deactivated in the manager
    User stored = new User();
    stored.setEmail(sessionUser.getEmail());
    stored.setDeactivated(true);
    when(userManagerMock.get(sessionUser.getEmail())).thenReturn(stored);

    assertEquals("success", interceptor.intercept(invocationMock));
    assertNull(session.get(Constants.SESSION_USER));
  }
}
