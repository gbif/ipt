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
public class GbifRegistry implements RegistryService {

  public static interface CreateOrgRequest extends
      RpcRequest<CreateOrgResponse, GbifOrganisation> {
  }

  public static interface CreateOrgResponse extends
      RpcResponse<GbifOrganisation> {
  }

  public static interface DeleteOrgRequest extends
      RpcRequest<DeleteOrgResponse, Boolean> {
  }

  public static interface DeleteOrgResponse extends RpcResponse<Boolean> {
  }

  public static interface ListOrgRequest extends
      RpcRequest<ListOrgResponse, List<GbifOrganisation>> {
  }

  public static interface ListOrgResponse extends
      RpcResponse<List<GbifOrganisation>> {
  }

  public static interface ReadOrgRequest extends
      RpcRequest<ReadOrgResponse, GbifOrganisation> {
  }

  public static interface ReadOrgResponse extends RpcResponse<GbifOrganisation> {
  }

  public static interface UpdateOrgRequest extends
      RpcRequest<UpdateOrgResponse, GbifOrganisation> {
  }

  public static interface UpdateOrgResponse extends
      RpcResponse<GbifOrganisation> {
  }

  static class OrgUtil {
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
        OrgXmlHandler h = new OrgXmlHandler();
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
      return new Gson().fromJson(json, new TypeToken<List<GbifOrganisation>>() {
      }.getType());
    }
  }

  static class OrgXmlHandler extends DefaultHandler {
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

  private static class OrgApi implements OrganisationApi {

    static class CreateRequest extends OrgRequest implements CreateOrgRequest {

      final GbifOrganisation org;

      CreateRequest(GbifOrganisation org, RegistryService registry) {
        super(registry);
        this.org = org;
      }

      /**
       * @see org.gbif.registry.api.client.RegistryService.RpcRequest#execute()
       */
      public CreateOrgResponse execute() {
        return new CreateResponse(this, registry.execute(this));
      }

      @Override
      public String getHttpMethodType() {
        return "POST";
      }

      @Override
      public ImmutableMap<String, String> getPayload() {
        return OrgUtil.asImmutableMap(org);
      }

      @Override
      public String getRequestPath() {
        return "/registry/organisation";
      }
    }

    static class CreateResponse extends OrgResponse<CreateRequest> implements
        CreateOrgResponse {

      CreateResponse(CreateRequest request, Response response) {
        super(request, response);
      }

      public GbifOrganisation getResult() {
        String body = getBody();
        if (!body.contains("<organisationKey>")) {
          return null;
        }
        return OrgUtil.fromXml(body);
      }

    }

    static class DeleteRequest extends OrgRequest implements DeleteOrgRequest {

      final GbifOrganisation org;
      final Credentials credentials;
      final String method;
      final String path;
      final ImmutableMap<String, String> payload;

      DeleteRequest(GbifOrganisation org, RegistryService registry) {
        super(registry);
        this.org = org;
        credentials = Credentials.with(org.getKey(), org.getPassword());
        method = "DELETE";
        path = String.format("/registry/organisation/%s", org.getKey());
        payload = OrgUtil.asImmutableMap(org);
      }

      /**
       * @see org.gbif.registry.api.client.RegistryService.RpcRequest#execute()
       */
      public DeleteOrgResponse execute() {
        return new DeleteResponse(this, registry.execute(this));
      }

      @Override
      public Credentials getCredentials() {
        return credentials;
      }

      @Override
      public String getHttpMethodType() {
        return method;
      }

      @Override
      public ImmutableMap<String, String> getPayload() {
        return payload;
      }

      @Override
      public String getRequestPath() {
        return path;
      }
    }

    static class DeleteResponse extends OrgResponse<DeleteRequest> implements
        DeleteOrgResponse {

      DeleteResponse(DeleteRequest request, Response response) {
        super(request, response);
      }

      /**
       * @see org.gbif.registry.api.client.RegistryService.RpcResponse#getResult()
       */
      public Boolean getResult() {
        return getBody().contains("Organisation deleted successfully");
      }
    }

    static class ListRequest implements ListOrgRequest {

      final RegistryService registry;

      ListRequest(RegistryService registry) {
        this.registry = registry;
      }

      public ListResponse execute() {
        return new ListResponse(this, registry.execute(this));
      }

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
        return "/registry/organisation.json";
      }
    }

    static class ListResponse extends OrgResponse<ListRequest> implements
        ListOrgResponse {

      private ListResponse(ListRequest request, Response response) {
        super(request, response);
      }

      public List<GbifOrganisation> getResult() {
        String body = getBody();
        if (body == null || body.length() < 1) {
          return null;
        }
        return OrgUtil.listFromJson(body);
      }
    }

    static abstract class OrgRequest implements Request {

      final RegistryService registry;

      OrgRequest(RegistryService registry) {
        this.registry = registry;
      }

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
        throw new UnsupportedOperationException("Subclass must override.");
      }
    }

    static abstract class OrgResponse<T> implements Response {
      final Response response;
      final T rpcRequest;

      OrgResponse(T rpcRequest, Response response) {
        this.rpcRequest = rpcRequest;
        this.response = response;
      }

      public String getBody() {
        return response.getBody();
      }

      public Throwable getError() {
        return response.getError();
      }

      public Request getRequest() {
        return response.getRequest();
      }

      public int getStatusCode() {
        return response.getStatusCode();
      }
    }

    static class ReadRequest extends OrgRequest implements ReadOrgRequest {

      final String path;

      ReadRequest(String organisationKey, RegistryService registry) {
        super(registry);
        path = String.format("/registry/organisation/%s.json", organisationKey);
      }

      /**
       * @see org.gbif.registry.api.client.RegistryService.RpcRequest#execute()
       */
      public ReadOrgResponse execute() {
        return new ReadResponse(this, registry.execute(this));
      }

      @Override
      public String getRequestPath() {
        return path;
      }
    }

    static class ReadResponse extends OrgResponse<ReadRequest> implements
        ReadOrgResponse {
      ReadResponse(ReadRequest request, Response response) {
        super(request, response);
      }

      public GbifOrganisation getResult() {
        String body = getBody();
        return OrgUtil.fromJson(body);
      }
    }

    static class UpdateRequest extends OrgRequest implements UpdateOrgRequest {

      final GbifOrganisation org;
      final String path;
      final ImmutableMap<String, String> payload;
      final String method;
      final Credentials credentials;

      UpdateRequest(GbifOrganisation org, RegistryService registry) {
        super(registry);
        this.org = org;
        path = String.format("/registry/organisation/%s", org.getKey());
        payload = OrgUtil.asImmutableMap(org);
        method = "POST";
        credentials = Credentials.with(org.getKey(), org.getPassword());
      }

      /**
       * @see org.gbif.registry.api.client.RegistryService.RpcRequest#execute()
       */
      public UpdateOrgResponse execute() {
        return new UpdateResponse(this, registry.execute(this));
      }

      @Override
      public Credentials getCredentials() {
        return credentials;
      }

      @Override
      public String getHttpMethodType() {
        return method;
      }

      @Override
      public ImmutableMap<String, String> getPayload() {
        return payload;
      }

      @Override
      public String getRequestPath() {
        return path;
      }
    }

    static class UpdateResponse extends OrgResponse<UpdateRequest> implements
        UpdateOrgResponse {
      UpdateResponse(UpdateRequest request, Response response) {
        super(request, response);
      }

      public GbifOrganisation getResult() {
        if (!getBody().contains(rpcRequest.org.getKey())) {
          return null;
        }
        return rpcRequest.org;
      }
    }

    private final RegistryService registry;

    OrgApi(RegistryService registry) {
      this.registry = registry;
    }

    /**
     * @see OrganisationApi#create(GbifOrganisation)
     */
    public CreateOrgRequest create(GbifOrganisation org) {
      checkNotNull(org);
      checkArgument(notNullOrEmpty(org.getName()));
      checkArgument(notNullOrEmpty(org.getPrimaryContactType()));
      checkArgument(notNullOrEmpty(org.getPrimaryContactEmail()));
      checkArgument(notNullOrEmpty(org.getNodeKey()));
      return new CreateRequest(org, registry);
    }

    /**
     * @see OrganisationApi#delete(GbifOrganisation)
     */
    public DeleteOrgRequest delete(GbifOrganisation org) {
      checkNotNull(org);
      checkArgument(notNullOrEmpty(org.getKey()));
      checkArgument(notNullOrEmpty(org.getPrimaryContactType()));
      checkArgument(notNullOrEmpty(org.getPassword()));
      return new DeleteRequest(org, registry);
    }

    /**
     * @see OrganisationApi#list()
     */
    public ListOrgRequest list() {
      return new ListRequest(registry);
    }

    /**
     * @see OrganisationApi#read(String)
     */
    public ReadOrgRequest read(String orgKey) {
      checkArgument(notNullOrEmpty(orgKey));
      return new ReadRequest(orgKey, registry);
    }

    /**
     * @see OrganisationApi#update(GbifOrganisation)
     */
    public UpdateOrgRequest update(GbifOrganisation org) {
      checkNotNull(org);
      checkArgument(notNullOrEmpty(org.getKey()));
      checkArgument(notNullOrEmpty(org.getPrimaryContactType()));
      checkArgument(notNullOrEmpty(org.getPassword()));
      return new UpdateRequest(org, registry);
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

  private static HttpMethod createMethod(String host, Request request) {
    String url = String.format("%s%s", host, request.getRequestPath());
    HttpMethod method;
    ImmutableMap<String, String> params = request.getRequestParams();
    if (params == null) {
      params = ImmutableMap.of();
    }
    ImmutableMap<String, String> payload = request.getPayload();
    if (payload == null) {
      payload = ImmutableMap.of();
    }
    String type = request.getHttpMethodType();
    if (type.equalsIgnoreCase("GET")) {
      GetMethod get = new GetMethod(url);
      get.setQueryString(createNameValuePairs(params));
      method = get;
    } else if (type.equalsIgnoreCase("POST")) {
      PostMethod post = new PostMethod(url);
      post.setDoAuthentication(true);
      post.addParameters(createNameValuePairs(payload));
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

  private static NameValuePair[] createNameValuePairs(
      ImmutableMap<String, String> map) {
    NameValuePair[] pairs = new NameValuePair[map.size()];
    int i = 0;
    for (String name : map.keySet()) {
      pairs[i++] = new NameValuePair(name, map.get(name));
    }
    return pairs;
  }

  private static Response createResponse(final Request request,
      final int status, final String body, final Throwable error) {
    return new Response() {

      public String getBody() {
        return body;
      }

      public Throwable getError() {
        return error;
      }

      public Request getRequest() {
        return request;
      }

      public int getStatusCode() {
        return status;
      }

    };
  }

  private static boolean notNullOrEmpty(String val) {
    return val != null && val.trim().length() > 0;
  }

  private static void setCredentials(HttpClient client, String host,
      Credentials credentials) {
    checkNotNull(credentials);
    checkNotNull(client);
    checkNotNull(host);
    log.info(String.format("Setting credentials: Host=%s, Credentials=%s",
        host, credentials));
    AuthScope scope = new AuthScope(host, -1, AuthScope.ANY_REALM);
    client.getState().setCredentials(
        scope,
        new UsernamePasswordCredentials(credentials.getId(),
            credentials.getPasswd()));
    client.getParams().setAuthenticationPreemptive(true);
  }

  private final String url;
  private final String host;
  private final HttpClient client;
  private final OrgApi orgApi;

  private GbifRegistry(String host) {
    this.host = host;
    url = String.format("http://%s", host);
    client = new HttpClient(new MultiThreadedHttpConnectionManager());
    orgApi = new OrgApi(this);
  }

  /**
   * @see org.gbif.registry.api.client.RegistryService#execute(org.gbif.registry.api.client.RegistryService.Request)
   */
  public Response execute(Request request) {
    checkNotNull(request, "Request is null");
    checkNotNull(request.getHttpMethodType(), "Method is null");
    checkArgument(request.getHttpMethodType().length() > 0, "Method is empty");
    HttpMethod method = createMethod(url, request);
    Throwable error = null;
    String body = null;
    int status = 0;
    try {
      Credentials cred = request.getCredentials();
      if (cred != null) {
        setCredentials(client, host, cred);
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
    return createResponse(request, status, body, error);
  }

  /**
   * 
   * @see org.gbif.registry.api.client.Registry#getOrganisationApi()
   */
  public OrganisationApi getOrganisationApi() {
    return orgApi;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("Host", host).toString();
  }
}
