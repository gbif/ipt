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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.struts2.interceptor.SessionAware;
import org.appfuse.model.LabelValue;
import org.gbif.provider.model.Extension;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.ViewMappingBase;
import org.gbif.provider.service.CacheManager;
import org.gbif.provider.service.GenericManager;
import org.gbif.provider.service.ProviderCfgManager;
import org.gbif.provider.service.RegistryManager;
import org.gbif.provider.service.ResourceFactory;
import org.gbif.provider.service.UploadEventManager;
import org.gbif.provider.service.impl.GeoserverManagerImpl;
import org.gbif.provider.util.AppConfig;
import org.gbif.provider.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.Preparable;

public class ProxyAction extends ActionSupport  {
	protected static HttpClient httpClient =  new HttpClient(new MultiThreadedHttpConnectionManager());
	protected final Log log = LogFactory.getLog(getClass());
	private GetMethod method;
	private InputStream inputStream;
    private String result = "";
	private String uri;
	
	public String execute() {		
        method = new GetMethod(uri);
        method.setFollowRedirects(true);
        try {
	        httpClient.executeMethod(method);
	        inputStream = method.getResponseBodyAsStream();
    	} catch (HttpException e) {
			log.warn("Error retrieving the proxy URI "+uri, e);
		} catch (IOException e) {
			log.warn("Error retrieving the proxy URI "+uri, e);
		}
//		inputStream = new ByteArrayInputStream(result.getBytes());
		return SUCCESS;
	}

	public void destroy(){
		method.releaseConnection();
	}
	
    public InputStream getInputStream(){
		return inputStream;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

}