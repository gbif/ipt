/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
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
 ***************************************************************************/

package org.gbif.ipt.utils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.internal.Nullable;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Map;

/**
 * @author markus
 * 
 */
@Singleton
public class HttpUtil {
  public class Response {
    private HttpResponse response;
    public String content;

    public Response(HttpResponse resp) {
      response = resp;
    }

    public boolean containsHeader(String name) {
      return response.containsHeader(name);
    }

    public Header[] getAllHeaders() {
      return response.getAllHeaders();
    }

    public Header getFirstHeader(String name) {
      return response.getFirstHeader(name);
    }

    public Header[] getHeaders(String name) {
      return response.getHeaders(name);
    }

    public Header getLastHeader(String name) {
      return response.getLastHeader(name);
    }

    public Locale getLocale() {
      return response.getLocale();
    }

    public HttpParams getParams() {
      return response.getParams();
    }

    public ProtocolVersion getProtocolVersion() {
      return response.getProtocolVersion();
    }

    public int getStatusCode() {
      return response.getStatusLine().getStatusCode();
    }

    public StatusLine getStatusLine() {
      return response.getStatusLine();
    }

    public HeaderIterator headerIterator() {
      return response.headerIterator();
    }

    public HeaderIterator headerIterator(String name) {
      return response.headerIterator(name);
    }

  }

  protected static final Logger log = Logger.getLogger(HttpUtil.class);
  private DefaultHttpClient client;

  @Inject
  public HttpUtil(DefaultHttpClient client) {
    super();
    this.client = client;
  }

  private HttpContext buildContext(String uri, UsernamePasswordCredentials credentials) throws URISyntaxException {
    HttpContext authContext = new BasicHttpContext();
    if (credentials != null) {
      URI authUri = new URI(uri);
      AuthScope scope = new AuthScope(authUri.getHost(), AuthScope.ANY_PORT, AuthScope.ANY_REALM);

      CredentialsProvider credsProvider = new BasicCredentialsProvider();
      credsProvider.setCredentials(scope, credentials);

      authContext.setAttribute(ClientContext.CREDS_PROVIDER, credsProvider);
    }

    return authContext;
  }

  public UsernamePasswordCredentials credentials(String username, String password) {
    return new UsernamePasswordCredentials(StringUtils.trimToEmpty(username), StringUtils.trimToEmpty(password));
  }

  /**
   * Executes a generic DELETE request
   * 
   * @param uri
   * @param params
   * @param authenticate
   * @return
   * @throws IOException
   */
  public Response delete(String url, boolean authenticate) throws IOException {
    log.info("Http delete to " + url);
    HttpDelete http = new HttpDelete(url);
    HttpGet get = new HttpGet(url);
    HttpResponse response = client.execute(get);
    Response result = new Response(response);
    HttpEntity entity = response.getEntity();
    if (entity != null) {
      result.content = EntityUtils.toString(entity);
      entity.consumeContent();
    }
    return result;
  }

  /**
   * @param url
   * @return
   * @throws JSONException if no proper json was returned
   * @throws IOException in case of a problem or the connection was aborted
   * @throws URISyntaxException
   */
  public Response get(String url) throws IOException, URISyntaxException {
    return get(url, null, null);
  }

  public Response get(String url, @Nullable Map<String, String> headers,
      @Nullable UsernamePasswordCredentials credentials) throws IOException, URISyntaxException {
    HttpGet get = new HttpGet(url);
    // http header
    if (headers != null) {
      for (String name : headers.keySet()) {
        get.addHeader(StringUtils.trimToEmpty(name), StringUtils.trimToEmpty(headers.get(name)));
      }
    }
    // authentication
    HttpContext authContext = buildContext(url, credentials);
    HttpResponse response = client.execute(get, authContext);

    Response result = new Response(response);
    HttpEntity entity = response.getEntity();
    if (entity != null) {
      result.content = EntityUtils.toString(entity);
      entity.consumeContent();
    }
    return result;
  }

  public JSONArray getJsonArray(String url) throws JSONException, IOException, URISyntaxException {
    JSONArray json = null;
    Response resp = get(url);
    if (resp.content != null) {
      json = new JSONArray(resp.content);
    }
    return json;
  }

  /**
   * @param url
   * @return
   * @throws JSONException if no proper json was returned
   * @throws IOException in case of a problem or the connection was aborted
   * @throws URISyntaxException
   */
  public JSONObject getJsonObj(String url) throws JSONException, IOException, URISyntaxException {
    JSONObject json = null;
    Response resp = get(url);
    if (resp.content != null) {
      json = new JSONObject(resp.content);
    }
    return json;
  }

  public HttpParams params(Map<String, Object> params) {
    HttpParams p = new BasicHttpParams();
    for (String name : params.keySet()) {
      p.setParameter(name, params.get(name));
    }
    return p;
  }

  /**
   * Executes a generic POST request
   * 
   * @param uri
   * @param params
   * @param authenticate
   * @return
   * @throws URISyntaxException
   * @throws IOException
   */
  public Response post(String uri, HttpParams params) throws IOException, URISyntaxException {
    return post(uri, params, null, null);
  }

  public Response post(String uri, HttpParams params, @Nullable Map<String, String> headers,
      @Nullable UsernamePasswordCredentials credentials) throws IOException, URISyntaxException {
    HttpPost post = new HttpPost(uri);
    if (params != null) {
      post.setParams(params);
    }
    // authentication
    HttpContext authContext = buildContext(uri, credentials);
    HttpResponse response = client.execute(post, authContext);

    // response
    if (response != null) {
      Response result = new Response(response);
      HttpEntity entity = response.getEntity();
      if (entity != null) {
        result.content = EntityUtils.toString(entity);
        entity.consumeContent();
      }
      return result;
    }
    return null;
  }

  /**
   * Whether a request has succedded, i.e.: 200 response code
   * 
   * @param method
   * @return
   */
  public boolean success(Response resp) {
    if (resp.getStatusLine().getStatusCode() >= 200 && resp.getStatusLine().getStatusCode() < 300) {
      return true;
    }
    return false;
  }
}
