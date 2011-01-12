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
import com.google.common.collect.ImmutableList;
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
 * This class provides a default implementation of {@link Gbrds}.
 * 
 * @see http://code.google.com/p/gbif-registry
 * 
 */
public class GbrdsImpl implements Gbrds {

  static class CredentialsXmlHandler extends DefaultHandler {
    String organisationKey;
    String password;
    String content;

    @Override
    public void characters(char[] ch, int start, int length)
        throws SAXException {
      content += String.valueOf(ArrayUtils.subarray(ch, start, start + length));
    }

    @Override
    public void endElement(String uri, String localName, String name)
        throws SAXException {
      if (name.equalsIgnoreCase("password")) {
        password = content;
      } else if (name.equalsIgnoreCase("organisationKey")) {
        organisationKey = content.replaceAll("\\s", "");
      }
      content = "";
    }

    @Override
    public void startDocument() throws SAXException {
      content = "";
      organisationKey = "";
      password = "";
    }

    @Override
    public void startElement(String uri, String localName, String name,
        Attributes attributes) throws SAXException {
      content = "";
    }
  }

  static class OrgUtil {
    static ImmutableMap<String, String> asImmutableMap(GbrdsOrganisation org) {
      String json = new Gson().toJson(org, GbrdsOrganisation.class);
      Map<String, String> map = new Gson().fromJson(json,
          new TypeToken<Map<String, String>>() {
          }.getType());
      return ImmutableMap.copyOf(map);
    }

    static OrgCredentials credsFromXml(String xml) {
      OrgCredentials creds = null;
      try {
        SAXParser p = SAXParserFactory.newInstance().newSAXParser();
        CredentialsXmlHandler h = new CredentialsXmlHandler();
        InputStream s = new ByteArrayInputStream(xml.getBytes());
        p.parse(s, h);
        creds = OrgCredentials.with(h.organisationKey, h.password);
      } catch (Exception e) {
        e.printStackTrace();
      }
      return creds;
    }

    static GbrdsOrganisation fromJson(String json) {
      return new Gson().fromJson(json, GbrdsOrganisation.Builder.class).build();
    }

    static GbrdsOrganisation fromXml(String xml) {
      GbrdsOrganisation org = null;
      try {
        SAXParser p = SAXParserFactory.newInstance().newSAXParser();
        OrgXmlHandler h = new OrgXmlHandler();
        InputStream s = new ByteArrayInputStream(xml.getBytes());
        p.parse(s, h);
        org = GbrdsOrganisation.builder().description(h.description).descriptionLanguage(
            h.descriptionLanguage).homepageURL(h.homepageURL).key(h.key).name(
            h.name).nameLanguage(h.nameLanguage).nodeContactEmail(
            h.nodeContactEmail).nodeKey(h.nodeKey).nodeName(h.nodeName).password(
            h.password).primaryContactAddress(h.primaryContactAddress).primaryContactDescription(
            h.primaryContactDescription).primaryContactEmail(
            h.primaryContactEmail).primaryContactName(h.primaryContactName).primaryContactPhone(
            h.primaryContactPhone).primaryContactType(h.primaryContactType).build();
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

  static class OrgXmlHandler extends DefaultHandler {
    String description;
    String descriptionLanguage;
    String homepageURL;
    String key;
    String name;
    String nameLanguage;
    String nodeContactEmail;
    String nodeKey;
    String nodeName;
    String password;
    String primaryContactAddress;
    String primaryContactDescription;
    String primaryContactEmail;
    String primaryContactName;
    String primaryContactPhone;
    String primaryContactType;

    String content;

    @Override
    public void characters(char[] ch, int start, int length)
        throws SAXException {
      content += String.valueOf(ArrayUtils.subarray(ch, start, start + length));
    }

    @Override
    public void endElement(String uri, String localName, String name)
        throws SAXException {
      if (name.equalsIgnoreCase("description")) {
        description = content;
      } else if (name.equalsIgnoreCase("descriptionLanguage")) {
        descriptionLanguage = content;
      } else if (name.equalsIgnoreCase("homepageURL")) {
        homepageURL = content;
      } else if (name.equalsIgnoreCase("key")) {
        key = content;
      } else if (name.equalsIgnoreCase("name")) {
        name = content;
      } else if (name.equalsIgnoreCase("nameLanguage")) {
        nameLanguage = content;
      } else if (name.equalsIgnoreCase("nodeContactEmail")) {
        nodeContactEmail = content;
      } else if (name.equalsIgnoreCase("nodeKey")) {
        nodeKey = content;
      } else if (name.equalsIgnoreCase("nodeName")) {
        nodeName = content;
      } else if (name.equalsIgnoreCase("password")) {
        password = content;
      } else if (name.equalsIgnoreCase("primaryContactAddress")) {
        primaryContactAddress = content;
      } else if (name.equalsIgnoreCase("primaryContactDescription")) {
        primaryContactDescription = content;
      } else if (name.equalsIgnoreCase("primaryContactEmail")) {
        primaryContactEmail = content;
      } else if (name.equalsIgnoreCase("primaryContactName")) {
        primaryContactName = content;
      } else if (name.equalsIgnoreCase("primaryContactPhone")) {
        primaryContactPhone = content;
      } else if (name.equalsIgnoreCase("primaryContactType")) {
        primaryContactType = content;
      }

      content = "";
    }

    @Override
    public void startDocument() throws SAXException {
      content = "";
      description = "";
      descriptionLanguage = "";
      homepageURL = "";
      key = "";
      name = "";
      nameLanguage = "";
      nodeContactEmail = "";
      nodeKey = "";
      nodeName = "";
      password = "";
      primaryContactAddress = "";
      primaryContactDescription = "";
      primaryContactEmail = "";
      primaryContactName = "";
      primaryContactPhone = "";
      primaryContactType = "";
    }

    @Override
    public void startElement(String uri, String localName, String name,
        Attributes attributes) throws SAXException {
      content = "";
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
        ResourceXmlHandler h = new ResourceXmlHandler();
        InputStream s = new ByteArrayInputStream(xml.getBytes());
        p.parse(s, h);
        resource = GbrdsResource.builder().description(h.description).descriptionLanguage(
            h.descriptionLanguage).homepageURL(h.homepageURL).key(h.key).name(
            h.name).nameLanguage(h.nameLanguage).organisationKey(
            h.organisationKey).primaryContactAddress(h.primaryContactAddress).primaryContactDescription(
            h.primaryContactDescription).primaryContactEmail(
            h.primaryContactEmail).primaryContactName(h.primaryContactName).primaryContactPhone(
            h.primaryContactPhone).primaryContactType(h.primaryContactType).build();
      } catch (Exception e) {
        e.printStackTrace();
      }
      return resource;
    }

    static List<GbrdsResource> listFromJson(String json) {
      return new Gson().fromJson(json, new TypeToken<List<GbrdsResource>>() {
      }.getType());
    }
  }

  static class ResourceXmlHandler extends DefaultHandler {
    String description;
    String descriptionLanguage;
    String homepageURL;
    String key;
    String name;
    String nameLanguage;
    String organisationKey;
    String primaryContactAddress;
    String primaryContactDescription;
    String primaryContactEmail;
    String primaryContactName;
    String primaryContactPhone;
    String primaryContactType;;

    String content;

    @Override
    public void characters(char[] ch, int start, int length)
        throws SAXException {
      content += String.valueOf(ArrayUtils.subarray(ch, start, start + length));
    }

    @Override
    public void endElement(String uri, String localName, String name)
        throws SAXException {
      if (name.equalsIgnoreCase("description")) {
        description = content;
      } else if (name.equalsIgnoreCase("descriptionLanguage")) {
        descriptionLanguage = content;
      } else if (name.equalsIgnoreCase("homepageURL")) {
        homepageURL = content;
      } else if (name.equalsIgnoreCase("key")) {
        key = content;
      } else if (name.equalsIgnoreCase("name")) {
        name = content;
      } else if (name.equalsIgnoreCase("nameLanguage")) {
        nameLanguage = content;
      } else if (name.equalsIgnoreCase("organisationKey")) {
        organisationKey = content;
      } else if (name.equalsIgnoreCase("primaryContactAddress")) {
        primaryContactAddress = content;
      } else if (name.equalsIgnoreCase("primaryContactDescription")) {
        primaryContactDescription = content;
      } else if (name.equalsIgnoreCase("primaryContactEmail")) {
        primaryContactEmail = content;
      } else if (name.equalsIgnoreCase("primaryContactName")) {
        primaryContactName = content;
      } else if (name.equalsIgnoreCase("primaryContactPhone")) {
        primaryContactPhone = content;
      } else if (name.equalsIgnoreCase("primaryContactType")) {
        primaryContactType = content;
      }

      content = "";
    }

    @Override
    public void startDocument() throws SAXException {
      description = "";
      descriptionLanguage = "";
      homepageURL = "";
      key = "";
      name = "";
      nameLanguage = "";
      organisationKey = "";
      primaryContactAddress = "";
      primaryContactDescription = "";
      primaryContactEmail = "";
      primaryContactName = "";
      primaryContactPhone = "";
      primaryContactType = "";
    }

    @Override
    public void startElement(String uri, String localName, String name,
        Attributes attributes) throws SAXException {
      content = "";
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
        ServiceXmlHandler h = new ServiceXmlHandler();
        InputStream s = new ByteArrayInputStream(xml.getBytes());
        p.parse(s, h);
        service = GbrdsService.builder().accessPointURL(h.accessPointURL).description(
            h.description).descriptionLanguage(h.descriptionLanguage).key(h.key).resourceKey(
            h.resourceKey).type(h.type).typeDescription(h.typeDescription).build();
      } catch (Exception e) {
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

  static class ServiceXmlHandler extends DefaultHandler {
    String accessPointURL;
    String description;
    String descriptionLanguage;
    String key;
    String resourceKey;
    String type;
    String typeDescription;

    String content;

    @Override
    public void characters(char[] ch, int start, int length)
        throws SAXException {
      content += String.valueOf(ArrayUtils.subarray(ch, start, start + length));
    }

    @Override
    public void endElement(String uri, String localName, String name)
        throws SAXException {
      if (name.equalsIgnoreCase("accessPointURL")) {
        accessPointURL = content;
      } else if (name.equalsIgnoreCase("description")) {
        description = content;
      } else if (name.equalsIgnoreCase("descriptionLanguage")) {
        descriptionLanguage = content;
      } else if (name.equalsIgnoreCase("key")) {
        key = content;
      } else if (name.equalsIgnoreCase("resourceKey")) {
        resourceKey = content;
      } else if (name.equalsIgnoreCase("type")) {
        type = content;
      } else if (name.equalsIgnoreCase("typeDescription")) {
        typeDescription = content;
      }

      content = "";
    }

    @Override
    public void startDocument() throws SAXException {
      accessPointURL = "";
      description = "";
      descriptionLanguage = "";
      key = "";
      resourceKey = "";
      type = "";
      typeDescription = "";
    }

    @Override
    public void startElement(String uri, String localName, String name,
        Attributes attributes) throws SAXException {
      content = "";
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

      public ImmutableList<GbrdsExtension> getResult() {
        if (getStatus() != HttpStatus.SC_OK) {
          return ImmutableList.of();
        }
        String body = getBody();
        if (body == null || body.length() < 1) {
          return ImmutableList.of();
        }
        String json = getBody();

        // Modifies JSON to be a list instead of a map:
        json = json.replace("{\"extensions\":", "");
        json = json.substring(0, json.length() - 1);

        try {
          List<GbrdsExtension> results = new Gson().fromJson(json,
              new TypeToken<List<GbrdsExtension>>() {
              }.getType());
          return ImmutableList.copyOf(results);
        } catch (Exception e) {
          return ImmutableList.of();
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

      public ImmutableList<GbrdsThesaurus> getResult() {
        if (getStatus() != HttpStatus.SC_OK) {
          return ImmutableList.of();
        }
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
          return ImmutableList.copyOf(results);
        } catch (Exception e) {
          return ImmutableList.of();
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

  private static class OrgApiImpl implements OrganisationApi {

    static class CreateRequest extends OrgRequest implements CreateOrgRequest {

      final GbrdsOrganisation org;
      final ImmutableMap<String, String> payload;

      CreateRequest(GbrdsOrganisation org, Gbrds registry) {
        super(registry);
        this.org = org;
        payload = OrgUtil.asImmutableMap(org);
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
        return payload;
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
        return OrgUtil.credsFromXml(getBody());
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

      public ImmutableList<GbrdsOrganisation> getResult() {
        if (getStatus() != HttpStatus.SC_OK) {
          return ImmutableList.of();
        }
        return ImmutableList.copyOf(OrgUtil.listFromJson(getBody()));
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

    OrgApiImpl(Gbrds registry) {
      this.registry = registry;
    }

    /**
     * @see OrganisationApi#create(GbrdsOrganisation)
     */
    public CreateOrgRequest create(GbrdsOrganisation org) {
      checkNotNull(org, "Organisation is null");
      checkArgument(notNullOrEmpty(org.getName()),
          "Name is null or empty string");
      checkArgument(notNullOrEmpty(org.getPrimaryContactEmail()),
          "Contact email is null or empty string");
      checkArgument(notNullOrEmpty(org.getNodeKey()),
          "Node key is null or empty string");
      String type = org.getPrimaryContactType();
      checkArgument(notNullOrEmpty(type),
          "Contact type is null or empty string");
      checkArgument(type.trim().equalsIgnoreCase("technical")
          || type.trim().equalsIgnoreCase("administrative"),
          "Contact type must be technical or administrative");
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
      checkArgument(notNullOrEmpty(org.getKey()), "Key is null or empty string");
      String type = org.getPrimaryContactType();
      checkArgument(notNullOrEmpty(type),
          "Contact type is null or empty string");
      checkArgument(type.trim().equalsIgnoreCase("technical")
          || type.trim().equalsIgnoreCase("administrative"),
          "Contact type must be technical or administrative");
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

      public ImmutableList<GbrdsResource> getResult() {
        if (getStatus() != HttpStatus.SC_OK) {
          return ImmutableList.of();
        }
        return ImmutableList.copyOf(ResourceUtil.listFromJson(getBody()));
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
      checkArgument(notNullOrEmpty(resource.getName()),
          "Name is null or empty string");
      checkArgument(notNullOrEmpty(resource.getPrimaryContactEmail()),
          "Contact email is null or empty string");
      checkArgument(notNullOrEmpty(resource.getOrganisationKey()),
          "Organisation key is null or empty string");
      String type = resource.getPrimaryContactType();
      checkArgument(notNullOrEmpty(type),
          "Contact type is null or empty string");
      checkArgument(type.trim().equalsIgnoreCase("technical")
          || type.trim().equalsIgnoreCase("administrative"),
          "Contact type must be technical or administrative");
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
      checkArgument(notNullOrEmpty(organisationKey), "Invalid organisation key");
      return new ListRequest(organisationKey, registry);
    }

    /**
     * @see ResourceanisationApi#read(String)
     */
    public ReadResourceRequest read(String resourceKey) {
      checkArgument(notNullOrEmpty(resourceKey), "Invalid resource key");
      return new ReadRequest(resourceKey, registry);
    }

    /**
     * @see ResourceanisationApi#update(GbrdsResource)
     */
    public UpdateResourceRequest update(GbrdsResource resource) {
      checkNotNull(resource);
      checkArgument(notNullOrEmpty(resource.getKey()), "Invalid resource key");
      checkArgument(notNullOrEmpty(resource.getPrimaryContactType()),
          "Invalid contact type");
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

    static class ListForResourceRequest implements ListServiceRequest {

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
        ServiceResponse<ListForResourceRequest> implements ListServiceResponse {

      private ListForResourceResponse(ListForResourceRequest request,
          Response response) {
        super(request, response);
      }

      public ImmutableList<GbrdsService> getResult() {
        if (getStatus() != HttpStatus.SC_OK) {
          return ImmutableList.of();
        }
        return ImmutableList.copyOf(ServiceUtil.listFromJson(getBody()));
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
      checkArgument(notNullOrEmpty(service.getResourceKey()),
          "Invalid resource key");
      checkArgument(notNullOrEmpty(service.getType()), "Invalid contact type");
      checkArgument(notNullOrEmpty(service.getAccessPointURL()),
          "Invalid access point URL");
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
    public ListServiceRequest list(String resourceKey) {
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

  private static final Log log = LogFactory.getLog(GbrdsImpl.class);

  public static GbrdsImpl init(String host) {
    try {
      URL url = new URL(host);
      return new GbrdsImpl(url.getHost());
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
  private final OrgApiImpl orgApi;
  private final ResourceApiImpl resourceApi;
  private final ServiceApiImpl serviceApi;
  private final IptApiImpl extensionApi;

  private GbrdsImpl(String host) {
    this.host = host;
    url = String.format("http://%s", host);
    client = new HttpClient(new MultiThreadedHttpConnectionManager());
    orgApi = new OrgApiImpl(this);
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