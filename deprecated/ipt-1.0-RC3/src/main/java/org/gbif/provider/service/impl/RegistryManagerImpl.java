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

import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.ResourceMetadata;
import org.gbif.provider.model.voc.ContactType;
import org.gbif.provider.model.voc.ServiceType;
import org.gbif.provider.model.xml.NewRegistryEntryHandler;
import org.gbif.provider.model.xml.ResourceMetadataHandler;
import org.gbif.provider.service.RegistryException;
import org.gbif.provider.service.RegistryManager;

import com.googlecode.jsonplugin.JSONUtil;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * TODO: Documentation.
 * 
 */
public class RegistryManagerImpl extends HttpBaseManager implements
    RegistryManager {
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
    if (StringUtils.trimToNull(cfg.getIpt().getUddiID()) != null) {
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

  public String registerOrg() throws RegistryException {
    // need to register a new organisation?
    if (StringUtils.trimToNull(cfg.getOrg().getUddiID()) != null) {
      String msg = "Organisation is already registered";
      log.warn(msg);
      throw new IllegalArgumentException(msg);
    }
    setRegistryCredentials();
    // http://code.google.com/p/gbif-registry/wiki/ExplanationUDDI#CREATE_ORGANISATION
    NameValuePair[] data = {
        new NameValuePair("nodeKey", StringUtils.trimToEmpty(cfg.getOrgNode())),
        new NameValuePair("name",
            StringUtils.trimToEmpty(cfg.getOrg().getTitle())),
        new NameValuePair("description",
            StringUtils.trimToEmpty(cfg.getOrg().getDescription())),
        new NameValuePair("homepageURL",
            StringUtils.trimToEmpty(cfg.getOrg().getLink())),
        new NameValuePair("primaryContactType", ContactType.technical.name()),
        new NameValuePair("primaryContactName",
            StringUtils.trimToEmpty(cfg.getOrg().getContactName())),
        new NameValuePair("primaryContactEmail",
            StringUtils.trimToEmpty(cfg.getOrg().getContactEmail()))};
    String result = executePost(cfg.getRegistryOrgUrl(), data, true);
    if (result != null) {
      // read new UDDI ID
      try {
        saxParser.parse(getStream(result), newRegistryEntryHandler);
        cfg.setOrgPassword(newRegistryEntryHandler.password);
        String key = newRegistryEntryHandler.key;
        if (StringUtils.trimToNull(key) == null) {
          key = newRegistryEntryHandler.organisationKey;
        }
        cfg.getOrg().setUddiID(key);
        log.info("A new organisation has been registered with GBIF under node "
            + cfg.getOrgNode() + " and with key " + key);
        return key;
      } catch (Exception e) {
        throw new RegistryException("Error reading registry response", e);
      }
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
    // http://server:port/registration/organisation/30?op=login
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
          new NameValuePair("name",
              StringUtils.trimToEmpty(cfg.getIpt().getTitle())),
          new NameValuePair("description",
              StringUtils.trimToEmpty(cfg.getIpt().getDescription())),
          new NameValuePair("homepageURL",
              StringUtils.trimToEmpty(cfg.getIpt().getLink())),
          new NameValuePair("primaryContactName",
              StringUtils.trimToEmpty(cfg.getIpt().getContactName())),
          new NameValuePair("primaryContactEmail",
              StringUtils.trimToEmpty(cfg.getIpt().getContactEmail()))};
      String result = executePost(getIptUri(), data, true);
      if (result == null) {
        throw new RegistryException("Bad registry response");
      }
    }
  }

  public void updateOrg() throws RegistryException {
    if (!cfg.isOrgRegistered()) {
      String msg = "Organisation is not registered. Cannot update";
      log.warn(msg);
    } else {
      setRegistryCredentials();
      NameValuePair[] data = {
          // new NameValuePair("nodeKey",
          // StringUtils.trimToEmpty(cfg.getOrgNode())),
          new NameValuePair("name",
              StringUtils.trimToEmpty(cfg.getOrg().getTitle())),
          new NameValuePair("description",
              StringUtils.trimToEmpty(cfg.getOrg().getDescription())),
          new NameValuePair("homepageURL",
              StringUtils.trimToEmpty(cfg.getOrg().getLink())),
          new NameValuePair("primaryContactName",
              StringUtils.trimToEmpty(cfg.getOrg().getContactName())),
          new NameValuePair("primaryContactEmail",
              StringUtils.trimToEmpty(cfg.getOrg().getContactEmail()))};
      String result = executePost(getOrganisationUri(), data, true);
      if (result == null) {
        throw new RegistryException("Bad registry response");
      }
    }
  }

  public void updateResource(Resource resource) throws RegistryException {
    if (!resource.isRegistered()) {
      String msg = "Resource is not registered. Cannot update";
      log.warn(msg);
    } else {
      setRegistryCredentials();
      NameValuePair[] data = {
          new NameValuePair("organisationKey",
              StringUtils.trimToEmpty(cfg.getOrg().getUddiID())),
          new NameValuePair("name",
              StringUtils.trimToEmpty(resource.getTitle())),
          new NameValuePair("description",
              StringUtils.trimToEmpty(resource.getDescription())),
          new NameValuePair("homepageURL",
              StringUtils.trimToEmpty(resource.getLink())),
          new NameValuePair("primaryContactName",
              StringUtils.trimToEmpty(resource.getContactName())),
          new NameValuePair("primaryContactEmail",
              StringUtils.trimToEmpty(resource.getContactEmail()))};
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
            StringUtils.trimToEmpty(cfg.getOrg().getUddiID())),
        new NameValuePair("name", StringUtils.trimToEmpty(meta.getTitle())), // name
        new NameValuePair("description",
            StringUtils.trimToEmpty(meta.getDescription())), // description
        new NameValuePair("homepageURL",
            StringUtils.trimToEmpty(meta.getLink())),
        new NameValuePair("primaryContactType",
            ContactType.administrative.name()),
        new NameValuePair("primaryContactName",
            StringUtils.trimToEmpty(meta.getContactName())),
        new NameValuePair("primaryContactEmail",
            StringUtils.trimToEmpty(meta.getContactEmail()))};
    String result = executePost(cfg.getRegistryResourceUrl(), data, true);
    if (result != null) {
      // read new UDDI ID
      try {
        saxParser.parse(getStream(result), newRegistryEntryHandler);
        String key = newRegistryEntryHandler.key;
        if (StringUtils.trimToNull(key) == null) {
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
        new NameValuePair("resourceKey", StringUtils.trimToEmpty(resourceKey)),
        new NameValuePair("type", serviceType.code),
        new NameValuePair("accessPointURL",
            StringUtils.trimToEmpty(accessPointURL))};
    String result = executePost(cfg.getRegistryServiceUrl(), data, true);
    if (result != null) {
      // read new UDDI ID
      try {
        saxParser.parse(getStream(result), newRegistryEntryHandler);
        String key = newRegistryEntryHandler.key;
        if (StringUtils.trimToNull(key) == null) {
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
