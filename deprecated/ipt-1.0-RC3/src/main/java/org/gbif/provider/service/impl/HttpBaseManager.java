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
package org.gbif.provider.service.impl;

import org.gbif.provider.util.AppConfig;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import freemarker.template.Configuration;

/**
 * TODO: Documentation.
 * 
 */
public class HttpBaseManager {
  public static final String FORM_URL_ENCODED_CONTENT_TYPE = "application/x-www-form-urlencoded";

  protected static HttpClient client = new HttpClient(
      new MultiThreadedHttpConnectionManager());
  protected final Log log = LogFactory.getLog(HttpBaseManager.class);
  @Autowired
  protected AppConfig cfg;
  @Autowired
  protected Configuration freemarker;

  protected String executeDelete(String uri, boolean authenticate) {
    String result = null;
    log.debug("Deleting " + uri);
    DeleteMethod method = newHttpDelete(uri, authenticate);
    try {
      client.executeMethod(method);
      if (succeeded(method)) {
        result = method.getResponseBodyAsString();
      }
    } catch (Exception e) {
      log.warn(uri + ": " + e.toString());
    } finally {
      if (method != null) {
        method.releaseConnection();
      }
    }
    return result;
  }

  protected String executeGet(String uri, boolean authenticate) {
    NameValuePair[] params = new NameValuePair[0];
    return executeGet(uri, params, authenticate);
  }

  protected String executeGet(String uri, NameValuePair[] params,
      boolean authenticate) {
    log.info("Getting " + uri);
    String result = null;
    GetMethod method = newHttpGet(uri, authenticate);
    method.setQueryString(params);
    try {
      client.executeMethod(method);
      if (succeeded(method)) {
        result = method.getResponseBodyAsString();
      }
    } catch (Exception e) {
      log.warn(uri + ": " + e.toString());
    } finally {
      if (method != null) {
        method.releaseConnection();
      }
    }
    return result;
  }

  protected String executePost(String uri, NameValuePair[] params,
      boolean authenticate) {
    String result = null;
    log.info("Posting to " + uri);
    PostMethod method = newHttpPost(uri, authenticate);
    method.setRequestBody(params);
    try {
      client.executeMethod(method);
      if (succeeded(method)) {
        result = method.getResponseBodyAsString();
      }
    } catch (Exception e) {
      log.warn(uri + ": " + e.toString());
    } finally {
      if (method != null) {
        method.releaseConnection();
      }
    }
    return result;
  }

  protected String executePost(String uri, String content, String contentType,
      boolean authenticate) {
    log.info("Posting to " + uri);
    String result = null;
    PostMethod method = newHttpPost(uri, authenticate);
    RequestEntity body = null;
    try {
      body = new StringRequestEntity(content, contentType, "utf-8");
    } catch (UnsupportedEncodingException e1) {
      e1.printStackTrace();
    }
    method.setRequestEntity(body);
    try {
      client.executeMethod(method);
      if (succeeded(method)) {
        result = method.getResponseBodyAsString();
      }
    } catch (Exception e) {
      log.warn(uri + ": " + e.toString());
    } finally {
      if (method != null) {
        method.releaseConnection();
      }
    }
    return result;
  }

  protected InputStream getStream(String source) {
    return new ByteArrayInputStream(source.getBytes());
  }

  protected void setCredentials(String host, String username, String password) {
    AuthScope scope = new AuthScope(host, -1, AuthScope.ANY_REALM); // AuthScope.ANY;
    client.getState().setCredentials(
        scope,
        new UsernamePasswordCredentials(StringUtils.trimToEmpty(username),
            StringUtils.trimToEmpty(password)));
    client.getParams().setAuthenticationPreemptive(true);
  }

  protected boolean succeeded(HttpMethodBase method) {
    if (method.getStatusCode() >= 200 && method.getStatusCode() < 300) {
      return true;
    }
    try {
      log.warn("Http request to " + method.getURI() + " failed: "
          + method.getStatusLine());
    } catch (URIException e) {
      log.warn("Http request to ??? failed: " + method.getStatusLine());
    }
    // logRequest(method);
    return false;
  }

  private String executePut(String uri, NameValuePair[] params,
      boolean authenticate) {
    String result = null;
    log.info("Putting to " + uri);
    PutMethod method = newHttpPut(uri, authenticate);
    // this bit is taken from the PostMethod source code
    // http://www.docjar.org/html/api/org/apache/commons/httpclient/methods/PostMethod.java.html
    String content = EncodingUtil.formUrlEncode(params,
        method.getRequestCharSet());
    ByteArrayRequestEntity entity = new ByteArrayRequestEntity(
        EncodingUtil.getAsciiBytes(content), FORM_URL_ENCODED_CONTENT_TYPE);
    // ... up to here
    method.setRequestEntity(entity);
    try {
      client.executeMethod(method);
      if (succeeded(method)) {
        result = method.getResponseBodyAsString();
      }
    } catch (Exception e) {
      log.warn(uri + ": " + e.toString());
    } finally {
      if (method != null) {
        method.releaseConnection();
      }
    }
    return result;
  }

  private void logRequest(HttpMethodBase method) {
    String head = "Unknown URI";
    try {
      head = method.getURI().toString();
    } catch (URIException e) {
    }

    head += "\nREQUEST";
    for (Header h : method.getRequestHeaders()) {
      head += h.toString();
    }
    head += "\n----------\nREPONSE";
    for (Header h : method.getRequestHeaders()) {
      head += "\n" + h.toString();
    }
    log.info(head);
  }

  private DeleteMethod newHttpDelete(String url, boolean authenticate) {
    DeleteMethod method = new DeleteMethod(url);
    // method.setFollowRedirects(true);
    method.setDoAuthentication(authenticate);
    return method;
  }

  private GetMethod newHttpGet(String url, boolean authenticate) {
    GetMethod method = new GetMethod(url);
    method.setFollowRedirects(true);
    method.setDoAuthentication(authenticate);
    return method;
  }

  private PostMethod newHttpPost(String url, boolean authenticate) {
    PostMethod method = new PostMethod(url);
    // method.setFollowRedirects(true);
    method.setDoAuthentication(authenticate);
    return method;
  }

  private PutMethod newHttpPut(String url, boolean authenticate) {
    PutMethod method = new PutMethod(url);
    // method.setFollowRedirects(true);
    method.setDoAuthentication(authenticate);
    return method;
  }

}
