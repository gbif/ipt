/***************************************************************************
* Copyright (C) 2008 Global Biodiversity Information Facility Secretariat.
* All Rights Reserved.
*
* The contents of this file are subject to the Mozilla Public
* License Version 1.1 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of
* the License at http://www.mozilla.org/MPL/
*
* Software distributed under the License is distributed on an "AS
* IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
* implied. See the License for the specific language governing
* rights and limitations under the License.

***************************************************************************/

package org.gbif.provider.webapp;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.provider.service.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.opensymphony.xwork2.interceptor.Interceptor;

public class ResourceInterceptor extends AbstractInterceptor{
	protected static final Log log = LogFactory.getLog(ResourceInterceptor.class);
	public static final String RESOURCE_BUSY = "resource-busy";
	public static final String PARAMETER_NAME = "resource_id";
	@Autowired
	private CacheManager cacheManager;

	public String intercept(ActionInvocation invocation) throws Exception {
		//get requested resource
		Object requested_id = invocation.getInvocationContext().getParameters().get(PARAMETER_NAME);
		if (requested_id != null && requested_id.getClass().isArray() && ((Object[]) requested_id).length == 1) {
			requested_id = ((Object[]) requested_id)[0];
		}
		// cast to integer
		Long resourceId = null;
		if (requested_id != null) {
			try {
				resourceId = Long.valueOf(requested_id.toString());
				if (cacheManager.isBusy(resourceId)){
					log.debug(String.format("Resource %s is busy. Issue resultname '%s'", resourceId, RESOURCE_BUSY));
					return "resource-busy";
				}
			} catch (NumberFormatException e) {
				// do nothing, aint no proper resource anyway
			}
		}

		// continue with the rest of the interceptor stack not setting the result name here
        return invocation.invoke();
	}

}
