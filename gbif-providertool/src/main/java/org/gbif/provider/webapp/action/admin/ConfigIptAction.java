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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.interceptor.RequestAware;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.gbif.provider.service.GeoserverManager;
import org.gbif.provider.service.RegistryException;
import org.gbif.provider.service.RegistryManager;
import org.gbif.provider.service.impl.RegistryManagerImpl;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.webapp.action.BaseAction;
import org.gbif.provider.webapp.action.BasePostAction;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionContext;

public class ConfigIptAction extends BasePostAction{
	@Autowired
	private RegistryManager registryManager;
	
	public String read() {
		check();
		return SUCCESS;
	}

	public String save(){
		// check if already registered. If yes, also update GBIF registry
		if (cfg.isIptRegistered()){
			try {
				registryManager.updateIPT();
				saveMessage(getText("registry.updated"));
			} catch (RegistryException e) {
				saveMessage(getText("registry.problem"));
				log.warn(e);
			}
		}else{
			saveMessage(getText("config.updated"));
		}
		this.cfg.save();
		check();
		return SUCCESS;
	}
	
	public String register(){
		if (cfg.isIptRegistered()){
			saveMessage(getText("register.ipt.already"));
		}else if (!cfg.isOrgRegistered()){
			saveMessage(getText("register.org.missing"));
		}else if (StringUtils.trimToNull(cfg.getOrgPassword())==null){
			saveMessage(getText("register.org.password.missing"));
		}else{
			// register IPT with organisation
			try {
				registryManager.registerIPT();
				saveMessage(getText("register.ipt.success"));
				this.cfg.save();
			} catch (RegistryException e) {
				saveMessage(getText("register.ipt.problem"));
			}
		}
		return SUCCESS;
	}


	private void check() {
		// tests
		if (StringUtils.trimToNull(cfg.getIpt().getContactEmail())==null || StringUtils.trimToNull(cfg.getIpt().getContactName())==null){
			saveMessage(getText("config.check.contact"));
		}
		if (StringUtils.trimToNull(cfg.getOrg().getUddiID())==null){
			saveMessage(getText("config.check.orgRegistered"));
		}else if (StringUtils.trimToNull(cfg.getOrgPassword())==null){
			saveMessage(getText("config.check.orgPassword"));
		}else{
			if (!registryManager.testLogin()){
				// authorization error
				saveMessage(getText("config.check.orgLogin"));
			}
		}
	}
	
	public AppConfig getConfig() {
		return this.cfg;
	}
	public void setConfig(AppConfig cfg) {
		this.cfg = cfg;
	}
}