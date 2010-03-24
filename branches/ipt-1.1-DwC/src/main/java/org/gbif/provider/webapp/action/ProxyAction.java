/*
 * Copyright 2009 GBIF.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gbif.provider.webapp.action;

import org.gbif.provider.util.AppConfig;

import com.opensymphony.xwork2.ActionSupport;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * TODO: Documentation.
 * 
 */
public class ProxyAction extends ActionSupport {
  protected static HttpClient httpClient = new HttpClient(
      new MultiThreadedHttpConnectionManager());
  protected final Log log = LogFactory.getLog(getClass());
  private GetMethod method;
  private InputStream inputStream;
  private final String result = "";
  private String uri;

  public void destroy() {
    method.releaseConnection();
  }

  @Override
  public String execute() {
    log.debug("Proxying " + uri);
    method = new GetMethod(uri);
    method.setFollowRedirects(true);
    try {
      httpClient.executeMethod(method);
      inputStream = method.getResponseBodyAsStream();
    } catch (HttpException e) {
      log.warn("Error retrieving the proxy URI " + uri, e);
    } catch (IOException e) {
      log.warn("Error retrieving the proxy URI " + uri, e);
    }
    // inputStream = new ByteArrayInputStream(result.getBytes());
    return SUCCESS;
  }

  public InputStream getInputStream() {
    return inputStream;
  }

  public String getUri() {
    return uri;
  }

  public String organisations() {
    method = new GetMethod(AppConfig.getRegistryOrgUrl() + ".json");
    method.setFollowRedirects(true);
    try {
      httpClient.executeMethod(method);
      inputStream = method.getResponseBodyAsStream();
    } catch (HttpException e) {
      log.warn("Error retrieving registry organisations", e);
    } catch (IOException e) {
      log.warn("Error retrieving registry organisations", e);
    }
    return SUCCESS;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

}