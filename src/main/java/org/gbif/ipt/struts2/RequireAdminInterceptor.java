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
import org.gbif.ipt.model.User;

import java.io.Serial;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.struts2.ActionInvocation;
import org.apache.struts2.interceptor.AbstractInterceptor;

import static org.apache.struts2.StrutsStatics.HTTP_REQUEST;

/**
 * An Interceptor that makes sure an admin user is currently logged in and returns a notAllowed otherwise.
 */
public class RequireAdminInterceptor extends AbstractInterceptor {

  @Serial
  private static final long serialVersionUID = -975622498498574759L;


  @Override
  public String intercept(ActionInvocation invocation) throws Exception {
    Map<String, Object> session = invocation.getInvocationContext().getSession();
    User user = (User) session.get(Constants.SESSION_USER);

    // user is not logged in: redirect to the login page, or 401 for AJAX requests
    if (user == null) {
      HttpServletRequest request = (HttpServletRequest) invocation.getInvocationContext().get(HTTP_REQUEST);
      return LoginRedirect.loginRequired(request, session);
    }

    // user is logged in, check if user has admin rights
    if (user.hasAdminRights()) {
      return invocation.invoke();
    }
    return BaseAction.NOT_ALLOWED;
  }

}
