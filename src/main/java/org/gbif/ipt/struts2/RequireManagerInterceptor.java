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

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serial;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.dispatcher.Parameter;
import org.apache.struts2.ActionContext;
import org.apache.struts2.ActionInvocation;
import org.apache.struts2.interceptor.AbstractInterceptor;

import static org.apache.struts2.StrutsStatics.HTTP_REQUEST;

/**
 * An Interceptor that makes sure a user with manager rights (=admin or manager role) is currently logged in and
 * returns a notAllowed otherwise. Locked resources are checked by {@link ResourceLockedInterceptor}.
 * If a resource is requested, it also checks that the logged-in user has permissions to manage that specific resource.
 * Actions working on a single resource (see {@link ManagerBaseAction#requiresResource()}) get a 400 without the
 * resource parameter, and a 404 if the resource doesn't exist.
 */
public class RequireManagerInterceptor extends AbstractInterceptor {

  @Serial
  private static final long serialVersionUID = -7688584369470756187L;

  private static final Logger LOG = LogManager.getLogger(RequireManagerInterceptor.class);

  private ResourceManager resourceManager;


  protected static String getResourceParam(ActionInvocation invocation) {
    String requestedResource = null;
    Parameter requestedResourceName = invocation.getInvocationContext().getParameters().get(Constants.REQ_PARAM_RESOURCE);
    if (requestedResourceName.isDefined()) {
      requestedResource = StringUtils.trimToNull(requestedResourceName.getValue());
    }
    return requestedResource;
  }

  /**
   * Checks whether the resource parameter is used in the request.
   *
   * @param invocation ActionInvocation
   * @return true if the resource parameter was used in the request, false otherwise.
   */
  protected static boolean hasResourceParam(ActionInvocation invocation) {
    return invocation.getInvocationContext().getParameters().containsKey(Constants.REQ_PARAM_RESOURCE);
  }

  public static boolean isAuthorized(User user, Resource resource) {
    if (user.hasAdminRights()) {
      return true;
    }
    if (resource != null && user.hasManagerRights()) {
      // even resource creators need still to be managers
      if (resource.getCreator().equals(user)) {
        return true;
      }
      for (User m : resource.getManagers()) {
        if (user.equals(m)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {
    Map<String, Object> session = invocation.getInvocationContext().getSession();
    User user = (User) session.get(Constants.SESSION_USER);

    // user is not logged in, redirect to login page
    // remember referer and redirect there after successful authentication
    if (user == null) {
      LOG.debug("User is not logged in, redirecting to login page");
      ActionContext context = invocation.getInvocationContext();
      HttpServletRequest request = (HttpServletRequest) context.get(HTTP_REQUEST);

      String queryString = request.getQueryString();
      String referer = request.getServletPath();
      // check if there is a query string, if so append it
      if (queryString != null) {
        referer = referer + '?' + queryString;
      }

      // put referer into session
      LOG.debug("Put referer into session: {}", referer);
      session.put(Constants.SESSION_REFERER, referer);

      return BaseAction.LOGIN;
    }

    // user is logged in, check if user has manager rights
    if (user.hasManagerRights()) {
      // now also check if we have rights for a specific resource requested
      // lets see if we are about to manage a new resource
      String requestedResource = getResourceParam(invocation);
      if (requestedResource == null
          && invocation.getAction() instanceof ManagerBaseAction action && action.requiresResource()) {
        LOG.debug("Resource parameter missing for {}", action.getClass().getSimpleName());
        return BaseAction.BAD_REQUEST;
      }
      if (requestedResource != null) {
        // does resource exist at all?
        Resource resource = resourceManager.get(requestedResource);
        if (resource == null) {
          return BaseAction.NOT_FOUND;
        }
        // authorized?
        if (!isAuthorized(user, resource)) {
          return BaseAction.NOT_ALLOWED;
        }
      } else {
        // no resource parameter: check the resource stored in the session instead
        dropUnauthorizedSessionResource(session, user);
      }
      return invocation.invoke();
    }

    return BaseAction.NOT_ALLOWED_MANAGER;
  }

  private void dropUnauthorizedSessionResource(Map<String, Object> session, User user) {
    Object sessionResource = session.get(Constants.SESSION_RESOURCE);
    if (sessionResource instanceof String shortname) {
      Resource resource = resourceManager.get(shortname);
      if (resource == null || !isAuthorized(user, resource)) {
        LOG.debug("Removing resource {} from session", shortname);
        session.remove(Constants.SESSION_RESOURCE);
      }
    }
  }

  @Inject
  public void setResourceManager(ResourceManager resourceManager) {
    this.resourceManager = resourceManager;
  }
}
