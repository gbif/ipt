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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.struts2.interceptor.SessionAware;
import org.appfuse.model.LabelValue;
import org.gbif.provider.geo.GeoserverUtils;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.service.CacheManager;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.service.ProviderCfgManager;
import org.gbif.provider.service.RegistryManager;
import org.gbif.provider.service.ResourceFactory;
import org.gbif.provider.service.UploadEventManager;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.Constants;
import org.gbif.provider.webapp.action.BaseAction;
import org.gbif.provider.webapp.action.BaseOccurrenceResourceAction;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Preparable;

public class ProviderCfgAction extends BaseAction  {
	private static final String GOOGLE_MAPS_LOCALHOST_KEY = "";
	private static final String REGISTRY_ORG_URL = "";
	private static final String REGISTRY_SERVICE_URL = "";
	@Autowired
	private RegistryManager registryManager;
	@Autowired
	private GeoserverUtils geoUtils;
	private String organisationKey;
	
	
	public String execute() {		
		check();
		return SUCCESS;
	}

	public String save() throws Exception {
		if (cancel != null) {
			return "cancel";
		}
		// see if new organisation key was selected
		if (StringUtils.trimToNull(organisationKey)!=null){
			if (organisationKey.equalsIgnoreCase("new")){
				// register a new organisation with GBIF!
			}else{
				// a new organisation was selected. Needs full endorsement of GBIF node
				if (cfg.getIpt().getUddiID()==null){
					// never registered IPT before
					cfg.getOrg().setUddiID(organisationKey);
					if (registryManager.registerIPT()){
						saveMessage(getText("register.ipt.success"));
					}else{
						saveMessage(getText("register.ipt.problem"));
					}
				}else{
					saveMessage("Migration of registered IPTs to another Organisation is currently not supported!");
				}
			}
		}
		
		this.cfg.save();
		saveMessage(getText("config.updated"));
		check();
		return SUCCESS;
	}
	
	public void register(){
		saveMessage("Thanks for registering with GBIF. The password for managing your organisation in GBIFs registry will be mailed the contact. Please enter it here to be able to publish data resources to GBIFs registry.");
	}

	public String updateGeoserver() throws Exception {
		try {
			geoUtils.updateCatalog();
			saveMessage(getText("config.geoserverUpdated"));
		} catch (IOException e) {
			saveMessage(getText("config.geoserverNotUpdated"));
		}
		return SUCCESS;
	}

	private void check() {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		File f = new File(cfg.getDataDir());
		// tests
		if (StringUtils.trimToNull(cfg.getIpt().getContactEmail())==null || StringUtils.trimToNull(cfg.getIpt().getContactName())==null){
			saveMessage(getText("config.check.contact"));
		}
		if (!f.isDirectory() || !f.canWrite()){
			saveMessage(getText("config.check.iptDataDir"));
		}
		if (StringUtils.trimToNull(cfg.getGeoserverUrl())==null || !cfg.getGeoserverUrl().startsWith("http")){
			saveMessage(getText("config.check.geoserverUrl"));
		}
		f = new File(cfg.getGeoserverDataDir());
		if (!f.isDirectory() || !f.canWrite()){
			saveMessage(getText("config.check.geoserverDataDir"));
		}
		if (!geoUtils.login(cfg.getGeoserverUser(), cfg.getGeoserverUser(), cfg.getGeoserverUrl())){
			saveMessage(getText("config.check.geoserverLogin"));
		}
		if (StringUtils.trimToNull(cfg.getGoogleMapsApiKey())==null || StringUtils.trimToEmpty(cfg.getGoogleMapsApiKey()).equalsIgnoreCase(GOOGLE_MAPS_LOCALHOST_KEY)){
			saveMessage(getText("config.check.googleMapsApiKey"));
		}
		if (StringUtils.trimToNull(cfg.getOrg().getUddiID())==null){
			saveMessage(getText("config.check.org.uddi"));
		}
		if (StringUtils.trimToNull(cfg.getOrgPassword())==null){
			saveMessage(getText("config.check.orgPassword"));
		}else{
			// test login credentials at:
			// http://gbrds.gbif.org/registry/organization/4BEC1EC0-04B9-11DE-BBF6-C4393BAE3AC3?op=login
			// no authorization token = error
			// auth token is not a valid = 400
			// auth token is valid = 200 

		}
	}

	
	
	
	public AppConfig getConfig() {
		return this.cfg;
	}
	public void setConfig(AppConfig cfg) {
		this.cfg = cfg;
	}
	public String getRegistryOrgUrl(){
		return REGISTRY_ORG_URL;
	}
	public String getRegistryServiceUrl(){
		return REGISTRY_SERVICE_URL;
	}

	public void setOrganisationKey(String organisationKey) {
		this.organisationKey = organisationKey;
	}
	
}