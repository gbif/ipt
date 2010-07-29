package org.gbif.ipt.struts2;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.action.manage.ResourceManagerSession;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.service.manage.ResourceManager;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

import org.apache.log4j.Logger;

import java.util.Map;

/**
 * An Interceptor that authorizes allowed managers for a resource and loads the requested resource into the users
 * session. It looks for a resource request parameter to switch the current session to this new resource - thereby
 * dropping any previously kept resource in the session.
 * 
 * Authorization is based on the following rules:
 * - any admin is granted access
 * - the resource creator is granted access
 * - any manager listed as additional managers in the resource is granted access
 * - anyone else is rejected!
 */
@Singleton
public class ResourceInterceptor extends AbstractInterceptor {
  private static Logger log = Logger.getLogger(ResourceInterceptor.class);
  @Inject
  private ResourceManager resourceManager;

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {
    // lets see if we are about to manage a new resource
    Object requested = invocation.getInvocationContext().getParameters().get(Constants.REQ_PARAM_RESOURCE);
    if (requested != null && requested.getClass().isArray() && ((Object[]) requested).length == 1) {
      String requestedResource = ((Object[]) requested)[0].toString();
      // already loaded?
      Map session = invocation.getInvocationContext().getSession();
      ResourceManagerSession rms = (ResourceManagerSession) session.get(Constants.SESSION_RESOURCE);
      if (rms != null && rms.getCurrentResourceShortname().equalsIgnoreCase(requestedResource)) {
        // yes - exists and is the same. Ignore request param
      } else {
        // nope - different or first one
        // does resource exist at all?
        Resource resource = resourceManager.get(requestedResource);
        if (resource == null) {
          return BaseAction.NOT_FOUND;
        }
        // authorized?
        User user = (User) session.get(Constants.SESSION_USER);
        if (user == null || !isAuthorized(user, resource, invocation)) {
          return BaseAction.NOT_ALLOWED;
        }
        switchResource(user, resource, session);
      }
    }
    return invocation.invoke();
  }

  private boolean isAuthorized(User user, Resource resource, ActionInvocation invocation) {
    if (user.hasAdminRights()) {
      return true;
    }
    if (resource.getCreator().equals(user)) {
      return true;
    }
    if (user.hasManagerRights()) {
      for (String allowedEmail : resource.getManagers()) {
        if (user.getEmail().equalsIgnoreCase(allowedEmail)) {
          return true;
        }
      }
    }
    return false;
  }

  private void switchResource(User user, Resource resource, Map session) {
    ResourceManagerSession rms = (ResourceManagerSession) session.get(Constants.SESSION_RESOURCE);
    if (rms == null) {
      log.warn("ResourceManagerSession null in resource interceptor");
    } else {
      rms.load(user, resource);
    }
  }
}