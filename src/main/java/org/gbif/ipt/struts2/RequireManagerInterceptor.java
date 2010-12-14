package org.gbif.ipt.struts2;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.service.manage.ResourceManager;

import com.google.inject.Inject;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

import org.apache.commons.lang.xwork.StringUtils;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * An Interceptor that makes sure a user with manager rights (=admin or manager role) is currently logged in and returns
 * a notAllowed otherwise. It also checks if the resource is currently locked and returns locked in that case regardless
 * of user rights.
 * If a resource is requested it also checks that the logged in user has permissions to manage that specific resource.
 */
public class RequireManagerInterceptor extends AbstractInterceptor {
  private static Logger log = Logger.getLogger(RequireAdminInterceptor.class);
  @Inject
  private ResourceManager resourceManager;

  protected static String getResourceParam(ActionInvocation invocation) {
    String requestedResource = null;
    Object requestedResourceName = invocation.getInvocationContext().getParameters().get(Constants.REQ_PARAM_RESOURCE);
    if (requestedResourceName != null && requestedResourceName.getClass().isArray()
        && ((Object[]) requestedResourceName).length == 1) {
      requestedResource = StringUtils.trimToNull(((Object[]) requestedResourceName)[0].toString());
    }
    return requestedResource;
  }

  public static boolean isAuthorized(User user, Resource resource) {
    if (user.hasAdminRights()) {
      return true;
    }
    if (resource != null && user.hasManagerRights()) {
      // even resource creators need still to be managers
      if (resource.getCreator() != null && resource.getCreator().equals(user)) {
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
    Map session = invocation.getInvocationContext().getSession();
    User user = (User) session.get(Constants.SESSION_USER);
    if (user != null && user.hasManagerRights()) {
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
        if (user == null || !isAuthorized(user, resource)) {
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