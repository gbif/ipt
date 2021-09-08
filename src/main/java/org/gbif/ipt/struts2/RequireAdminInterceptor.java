package org.gbif.ipt.struts2;

import com.opensymphony.xwork2.ActionContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.User;

import java.util.Map;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

import javax.servlet.http.HttpServletRequest;

import static org.apache.struts2.StrutsStatics.HTTP_REQUEST;

/**
 * An Interceptor that makes sure an admin user is currently logged in and returns a notAllowed otherwise.
 */
public class RequireAdminInterceptor extends AbstractInterceptor {

  // logging
  private static final Logger LOG = LogManager.getLogger(RequireAdminInterceptor.class);

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

    // user is logged in, check if user has admin rights
    if (user.hasAdminRights()) {
      return invocation.invoke();
    }
    return BaseAction.NOT_ALLOWED;
  }

}
