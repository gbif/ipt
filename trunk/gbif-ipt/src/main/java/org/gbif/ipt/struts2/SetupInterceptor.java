package org.gbif.ipt.struts2;

import org.gbif.ipt.config.SetupAction;
import org.gbif.ipt.service.admin.ConfigManager;

import com.google.inject.Inject;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An Interceptor that checks if the basic IPT setup is complete and redirects to the respective setup page otherwise
 */
public class SetupInterceptor extends AbstractInterceptor {
  public final String SETUP_RESULTNAME = "setupIncomplete";
  private static Log log = LogFactory.getLog(SetupInterceptor.class);
  @Inject
  private ConfigManager configManager;

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
    return invocation.invoke();
  }

}