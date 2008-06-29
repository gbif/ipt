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

import java.util.List;

import org.appfuse.service.GenericManager;
import org.appfuse.webapp.action.BaseAction;
import org.gbif.provider.model.Extension;

import com.opensymphony.xwork2.Preparable;

public class DwcExtensionAction extends BaseAction implements Preparable{
    private GenericManager<Extension, Long> dwcExtensionManager;
    private List<Extension> extensions;
    private Extension extension;
    private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<Extension> getExtensions() {
		return extensions;
	}

	public Extension getExtension() {
		return extension;
	}

	public void setDwcExtensionManager(
			GenericManager<Extension, Long> dwcExtensionManager) {
		this.dwcExtensionManager = dwcExtensionManager;
	}

	public void prepare() throws Exception {
		// TODO Auto-generated method stub		
	}
	
	public String execute(){
		extension = dwcExtensionManager.get(id);
		extension.getProperties();
		return SUCCESS;
	}

	public String list(){
		extensions = dwcExtensionManager.getAll();
		return SUCCESS;
	}

}
