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
import org.apache.commons.httpclient.HttpStatus;
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
public class GbifRegistry implements Gbrds {

  public static interface CreateOrgRequest extends
      RpcRequest<CreateOrgResponse, GbifOrganisation> {
  }
  public static interface CreateOrgResponse extends
      RpcResponse<GbifOrganisation> {
  }

  public static interface CreateResourceRequest extends
      RpcRequest<CreateResourceResponse, GbifResource> {
  }
  public static interface CreateResourceResponse extends
      RpcResponse<GbifResource> {
  }

  public static interface CreateServiceRequest extends
      RpcRequest<CreateServiceResponse, GbifService> {
  }

  public static interface CreateServiceResponse extends
      RpcResponse<GbifService> {
  }

  public static interface DeleteOrgRequest extends
      RpcRequest<DeleteOrgResponse, Boolean> {
  }

  public static interface DeleteOrgResponse extends RpcResponse<Boolean> {
  }

  public static interface DeleteResourceRequest extends
      RpcRequest<DeleteResourceResponse, Boolean> {
  }

  public static interface DeleteResourceResponse extends RpcResponse<Boolean> {
  }

  public static interface DeleteServiceRequest extends
      RpcRequest<DeleteServiceResponse, Boolean> {
  }

  public static interface DeleteServiceResponse extends RpcResponse<Boolean> {
  }

  public static interface ListExtensionsRequest extends
      RpcRequest<ListExtensionsResponse, List<GbifExtension>> {
  }

  public static interface ListExtensionsResponse extends
      RpcResponse<List<GbifExtension>> {
  }

  public static interface ListOrgRequest extends
      RpcRequest<ListOrgResponse, List<GbifOrganisation>> {
  }

  public static interface ListOrgResponse extends
      RpcResponse<List<GbifOrganisation>> {
  }

  public static interface ListResourceRequest extends
      RpcRequest<ListResourceResponse, List<GbifResource>> {
  }

  public static interface ListResourceResponse extends
      RpcResponse<List<GbifResource>> {
  }

  public static interface ListServicesForResourceRequest extends
      RpcRequest<ListServicesForResourceResponse, List<GbifService>> {
  }

  public static interface ListServicesForResourceResponse extends
      RpcResponse<List<GbifService>> {
  }

  public static interface ReadOrgRequest extends
      RpcRequest<ReadOrgResponse, GbifOrganisation> {
  }

  public static interface ReadOrgResponse extends RpcResponse<GbifOrganisation> {
  }

  public static interface ReadResourceRequest extends
      RpcRequest<ReadResourceResponse, GbifResource> {
  }

  public static interface ReadResourceResponse extends
      RpcResponse<GbifResource> {
  }

  public static interface ReadServiceRequest extends
      RpcRequest<ReadServiceResponse, GbifService> {
  }

  public static interface ReadServiceResponse extends RpcResponse<GbifService> {
  }

  public static interface UpdateOrgRequest extends
      RpcRequest<UpdateOrgResponse, GbifOrganisation> {
  }

  public static interface UpdateOrgResponse extends
      RpcResponse<GbifOrganisation> {
  }

  public static interface UpdateResourceRequest extends
      RpcRequest<UpdateResourceResponse, GbifResource> {
  }

  public static interface UpdateResourceResponse extends
      RpcResponse<GbifResource> {
  }

  public static interface UpdateServiceRequest extends
      RpcRequest<UpdateServiceResponse, GbifService> {
  }

  public static interface UpdateServiceResponse extends
      RpcResponse<GbifService> {
  }

  public static interface ValidateOrgCredentialsRequest extends
      RpcRequest<ValidateOrgCredentialsResponse, Boolean> {
  }

  public static interface ValidateOrgCredentialsResponse extends
      RpcResponse<Boolean> {
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
      return new Gson().fromJson(json, new TypeToken<List<GbifOrganisation>>() {
      }.getType());
    }
  }
  static class ResourceUtil {
    static ImmutableMap<String, String> asImmutableMap(GbifResource resource) {
      String json = new Gson().toJson(resource, GbifResource.class);
      Map<String, String> map = new Gson().fromJson(json,
          new TypeToken<Map<String, String>>() {
          }.getType());
      return ImmutableMap.copyOf(map);
    }

    static GbifResource fromJson(String json) {
      return new Gson().fromJson(json, GbifResource.class);
    }

    static GbifResource fromXml(String xml) {
      GbifResource resource = null;
      try {
        SAXParser p = SAXParserFactory.newInstance().newSAXParser();
        XmlHandler h = new XmlHandler();
        InputStream s = new ByteArrayInputStream(xml.getBytes());
        p.parse(s, h);
        resource = GbifResource.builder().key(h.key).organisationKey(
            h.organisationKey).build();
      } catch (ParserConfigurationException e) {
        e.printStackTrace();
      } catch (SAXException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
      return resource;
    }

    static List<GbifResource> listFromJson(String json) {
      return new Gson().fromJson(json, new TypeToken<List<GbifResource>>() {
      }.getType());
    }
  }

  static class ServiceUtil {
    private static ImmutableMap<String, String> asImmutableMap(
        GbifService service) {
      String json = new Gson().toJson(service, GbifService.class);
      Map<String, String> map = new Gson().fromJson(json,
          new TypeToken<Map<String, String>>() {
          }.getType());
      return ImmutableMap.copyOf(map);
    }

    private static GbifService fromJson(String json) {
      return new Gson().fromJson(json, GbifService.class);
    }

    private static GbifService fromXml(String xml) {
      GbifService service = null;
      try {
        SAXParser p = SAXParserFactory.newInstance().newSAXParser();
        XmlHandler h = new XmlHandler();
        InputStream s = new ByteArrayInputStream(xml.getBytes());
        p.parse(s, h);
        service = GbifService.builder().key(h.key).build();
      } catch (ParserConfigurationException e) {
        e.printStackTrace();
      } catch (SAXException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
      return service;
    }

    private static List<GbifService> listFromJson(String json) {
      return new Gson().fromJson(json, new TypeToken<List<GbifService>>() {
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

  private static class ExtensionApiImpl implements ExtensionApi {

    static abstract class ExtensionRequest implements Request {

      final Gbrds registry;

      ExtensionRequest(Gbrds registry) {
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

    static abstract class ExtensionResponse<T> implements Response {
      final Response response;
      final T rpcRequest;

      ExtensionResponse(T rpcRequest, Response response) {
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

      public int getStatus() {
        return response.getStatus();
      }
    }

    static class ListRequest implements ListExtensionsRequest {

      final Gbrds registry;

      ListRequest(Gbrds registry) {
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
        return "/registry/ipt/extensions.json";
      }
    }

    static class ListResponse extends ExtensionResponse<ListRequest> implements
        ListExtensionsResponse {

      private ListResponse(ListRequest request, Response response) {
        super(request, response);
      }

      public List<GbifExtension> getResult() {
        String body = getBody();
        if (body == null || body.length() < 1) {
          return null;
        }
        String json = getBody();

        // Modifies JSON to be a list instead of a map:
        json = json.replace("{\"extensions\":", "");
        json = json.substring(0, json.length() - 1);

        try {
          List<GbifExtension> results = new Gson().fromJson(json,
              new TypeToken<List<GbifExtension>>() {
              }.getType());
          return results;
        } catch (Exception e) {
          return null;
        }
      }
    }

    private final Gbrds registry;

    ExtensionApiImpl(Gbrds registry) {
      this.registry = registry;
    }

    /**
     * @see Gbrds.ExtensionApi#list()
     */
    public ListExtensionsRequest list() {
      return new ListRequest(registry);
    }
  }

  private static class OrganisationApiImpl implements OrganisationApi {

    static class CreateRequest extends OrgRequest implements CreateOrgRequest {

      final GbifOrganisation org;

      CreateRequest(GbifOrganisation org, Gbrds registry) {
        super(registry);
        this.org = org;
      }

      /**
       * @see org.gbif.registry.api.client.Gbrds.RpcRequest#execute()
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

      DeleteRequest(GbifOrganisation org, Gbrds registry) {
        super(registry);
        this.org = org;
        credentials = Credentials.with(org.getKey(), org.getPassword());
        method = "DELETE";
        path = String.format("/registry/organisation/%s", org.getKey());
        payload = OrgUtil.asImmutableMap(org);
      }

      /**
       * @see org.gbif.registry.api.client.Gbrds.RpcRequest#execute()
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
       * @see org.gbif.registry.api.client.Gbrds.RpcResponse#getResult()
       */
      public Boolean getResult() {
        return getBody().contains("Organisation deleted successfully");
      }
    }

    static class ListRequest implements ListOrgRequest {

      final Gbrds registry;

      ListRequest(Gbrds registry) {
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

      final Gbrds registry;

      OrgRequest(Gbrds registry) {
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

      public int getStatus() {
        return response.getStatus();
      }
    }

    static class ReadRequest extends OrgRequest implements ReadOrgRequest {

      final String path;

      ReadRequest(String organisationKey, Gbrds registry) {
        super(registry);
        path = String.format("/registry/organisation/%s.json", organisationKey);
      }

      /**
       * @see org.gbif.registry.api.client.Gbrds.RpcRequest#execute()
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
        if (body.contains("Error")) {
          return null;
        }
        return OrgUtil.fromJson(body);
      }
    }

    static class UpdateRequest extends OrgRequest implements UpdateOrgRequest {

      final GbifOrganisation org;
      final String path;
      final ImmutableMap<String, String> payload;
      final String method;
      final Credentials credentials;

      UpdateRequest(GbifOrganisation org, Gbrds registry) {
        super(registry);
        this.org = org;
        path = String.format("/registry/organisation/%s", org.getKey());
        payload = OrgUtil.asImmutableMap(org);
        method = "POST";
        credentials = Credentials.with(org.getKey(), org.getPassword());
      }

      /**
       * @see org.gbif.registry.api.client.Gbrds.RpcRequest#execute()
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
        int status = getStatus();
        if (status == HttpStatus.SC_UNAUTHORIZED) {
          return null;
        }
        if (!getBody().contains(rpcRequest.org.getKey())) {
          return null;
        }
        return OrgUtil.fromXml(getBody());
      }
    }

    static class ValidateRequest extends OrgRequest implements
        ValidateOrgCredentialsRequest {

      final GbifOrganisation org;
      final String path;
      final ImmutableMap<String, String> payload;
      final Credentials credentials;
      final ImmutableMap<String, String> requestParams;

      ValidateRequest(GbifOrganisation org, Gbrds registry) {
        super(registry);
        this.org = org;
        path = String.format("/registry/organisation/%s", org.getKey());
        payload = OrgUtil.asImmutableMap(org);
        requestParams = ImmutableMap.of("op", "login");
        credentials = Credentials.with(org.getKey(), org.getPassword());
      }

      /**
       * @see org.gbif.registry.api.client.Gbrds.RpcRequest#execute()
       */
      public ValidateOrgCredentialsResponse execute() {
        return new ValidateResponse(this, registry.execute(this));
      }

      @Override
      public Credentials getCredentials() {
        return credentials;
      }

      @Override
      public ImmutableMap<String, String> getPayload() {
        return payload;
      }

      @Override
      public ImmutableMap<String, String> getRequestParams() {
        return requestParams;
      }

      @Override
      public String getRequestPath() {
        return path;
      }
    }

    static class ValidateResponse extends OrgResponse<ValidateRequest>
        implements ValidateOrgCredentialsResponse {
      ValidateResponse(ValidateRequest request, Response response) {
        super(request, response);
      }

      public Boolean getResult() {
        String body = getBody();
        int status = getStatus();
        if (status == HttpStatus.SC_UNAUTHORIZED
            || status == HttpStatus.SC_NOT_FOUND
            || body.contains("No organisation matches the key provided")
            || body.contains("Incorrect Authorization information")) {
          return false;
        }
        return true;
      }
    }

    private final Gbrds registry;

    OrganisationApiImpl(Gbrds registry) {
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

    /**
     * @see OrganisationApi#validateCredentials (String, Credentials)
     */
    public ValidateOrgCredentialsRequest validateCredentials(
        String organisationKey, Credentials credentials) {
      checkNotNull(organisationKey);
      checkArgument(notNullOrEmpty(organisationKey));
      checkNotNull(credentials);
      return new ValidateRequest(GbifOrganisation.builder().key(
          credentials.getId()).password(credentials.getPasswd()).build(),
          registry);
    }
  }

  private static class ResourceApiImpl implements ResourceApi {

    static class CreateRequest extends ResourceRequest implements
        CreateResourceRequest {

      final GbifResource resource;
      final Credentials credentials;

      CreateRequest(GbifResource resource, Gbrds registry) {
        super(registry);
        this.resource = resource;
        credentials = Credentials.with(resource.getOrganisationKey(),
            resource.getOrganisationPassword());
      }

      /**
       * @see Gbrds.gbif.registry.api.client.RegistryService.RpcRequest#execute()
       */
      public CreateResourceResponse execute() {
        return new CreateResponse(this, registry.execute(this));
      }

      @Override
      public Credentials getCredentials() {
        return credentials;
      }

      @Override
      public String getHttpMethodType() {
        return "POST";
      }

      @Override
      public ImmutableMap<String, String> getPayload() {
        return ResourceUtil.asImmutableMap(resource);
      }

      @Override
      public String getRequestPath() {
        return "/registry/resource";
      }
    }

    static class CreateResponse extends ResourceResponse<CreateRequest>
        implements CreateResourceResponse {

      CreateResponse(CreateRequest request, Response response) {
        super(request, response);
      }

      public GbifResource getResult() {
        String body = getBody();
        if (!body.contains("<organisationKey>")) {
          return null;
        }
        return ResourceUtil.fromXml(body);
      }

    }

    static class DeleteRequest extends ResourceRequest implements
        DeleteResourceRequest {

      final GbifResource resource;
      final Credentials credentials;
      final String method;
      final String path;
      final ImmutableMap<String, String> payload;

      DeleteRequest(GbifResource resource, Gbrds registry) {
        super(registry);
        this.resource = resource;
        credentials = Credentials.with(resource.getOrganisationKey(),
            resource.getOrganisationPassword());
        method = "DELETE";
        path = String.format("/registry/resource/%s", resource.getKey());
        payload = ResourceUtil.asImmutableMap(resource);
      }

      /**
       * @see Gbrds.gbif.registry.api.client.RegistryService.RpcRequest#execute()
       */
      public DeleteResourceResponse execute() {
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

    static class DeleteResponse extends ResourceResponse<DeleteRequest>
        implements DeleteResourceResponse {

      DeleteResponse(DeleteRequest request, Response response) {
        super(request, response);
      }

      /**
       * @see Gbrds.gbif.registry.api.client.RegistryService.RpcResponse#getResult()
       */
      public Boolean getResult() {
        return getBody().contains("Resourceanisation deleted successfully");
      }
    }

    static class ListRequest implements ListResourceRequest {

      final Gbrds registry;

      ListRequest(Gbrds registry) {
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
        return "/registry/resource.json";
      }
    }

    static class ListResponse extends ResourceResponse<ListRequest> implements
        ListResourceResponse {

      private ListResponse(ListRequest request, Response response) {
        super(request, response);
      }

      public List<GbifResource> getResult() {
        String body = getBody();
        if (body == null || body.length() < 1) {
          return null;
        }
        return ResourceUtil.listFromJson(body);
      }
    }

    static class ReadRequest extends ResourceRequest implements
        ReadResourceRequest {

      final String path;

      ReadRequest(String organisationKey, Gbrds registry) {
        super(registry);
        path = String.format("/registry/resource/%s.json", organisationKey);
      }

      /**
       * @see Gbrds.gbif.registry.api.client.RegistryService.RpcRequest#execute()
       */
      public ReadResourceResponse execute() {
        return new ReadResponse(this, registry.execute(this));
      }

      @Override
      public String getRequestPath() {
        return path;
      }
    }

    static class ReadResponse extends ResourceResponse<ReadRequest> implements
        ReadResourceResponse {
      ReadResponse(ReadRequest request, Response response) {
        super(request, response);
      }

      public GbifResource getResult() {
        String body = getBody();
        if (body.contains("Error")) {
          return null;
        }
        return ResourceUtil.fromJson(body);
      }
    }

    static abstract class ResourceRequest implements Request {

      final Gbrds registry;

      ResourceRequest(Gbrds registry) {
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

    static abstract class ResourceResponse<T> implements Response {
      final Response response;
      final T rpcRequest;

      ResourceResponse(T rpcRequest, Response response) {
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

      public int getStatus() {
        return response.getStatus();
      }
    }

    static class UpdateRequest extends ResourceRequest implements
        UpdateResourceRequest {

      final GbifResource resource;
      final String path;
      final ImmutableMap<String, String> payload;
      final String method;
      final Credentials credentials;

      UpdateRequest(GbifResource resource, Gbrds registry) {
        super(registry);
        this.resource = resource;
        path = String.format("/registry/resource/%s", resource.getKey());
        payload = ResourceUtil.asImmutableMap(resource);
        method = "POST";
        credentials = Credentials.with(resource.getOrganisationKey(),
            resource.getOrganisationPassword());
      }

      /**
       * @see Gbrds.gbif.registry.api.client.RegistryService.RpcRequest#execute()
       */
      public UpdateResourceResponse execute() {
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

    static class UpdateResponse extends ResourceResponse<UpdateRequest>
        implements UpdateResourceResponse {
      UpdateResponse(UpdateRequest request, Response response) {
        super(request, response);
      }

      public GbifResource getResult() {
        if (!getBody().contains(rpcRequest.resource.getKey())) {
          return null;
        }
        return rpcRequest.resource;
      }
    }

    private final Gbrds registry;

    ResourceApiImpl(Gbrds registry) {
      this.registry = registry;
    }

    /**
     * @see ResourceanisationApi#create(GbifResource)
     */
    public CreateResourceRequest create(GbifResource resource) {
      checkNotNull(resource);
      checkArgument(notNullOrEmpty(resource.getName()));
      checkArgument(notNullOrEmpty(resource.getDescription()));
      checkArgument(notNullOrEmpty(resource.getPrimaryContactType()));
      checkArgument(notNullOrEmpty(resource.getPrimaryContactEmail()));
      checkArgument(notNullOrEmpty(resource.getOrganisationKey()));
      return new CreateRequest(resource, registry);
    }

    /**
     * @see ResourceanisationApi#delete(GbifResource)
     */
    public DeleteResourceRequest delete(GbifResource resource) {
      checkNotNull(resource);
      checkArgument(notNullOrEmpty(resource.getKey()));
      checkArgument(notNullOrEmpty(resource.getOrganisationPassword()));
      checkArgument(notNullOrEmpty(resource.getOrganisationKey()));
      return new DeleteRequest(resource, registry);
    }

    /**
     * @see ResourceanisationApi#list(String)
     */
    public ListResourceRequest list() {
      return new ListRequest(registry);
    }

    /**
     * @see ResourceanisationApi#read(String)
     */
    public ReadResourceRequest read(String orgKey) {
      checkArgument(notNullOrEmpty(orgKey));
      return new ReadRequest(orgKey, registry);
    }

    /**
     * @see ResourceanisationApi#update(GbifResource)
     */
    public UpdateResourceRequest update(GbifResource resource) {
      checkNotNull(resource);
      checkArgument(notNullOrEmpty(resource.getKey()));
      checkArgument(notNullOrEmpty(resource.getPrimaryContactType()));
      checkArgument(notNullOrEmpty(resource.getOrganisationPassword()));
      return new UpdateRequest(resource, registry);
    }
  }

  private static class ServiceApiImpl implements ServiceApi {
    static class CreateRequest extends ServiceRequest implements
        CreateServiceRequest {

      final GbifService service;
      final Credentials credentials;

      CreateRequest(GbifService service, Gbrds registry) {
        super(registry);
        this.service = service;
        credentials = Credentials.with(service.getOrganisationKey(),
            service.getResourcePassword());
      }

      /**
       * @see Gbrds.gbif.registry.api.client.RegistryService.RpcRequest#execute()
       */
      public CreateServiceResponse execute() {
        return new CreateResponse(this, registry.execute(this));
      }

      @Override
      public Credentials getCredentials() {
        return credentials;
      }

      @Override
      public String getHttpMethodType() {
        return "POST";
      }

      @Override
      public ImmutableMap<String, String> getPayload() {
        return ServiceUtil.asImmutableMap(service);
      }

      @Override
      public String getRequestPath() {
        return "/registry/service";
      }
    }

    static class CreateResponse extends ServiceResponse<CreateRequest>
        implements CreateServiceResponse {

      CreateResponse(CreateRequest request, Response response) {
        super(request, response);
      }

      public GbifService getResult() {
        String body = getBody();
        if (!body.contains("<key>")) {
          return null;
        }
        return ServiceUtil.fromXml(body);
      }

    }

    static class DeleteRequest extends ServiceRequest implements
        DeleteServiceRequest {

      final GbifService service;
      final Credentials credentials;
      final String method;
      final String path;
      final ImmutableMap<String, String> payload;

      DeleteRequest(GbifService service, Gbrds registry) {
        super(registry);
        this.service = service;
        credentials = Credentials.with(service.getOrganisationKey(),
            service.getResourcePassword());
        method = "DELETE";
        path = String.format("/registry/service/%s", service.getKey());
        payload = ServiceUtil.asImmutableMap(service);
      }

      /**
       * @see Gbrds.gbif.registry.api.client.RegistryService.RpcRequest#execute()
       */
      public DeleteServiceResponse execute() {
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

    static class DeleteResponse extends ServiceResponse<DeleteRequest>
        implements DeleteServiceResponse {

      DeleteResponse(DeleteRequest request, Response response) {
        super(request, response);
      }

      /**
       * @see Gbrds.gbif.registry.api.client.RegistryService.RpcResponse#getResult()
       */
      public Boolean getResult() {
        return getStatus() == HttpStatus.SC_OK;
      }
    }

    static class ListForResourceRequest implements
        ListServicesForResourceRequest {

      final Gbrds registry;
      private String resourceKey;
      private String method;
      private Credentials credentials;
      private ImmutableMap<String, String> requestParams;
      private ImmutableMap<String, String> payload;
      private String path;

      ListForResourceRequest(String resourceKey, Gbrds registry) {
        this.registry = registry;
        this.resourceKey = resourceKey;
        method = "GET";
        credentials = null;
        payload = ImmutableMap.of();
        requestParams = ImmutableMap.of("resourceKey", resourceKey);
        path = "/registry/service";
      }

      public ListForResourceResponse execute() {
        return new ListForResourceResponse(this, registry.execute(this));
      }

      public Credentials getCredentials() {
        return credentials;
      }

      public String getHttpMethodType() {
        return method;
      }

      public ImmutableMap<String, String> getPayload() {
        return payload;
      }

      public ImmutableMap<String, String> getRequestParams() {
        return requestParams;
      }

      public String getRequestPath() {
        return path;
      }
    }

    static class ListForResourceResponse extends
        ServiceResponse<ListForResourceRequest> implements
        ListServicesForResourceResponse {

      private ListForResourceResponse(ListForResourceRequest request,
          Response response) {
        super(request, response);
      }

      public List<GbifService> getResult() {
        String body = getBody();
        if (body == null || body.length() < 1) {
          return null;
        }
        return ServiceUtil.listFromJson(body);
      }
    }

    static class ReadRequest extends ServiceRequest implements
        ReadServiceRequest {

      final String path;

      ReadRequest(String serviceKey, Gbrds registry) {
        super(registry);
        path = String.format("/registry/service/%s.json", serviceKey);
      }

      /**
       * @see Gbrds.gbif.registry.api.client.RegistryService.RpcRequest#execute()
       */
      public ReadServiceResponse execute() {
        return new ReadResponse(this, registry.execute(this));
      }

      @Override
      public String getRequestPath() {
        return path;
      }
    }

    static class ReadResponse extends ServiceResponse<ReadRequest> implements
        ReadServiceResponse {
      ReadResponse(ReadRequest request, Response response) {
        super(request, response);
      }

      public GbifService getResult() {
        String body = getBody();
        if (body.contains("Error")) {
          return null;
        }
        return ServiceUtil.fromJson(body);
      }
    }

    static abstract class ServiceRequest implements Request {

      final Gbrds registry;

      ServiceRequest(Gbrds registry) {
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

    static abstract class ServiceResponse<T> implements Response {
      final Response response;
      final T rpcRequest;

      ServiceResponse(T rpcRequest, Response response) {
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

      public int getStatus() {
        return response.getStatus();
      }
    }

    static class UpdateRequest extends ServiceRequest implements
        UpdateServiceRequest {

      final GbifService service;
      final String path;
      final ImmutableMap<String, String> payload;
      final String method;
      final Credentials credentials;

      UpdateRequest(GbifService service, Gbrds registry) {
        super(registry);
        this.service = service;
        path = String.format("/registry/service/%s", service.getKey());
        payload = ServiceUtil.asImmutableMap(service);
        method = "POST";
        credentials = Credentials.with(service.getOrganisationKey(),
            service.getResourcePassword());
      }

      /**
       * @see Gbrds.gbif.registry.api.client.RegistryService.RpcRequest#execute()
       */
      public UpdateServiceResponse execute() {
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

    static class UpdateResponse extends ServiceResponse<UpdateRequest>
        implements UpdateServiceResponse {
      UpdateResponse(UpdateRequest request, Response response) {
        super(request, response);
      }

      public GbifService getResult() {
        if (!getBody().contains(rpcRequest.service.getKey())) {
          return null;
        }
        return rpcRequest.service;
      }
    }

    private final Gbrds registry;

    ServiceApiImpl(Gbrds registry) {
      this.registry = registry;
    }

    /**
     * @see ServiceanisationApi#create(GbifService)
     */
    public CreateServiceRequest create(GbifService service) {
      checkNotNull(service);
      checkArgument(notNullOrEmpty(service.getType()));
      checkArgument(notNullOrEmpty(service.getAccessPointURL()));
      checkArgument(notNullOrEmpty(service.getResourceKey()));
      return new CreateRequest(service, registry);
    }

    /**
     * @see ServiceanisationApi#delete(GbifService)
     */
    public DeleteServiceRequest delete(GbifService service) {
      checkNotNull(service);
      checkArgument(notNullOrEmpty(service.getKey()));
      checkArgument(notNullOrEmpty(service.getResourcePassword()));
      checkArgument(notNullOrEmpty(service.getOrganisationKey()));
      return new DeleteRequest(service, registry);
    }

    /**
     * @see ServiceanisationApi#list(String)
     */
    public ListServicesForResourceRequest list(String resourceKey) {
      return new ListForResourceRequest(resourceKey, registry);
    }

    /**
     * @see ServiceanisationApi#read(String)
     */
    public ReadServiceRequest read(String orgKey) {
      checkArgument(notNullOrEmpty(orgKey));
      return new ReadRequest(orgKey, registry);
    }

    /**
     * @see ServiceanisationApi#update(GbifService)
     */
    public UpdateServiceRequest update(GbifService service) {
      checkNotNull(service);
      checkArgument(notNullOrEmpty(service.getKey()));
      checkArgument(notNullOrEmpty(service.getResourcePassword()));
      return new UpdateRequest(service, registry);
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

      public int getStatus() {
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
  private final OrganisationApiImpl orgApi;
  private final ResourceApiImpl resourceApi;
  private final ServiceApiImpl serviceApi;
  private final ExtensionApiImpl extensionApi;

  private GbifRegistry(String host) {
    this.host = host;
    url = String.format("http://%s", host);
    client = new HttpClient(new MultiThreadedHttpConnectionManager());
    orgApi = new OrganisationApiImpl(this);
    resourceApi = new ResourceApiImpl(this);
    serviceApi = new ServiceApiImpl(this);
    extensionApi = new ExtensionApiImpl(this);
  }

  /**
   * @see org.gbif.registry.api.client.Gbrds#execute(org.gbif.registry.api.client.Gbrds.Request)
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
   * @see Gbrds#getExtensionApi()
   */
  public ExtensionApi getExtensionApi() {
    return extensionApi;
  }

  /**
   * @see Gbrds#getOrganisationApi()
   */
  public OrganisationApi getOrganisationApi() {
    return orgApi;
  }

  /**
   * @see Gbrds#getResourceApi()
   */
  public ResourceApi getResourceApi() {
    return resourceApi;
  }

  /**
   * @see Gbrds#getServiceApi()
   */
  public ServiceApi getServiceApi() {
    return serviceApi;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("Host", host).toString();
  }
}
