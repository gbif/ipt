/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
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

/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.Constants;

import java.util.Map;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

/**
 * An Interceptor to set the current resource in a users session.
 */
public class ResourceSessionInterceptor extends AbstractInterceptor {

  private static final long serialVersionUID = -184757845342974320L;

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {
    // reset the current resource in a user's session, only if resource parameter included in request
    if (RequireManagerInterceptor.hasResourceParam(invocation)) {
      Map<String, Object> session = invocation.getInvocationContext().getSession();
      String requestedResource = RequireManagerInterceptor.getResourceParam(invocation);
      if (requestedResource != null) {
        // if the value was not null, the current resource in the session gets replaced
        session.put(Constants.SESSION_RESOURCE, requestedResource);
      } else {
        // if the value was null, the current resource in the session gets removed
        session.remove(Constants.SESSION_RESOURCE);
        return BaseAction.NOT_FOUND;
      }
    }
    return invocation.invoke();
  }
}
