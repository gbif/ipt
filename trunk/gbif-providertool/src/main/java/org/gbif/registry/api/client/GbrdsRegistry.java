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
import com.google.common.collect.Lists;
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
public class GbrdsRegistry implements Gbrds {

  public static interface CreateOrgRequest extends
      RpcRequest<CreateOrgResponse, OrgCredentials> {
  }
  public static interface CreateOrgResponse extends RpcResponse<OrgCredentials> {
  }

  public static interface CreateResourceRequest extends
      AuthRpcRequest<CreateResourceResponse, GbrdsResource> {
  }
  public static interface CreateResourceResponse extends
      RpcResponse<GbrdsResource> {
  }

  /**
   * Interface for creating GBRDS services using an {@link AuthRpcRequest}.
   */
  public static interface CreateServiceRequest extends
      AuthRpcRequest<CreateServiceResponse, GbrdsService> {
  }

  public static interface CreateServiceResponse extends
      RpcResponse<GbrdsService> {
  }

  public static interface DeleteOrgRequest extends
      AuthRpcRequest<DeleteOrgResponse, Boolean> {
  }

  public static interface DeleteOrgResponse extends RpcResponse<Boolean> {
  }

  public static interface DeleteResourceRequest extends
      AuthRpcRequest<DeleteResourceResponse, Boolean> {
  }

  public static interface DeleteResourceResponse extends RpcResponse<Boolean> {
  }

  /**
   * Interface for deleting GBRDS services using an {@link AuthRpcRequest}.
   */
  public static interface DeleteServiceRequest extends
      AuthRpcRequest<DeleteServiceResponse, Boolean> {
  }

  public static interface DeleteServiceResponse extends RpcResponse<Boolean> {
  }

  public static interface ListExtensionsRequest extends
      RpcRequest<ListExtensionsResponse, List<GbrdsExtension>> {
  }

  public static interface ListExtensionsResponse extends
      RpcResponse<List<GbrdsExtension>> {
  }

  public static interface ListOrgRequest extends
      RpcRequest<ListOrgResponse, List<GbrdsOrganisation>> {
  }

  public static interface ListOrgResponse extends
      RpcResponse<List<GbrdsOrganisation>> {
  }

  public static interface ListResourceRequest extends
      RpcRequest<ListResourceResponse, List<GbrdsResource>> {
  }

  public static interface ListResourceResponse extends
      RpcResponse<List<GbrdsResource>> {
  }

  public static interface ListServicesRequest extends
      RpcRequest<ListServicesResponse, List<GbrdsService>> {
  }

  public static interface ListServicesResponse extends
      RpcResponse<List<GbrdsService>> {
  }

  public static interface ListThesauriRequest extends
      RpcRequest<ListThesauriResponse, List<GbrdsThesaurus>> {
  }

  public static interface ListThesauriResponse extends
      RpcResponse<List<GbrdsThesaurus>> {
  }

  public static interface ReadOrgRequest extends
      RpcRequest<ReadOrgResponse, GbrdsOrganisation> {
  }

  public static interface ReadOrgResponse extends
      RpcResponse<GbrdsOrganisation> {
  }

  public static interface ReadResourceRequest extends
      RpcRequest<ReadResourceResponse, GbrdsResource> {
  }

  public static interface ReadResourceResponse extends
      RpcResponse<GbrdsResource> {
  }

  public static interface ReadServiceRequest extends
      RpcRequest<ReadServiceResponse, GbrdsService> {
  }

  public static interface ReadServiceResponse extends RpcResponse<GbrdsService> {
  }

  public static interface UpdateOrgRequest extends
      AuthRpcRequest<UpdateOrgResponse, Boolean> {
  }

  public static interface UpdateOrgResponse extends RpcResponse<Boolean> {
  }

  public static interface UpdateResourceRequest extends
      AuthRpcRequest<UpdateResourceResponse, Boolean> {
  }

  public static interface UpdateResourceResponse extends RpcResponse<Boolean> {
  }

  /**
   * Interface for updating GBRDS services using an {@link AuthRpcRequest}.
   */
  public static interface UpdateServiceRequest extends
      AuthRpcRequest<UpdateServiceResponse, Boolean> {
  }

  public static interface UpdateServiceResponse extends RpcResponse<Boolean> {
  }

  public static interface ValidateOrgCredentialsRequest extends
      RpcRequest<ValidateOrgCredentialsResponse, Boolean> {
  }

  public static interface ValidateOrgCredentialsResponse extends
      RpcResponse<Boolean> {
  }

  static class OrgUtil {
    private static ImmutableMap<String, String> asImmutableMap(
        GbrdsOrganisation org) {
      String json = new Gson().toJson(org, GbrdsOrganisation.class);
      Map<String, String> map = new Gson().fromJson(json,
          new TypeToken<Map<String, String>>() {
          }.getType());
      return ImmutableMap.copyOf(map);
    }

    private static GbrdsOrganisation fromJson(String json) {
      return new Gson().fromJson(json, GbrdsOrganisation.class);
    }

    private static GbrdsOrganisation fromXml(String xml) {
      GbrdsOrganisation org = null;
      try {
        SAXParser p = SAXParserFactory.newInstance().newSAXParser();
        XmlHandler h = new XmlHandler();
        InputStream s = new ByteArrayInputStream(xml.getBytes());
        p.parse(s, h);
        org = GbrdsOrganisation.builder().key(h.organisationKey).password(
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

    private static List<GbrdsOrganisation> listFromJson(String json) {
      return new Gson().fromJson(json,
          new TypeToken<List<GbrdsOrganisation>>() {
          }.getType());
    }
  }
  static class ResourceUtil {
    static ImmutableMap<String, String> asImmutableMap(GbrdsResource resource) {
      String json = new Gson().toJson(resource, GbrdsResource.class);
      Map<String, String> map = new Gson().fromJson(json,
          new TypeToken<Map<String, String>>() {
          }.getType());
      return ImmutableMap.copyOf(map);
    }

    static GbrdsResource fromJson(String json) {
      return new Gson().fromJson(json, GbrdsResource.class);
    }

    static GbrdsResource fromXml(String xml) {
      GbrdsResource resource = null;
      try {
        SAXParser p = SAXParserFactory.newInstance().newSAXParser();
        XmlHandler h = new XmlHandler();
        InputStream s = new ByteArrayInputStream(xml.getBytes());
        p.parse(s, h);
        resource = GbrdsResource.builder().key(h.key).organisationKey(
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

    static List<GbrdsResource> listFromJson(String json) {
      return new Gson().fromJson(json, new TypeToken<List<GbrdsResource>>() {
      }.getType());
    }
  }

  static class ServiceUtil {

    static ImmutableMap<String, String> asImmutableMap(GbrdsService service) {
      String json = new Gson().toJson(service, GbrdsService.class);
      Map<String, String> map = new Gson().fromJson(json,
          new TypeToken<Map<String, String>>() {
          }.getType());
      return ImmutableMap.copyOf(map);
    }

    static GbrdsService fromJson(String json) {
      return new Gson().fromJson(json, GbrdsService.class);
    }

    static GbrdsService fromXml(String xml) {
      GbrdsService service = null;
      try {
        SAXParser p = SAXParserFactory.newInstance().newSAXParser();
        XmlHandler h = new XmlHandler();
        InputStream s = new ByteArrayInputStream(xml.getBytes());
        p.parse(s, h);
        service = GbrdsService.builder().key(h.key).build();
      } catch (ParserConfigurationException e) {
        e.printStackTrace();
      } catch (SAXException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
      return service;
    }

    static List<GbrdsService> listFromJson(String json) {
      return new Gson().fromJson(json, new TypeToken<List<GbrdsService>>() {
      }.getType());
    }

    static List<GbrdsService> listFromXml(String body) {
      List<GbrdsService> list = Lists.newArrayList();
      try {
        SAXParser p = SAXParserFactory.newInstance().newSAXParser();
        XmlHandler h = new XmlHandler();
        InputStream s = new ByteArrayInputStream(body.getBytes());
        p.parse(s, h);
        int keyCount = h.keys.size();
        for (int i = 0; i < keyCount; i++) {
          list.add(GbrdsService.builder().key(h.keys.get(i)).type(
              h.types.get(i)).build());
        }
      } catch (ParserConfigurationException e) {
        e.printStackTrace();
      } catch (SAXException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
      return list;
    }
  }

  static class XmlHandler extends DefaultHandler {
    String content;
    String organisationKey;
    String resourceKey;
    String serviceKey;
    String password;
    String key;
    List<String> keys = Lists.newArrayList();
    List<String> types = Lists.newArrayList();

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
        keys.add(key);
      } else if (name.equalsIgnoreCase("type")) {
        types.add(content.replaceAll("\\s", ""));
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

  private static class IptApiImpl implements IptApi {

    static abstract class IptRequest implements Request {

      final Gbrds registry;

      IptRequest(Gbrds registry) {
        this.registry = registry;
      }

      public OrgCredentials getCredentials() {
        return null;
      }

      public String getMethod() {
        return "GET";
      }

      public ImmutableMap<String, String> getParams() {
        return ImmutableMap.of();
      }

      public String getPath() {
        throw new UnsupportedOperationException("Subclass must override.");
      }

      public ImmutableMap<String, String> getPayload() {
        return ImmutableMap.of();
      }
    }

    static abstract class IptResponse<T> implements Response {
      final Response response;
      final T rpcRequest;

      IptResponse(T rpcRequest, Response response) {
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

    static class ListExtRequest implements ListExtensionsRequest {

      final Gbrds registry;

      ListExtRequest(Gbrds registry) {
        this.registry = registry;
      }

      public ListExtResponse execute() {
        return new ListExtResponse(this, registry.execute(this));
      }

      public OrgCredentials getCredentials() {
        return null;
      }

      public String getMethod() {
        return "GET";
      }

      public ImmutableMap<String, String> getParams() {
        return ImmutableMap.of();
      }

      public String getPath() {
        return "/registry/ipt/extensions.json";
      }

      public ImmutableMap<String, String> getPayload() {
        return ImmutableMap.of();
      }
    }

    static class ListExtResponse extends IptResponse<ListExtRequest> implements
        ListExtensionsResponse {

      private ListExtResponse(ListExtRequest request, Response response) {
        super(request, response);
      }

      public List<GbrdsExtension> getResult() {
        String body = getBody();
        if (body == null || body.length() < 1) {
          return null;
        }
        String json = getBody();

        // Modifies JSON to be a list instead of a map:
        json = json.replace("{\"extensions\":", "");
        json = json.substring(0, json.length() - 1);

        try {
          List<GbrdsExtension> results = new Gson().fromJson(json,
              new TypeToken<List<GbrdsExtension>>() {
              }.getType());
          return results;
        } catch (Exception e) {
          return null;
        }
      }
    }

    static class ListThesaurusRequest implements ListThesauriRequest {

      final Gbrds registry;

      ListThesaurusRequest(Gbrds registry) {
        this.registry = registry;
      }

      public ListThesaurusResponse execute() {
        return new ListThesaurusResponse(this, registry.execute(this));
      }

      public OrgCredentials getCredentials() {
        return null;
      }

      public String getMethod() {
        return "GET";
      }

      public ImmutableMap<String, String> getParams() {
        return ImmutableMap.of();
      }

      public String getPath() {
        return "/registry/ipt/thesauri.json";
      }

      public ImmutableMap<String, String> getPayload() {
        return ImmutableMap.of();
      }
    }

    static class ListThesaurusResponse extends
        IptResponse<ListThesaurusRequest> implements ListThesauriResponse {

      private ListThesaurusResponse(ListThesaurusRequest request,
          Response response) {
        super(request, response);
      }

      public List<GbrdsThesaurus> getResult() {
        String body = getBody();
        if (body == null || body.length() < 1) {
          return null;
        }
        String json = getBody();

        // Modifies JSON to be a list instead of a map:
        json = json.replace("{\"thesauri\":", "");
        json = json.substring(0, json.length() - 1);

        try {
          List<GbrdsThesaurus> results = new Gson().fromJson(json,
              new TypeToken<List<GbrdsThesaurus>>() {
              }.getType());
          return results;
        } catch (Exception e) {
          return null;
        }
      }
    }

    private final Gbrds registry;

    IptApiImpl(Gbrds registry) {
      this.registry = registry;
    }

    /**
     * @see Gbrds.IptApi#listExtensions()
     */
    public ListExtensionsRequest listExtensions() {
      return new ListExtRequest(registry);
    }

    /**
     * @see Gbrds.IptApi#listThesauri()
     */
    public ListThesauriRequest listThesauri() {
      return new ListThesaurusRequest(registry);
    }
  }

  private static class OrganisationApiImpl implements OrganisationApi {

    static class CreateRequest extends OrgRequest implements CreateOrgRequest {

      final GbrdsOrganisation org;

      CreateRequest(GbrdsOrganisation org, Gbrds registry) {
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
      public String getMethod() {
        return "POST";
      }

      @Override
      public String getPath() {
        return "/registry/organisation";
      }

      @Override
      public ImmutableMap<String, String> getPayload() {
        return OrgUtil.asImmutableMap(org);
      }
    }

    static class CreateResponse extends OrgResponse<CreateRequest> implements
        CreateOrgResponse {

      CreateResponse(CreateRequest request, Response response) {
        super(request, response);
      }

      public OrgCredentials getResult() {
        if (getStatus() != HttpStatus.SC_CREATED) {
          return null;
        }
        GbrdsOrganisation o = OrgUtil.fromXml(getBody());
        try {
          return OrgCredentials.with(o.getKey(), o.getPassword());
        } catch (Exception e) {
          e.printStackTrace();
          return null;
        }
      }

    }

    static class DeleteRequest extends OrgRequest implements DeleteOrgRequest {

      final String organisationKey;
      OrgCredentials creds;
      final String method;
      final String path;

      DeleteRequest(String organisationKey, Gbrds registry) {
        super(registry);
        this.organisationKey = organisationKey;
        method = "DELETE";
        path = String.format("/registry/organisation/%s", organisationKey);
      }

      public DeleteOrgResponse execute(OrgCredentials creds)
          throws BadCredentialsException {
        checkNotNull(creds, "Credentials are null");
        this.creds = creds;
        Response response = registry.execute(this);
        if (response.getStatus() == HttpStatus.SC_UNAUTHORIZED) {
          throw new BadCredentialsException("Unauthorized: " + creds);
        }
        return new DeleteResponse(this, response);
      }

      @Override
      public OrgCredentials getCredentials() {
        return creds;
      }

      @Override
      public String getMethod() {
        return method;
      }

      @Override
      public String getPath() {
        return path;
      }
    }

    static class DeleteResponse extends OrgResponse<DeleteRequest> implements
        DeleteOrgResponse {

      DeleteResponse(DeleteRequest request, Response response) {
        super(request, response);
      }

      public Boolean getResult() {
        return getStatus() == HttpStatus.SC_OK;
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

      public OrgCredentials getCredentials() {
        return null;
      }

      public String getMethod() {
        return "GET";
      }

      public ImmutableMap<String, String> getParams() {
        return ImmutableMap.of();
      }

      public String getPath() {
        return "/registry/organisation.json";
      }

      public ImmutableMap<String, String> getPayload() {
        return ImmutableMap.of();
      }
    }

    static class ListResponse extends OrgResponse<ListRequest> implements
        ListOrgResponse {

      private ListResponse(ListRequest request, Response response) {
        super(request, response);
      }

      public List<GbrdsOrganisation> getResult() {
        if (getStatus() != HttpStatus.SC_OK) {
          return null;
        }
        return OrgUtil.listFromJson(getBody());
      }
    }

    static abstract class OrgRequest implements Request {

      final Gbrds registry;

      OrgRequest(Gbrds registry) {
        this.registry = registry;
      }

      public OrgCredentials getCredentials() {
        return null;
      }

      public String getMethod() {
        return "GET";
      }

      public ImmutableMap<String, String> getParams() {
        return ImmutableMap.of();
      }

      public String getPath() {
        throw new UnsupportedOperationException("Subclass must override.");
      }

      public ImmutableMap<String, String> getPayload() {
        return ImmutableMap.of();
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
      public String getPath() {
        return path;
      }
    }

    static class ReadResponse extends OrgResponse<ReadRequest> implements
        ReadOrgResponse {
      ReadResponse(ReadRequest request, Response response) {
        super(request, response);
      }

      public GbrdsOrganisation getResult() {
        if (getStatus() != HttpStatus.SC_OK) {
          return null;
        }
        return OrgUtil.fromJson(getBody());
      }
    }

    static class UpdateRequest extends OrgRequest implements UpdateOrgRequest {

      final GbrdsOrganisation org;
      final String path;
      final ImmutableMap<String, String> payload;
      final String method;
      OrgCredentials creds;

      UpdateRequest(GbrdsOrganisation org, Gbrds registry) {
        super(registry);
        this.org = org;
        path = String.format("/registry/organisation/%s", org.getKey());
        payload = OrgUtil.asImmutableMap(org);
        method = "POST";
      }

      /**
       * @see org.gbif.registry.api.client.Gbrds.RpcRequest#execute()
       */
      public UpdateOrgResponse execute(OrgCredentials creds)
          throws BadCredentialsException {
        checkNotNull(creds, "Credentials are null");
        this.creds = creds;
        Response response = registry.execute(this);
        if (response.getStatus() == HttpStatus.SC_UNAUTHORIZED) {
          throw new BadCredentialsException("Unauthorized: " + creds);
        }
        return new UpdateResponse(this, response);
      }

      @Override
      public OrgCredentials getCredentials() {
        return creds;
      }

      @Override
      public String getMethod() {
        return method;
      }

      @Override
      public String getPath() {
        return path;
      }

      @Override
      public ImmutableMap<String, String> getPayload() {
        return payload;
      }
    }

    static class UpdateResponse extends OrgResponse<UpdateRequest> implements
        UpdateOrgResponse {
      UpdateResponse(UpdateRequest request, Response response) {
        super(request, response);
      }

      public Boolean getResult() {
        return getStatus() == HttpStatus.SC_OK;
      }
    }

    static class ValidateRequest extends OrgRequest implements
        ValidateOrgCredentialsRequest {

      final OrgCredentials creds;
      final String path;
      final ImmutableMap<String, String> requestParams;

      ValidateRequest(OrgCredentials creds, Gbrds registry) {
        super(registry);
        this.creds = creds;
        path = String.format("/registry/organisation/%s", creds.getKey());
        requestParams = ImmutableMap.of("op", "login");
      }

      /**
       * @see org.gbif.registry.api.client.Gbrds.RpcRequest#execute()
       */
      public ValidateOrgCredentialsResponse execute() {
        return new ValidateResponse(this, registry.execute(this));
      }

      @Override
      public OrgCredentials getCredentials() {
        return creds;
      }

      @Override
      public ImmutableMap<String, String> getParams() {
        return requestParams;
      }

      @Override
      public String getPath() {
        return path;
      }
    }

    static class ValidateResponse extends OrgResponse<ValidateRequest>
        implements ValidateOrgCredentialsResponse {
      ValidateResponse(ValidateRequest request, Response response) {
        super(request, response);
      }

      public Boolean getResult() {
        return getStatus() == HttpStatus.SC_OK;
      }
    }

    private final Gbrds registry;

    OrganisationApiImpl(Gbrds registry) {
      this.registry = registry;
    }

    /**
     * @see OrganisationApi#create(GbrdsOrganisation)
     */
    public CreateOrgRequest create(GbrdsOrganisation org) {
      checkNotNull(org, "Organisation is null");
      checkArgument(notNullOrEmpty(org.getName()), "Organisation name is null");
      checkArgument(notNullOrEmpty(org.getPrimaryContactType()),
          "Organisation contact type is null");
      checkArgument(notNullOrEmpty(org.getPrimaryContactEmail()),
          "Organisation contact email is null");
      checkArgument(notNullOrEmpty(org.getNodeKey()),
          "Organisation node key is null");
      return new CreateRequest(org, registry);
    }

    /**
     * @see OrganisationApi#delete(String)
     */
    public DeleteOrgRequest delete(String organisationKey) {
      checkArgument(notNullOrEmpty(organisationKey), "Organisation key is null");
      return new DeleteRequest(organisationKey, registry);
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
     * @see OrganisationApi#update(GbrdsOrganisation)
     */
    public UpdateOrgRequest update(GbrdsOrganisation org) {
      checkNotNull(org, "Organisation is null");
      checkArgument(notNullOrEmpty(org.getKey()), "Organisation key is null");
      checkArgument(notNullOrEmpty(org.getPrimaryContactType()),
          "Organisation contact type is null");
      return new UpdateRequest(org, registry);
    }

    /**
     * @see OrganisationApi#validateCredentials (OrgCredentials)
     */
    public ValidateOrgCredentialsRequest validateCredentials(
        OrgCredentials creds) {
      checkNotNull(creds, "Credentials are null");
      return new ValidateRequest(creds, registry);
    }
  }

  private static class ResourceApiImpl implements ResourceApi {

    static class CreateRequest extends ResourceRequest implements
        CreateResourceRequest {

      final GbrdsResource resource;
      OrgCredentials creds;

      CreateRequest(GbrdsResource resource, Gbrds registry) {
        super(registry);
        this.resource = resource;
      }

      public CreateResourceResponse execute(OrgCredentials creds)
          throws BadCredentialsException {
        checkNotNull(creds, "Credentials are null");
        this.creds = creds;
        Response response = registry.execute(this);
        if (response.getStatus() == HttpStatus.SC_UNAUTHORIZED) {
          throw new BadCredentialsException("Unauthorized: " + creds);
        }
        return new CreateResponse(this, response);
      }

      @Override
      public OrgCredentials getCredentials() {
        return creds;
      }

      @Override
      public String getMethod() {
        return "POST";
      }

      @Override
      public String getPath() {
        return "/registry/resource";
      }

      @Override
      public ImmutableMap<String, String> getPayload() {
        return ResourceUtil.asImmutableMap(resource);
      }
    }

    static class CreateResponse extends ResourceResponse<CreateRequest>
        implements CreateResourceResponse {

      CreateResponse(CreateRequest request, Response response) {
        super(request, response);
      }

      public GbrdsResource getResult() {
        if (getStatus() != HttpStatus.SC_CREATED) {
          return null;
        }
        return ResourceUtil.fromXml(getBody());
      }

    }

    static class DeleteRequest extends ResourceRequest implements
        DeleteResourceRequest {

      OrgCredentials creds;
      final String resourceKey;
      final String method;
      final String path;

      DeleteRequest(String resourceKey, Gbrds registry) {
        super(registry);
        this.resourceKey = resourceKey;
        method = "DELETE";
        path = String.format("/registry/resource/%s", resourceKey);
      }

      public DeleteResourceResponse execute(OrgCredentials creds)
          throws BadCredentialsException {
        checkNotNull(creds, "Credentials are null");
        this.creds = creds;
        Response response = registry.execute(this);
        if (response.getStatus() == HttpStatus.SC_UNAUTHORIZED) {
          throw new BadCredentialsException("Unauthorized: " + creds);
        }
        return new DeleteResponse(this, response);
      }

      @Override
      public OrgCredentials getCredentials() {
        return creds;
      }

      @Override
      public String getMethod() {
        return method;
      }

      @Override
      public String getPath() {
        return path;
      }
    }

    static class DeleteResponse extends ResourceResponse<DeleteRequest>
        implements DeleteResourceResponse {

      DeleteResponse(DeleteRequest request, Response response) {
        super(request, response);
      }

      public Boolean getResult() {
        return getStatus() == HttpStatus.SC_OK;
      }
    }

    static class ListRequest implements ListResourceRequest {

      final Gbrds registry;
      final String organisationKey;
      final ImmutableMap<String, String> params;

      ListRequest(String organisationKey, Gbrds registry) {
        this.registry = registry;
        this.organisationKey = organisationKey;
        params = ImmutableMap.of("organisationKey", organisationKey);
      }

      public ListResponse execute() {
        return new ListResponse(this, registry.execute(this));
      }

      public OrgCredentials getCredentials() {
        return null;
      }

      public String getMethod() {
        return "GET";
      }

      public ImmutableMap<String, String> getParams() {
        return params;
      }

      public String getPath() {
        return "/registry/resource.json";
      }

      public ImmutableMap<String, String> getPayload() {
        return ImmutableMap.of();
      }
    }

    static class ListResponse extends ResourceResponse<ListRequest> implements
        ListResourceResponse {

      private ListResponse(ListRequest request, Response response) {
        super(request, response);
      }

      public List<GbrdsResource> getResult() {
        if (getStatus() != HttpStatus.SC_OK) {
          return null;
        }
        return ResourceUtil.listFromJson(getBody());
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
      public String getPath() {
        return path;
      }
    }

    static class ReadResponse extends ResourceResponse<ReadRequest> implements
        ReadResourceResponse {
      ReadResponse(ReadRequest request, Response response) {
        super(request, response);
      }

      public GbrdsResource getResult() {
        if (getStatus() != HttpStatus.SC_OK) {
          return null;
        }
        return ResourceUtil.fromJson(getBody());
      }
    }

    static abstract class ResourceRequest implements Request {

      final Gbrds registry;

      ResourceRequest(Gbrds registry) {
        this.registry = registry;
      }

      public OrgCredentials getCredentials() {
        return null;
      }

      public String getMethod() {
        return "GET";
      }

      public ImmutableMap<String, String> getParams() {
        return ImmutableMap.of();
      }

      public String getPath() {
        throw new UnsupportedOperationException("Subclass must override.");
      }

      public ImmutableMap<String, String> getPayload() {
        return ImmutableMap.of();
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

      final GbrdsResource resource;
      final String path;
      final ImmutableMap<String, String> payload;
      final String method;
      OrgCredentials creds;

      UpdateRequest(GbrdsResource resource, Gbrds registry) {
        super(registry);
        this.resource = resource;
        path = String.format("/registry/resource/%s", resource.getKey());
        payload = ResourceUtil.asImmutableMap(resource);
        method = "POST";
      }

      public UpdateResourceResponse execute(OrgCredentials creds)
          throws BadCredentialsException {
        checkNotNull(creds, "Credentials are null");
        this.creds = creds;
        Response response = registry.execute(this);
        if (response.getStatus() == HttpStatus.SC_UNAUTHORIZED) {
          throw new BadCredentialsException("Unauthorized: " + creds);
        }
        return new UpdateResponse(this, response);
      }

      @Override
      public OrgCredentials getCredentials() {
        return creds;
      }

      @Override
      public String getMethod() {
        return method;
      }

      @Override
      public String getPath() {
        return path;
      }

      @Override
      public ImmutableMap<String, String> getPayload() {
        return payload;
      }
    }

    static class UpdateResponse extends ResourceResponse<UpdateRequest>
        implements UpdateResourceResponse {
      UpdateResponse(UpdateRequest request, Response response) {
        super(request, response);
      }

      public Boolean getResult() {
        return getStatus() == HttpStatus.SC_OK;
      }
    }

    private final Gbrds registry;

    ResourceApiImpl(Gbrds registry) {
      this.registry = registry;
    }

    /**
     * @see ResourceanisationApi#create(GbrdsResource)
     */
    public CreateResourceRequest create(GbrdsResource resource) {
      checkNotNull(resource);
      checkArgument(notNullOrEmpty(resource.getName()));
      checkArgument(notNullOrEmpty(resource.getPrimaryContactType()));
      checkArgument(notNullOrEmpty(resource.getPrimaryContactEmail()));
      checkArgument(notNullOrEmpty(resource.getOrganisationKey()));
      return new CreateRequest(resource, registry);
    }

    /**
     * @see ResourceanisationApi#delete(String)
     */
    public DeleteResourceRequest delete(String resourceKey) {
      checkArgument(notNullOrEmpty(resourceKey), "Resource key is null");
      return new DeleteRequest(resourceKey, registry);
    }

    /**
     * @see ResourceanisationApi#list(String)
     */
    public ListResourceRequest list(String organisationKey) {
      return new ListRequest(organisationKey, registry);
    }

    /**
     * @see ResourceanisationApi#read(String)
     */
    public ReadResourceRequest read(String resourceKey) {
      checkArgument(notNullOrEmpty(resourceKey));
      return new ReadRequest(resourceKey, registry);
    }

    /**
     * @see ResourceanisationApi#update(GbrdsResource)
     */
    public UpdateResourceRequest update(GbrdsResource resource) {
      checkNotNull(resource);
      checkArgument(notNullOrEmpty(resource.getKey()));
      checkArgument(notNullOrEmpty(resource.getPrimaryContactType()));
      return new UpdateRequest(resource, registry);
    }
  }

  private static class ServiceApiImpl implements ServiceApi {

    /**
     * This class provides an internal implementation of
     * {@link CreateServiceRequest}.
     * 
     */
    static class CreateRequest extends ServiceRequest implements
        CreateServiceRequest {

      final GbrdsService service;
      OrgCredentials creds;

      CreateRequest(GbrdsService service, Gbrds registry) {
        super(registry);
        this.service = service;
      }

      public CreateServiceResponse execute(OrgCredentials creds)
          throws BadCredentialsException {
        checkNotNull(creds, "Credentials are null");
        this.creds = creds;
        Response response = registry.execute(this);
        if (response.getStatus() == HttpStatus.SC_UNAUTHORIZED) {
          throw new BadCredentialsException("Unauthorized: " + creds);
        }
        return new CreateResponse(this, response);
      }

      @Override
      public OrgCredentials getCredentials() {
        return creds;
      }

      @Override
      public String getMethod() {
        return "POST";
      }

      @Override
      public String getPath() {
        return "/registry/service";
      }

      @Override
      public ImmutableMap<String, String> getPayload() {
        return ServiceUtil.asImmutableMap(service);
      }
    }

    /**
     * This class provides an internal implementation of
     * {@link CreateServiceResponse}.
     * 
     */
    static class CreateResponse extends ServiceResponse<CreateRequest>
        implements CreateServiceResponse {

      CreateResponse(CreateRequest request, Response response) {
        super(request, response);
      }

      public GbrdsService getResult() {
        if (getStatus() != HttpStatus.SC_CREATED) {
          return null;
        }
        return ServiceUtil.fromXml(getBody());
      }
    }

    /**
     * This class provides an internal implementation of
     * {@link DeleteServiceRequest}.
     * 
     */
    static class DeleteRequest extends ServiceRequest implements
        DeleteServiceRequest {

      final String serviceKey;
      OrgCredentials creds;
      final String method;
      final String path;

      DeleteRequest(String serviceKey, Gbrds registry) {
        super(registry);
        this.serviceKey = serviceKey;
        method = "DELETE";
        path = String.format("/registry/service/%s", serviceKey);
      }

      public DeleteServiceResponse execute(OrgCredentials creds)
          throws BadCredentialsException {
        checkNotNull(creds, "Credentials are null");
        this.creds = creds;
        Response response = registry.execute(this);
        if (response.getStatus() == HttpStatus.SC_UNAUTHORIZED) {
          throw new BadCredentialsException("Unauthorized: " + creds);
        }
        return new DeleteResponse(this, response);
      }

      @Override
      public OrgCredentials getCredentials() {
        return creds;
      }

      @Override
      public String getMethod() {
        return method;
      }

      @Override
      public String getPath() {
        return path;
      }
    }

    /**
     * This class provides an internal implementation of
     * {@link DeleteServiceResponse}.
     * 
     */
    static class DeleteResponse extends ServiceResponse<DeleteRequest>
        implements DeleteServiceResponse {

      DeleteResponse(DeleteRequest request, Response response) {
        super(request, response);
      }

      public Boolean getResult() {
        return getStatus() == HttpStatus.SC_OK;
      }
    }

    static class ListForResourceRequest implements ListServicesRequest {

      final Gbrds registry;
      private String resourceKey;
      private String method;
      private OrgCredentials credentials;
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
        path = "/registry/service.json";
      }

      public ListForResourceResponse execute() {
        return new ListForResourceResponse(this, registry.execute(this));
      }

      public OrgCredentials getCredentials() {
        return credentials;
      }

      public String getMethod() {
        return method;
      }

      public ImmutableMap<String, String> getParams() {
        return requestParams;
      }

      public String getPath() {
        return path;
      }

      public ImmutableMap<String, String> getPayload() {
        return payload;
      }
    }

    static class ListForResourceResponse extends
        ServiceResponse<ListForResourceRequest> implements ListServicesResponse {

      private ListForResourceResponse(ListForResourceRequest request,
          Response response) {
        super(request, response);
      }

      public List<GbrdsService> getResult() {
        if (getStatus() != HttpStatus.SC_OK) {
          return null;
        }
        return ServiceUtil.listFromJson(getBody());
      }
    }

    /**
     * This class provides an internal implementation of
     * {@link ReadServiceRequest}.
     * 
     */
    static class ReadRequest extends ServiceRequest implements
        ReadServiceRequest {

      final String path;

      ReadRequest(String serviceKey, Gbrds registry) {
        super(registry);
        path = String.format("/registry/service/%s.json", serviceKey);
      }

      public ReadServiceResponse execute() {
        return new ReadResponse(this, registry.execute(this));
      }

      @Override
      public String getPath() {
        return path;
      }
    }

    /**
     * This class provides an internal implementation of
     * {@link ReadServiceResponse}.
     * 
     */
    static class ReadResponse extends ServiceResponse<ReadRequest> implements
        ReadServiceResponse {

      ReadResponse(ReadRequest request, Response response) {
        super(request, response);
      }

      public GbrdsService getResult() {
        if (getStatus() != HttpStatus.SC_OK) {
          return null;
        }
        return ServiceUtil.fromJson(getBody());
      }
    }

    static abstract class ServiceRequest implements Request {

      final Gbrds registry;

      ServiceRequest(Gbrds registry) {
        this.registry = registry;
      }

      public OrgCredentials getCredentials() {
        return null;
      }

      public String getMethod() {
        return "GET";
      }

      public ImmutableMap<String, String> getParams() {
        return ImmutableMap.of();
      }

      public String getPath() {
        throw new UnsupportedOperationException("Subclass must override.");
      }

      public ImmutableMap<String, String> getPayload() {
        return ImmutableMap.of();
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

    /**
     * This class provides an internal implementation of
     * {@link UpdateServiceRequest}.
     * 
     */
    static class UpdateRequest extends ServiceRequest implements
        UpdateServiceRequest {

      final GbrdsService service;
      final String path;
      final ImmutableMap<String, String> payload;
      final String method;
      OrgCredentials creds;

      UpdateRequest(GbrdsService service, Gbrds registry) {
        super(registry);
        this.service = service;
        path = String.format("/registry/service/%s", service.getKey());
        payload = ServiceUtil.asImmutableMap(service);
        method = "POST";
      }

      public UpdateServiceResponse execute(OrgCredentials creds)
          throws BadCredentialsException {
        checkNotNull(creds, "Credentials are null");
        this.creds = creds;
        Response response = registry.execute(this);
        if (response.getStatus() == HttpStatus.SC_UNAUTHORIZED) {
          throw new BadCredentialsException("Unauthorized: " + creds);
        }
        return new UpdateResponse(this, response);
      }

      @Override
      public OrgCredentials getCredentials() {
        return creds;
      }

      @Override
      public String getMethod() {
        return method;
      }

      @Override
      public String getPath() {
        return path;
      }

      @Override
      public ImmutableMap<String, String> getPayload() {
        return payload;
      }
    }

    /**
     * This class provides an internal implementation of
     * {@link UpdateServiceResponse}.
     * 
     */
    static class UpdateResponse extends ServiceResponse<UpdateRequest>
        implements UpdateServiceResponse {

      UpdateResponse(UpdateRequest request, Response response) {
        super(request, response);
      }

      public Boolean getResult() {
        return getStatus() == HttpStatus.SC_OK;
      }
    }

    private final Gbrds registry;

    ServiceApiImpl(Gbrds registry) {
      this.registry = registry;
    }

    /**
     * @see Gbrds.ServiceApi#create(GbrdsService, OrgCredentials)
     */
    public CreateServiceRequest create(GbrdsService service) {
      checkNotNull(service);
      checkArgument(notNullOrEmpty(service.getResourceKey()));
      checkArgument(notNullOrEmpty(service.getType()));
      checkArgument(notNullOrEmpty(service.getAccessPointURL()));
      return new CreateRequest(service, registry);
    }

    /**
     * @see ServiceanisationApi#delete(String)
     */
    public DeleteServiceRequest delete(String serviceKey) {
      checkArgument(notNullOrEmpty(serviceKey), "Service key is null");
      return new DeleteRequest(serviceKey, registry);
    }

    /**
     * @see ServiceanisationApi#list(String)
     */
    public ListServicesRequest list(String resourceKey) {
      return new ListForResourceRequest(resourceKey, registry);
    }

    /**
     * @see ServiceanisationApi#read(String)
     */
    public ReadServiceRequest read(String serviceKey) {
      checkArgument(notNullOrEmpty(serviceKey));
      return new ReadRequest(serviceKey, registry);
    }

    /**
     * @see ServiceanisationApi#update(GbrdsService)
     */
    public UpdateServiceRequest update(GbrdsService service) {
      checkNotNull(service, "Service to update is null");
      checkArgument(notNullOrEmpty(service.getKey()), "Service key is null");
      return new UpdateRequest(service, registry);
    }
  }

  private static final Log log = LogFactory.getLog(GbrdsRegistry.class);

  public static GbrdsRegistry init(String host) {
    try {
      URL url = new URL(host);
      return new GbrdsRegistry(url.getHost());
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private static HttpMethod createMethod(String host, Request request) {
    String url = String.format("%s%s", host, request.getPath());
    HttpMethod method;
    ImmutableMap<String, String> params = request.getParams();
    if (params == null) {
      params = ImmutableMap.of();
    }
    ImmutableMap<String, String> payload = request.getPayload();
    if (payload == null) {
      payload = ImmutableMap.of();
    }
    String type = request.getMethod();
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
      OrgCredentials credentials) {
    checkNotNull(credentials);
    checkNotNull(client);
    checkNotNull(host);
    log.info(String.format("Setting credentials: Host=%s, Credentials=%s",
        host, credentials));
    AuthScope scope = new AuthScope(host, -1, AuthScope.ANY_REALM);
    client.getState().setCredentials(
        scope,
        new UsernamePasswordCredentials(credentials.getKey(),
            credentials.getPassword()));
    client.getParams().setAuthenticationPreemptive(true);
  }

  private final String url;
  private final String host;
  private final HttpClient client;
  private final OrganisationApiImpl orgApi;
  private final ResourceApiImpl resourceApi;
  private final ServiceApiImpl serviceApi;
  private final IptApiImpl extensionApi;

  private GbrdsRegistry(String host) {
    this.host = host;
    url = String.format("http://%s", host);
    client = new HttpClient(new MultiThreadedHttpConnectionManager());
    orgApi = new OrganisationApiImpl(this);
    resourceApi = new ResourceApiImpl(this);
    serviceApi = new ServiceApiImpl(this);
    extensionApi = new IptApiImpl(this);
  }

  /**
   * @see org.gbif.registry.api.client.Gbrds#execute(org.gbif.registry.api.client.Gbrds.Request)
   */
  public Response execute(Request request) {
    checkNotNull(request, "Request is null");
    checkNotNull(request.getPayload(), "Request payload is null");
    checkNotNull(request.getParams(), "Request parameters are null");
    checkArgument(notNullOrEmpty(request.getMethod()), "Request method is null");
    checkArgument(notNullOrEmpty(request.getPath()), "Request path is null");

    HttpMethod method = createMethod(url, request);
    Throwable error = null;
    String body = null;
    int status = 0;
    try {
      OrgCredentials cred = request.getCredentials();
      if (cred != null) {
        setCredentials(client, host, cred);
      }
      status = client.executeMethod(method);
      log.info(String.format("Executed %s %d %s (parmas=%s and payload=%s)",
          request.getMethod(), status, method.getURI(), request.getParams(),
          request.getPayload()));
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
   * @see Gbrds#getIptApi()
   */
  public IptApi getIptApi() {
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