package org.gbif.ipt.struts2;

import com.opensymphony.xwork2.ActionContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.dispatcher.Parameter;
import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.service.manage.ResourceManager;

import java.util.Map;

import com.google.inject.Inject;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

import static org.apache.struts2.StrutsStatics.HTTP_REQUEST;

/**
 * An Interceptor that makes sure a user with manager rights (=admin or manager role) is currently logged in and
 * returns a notAllowed otherwise. It also checks if the resource is currently locked and returns locked in that case
 * regardless of user rights.
 * If a resource is requested it also checks that the logged in user has permissions to manage that specific resource.
 */
public class RequireManagerInterceptor extends AbstractInterceptor {

  // logging
  private static final Logger LOG = LogManager.getLogger(RequireManagerInterceptor.class);

  private static final long serialVersionUID = -7688584369470756187L;

  @Inject
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
   *
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
      // check if there is query string, if so append it
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
          // locked?
          if (resourceManager.isLocked(requestedResource)) {
            return BaseAction.LOCKED;
          }
      }
      return invocation.invoke();
    }

    return BaseAction.NOT_ALLOWED_MANAGER;
  }
}
