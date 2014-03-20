package org.gbif.ipt.struts2;

import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.User;
import org.gbif.ipt.service.admin.UserAccountManager;

import java.util.Map;

import com.google.inject.Inject;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import org.apache.log4j.Logger;

/**
 * An Interceptor that makes sure an admin user is currently logged in and returns a notAllowed otherwise.
 */
public class AutoLoginAdminInterceptor extends AbstractInterceptor {

  private static final Logger LOG = Logger.getLogger(RequireAdminInterceptor.class);

  @Inject
  private UserAccountManager userManager;

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {
    Map<String, Object> session = invocation.getInvocationContext().getSession();
    User user = (User) session.get(Constants.SESSION_USER);
    if (user == null || !user.hasManagerRights()) {
      user = userManager.authenticate("admin", "carla");
      if (user != null) {
        session.put(Constants.SESSION_USER, user);
        LOG.debug("Auto logged in admin");
      } else {
        LOG.debug("Failed to auto-login the admin");
      }
    }
    return invocation.invoke();
  }
}
