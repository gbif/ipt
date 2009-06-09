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

package org.gbif.provider.webapp.action;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.interceptor.ServletRequestAware;

public class BasePostAction extends BaseAction  implements ServletRequestAware{
	protected HttpServletRequest request;
	
	
	public String execute() {
		if (request.getMethod().equalsIgnoreCase("post")){
			// call the save method for POSTs
			if (cancel != null) {
				return "cancel";
			}
			return save();
		}
		return read();
	}

	public String read(){
		return SUCCESS;
	}
	public String save(){
		return SUCCESS;
	}

	
	public void setServletRequest(HttpServletRequest request) {
		this.request=request;
	}
}