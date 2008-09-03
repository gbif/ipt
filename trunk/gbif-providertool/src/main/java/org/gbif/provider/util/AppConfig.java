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

package org.gbif.provider.util;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Constant values used throughout the application.
 */
public class AppConfig {
	protected static final Log log = LogFactory.getLog(AppConfig.class);
    private static File DATA_DIR_FILE;
    // base urls have NO trailing /
    private static String APP_BASE_URL="";
    private static String GEOSERVER_BASE_URL;
    private String providerTitle;
    private String providerDescription;
    private String providerLogo;
    private String providerHomepage;
    
    /**
     * only sets base dir once when first called
     * @param dataDir
     */
    public void setDataDir(String dataDir) {
    	if (DATA_DIR_FILE == null){
    		if (dataDir == null){
    		    throw new NullPointerException();    			
    		}else{
    			DATA_DIR_FILE = new File(dataDir);
    			if (DATA_DIR_FILE.exists()){
    	    		log.info("writable data directory set to "+dataDir);
    			}else{
    				throw new IllegalArgumentException(String.format("writable data directory %s doesn't exist.", dataDir));
    			}
    		}
    	}
	}

	public static File getDataDir() {
		return DATA_DIR_FILE;
	}

	public void setAppBaseUrl(String appBaseUrl) {
		appBaseUrl=appBaseUrl.trim();
		while(appBaseUrl.endsWith("/")){
			appBaseUrl = (String) appBaseUrl.subSequence(0, appBaseUrl.length()-1);
		}
		APP_BASE_URL = appBaseUrl;
	}
	public static String getAppBaseUrl(){
		return APP_BASE_URL;
	}

	public void setGeoserverBaseUrl(String geoserverBaseUrl) {
		geoserverBaseUrl=geoserverBaseUrl.trim();
		while(geoserverBaseUrl.endsWith("/")){
			geoserverBaseUrl = (String) geoserverBaseUrl.subSequence(0, geoserverBaseUrl.length()-1);
		}
		GEOSERVER_BASE_URL = geoserverBaseUrl;
	}

	public static String getGeoserverBaseUrl(){
		return GEOSERVER_BASE_URL;
	}

	public String getProviderTitle() {
		return providerTitle;
	}

	public void setProviderTitle(String providerTitle) {
		this.providerTitle = providerTitle;
	}

	public String getProviderDescription() {
		return providerDescription;
	}

	public void setProviderDescription(String providerDescription) {
		this.providerDescription = providerDescription;
	}

	public String getProviderLogo() {
		return providerLogo;
	}

	public void setProviderLogo(String providerLogo) {
		this.providerLogo = providerLogo;
	}

	public String getProviderHomepage() {
		return providerHomepage;
	}

	public void setProviderHomepage(String providerHomepage) {
		this.providerHomepage = providerHomepage;
	}

	public String toString(){
	    return String.format("Provider=%s, DATA_DIR=%s, APP_BASE_URL=%s, GEOSERVER_BASE_URL=%s", providerTitle, DATA_DIR_FILE, APP_BASE_URL, GEOSERVER_BASE_URL);		
	}
}
