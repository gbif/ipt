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

import java.io.Serial;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.struts2.ActionContext;
import org.apache.struts2.action.Action;
import org.apache.struts2.ActionInvocation;
import org.apache.struts2.interceptor.ValidationAware;
import org.apache.struts2.interceptor.AbstractInterceptor;

import org.apache.struts2.ServletActionContext;

/**
 * Reads the field-level XSS flags set by {@link SanitizeHtmlFilter} on the request (see
 * {@link SanitizeHtmlFilter#XSS_FLAGGED_FIELDS_ATTR}) and, if any are present, adds them as
 * field errors on the current action and short-circuits to the {@code INPUT} result - so the
 * form is redisplayed with the errors shown inline next to the offending field(s), the same
 * way any other validation error would be, instead of the filter itself hard-rejecting the
 * whole request with a generic error page.
 * <p>
 * Must be placed AFTER the "params" interceptor in the stack (so the action's properties are
 * already bound to the sanitized values by the time this runs) and BEFORE "workflow"/the
 * action's own execute() (so the redisplay happens instead of proceeding). The action itself
 * must implement {@link ValidationAware} - true for anything extending Struts' ActionSupport.
 */
public class XssFieldErrorInterceptor extends AbstractInterceptor {

  @Serial
  private static final long serialVersionUID = -975622498498574759L;

  protected SimpleTextProvider textProvider;

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {
    HttpServletRequest request = ServletActionContext.getRequest();

    @SuppressWarnings("unchecked")
    Map<String, String> flaggedFields =
        (Map<String, String>) request.getAttribute(SanitizeHtmlFilter.XSS_FLAGGED_FIELDS_ATTR);

    if (flaggedFields != null && !flaggedFields.isEmpty()) {
      Object action = invocation.getAction();
      if (action instanceof ValidationAware validationAwareAction) {
        for (Map.Entry<String, String> entry : flaggedFields.entrySet()) {
          validationAwareAction.addFieldError(entry.getKey(), entry.getValue());
        }
        return Action.INPUT;
      }
      // action doesn't support field errors (unusual for this codebase) - fall through and
      // let the request proceed with the already-sanitized values rather than blocking it.
    }

    return invocation.invoke();
  }
}
