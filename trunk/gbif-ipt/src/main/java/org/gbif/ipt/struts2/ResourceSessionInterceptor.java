package org.gbif.ipt.struts2;

/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

import org.gbif.ipt.config.Constants;

import java.util.Map;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

/**
 * An Interceptor to set the current resource in a users session.
 */
public class ResourceSessionInterceptor extends AbstractInterceptor {

  private static final long serialVersionUID = -184757845342974320L;

  public ResourceSessionInterceptor() {
  }

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {
    String requestedResource = RequireManagerInterceptor.getResourceParam(invocation);
    if (requestedResource != null) {
      Map<String, Object> session = invocation.getInvocationContext().getSession();
      session.put(Constants.SESSION_RESOURCE, requestedResource);
    }
    return invocation.invoke();
  }
}
