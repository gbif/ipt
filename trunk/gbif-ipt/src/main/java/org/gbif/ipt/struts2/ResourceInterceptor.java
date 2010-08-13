package org.gbif.ipt.struts2;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.action.manage.MissingResourceSession;
import org.gbif.ipt.action.manage.ResourceManagerSession;
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
public class ResourceInterceptor extends AbstractInterceptor {
  private static Logger log = Logger.getLogger(ResourceInterceptor.class);
  @Inject
  private ResourceManager resourceManager;
  @Inject
  private ResourceManagerSession rms;

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {
    // lets see if we are about to manage a new resource
    Object requested = invocation.getInvocationContext().getParameters().get(Constants.REQ_PARAM_RESOURCE);
    if (requested != null && requested.getClass().isArray() && ((Object[]) requested).length == 1) {
      String requestedResource = StringUtils.trimToNull(((Object[]) requested)[0].toString());
      if (requestedResource != null) {
        // already loaded?
        String currResource;
        try {
          currResource = rms.getResource().getShortname();
        } catch (MissingResourceSession e) {
          currResource = "";
        }
        if (currResource.equalsIgnoreCase(requestedResource)) {
          // yes - exists and is the same. Ignore request param
          log.debug("Resource " + requestedResource + " already in manager session " + rms.hashCode());
        } else {
          // nope - different or first one
          // does resource exist at all?
          Resource resource = resourceManager.get(requestedResource);
          if (resource == null) {
            return BaseAction.NOT_FOUND;
          }
          // authorized?
          Map session = invocation.getInvocationContext().getSession();
          User user = (User) session.get(Constants.SESSION_USER);
          if (user == null || !isAuthorized(user, resource, invocation)) {
            return BaseAction.NOT_ALLOWED;
          }
          rms.load(user, resource);
        }
      }
    }
    return invocation.invoke();
  }

  private boolean isAuthorized(User user, Resource resource, ActionInvocation invocation) {
    if (user.hasAdminRights()) {
      return true;
    }
    if (resource != null && resource.getCreator() != null && resource.getCreator().equals(user)) {
      return true;
    }
    if (user.hasManagerRights()) {
      for (User m : resource.getManagers()) {
        if (user.equals(m)) {
          return true;
        }
      }
    }
    return false;
  }

}