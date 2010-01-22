/*
 * Copyright 2009 GBIF.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gbif.provider.webapp.interceptor;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.gbif.provider.model.Resource;
import org.gbif.provider.service.GenericResourceManager;
import org.gbif.provider.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;

/**
 * TODO: Documentation.
 * 
 */
public class ManagerInterceptor implements Interceptor {
  @Autowired
  @Qualifier("resourceManager")
  protected GenericResourceManager<Resource> resourceManager;

  public void destroy() {
  }

  public void init() {
  }

  public String intercept(ActionInvocation invocation) throws Exception {
    HttpServletRequest request = ServletActionContext.getRequest();

    // admin users are allowed anything
    if (request.isUserInRole(Constants.ADMIN_ROLE)) {
      return invocation.invoke();
    }

    // managers can only manage resources they created
    if (request.isUserInRole(Constants.MANAGER_ROLE)
        && isResourceOwner(request)) {
      return invocation.invoke();
    }

    // not authorised
    HttpServletResponse response = ServletActionContext.getResponse();
    handleNotAuthorized(request, response);
    return null;
  }

  protected void handleNotAuthorized(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    response.sendError(HttpServletResponse.SC_FORBIDDEN);
  }

  private boolean isResourceOwner(HttpServletRequest request) {
    // get requested resource
    Object requestedId = request.getParameter(ResourceInterceptor.PARAMETER_NAME);
    if (requestedId != null && requestedId.getClass().isArray()
        && ((Object[]) requestedId).length == 1) {
      requestedId = ((Object[]) requestedId)[0];
    }
    // cast to integer
    Long resourceId = null;
    if (requestedId != null) {
      try {
        resourceId = Long.valueOf(requestedId.toString());
        // load resource and compare creator to user
        Resource res = resourceManager.get(resourceId);
        Principal user = request.getUserPrincipal();
        if (res.getCreator() != null
            && res.getCreator().getUsername() != user.getName()) {
          return false;
        }
      } catch (NumberFormatException e) {
        // do nothing, aint no proper resource anyway
      }
    }
    return true;
  }

}
