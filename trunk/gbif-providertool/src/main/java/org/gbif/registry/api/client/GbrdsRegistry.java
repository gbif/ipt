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
      RpcRequest<CreateOrgResponse, GbrdsOrganisation> {
  }
  public static interface CreateOrgResponse extends
      RpcResponse<GbrdsOrganisation> {
  }

  public static interface CreateResourceRequest extends
      RpcRequest<CreateResourceResponse, GbrdsResource> {
  }
  public static interface CreateResourceResponse extends
      RpcResponse<GbrdsResource> {
  }

  public static interface CreateServiceRequest extends
      RpcRequest<CreateServiceResponse, GbrdsService> {
  }

  public static interface CreateServiceResponse extends
      RpcResponse<GbrdsService> {
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

  public static interface ListServicesForResourceRequest extends
      RpcRequest<ListServicesForResourceResponse, List<GbrdsService>> {
  }

  public static interface ListServicesForResourceResponse extends
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
      RpcRequest<UpdateOrgResponse, GbrdsOrganisation> {
  }

  public static interface UpdateOrgResponse extends
      RpcResponse<GbrdsOrganisation> {
  }

  public static interface UpdateResourceRequest extends
      RpcRequest<UpdateResourceResponse, GbrdsResource> {
  }

  public static interface UpdateResourceResponse extends
      RpcResponse<GbrdsResource> {
  }

  public static interface UpdateServiceRequest extends
      RpcRequest<UpdateServiceResponse, GbrdsService> {
  }

  public static interface UpdateServiceResponse extends
      RpcResponse<GbrdsService> {
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
        return "/registry/ipt/thesauri.json";
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

      public GbrdsOrganisation getResult() {
        String body = getBody();
        if (!body.contains("<organisationKey>")) {
          return null;
        }
        return OrgUtil.fromXml(body);
      }

    }

    static class DeleteRequest extends OrgRequest implements DeleteOrgRequest {

      final GbrdsOrganisation org;
      final Credentials credentials;
      final String method;
      final String path;
      final ImmutableMap<String, String> payload;

      DeleteRequest(GbrdsOrganisation org, Gbrds registry) {
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

      public List<GbrdsOrganisation> getResult() {
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

      public GbrdsOrganisation getResult() {
        String body = getBody();
        if (body.contains("Error")) {
          return null;
        }
        return OrgUtil.fromJson(body);
      }
    }

    static class UpdateRequest extends OrgRequest implements UpdateOrgRequest {

      final GbrdsOrganisation org;
      final String path;
      final ImmutableMap<String, String> payload;
      final String method;
      final Credentials credentials;

      UpdateRequest(GbrdsOrganisation org, Gbrds registry) {
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

      public GbrdsOrganisation getResult() {
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

      final GbrdsOrganisation org;
      final String path;
      final ImmutableMap<String, String> payload;
      final Credentials credentials;
      final ImmutableMap<String, String> requestParams;

      ValidateRequest(GbrdsOrganisation org, Gbrds registry) {
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
     * @see OrganisationApi#create(GbrdsOrganisation)
     */
    public CreateOrgRequest create(GbrdsOrganisation org) {
      checkNotNull(org);
      checkArgument(notNullOrEmpty(org.getName()));
      checkArgument(notNullOrEmpty(org.getPrimaryContactType()));
      checkArgument(notNullOrEmpty(org.getPrimaryContactEmail()));
      checkArgument(notNullOrEmpty(org.getNodeKey()));
      return new CreateRequest(org, registry);
    }

    /**
     * @see OrganisationApi#delete(GbrdsOrganisation)
     */
    public DeleteOrgRequest delete(GbrdsOrganisation org) {
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
     * @see OrganisationApi#update(GbrdsOrganisation)
     */
    public UpdateOrgRequest update(GbrdsOrganisation org) {
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
      return new ValidateRequest(GbrdsOrganisation.builder().key(
          credentials.getId()).password(credentials.getPasswd()).build(),
          registry);
    }
  }

  private static class ResourceApiImpl implements ResourceApi {

    static class CreateRequest extends ResourceRequest implements
        CreateResourceRequest {

      final GbrdsResource resource;
      final Credentials credentials;

      CreateRequest(GbrdsResource resource, Gbrds registry) {
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

      public GbrdsResource getResult() {
        String body = getBody();
        if (!body.contains("<organisationKey>")) {
          return null;
        }
        return ResourceUtil.fromXml(body);
      }

    }

    static class DeleteRequest extends ResourceRequest implements
        DeleteResourceRequest {

      final GbrdsResource resource;
      final Credentials credentials;
      final String method;
      final String path;
      final ImmutableMap<String, String> payload;

      DeleteRequest(GbrdsResource resource, Gbrds registry) {
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

      public List<GbrdsResource> getResult() {
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

      public GbrdsResource getResult() {
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

      final GbrdsResource resource;
      final String path;
      final ImmutableMap<String, String> payload;
      final String method;
      final Credentials credentials;

      UpdateRequest(GbrdsResource resource, Gbrds registry) {
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

      public GbrdsResource getResult() {
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
     * @see ResourceanisationApi#create(GbrdsResource)
     */
    public CreateResourceRequest create(GbrdsResource resource) {
      checkNotNull(resource);
      checkArgument(notNullOrEmpty(resource.getName()));
      checkArgument(notNullOrEmpty(resource.getDescription()));
      checkArgument(notNullOrEmpty(resource.getPrimaryContactType()));
      checkArgument(notNullOrEmpty(resource.getPrimaryContactEmail()));
      checkArgument(notNullOrEmpty(resource.getOrganisationKey()));
      return new CreateRequest(resource, registry);
    }

    /**
     * @see ResourceanisationApi#delete(GbrdsResource)
     */
    public DeleteResourceRequest delete(GbrdsResource resource) {
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
     * @see ResourceanisationApi#update(GbrdsResource)
     */
    public UpdateResourceRequest update(GbrdsResource resource) {
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

      final GbrdsService service;
      final Credentials credentials;

      CreateRequest(GbrdsService service, Gbrds registry) {
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

      public GbrdsService getResult() {
        String body = getBody();
        if (!body.contains("<key>")) {
          return null;
        }
        return ServiceUtil.fromXml(body);
      }

    }

    static class DeleteRequest extends ServiceRequest implements
        DeleteServiceRequest {

      final GbrdsService service;
      final Credentials credentials;
      final String method;
      final String path;
      final ImmutableMap<String, String> payload;

      DeleteRequest(GbrdsService service, Gbrds registry) {
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

      public List<GbrdsService> getResult() {
        String body = getBody();
        if (body == null || body.length() < 1) {
          return null;
        }
        return ServiceUtil.listFromXml(body);
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

      public GbrdsService getResult() {
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

      final GbrdsService service;
      final String path;
      final ImmutableMap<String, String> payload;
      final String method;
      final Credentials credentials;

      UpdateRequest(GbrdsService service, Gbrds registry) {
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

      public GbrdsService getResult() {
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
     * @see ServiceanisationApi#create(GbrdsService)
     */
    public CreateServiceRequest create(GbrdsService service) {
      checkNotNull(service);
      checkArgument(notNullOrEmpty(service.getType()));
      checkArgument(notNullOrEmpty(service.getAccessPointURL()));
      checkArgument(notNullOrEmpty(service.getResourceKey()));
      return new CreateRequest(service, registry);
    }

    /**
     * @see ServiceanisationApi#delete(GbrdsService)
     */
    public DeleteServiceRequest delete(GbrdsService service) {
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
     * @see ServiceanisationApi#update(GbrdsService)
     */
    public UpdateServiceRequest update(GbrdsService service) {
      checkNotNull(service);
      checkArgument(notNullOrEmpty(service.getKey()));
      checkArgument(notNullOrEmpty(service.getResourcePassword()));
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
