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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang.StringUtils.trimToEmpty;
import static org.apache.commons.lang.StringUtils.trimToNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Organization;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.ResourceMetadata;
import org.gbif.provider.model.voc.ContactType;
import org.gbif.provider.model.voc.ServiceType;
import org.gbif.provider.model.xml.NewRegistryEntryHandler;
import org.gbif.provider.model.xml.ResourceMetadataHandler;
import org.gbif.provider.service.RegistryException;
import org.gbif.provider.service.RegistryManager;
import org.gbif.provider.util.AppConfig;
import org.xml.sax.SAXException;

import com.googlecode.jsonplugin.JSONUtil;

/**
 * This class provides a default implementation of {@link RegistryManager}.
 * 
 */
public class RegistryManagerImpl extends HttpBaseManager implements
    RegistryManager {

  /**
   * Private utility method that returns an {@link Organization} converted to an
   * array of {@link NameValuePair} objects.
   * 
   * @param org the organization
   * @return NameValuePair[]
   */
  private static NameValuePair[] nameValuePairs(Organization org) {
    checkNotNull(org);
    NameValuePair[] nvp = {
        new NameValuePair("name", org.getName()),
        new NameValuePair("description", org.getDescription()),
        new NameValuePair("homepageURL", org.getHomepageUrl()),
        new NameValuePair("nameLanguage", org.getNameLanguage()),
        new NameValuePair("descriptionLanguage", org.getDescriptionLanguage()),
        new NameValuePair("primaryContactType", org.getPrimaryContactType()),
        new NameValuePair("primaryContactName", org.getPrimaryContactName()),
        new NameValuePair("primaryContactAddress",
            org.getPrimaryContactAddress()),
        new NameValuePair("primaryContactDescription",
            org.getPrimaryContactDescription()),
        new NameValuePair("primaryContactEmail", org.getPrimaryContactEmail()),
        new NameValuePair("primaryContactPhone", org.getPrimaryContactPhone()),
        new NameValuePair("nodeKey", org.getNodeKey()),
        new NameValuePair("user", org.getUser()),
        new NameValuePair("organizationKey", org.getOrganizationKey()),
        new NameValuePair("password", org.getPassword()),};
    return nvp;
  }

  private final ResourceMetadataHandler metaHandler = new ResourceMetadataHandler();
  private final NewRegistryEntryHandler newRegistryEntryHandler = new NewRegistryEntryHandler();
  private final SAXParser saxParser;

  public RegistryManagerImpl() throws ParserConfigurationException,
      SAXException {
    super();
    SAXParserFactory factory = SAXParserFactory.newInstance();
    saxParser = factory.newSAXParser();
  }

  public void deleteResource(Resource resource) throws RegistryException {
    if (resource.isRegistered()) {
      setRegistryCredentials();
      String result = executeDelete(resource.getRegistryUrl(), true);
      if (result == null) {
        throw new RegistryException("Bad registry response");
      }
    } else {
      String msg = "Resource is not registered";
      log.warn(msg);
    }
  }

  public boolean isOrganizationRegistered(Organization org) {
    checkNotNull(org, "Organization was null");
    String key = org.getOrganizationKey();
    String password = org.getPassword();
    if (key == null || key.isEmpty() || password == null || password.isEmpty()) {
      return false;
    }
    NameValuePair[] params = {new NameValuePair("op", "login")};
    String registryUrl = AppConfig.getRegistryOrgUrl();
    String url = String.format("%s/%s", registryUrl, org.getOrganizationKey());
    setCredentials(org);
    return executeGet(url, params, true) != null;
  }

  public Collection<String> listAllExtensions() {
    log.info("Using extension definitions url: "
        + cfg.getExtensionDefinitionsUrl());
    return extractURLs(cfg.getExtensionDefinitionsUrl(), "extensions");
  }

  public Collection<String> listAllThesauri() {
    log.info("Using thesaurus definitions url: "
        + cfg.getThesaurusDefinitionsUrl());
    return extractURLs(cfg.getThesaurusDefinitionsUrl(), "thesauri");
  }

  /*
   * Service Binding: IPT RSS feed Service Binding: IPT EML (non-Javadoc)
   * 
   * @see org.gbif.provider.service.RegistryManager#registerIPT()
   */
  public String registerIPT() throws RegistryException {
    if (trimToNull(cfg.getIpt().getUddiID()) != null) {
      String msg = "IPT is already registered";
      log.warn(msg);
      throw new IllegalArgumentException(msg);
    }
    setRegistryCredentials();
    // registering IPT resource
    String key = registerResource(cfg.getIpt());
    if (key != null) {
      log.info("The IPT has been registered with GBIF as resource "
          + cfg.getIpt().getUddiID());
      // RSS resource feed service
      registerService(key, ServiceType.RSS, cfg.getAtomFeedURL());
      return key;
    }
    log.warn("Failed to register IPT with GBIF as a new resource");
    throw new RegistryException("No registry response or no key returned");
  }

  public Organization registerIptInstanceOrganization()
      throws RegistryException {
    return registerOrganization(getIptOrganization());
  }

  public Organization registerOrganization(Organization org)
      throws RegistryException {
    checkNotNull(org, "Organization was null");
    String orgKey = org.getOrganizationKey();
    checkArgument(trimToNull(orgKey) == null, String.format(
        "Organization alread registered: %s", orgKey));
    try {
      String registryUrl = AppConfig.getRegistryOrgUrl();
      setCredentials(org);
      String result = executePost(registryUrl, nameValuePairs(org), false);
      if (result != null) {
        log.info(String.format("Result: %s", result));
        saxParser.parse(getStream(result), newRegistryEntryHandler);
        org.setPassword(newRegistryEntryHandler.password);
        String key = newRegistryEntryHandler.key;
        if (trimToNull(key) == null) {
          key = newRegistryEntryHandler.organisationKey;
        }
        org.setOrganizationKey(key);
        log.info("A new organisation has been registered with GBIF under node "
            + org.getNodeKey() + " and with key " + key);
        return org;
      }
    } catch (Exception e) {
      throw new RegistryException("Error reading registry response", e);
    }
    throw new RegistryException("No registry response or no key returned");
  }

  /*
   * Potential services to register if available: Service Binding: TAPIR
   * Intermediate Service Binding: DwC Archive Service Binding: TCS Archive
   * Service Binding: EML Service Binding: WMS Service Binding: WFS
   * 
   * (non-Javadoc)
   * 
   * @see
   * org.gbif.provider.service.RegistryManager#registerResource(java.lang.Long)
   */
  public String registerResource(Resource resource) throws RegistryException {
    if (!resource.isPublic()) {
      String msg = "Resource " + resource.getId()
          + " needs to be published before it can be registered with GBIF";
      log.error(msg);
      throw new IllegalArgumentException(msg);
    }
    String resourceKey = registerResource(resource.getMeta());
    log.info("Resource " + resource.getId()
        + " has been registered with GBIF. Key = " + resource.getUddiID());
    try {
      registerService(resource, ServiceType.EML,
          cfg.getEmlUrl(resource.getGuid()));
    } catch (Exception e) {
      log.error(e);
    }
    if (DataResource.class.isAssignableFrom(resource.getClass())) {
      try {
        registerService(resource, ServiceType.DWC_ARCHIVE,
            cfg.getArchiveUrl(resource.getGuid()));
      } catch (Exception e) {
        log.error(e);
      }
    }
    if (resource instanceof OccurrenceResource) {
      try {
        registerService(resource, ServiceType.TAPIR,
            cfg.getTapirEndpoint(resource.getId()));
      } catch (Exception e) {
        log.error(e);
      }
      try {
        registerService(resource, ServiceType.WFS,
            cfg.getWfsEndpoint(resource.getId()));
      } catch (Exception e) {
        log.error(e);
      }
      try {
        registerService(resource, ServiceType.WMS,
            cfg.getWmsEndpoint(resource.getId()));
      } catch (Exception e) {
        log.error(e);
      }
    }
    if (resource instanceof ChecklistResource) {
      try {
        registerService(resource, ServiceType.TCS_RDF,
            cfg.getArchiveTcsUrl(resource.getGuid()));
      } catch (Exception e) {
        log.error(e);
      }
    }
    return resourceKey;
  }

  public boolean testLogin() {
    setRegistryCredentials();
    NameValuePair[] params = {new NameValuePair("op", "login")};
    return executeGet(getOrganisationUri(), params, true) != null;
  }

  public void updateIPT() throws RegistryException {
    if (!cfg.isIptRegistered()) {
      String msg = "IPT is not registered. Cannot update";
      log.warn(msg);
    } else {
      setRegistryCredentials();
      NameValuePair[] data = {
          new NameValuePair("name", trimToEmpty(cfg.getIpt().getTitle())),
          new NameValuePair("description",
              trimToEmpty(cfg.getIpt().getDescription())),
          new NameValuePair("homepageURL", trimToEmpty(cfg.getIpt().getLink())),
          new NameValuePair("primaryContactName",
              trimToEmpty(cfg.getIpt().getContactName())),
          new NameValuePair("primaryContactEmail",
              trimToEmpty(cfg.getIpt().getContactEmail()))};
      String result = executePost(getIptUri(), data, true);
      if (result == null) {
        throw new RegistryException("Bad registry response");
      }
    }
  }

  /**
   * Updates the organization associated with the IPT instance.
   */
  public Organization updateIptInstanceOrganization() throws RegistryException {
    return updateOrganization(getIptOrganization());
  }

  public Organization updateOrganization(Organization org)
      throws RegistryException {
    checkNotNull(org, "Organization was null");
    if (!isOrganizationRegistered(org)) {
      log.warn(String.format(
          "Organisation is not registered and cannot be updated: %s", org));
      return org;
    }
    setCredentials(org);
    String result = executePost(getOrganisationUri(), nameValuePairs(org), true);
    if (result == null) {
      throw new RegistryException("Bad registry response");
    }
    return org;
  }

  public void updateResource(Resource resource) throws RegistryException {
    if (!resource.isRegistered()) {
      String msg = "Resource is not registered. Cannot update";
      log.warn(msg);
    } else {
      setRegistryCredentials();
      NameValuePair[] data = {
          new NameValuePair("organisationKey",
              trimToEmpty(cfg.getOrg().getUddiID())),
          new NameValuePair("name", trimToEmpty(resource.getTitle())),
          new NameValuePair("description",
              trimToEmpty(resource.getDescription())),
          new NameValuePair("homepageURL", trimToEmpty(resource.getLink())),
          new NameValuePair("primaryContactName",
              trimToEmpty(resource.getContactName())),
          new NameValuePair("primaryContactEmail",
              trimToEmpty(resource.getContactEmail()))};
      String result = executePost(resource.getRegistryUrl(), data, true);
      if (result == null) {
        throw new RegistryException("Bad registry response");
      }
    }
  }

  /**
   * Executes a request to the provided registry URL and extracts the URLS of
   * each of the provided values
   * 
   * @param registryUrl To call, e.g.
   *          http://gbrds.gbif.org/registry/ipt/extensions.json
   * @param mapKey To extract the list from in the response JSON
   * @return The list of URL
   */
  protected List<String> extractURLs(String registryUrl, String mapKey) {
    GetMethod method = new GetMethod(registryUrl);
    try {
      client.executeMethod(method);
      String result = method.getResponseBodyAsString();

      if (result != null) {
        Map<String, Object> extensions = (Map) JSONUtil.deserialize(result);
        List<Map<String, String>> urlsInMaps = (List) extensions.get(mapKey);
        if (urlsInMaps != null) {
          List<String> urls = new LinkedList<String>();
          for (Map<String, String> urlMap : urlsInMaps) {
            urls.add(urlMap.get("url"));
          }
          return urls;
        } else {
          log.error("No urls were found in the response from: " + registryUrl);
        }
      }
    } catch (Exception e) {
      log.error("Error getting URLS from " + registryUrl, e);
    } finally {
      if (method != null) {
        method.releaseConnection();
      }
    }
    return new LinkedList<String>();
  }

  private Organization getIptOrganization() {
    return Organization.builder().description(cfg.getOrg().getDescription()).homepageUrl(
        cfg.getOrg().getLink()).name(cfg.getOrg().getTitle()).nodeKey(
        cfg.getOrgNode()).primaryContactType(ContactType.technical.name()).primaryContactName(
        cfg.getOrg().getContactName()).primaryContactEmail(
        cfg.getOrg().getContactEmail()).build();
  }

  private String getIptUri() {
    return String.format("%s/%s", cfg.getRegistryResourceUrl(),
        cfg.getIpt().getUddiID());
  }

  private String getOrganisationUri() {
    return String.format("%s/%s", cfg.getRegistryOrgUrl(),
        cfg.getOrg().getUddiID());
  }

  private String registerResource(ResourceMetadata meta)
      throws RegistryException {
    if (!cfg.isOrgRegistered()) {
      String msg = "Organisation is not registered. Cannot register resources";
      log.warn(msg);
      throw new RegistryException(msg);
    }
    // registering IPT resource
    NameValuePair[] data = {
        new NameValuePair("organisationKey",
            trimToEmpty(cfg.getOrg().getUddiID())),
        new NameValuePair("name", trimToEmpty(meta.getTitle())), // name
        new NameValuePair("description", trimToEmpty(meta.getDescription())), // description
        new NameValuePair("homepageURL", trimToEmpty(meta.getLink())),
        new NameValuePair("primaryContactType",
            ContactType.administrative.name()),
        new NameValuePair("primaryContactName",
            trimToEmpty(meta.getContactName())),
        new NameValuePair("primaryContactEmail",
            trimToEmpty(meta.getContactEmail()))};
    String result = executePost(cfg.getRegistryResourceUrl(), data, true);
    if (result != null) {
      // read new UDDI ID
      try {
        saxParser.parse(getStream(result), newRegistryEntryHandler);
        String key = newRegistryEntryHandler.key;
        if (trimToNull(key) == null) {
          key = newRegistryEntryHandler.resourceKey;
        }
        meta.setUddiID(key);
        if (meta.getUddiID() != null) {
          log.info("A new resource has been registered with GBIF. Key = " + key);
          return key;
        }
      } catch (Exception e) {
        throw new RegistryException("Error reading registry response", e);
      }
    }
    throw new RegistryException("No registry response or no key returned");
  }

  private String registerService(Resource resource, ServiceType serviceType,
      String accessPointURL) throws RegistryException {
    // validate that service is not already registered
    if (resource.getServiceUUID(serviceType) != null) {
      throw new IllegalArgumentException("Service is already registered");
    }
    String key = registerService(resource.getUddiID(), serviceType,
        accessPointURL);
    resource.putService(serviceType, key);
    return key;
  }

  private String registerService(String resourceKey, ServiceType serviceType,
      String accessPointURL) throws RegistryException {
    NameValuePair[] data = {
        new NameValuePair("resourceKey", trimToEmpty(resourceKey)),
        new NameValuePair("type", serviceType.code),
        new NameValuePair("accessPointURL", trimToEmpty(accessPointURL))};
    String result = executePost(cfg.getRegistryServiceUrl(), data, true);
    if (result != null) {
      // read new UDDI ID
      try {
        saxParser.parse(getStream(result), newRegistryEntryHandler);
        String key = newRegistryEntryHandler.key;
        if (trimToNull(key) == null) {
          key = newRegistryEntryHandler.serviceKey;
        }
        log.info("A new IPT service has been registered with GBIF. Key = "
            + key);
        return key;
      } catch (Exception e) {
        throw new RegistryException("Error reading registry response", e);
      }
    }
    throw new RegistryException("No registry response or no key returned");
  }

  /**
   * @param org void
   */
  private void setCredentials(Organization org) {
    String registryServiceUrl = AppConfig.getRegistryOrgUrl();
    URI uri;
    try {
      uri = new URI(registryServiceUrl);
    } catch (URISyntaxException e) {
      e.printStackTrace();
      return;
    }
    setCredentials(uri.getHost(), org.getOrganizationKey(), org.getPassword());
  }

  private void setRegistryCredentials() {
    try {
      URI geoURI = new URI(cfg.getRegistryOrgUrl());
      setCredentials(geoURI.getHost(), cfg.getOrg().getUddiID(),
          cfg.getOrgPassword());
    } catch (URISyntaxException e) {
      log.error("Exception setting the registry credentials", e);
    }
  }

}
