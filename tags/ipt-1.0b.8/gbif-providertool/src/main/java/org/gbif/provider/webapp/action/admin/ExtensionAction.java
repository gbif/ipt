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

package org.gbif.provider.webapp.action.admin;

import java.util.List;

import org.gbif.provider.service.ExtensionManager;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.webapp.action.BaseAction;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.hibernate.IptNamingStrategy;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Preparable;

public class ExtensionAction extends BaseAction {
	@Autowired
    private ExtensionManager extensionManager;
	@Autowired
	private IptNamingStrategy namingStrategy;
	private List<Extension> extensions;
    private Extension extension;
    private String tableName;
    private Long id;


	public String execute(){
		extension = extensionManager.get(id);
		tableName=namingStrategy.extensionTableName(extension);
		return SUCCESS;
	}
	public String list(){
		extensions = extensionManager.getAll();
		return SUCCESS;
	}
	public String add(){
		extension = extensionManager.get(id);
		extensionManager.installExtension(extension);
		tableName=namingStrategy.extensionTableName(extension);
		return SUCCESS;
	}
	public String remove(){
		extension = extensionManager.get(id);
		extensionManager.removeExtension(extension);
		tableName=namingStrategy.extensionTableName(extension);
		return SUCCESS;
	}
	
	public String synchroniseAll() {
		extensionManager.synchroniseExtensionsWithRepository();
		return SUCCESS;
	}

	
	
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

	public String getTableName() {
		return tableName;
	}

}
