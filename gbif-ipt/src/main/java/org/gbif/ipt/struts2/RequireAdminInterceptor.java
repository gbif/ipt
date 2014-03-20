package org.gbif.ipt.struts2;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.User;

import java.util.Map;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

/**
 * An Interceptor that makes sure an admin user is currently logged in and returns a notAllowed otherwise.
 */
public class RequireAdminInterceptor extends AbstractInterceptor {

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {
    Map<String, Object> session = invocation.getInvocationContext().getSession();
    User user = (User) session.get(Constants.SESSION_USER);
    if (user != null && user.hasAdminRights()) {
      return invocation.invoke();
    }
    return BaseAction.NOT_ALLOWED;
  }

}
