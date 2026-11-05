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

import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.User;
import org.gbif.ipt.service.admin.UserAccountManager;

import java.io.Serial;
import java.util.Map;

import jakarta.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.ActionInvocation;
import org.apache.struts2.interceptor.AbstractInterceptor;

/**
 * An Interceptor that logs out a user whose account has been deactivated after they logged in.
 * The session can hold an outdated copy of the user (e.g. once the account has been edited), so the current state
 * is looked up in the UserAccountManager.
 * The request then continues as anonymous, leaving it to the other interceptors to require a login where needed.
 */
public class DeactivatedUserInterceptor extends AbstractInterceptor {

  @Serial
  private static final long serialVersionUID = 4327925460357104539L;

  private static final Logger LOG = LogManager.getLogger(DeactivatedUserInterceptor.class);

  private UserAccountManager userManager;

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {
    Map<String, Object> session = invocation.getInvocationContext().getSession();
    User sessionUser = session == null ? null : (User) session.get(Constants.SESSION_USER);

    if (sessionUser != null) {
      User user = userManager.get(sessionUser.getEmail());
      if (user != null && user.isDeactivated()) {
        LOG.info("User {} has been deactivated, logging out", sessionUser.getEmail());
        session.clear();
      }
    }

    return invocation.invoke();
  }

  @Inject
  public void setUserManager(UserAccountManager userManager) {
    this.userManager = userManager;
  }
}
