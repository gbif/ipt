package org.gbif.ipt.struts2;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.User;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

/**
 * An Interceptor that makes sure an admin user is currently logged in and returns a notAllowed otherwise
 */
public class RequireManagerInterceptor extends AbstractInterceptor {
  private static Log log = LogFactory.getLog(RequireAdminInterceptor.class);

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {
    Map session = invocation.getInvocationContext().getSession();
    User user = (User) session.get(Constants.SESSION_USER);
    if (user != null && user.hasManagerRights()) {
      return invocation.invoke();
    }
    return BaseAction.NOT_ALLOWED_MANAGER;
  }

}