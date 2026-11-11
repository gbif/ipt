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
import org.gbif.ipt.service.manage.ResourcePublicationManager;

import jakarta.inject.Inject;
import java.io.Serial;

import org.apache.struts2.ActionInvocation;
import org.apache.struts2.interceptor.AbstractInterceptor;

/**
 * Returns the LOCKED result if the requested resource is locked, e.g. while it is being published.
 * Used together with {@link RequireManagerInterceptor}.
 */
public class ResourceLockedInterceptor extends AbstractInterceptor {

  @Serial
  private static final long serialVersionUID = 4425133781146347207L;

  private ResourcePublicationManager resourcePublicationManager;

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {
    String requestedResource = RequireManagerInterceptor.getResourceParam(invocation);
    if (requestedResource != null && resourcePublicationManager.isLocked(requestedResource)) {
      return BaseAction.LOCKED;
    }
    return invocation.invoke();
  }

  @Inject
  public void setResourcePublicationManager(ResourcePublicationManager resourcePublicationManager) {
    this.resourcePublicationManager = resourcePublicationManager;
  }
}
