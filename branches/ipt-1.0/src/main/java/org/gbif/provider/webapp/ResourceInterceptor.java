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
package org.gbif.provider.webapp;

import org.gbif.provider.service.CacheManager;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO: Documentation.
 * 
 */
public class ResourceInterceptor extends AbstractInterceptor {
  protected static final Log log = LogFactory.getLog(ResourceInterceptor.class);
  public static final String RESOURCE_BUSY = "resource-busy";
  public static final String PARAMETER_NAME = "resourceId";
  @Autowired
  private CacheManager cacheManager;

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {
    // get requested resource
    Object requestedId = invocation.getInvocationContext().getParameters().get(
        PARAMETER_NAME);
    if (requestedId != null && requestedId.getClass().isArray()
        && ((Object[]) requestedId).length == 1) {
      requestedId = ((Object[]) requestedId)[0];
    }
    // cast to integer
    Long resourceId = null;
    if (requestedId != null) {
      try {
        resourceId = Long.valueOf(requestedId.toString());
        if (cacheManager.isBusy(resourceId)) {
          log.debug(String.format("Resource %s is busy. Issue resultname '%s'",
              resourceId, RESOURCE_BUSY));
          return RESOURCE_BUSY;
        }
      } catch (NumberFormatException e) {
        // do nothing, aint no proper resource anyway
      }
    }

    // continue with the rest of the interceptor stack not setting the result
    // name here
    return invocation.invoke();
  }

}
