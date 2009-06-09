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

import org.apache.commons.lang.StringUtils;
import org.gbif.provider.service.RegistryManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.webapp.action.BasePostAction;
import org.springframework.beans.factory.annotation.Autowired;

public class ConfigOrgAction extends BasePostAction{
	@Autowired
	private RegistryManager registryManager;
	private String organisationKey;
	
	
	public String read() {
		if (cfg.isIptRegistered()){
			saveMessage("The IPT is already registered with this organisation. You can only update its metadata, not switch to another organisation");
		}
		return SUCCESS;
	}

	public String save(){
		// cannot change the organisation once an IPT has been registered. So test!
		this.cfg.save();
		saveMessage(getText("config.updated"));
		return SUCCESS;
	}
	
	
	public AppConfig getConfig() {
		return this.cfg;
	}
	public void setConfig(AppConfig cfg) {
		this.cfg = cfg;
	}
	
	public String getRegistryOrgUrl(){
		return AppConfig.getRegistryOrgUrl();
	}
	public String getRegistryNodeUrl(){
		return AppConfig.getRegistryNodeUrl();
	}

	public void setOrganisationKey(String organisationKey) {
		this.organisationKey = StringUtils.trimToNull(organisationKey);
	}
}