package org.gbif.ipt.struts2;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.config.SetupAction;
import org.gbif.ipt.service.admin.ConfigManager;

import com.google.inject.Inject;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

import org.apache.commons.lang.xwork.StringUtils;
import org.apache.log4j.Logger;

/**
 * An Interceptor that checks if the basic IPT setup is complete and redirects to the respective setup page otherwise.
 * It also looks for a request parameter "cancel" and returns a result with the same name in case there is any non empty
 * content existing.
 * This helps setting up forms to avoid the execution of the params interceptor early on.
 */
public class SetupAndCancelInterceptor extends AbstractInterceptor {
  public final String SETUP_RESULTNAME = "setupIncomplete";
  public final String CANCEL_RESULTNAME = "cancel";
  private static Logger log = Logger.getLogger(SetupAndCancelInterceptor.class);
  @Inject
  private ConfigManager configManager;
  @Inject
  private ConfigWarnings warnings;

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {
    if (!configManager.setupComplete()) {
      Object action = invocation.getAction();
      if (action instanceof SetupAction) {
        return invocation.invoke();
      } else {
        log.info("Setup incomplete - redirect to setup");
        return SETUP_RESULTNAME;
      }
    }

    // check if any non empty content exists in cancel request parameter
    Object cancel = invocation.getInvocationContext().getParameters().get(CANCEL_RESULTNAME);
    if (cancel != null && cancel.getClass().isArray() && ((Object[]) cancel).length > 0
        && StringUtils.trimToNull(((Object[]) cancel)[0].toString()) != null) {
      return CANCEL_RESULTNAME;
    }

    if (warnings.hasStartupErrors()) {
      Object action = invocation.getAction();
      if (action instanceof BaseAction) {
        BaseAction ba = ((BaseAction) action);
        ba.addActionWarning("IPT startup warnings, please see logs!");
//        for (String msg : warnings.getStartupErrors()) {
//          ba.addActionMessage(msg);
//        }
      }
    }

    return invocation.invoke();
  }
}