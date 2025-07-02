/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.ipt.struts2;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.config.SetupAction;
import org.gbif.ipt.service.admin.ConfigManager;

import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.dispatcher.Parameter;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

import static org.gbif.ipt.config.Constants.CANCEL;

/**
 * An Interceptor that checks if the basic IPT setup is complete and redirects to the respective setup page otherwise.
 * It also looks for a request parameter "cancel" and returns a result with the same name in case there is any non
 * empty content existing.
 * This helps setting up forms to avoid the execution of the params interceptor early on.
 */
public class SetupAndCancelInterceptor extends AbstractInterceptor {

  private static final long serialVersionUID = 1988717332926909383L;

  private static final Logger LOG = LogManager.getLogger(SetupAndCancelInterceptor.class);

  public static final String SETUP_RESULTNAME = "setupIncomplete";

  private ConfigManager configManager;
  private ConfigWarnings warnings;

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {
    if (!configManager.setupComplete()) {
      Object action = invocation.getAction();
      if (action instanceof SetupAction) {
        return invocation.invoke();
      } else {
        LOG.info("Setup incomplete - redirect to setup");
        return SETUP_RESULTNAME;
      }
    }

    // check if any non empty content exists in cancel request parameter
    Parameter cancel = invocation.getInvocationContext().getParameters().get(CANCEL);
    if (cancel.isDefined()) {
      Object action = invocation.getAction();
      if (action instanceof BaseAction) {
        BaseAction ba = (BaseAction) action;
        ba.setCancel("true");
      } else {
        return CANCEL;
      }
    }

    if (warnings.hasStartupErrors()) {
      Object action = invocation.getAction();
      if (action instanceof BaseAction) {
        BaseAction ba = (BaseAction) action;
        // ensure a 'unique' list of startup warnings gets displayed using i18n keys if possible

        // keep track of unique set of ActionWarnings
        Set<String> existing = new HashSet<>();
        for (String warning : ba.getActionWarnings()) {
          // find out if the ActionWarning has been added to the list of action warnings yet
          if (!existing.contains(StringUtils.trimToEmpty(warning))) {
            existing.add(warning);
          }
        }
        // keep track of unique set of warnings coming from managers
        for (String msg : warnings.getStartupErrors()) {
          // find out if the warning has been added to the list of action warnings yet
          if (!existing.contains(StringUtils.trimToEmpty(msg))) {
            existing.add(msg);
          }
        }
        // clear, then repopulate ActionWarnings from the unique Set combining ActionWarnings + warnings from managers
        ba.getActionWarnings().clear();
        ba.addActionWarning(ba.getText("admin.startup.warnings"));
        ba.getActionWarnings().addAll(existing);
      }
    }

    return invocation.invoke();
  }

  @Inject
  public void setConfigManager(ConfigManager configManager) {
    this.configManager = configManager;
  }

  @Inject
  public void setWarnings(ConfigWarnings warnings) {
    this.warnings = warnings;
  }
}
