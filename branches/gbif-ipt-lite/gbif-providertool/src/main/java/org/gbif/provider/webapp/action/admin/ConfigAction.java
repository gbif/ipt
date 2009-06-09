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

import org.gbif.provider.util.AppConfig;
import org.gbif.provider.webapp.action.BasePostAction;

public class ConfigAction extends BasePostAction{
	
	public String read() {
		check();
		return SUCCESS;
	}

	public String save(){
		if (cancel != null) {
			return "cancel";
		}
		this.cfg.save();
		cfg.reloadLogger();
		saveMessage(getText("config.updated"));
		check();
		return SUCCESS;
	}
	
	private void check() {
		// tests
		File f = new File(cfg.getDataDir());
		if (!f.isDirectory() || !f.canWrite()){
			saveMessage(getText("config.check.iptDataDir"));
		}
	}

	public AppConfig getConfig() {
		return this.cfg;
	}
	public void setConfig(AppConfig cfg) {
		this.cfg = cfg;
	}
}