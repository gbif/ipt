/*
 * Copyright 2010 GBIF.
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
package org.gbif.registry.api.client;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * This class provides a client API to GBIF Registry web services. It currently
 * supports creating, deleting, listing, reading, and updating organisations
 * using the Organisation API. Support for the Resource API, the Service API,
 * and the Node API is coming shortly.
 * 
 * Usage example for listing all organisations:
 * 
 * <code>
 * Registry api = GbifRegistry.init("http://gbrds.gbif.org");
 * List<GbifOrganisation> list = api.execute(OrganisationApi.list());
 * </code>
 * 
 * @see http://code.google.com/p/gbif-registry
 * 
 */
public class GbifRegistry implements Registry {

  /**
   * This class surfaces an RPC-style interface to the GBIF Registry
   * Organisation API.
   * 
   * @see http://code.google.com/p/gbif-registry/wiki/OrganisationAPI
   * 
   */
  public static class OrganisationApi {
    static class CreateRequest extends Request {
      final GbifOrganisation org;

      CreateRequest(GbifOrganisation org) {
        this.org = org;
      }

      @Override
      public String getHttpMethodType() {
        return "POST";
      }

      @Override
      public ImmutableMap<String, String> getPayload() {
        return Util.asImmutableMap(org);
      }

      @Override
      public String getRequestPath() {
        return "/registry/organisation";
      }
    }

    static class CreateResponse extends Response<GbifOrganisation> {
      CreateResponse(RpcRequest request, int status, String body,
          Throwable error) {
        super(request, status, body, error);
      }

      @Override
      public GbifOrganisation getResult() {
        if (!body.contains("<organisationKey>")) {
          return null;
        }
        return Util.fromXml(body);
      }
    }

    static class DeleteRequest extends Request {
      final GbifOrganisation org;

      DeleteRequest(GbifOrganisation org) {
        checkNotNull(org.getKey());
        checkNotNull(org.getPassword());
        this.org = org;
      }

      @Override
      public Credentials getCredentials() {
        return Credentials.with(org.getKey(), org.getPassword());
      }

      @Override
      public String getHttpMethodType() {
        return "DELETE";
      }

      @Override
      public String getRequestPath() {
        return String.format("/registry/organisation/%s", org.getKey());
      }
    }

    static class DeleteResponse extends Response<Boolean> {
      DeleteResponse(RpcRequest request, int status, String body,
          Throwable error) {
        super(request, status, body, error);
      }

      @Override
      public Boolean getResult() {
        if (!body.contains("<organisationKey>")) {
          return null;
        }
        return body.contains("Organisation deleted successfully");
      }
    }

    static class ListRequest extends Request {
      @Override
      public String getRequestPath() {
        return "/registry/organisation.json";
      }
    }

    static class ListResponse extends Response<List<GbifOrganisation>> {
      ListResponse(RpcRequest request, int status, String body, Throwable error) {
        super(request, status, body, error);
      }

      @Override
      public List<GbifOrganisation> getResult() {
        if (!body.contains("<organisationKey>")) {
          return null;
        }
        return Util.listFromJson(body);
      }
    }

    static class ReadRequest extends Request {
      final String key;

      ReadRequest(String key) {
        this.key = key;
      }

      @Override
      public String getRequestPath() {
        return String.format("/registry/organisation/%s.json", key);
      }
    }

    static class ReadResponse extends Response<GbifOrganisation> {

      ReadResponse(RpcRequest request, int status, String body, Throwable error) {
        super(request, status, body, error);
      }

      @Override
      public GbifOrganisation getResult() {
        if (body.contains("No organisation matches the key provided")) {
          return null;
        }
        return Util.fromJson(body);
      }
    }

    static abstract class Request implements RpcRequest {
      public Credentials getCredentials() {
        return null;
      }

      public String getHttpMethodType() {
        return "GET";
      }

      public ImmutableMap<String, String> getPayload() {
        return ImmutableMap.of();
      }

      public ImmutableMap<String, String> getRequestParams() {
        return ImmutableMap.of();
      }

      public String getRequestPath() {
        throw new UnsupportedOperationException();
      }
    }

    static abstract class Response<T> implements RpcResponse<T> {
      final RpcRequest request;
      final int status;
      final String body;
      final Throwable error;

      Response(RpcRequest request, int status, String body, Throwable error) {
        this.request = request;
        this.status = status;
        this.body = body;
        this.error = error;
      }

      public String getBody() {
        return body;
      }

      public Throwable getError() {
        return error;
      }

      public T getResult() {
        throw new UnsupportedOperationException();
      }

      public int getStatusCode() {
        return status;
      }

      @Override
      public String toString() {
        return Objects.toStringHelper(this).add("Request", request).add(
            "StatusCode", status).add("Body", body).add("Error", error).toString();
      }
    }

    static class UpdateRequest implements RpcRequest {
      final GbifOrganisation org;

      UpdateRequest(GbifOrganisation org) {
        this.org = org;
      }

      public Credentials getCredentials() {
        return Credentials.with(org.getKey(), org.getPassword());
      }

      public String getHttpMethodType() {
        return "POST";
      }

      public ImmutableMap<String, String> getPayload() {
        return Util.asImmutableMap(org);
      }

      public ImmutableMap<String, String> getRequestParams() {
        return ImmutableMap.of();
      }

      public String getRequestPath() {
        return String.format("/registry/organisation/%s", org.getKey());
      }
    }

    static class UpdateResponse extends Response<GbifOrganisation> {
      UpdateResponse(RpcRequest request, int status, String body,
          Throwable error) {
        super(request, status, body, error);
      }

      @Override
      public GbifOrganisation getResult() {
        UpdateRequest r = (UpdateRequest) request;
        if (!body.contains(r.org.getKey())) {
          return null;
        }
        return r.org;
      }
    }

    static class Util {
      private static ImmutableMap<String, String> asImmutableMap(
          GbifOrganisation org) {
        String json = new Gson().toJson(org, GbifOrganisation.class);
        Map<String, String> map = new Gson().fromJson(json,
            new TypeToken<Map<String, String>>() {
            }.getType());
        return ImmutableMap.copyOf(map);
      }

      private static GbifOrganisation fromJson(String json) {
        return new Gson().fromJson(json, GbifOrganisation.class);
      }

      private static GbifOrganisation fromXml(String xml) {
        GbifOrganisation org = null;
        try {
          SAXParser p = SAXParserFactory.newInstance().newSAXParser();
          XmlHandler h = new XmlHandler();
          InputStream s = new ByteArrayInputStream(xml.getBytes());
          p.parse(s, h);
          org = GbifOrganisation.builder().key(h.organisationKey).password(
              h.password).build();
        } catch (ParserConfigurationException e) {
          e.printStackTrace();
        } catch (SAXException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
        return org;
      }

      private static List<GbifOrganisation> listFromJson(String json) {
        return new Gson().fromJson(json,
            new TypeToken<List<GbifOrganisation>>() {
            }.getType());
      }
    }

    static class XmlHandler extends DefaultHandler {
      String content;
      String organisationKey;
      String resourceKey;
      String serviceKey;
      String password;
      String key;

      @Override
      public void characters(char[] ch, int start, int length)
          throws SAXException {
        content += String.valueOf(ArrayUtils.subarray(ch, start, start + length));
      }

      @Override
      public void endElement(String uri, String localName, String name)
          throws SAXException {
        if (name.equalsIgnoreCase("user")) {
        } else if (name.equalsIgnoreCase("password")) {
          password = content;
        } else if (name.equalsIgnoreCase("key")) {
          key = content.replaceAll("\\s", "");
        } else if (name.equalsIgnoreCase("organisationKey")) {
          organisationKey = content.replaceAll("\\s", "");
        } else if (name.equalsIgnoreCase("organizationKey")) {
          organisationKey = content.replaceAll("\\s", "");
        } else if (name.equalsIgnoreCase("resourceKey")) {
          resourceKey = content.replaceAll("\\s", "");
        } else if (name.equalsIgnoreCase("serviceKey")) {
          serviceKey = content.replaceAll("\\s", "");
        }
        content = "";
      }

      @Override
      public void startDocument() throws SAXException {
        content = "";
        key = "";
        organisationKey = "";
        resourceKey = "";
        serviceKey = "";
        password = "";
      }

      @Override
      public void startElement(String uri, String localName, String name,
          Attributes attributes) throws SAXException {
        content = "";
      }
    }

    /**
     * Returns a {@link RpcRequest} that creates the organisation.
     * 
     * If the organisation is null, a {@link NullPointerException} is thrown.
     * The organisation must include {@code name}, {@code primaryContactType},
     * {@code primaryContactEmail}, and {@code nodeKey}, otherwise an
     * {@link IllegalArgumentException} is thrown.
     * 
     * The {@link RpcResponse} result type for this request is a
     * {@link GbifOrganisation} with the {@code key} and {@code password} values
     * that were generated for the new organisation when it was created, or null
     * if there were errors.
     * 
     * @see http://goo.gl/H17q
     * 
     * @param org the organisation to create
     * @return RpcRequest the RPC request for creating the organisation
     */
    public static RpcRequest create(GbifOrganisation org) {
      checkArgument(notNullOrEmpty(org.getName()));
      checkArgument(notNullOrEmpty(org.getPrimaryContactType()));
      checkArgument(notNullOrEmpty(org.getPrimaryContactEmail()));
      checkArgument(notNullOrEmpty(org.getNodeKey()));
      return new CreateRequest(org);
    }

    /**
     * Returns a new {@link RpcRequest} requiring authentication that deletes
     * the organisation. The organisation must include a {@code key} and {@code
     * password}, otherwise an {@link IllegalArgumentException} is thrown.
     * 
     * The {@link RpcResponse} result type for this request is a {@link Boolean}
     * that is true if the organisation was deleted, otherwise false.
     * 
     * @see http://goo.gl/qJql
     * 
     * @param org the organisation to delete
     * @return RpcRequest the RPC request for deleting the organisation
     */
    public static RpcRequest delete(GbifOrganisation org) {
      checkArgument(notNullOrEmpty(org.getKey()));
      checkArgument(notNullOrEmpty(org.getPassword()));
      return new DeleteRequest(org);
    }

    /**
     * Returns a new {@link RpcRequest} that lists all organisations.
     * 
     * The {@link RpcResponse} result type for this request is a {@link List} of
     * {@link GbifOrganisation}s. If there are no organisations, an empty list
     * is returned.
     * 
     * @see http://goo.gl/D6qH
     * 
     * @return RpcRequest the RPC request for listing all organisations
     */
    public static RpcRequest list() {
      return new ListRequest();
    }

    /**
     * Returns a new {@link RpcRequest} that reads an organisation from the GBIF
     * Registry. The {@code key} is required, otherwise an
     * {@link IllegalArgumentException} is thrown.
     * 
     * The {@link RpcResponse} result type for this request is a
     * {@link GbifOrganisation} representing an existing organisation that
     * matches the {@code key} or {@code null} if an organisation could not be
     * found.
     * 
     * @see http://goo.gl/68dV
     * 
     * @param key the organisation key
     * @return RpcRequest the RPC request that reads the organisation
     */
    public static RpcRequest read(String key) {
      checkArgument(notNullOrEmpty(key));
      return new ReadRequest(key);
    }

    /**
     * Returns a {@link RpcRequest} requiring authentication that updates the
     * organisation. The organisation must include {@code key}, {@code
     * primaryContactType}, and {@code password}, otherwise an
     * {@link IllegalArgumentException} is thrown.
     * 
     * The {@link RpcResponse} result type for this request is a
     * {@link GbifOrganisation} with {@code key} and {@code password} values
     * that were generated for the new organisation when it was created.
     * 
     * @see http://goo.gl/H17q
     * 
     * @param org the organisation to update
     * @return RpcRequest the RPC request for updating the organisation
     */
    public static RpcRequest update(GbifOrganisation org) {
      checkArgument(notNullOrEmpty(org.getKey()));
      checkArgument(notNullOrEmpty(org.getPrimaryContactType()));
      checkArgument(notNullOrEmpty(org.getPassword()));
      return new UpdateRequest(org);
    }

    @SuppressWarnings("unchecked")
    private static <T> RpcResponse<T> createResponse(RpcRequest request,
        int status, String body, Throwable error) {
      RpcResponse<T> response = null;
      if (request instanceof CreateRequest) {
        response = (RpcResponse<T>) new CreateResponse(request, status, body,
            error);
      } else if (request instanceof DeleteRequest) {
        response = (RpcResponse<T>) new DeleteResponse(request, status, body,
            error);
      } else if (request instanceof ListRequest) {
        response = (RpcResponse<T>) new ListResponse(request, status, body,
            error);
      } else if (request instanceof ReadRequest) {
        response = (RpcResponse<T>) new ReadResponse(request, status, body,
            error);
      }
      return response;
    }

    private static boolean notNullOrEmpty(String val) {
      return val != null && val.trim().length() > 0;
    }

    private OrganisationApi() {
    }
  }

  private static final Log log = LogFactory.getLog(GbifRegistry.class);

  public static GbifRegistry init(String host) {
    try {
      URL url = new URL(host);
      return new GbifRegistry(url.getHost());
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private static HttpMethod httpMethod(String host, RpcRequest rpc) {
    String url = String.format("%s%s", host, rpc.getRequestPath());
    HttpMethod method;
    ImmutableMap<String, String> params = rpc.getRequestParams();
    if (params == null) {
      params = ImmutableMap.of();
    }
    ImmutableMap<String, String> payload = rpc.getPayload();
    if (payload == null) {
      payload = ImmutableMap.of();
    }
    String type = rpc.getHttpMethodType();
    if (type.equalsIgnoreCase("GET")) {
      GetMethod get = new GetMethod(url);
      get.setQueryString(params(params));
      method = get;
    } else if (type.equalsIgnoreCase("POST")) {
      PostMethod post = new PostMethod(url);
      post.setDoAuthentication(true);
      post.addParameters(params(payload));
      method = post;
    } else if (type.equalsIgnoreCase("DELETE")) {
      DeleteMethod delete = new DeleteMethod(url);
      delete.setDoAuthentication(true);
      method = delete;
    } else {
      throw new IllegalArgumentException("Unknown HTTP method: " + type);
    }
    method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
        new DefaultHttpMethodRetryHandler(3, false));
    return method;
  }

  private static NameValuePair[] params(ImmutableMap<String, String> map) {
    NameValuePair[] pairs = new NameValuePair[map.size()];
    int i = 0;
    for (String name : map.keySet()) {
      pairs[i++] = new NameValuePair(name, map.get(name));
    }
    return pairs;
  }

  private final String url;
  private final String host;
  private final HttpClient client;

  private GbifRegistry(String host) {
    url = String.format("http://%s", host);
    this.host = host;
    client = new HttpClient(new MultiThreadedHttpConnectionManager());
  }

  /**
   * @see org.gbif.registry.api.client.Registry#execute(org.gbif.registry.api.client.Registry.RpcRequest)
   */
  public <T> RpcResponse<T> execute(RpcRequest request) {
    checkNotNull(request, "Request is null");
    checkNotNull(request.getHttpMethodType(), "Method is null");
    checkArgument(request.getHttpMethodType().length() > 0, "Method is empty");
    HttpMethod method = httpMethod(url, request);
    Throwable error = null;
    String body = null;
    int status = 0;
    try {
      Credentials cred = request.getCredentials();
      if (cred != null) {
        setCredentials(cred);
      }
      status = client.executeMethod(method);
      log.info(String.format("Executed %s %d %s (parmas=%s and payload=%s)",
          request.getHttpMethodType(), status, method.getURI(),
          request.getRequestParams(), request.getPayload()));
      body = method.getResponseBodyAsString();
    } catch (HttpException e) {
      error = e;
    } catch (IOException e) {
      error = e;
    } finally {
      method.releaseConnection();
    }
    return OrganisationApi.createResponse(request, status, body, error);
  }

  private void setCredentials(Credentials credentials) {
    AuthScope scope = new AuthScope(host, -1, AuthScope.ANY_REALM); // AuthScope.ANY;
    client.getState().setCredentials(
        scope,
        new UsernamePasswordCredentials(credentials.getId(),
            credentials.getPasswd()));
    client.getParams().setAuthenticationPreemptive(true);
  }
}
