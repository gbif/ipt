package org.gbif.ipt.service.registry.impl;

import org.gbif.dwc.terms.DwcTerm;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.RegistryException.TYPE;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.utils.RegistryEntryHandler;
import org.gbif.ipt.validation.AgentValidator;
import org.gbif.metadata.eml.Agent;
import org.gbif.metadata.eml.Eml;
import org.gbif.utils.HttpUtil;
import org.gbif.utils.HttpUtil.Response;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.xml.sax.SAXException;

public class RegistryManagerImpl extends BaseManager implements RegistryManager {

  class RegistryServices {

    public String serviceURLs = null;
    public String serviceTypes = null;
  }

  private RegistryEntryHandler newRegistryEntryHandler = new RegistryEntryHandler();
  private static final String SERVICE_TYPE_EML = "EML";
  private static final String SERVICE_TYPE_OCCURRENCE = "DWC-ARCHIVE-OCCURRENCE";
  private static final String SERVICE_TYPE_CHECKLIST = "DWC-ARCHIVE-CHECKLIST";
  private static final String SERVICE_TYPE_RSS = "RSS";
  private static final String CONTACT_TYPE_TECHNICAL = "technical";
  private static final String CONTACT_TYPE_ADMINISTRATIVE = "administrative";

  private HttpUtil http;
  private SAXParser saxParser;

  private Gson gson;

  @Inject
  public RegistryManagerImpl(AppConfig cfg, DataDir dataDir, DefaultHttpClient client, SAXParserFactory saxFactory)
    throws ParserConfigurationException, SAXException {
    super(cfg, dataDir);
    this.saxParser = saxFactory.newSAXParser();
    this.http = new HttpUtil(client);
    this.gson = new Gson();
  }

  private List<NameValuePair> buildRegistryParameters(Resource resource) {
    List<NameValuePair> data = new ArrayList<NameValuePair>();

    Eml eml = resource.getEml();
    data.add(new BasicNameValuePair("name", resource.getTitle() != null ? StringUtils.trimToEmpty(resource.getTitle())
      : StringUtils.trimToEmpty(resource.getShortname())));
    data.add(new BasicNameValuePair("description", StringUtils.trimToEmpty(resource.getDescription())));
    data.add(new BasicNameValuePair("homepageURL", StringUtils.trimToEmpty(eml.getDistributionUrl())));
    data.add(new BasicNameValuePair("logoURL", StringUtils.trimToEmpty(eml.getLogoUrl())));

    // Eml contact agent:
    Agent primaryContact = getPrimaryContact(resource.getEml());
    // if primaryContact is null, use resource creator as primary contact.
    if (primaryContact == null) {
      primaryContact = new Agent();
      primaryContact.setEmail(resource.getCreator().getEmail());
      primaryContact.setFirstName(resource.getCreator().getFirstname());
      primaryContact.setLastName(resource.getCreator().getLastname());
      primaryContact.setRole(null);
    }
    String primaryContactType = primaryContact.getRole() == null ? CONTACT_TYPE_TECHNICAL : CONTACT_TYPE_ADMINISTRATIVE;

    // Change the role to null like was before.
    primaryContact.setRole(null);

    data.add(new BasicNameValuePair("primaryContactType", primaryContactType));
    data.add(new BasicNameValuePair("primaryContactEmail", StringUtils.trimToEmpty(primaryContact.getEmail())));
    data.add(new BasicNameValuePair("primaryContactName",
      StringUtils.trimToNull(StringUtils.trimToEmpty(primaryContact.getFullName()))));
    data.add(new BasicNameValuePair("primaryContactAddress",
      StringUtils.trimToEmpty(primaryContact.getAddress().toFormattedString())));
    data.add(new BasicNameValuePair("primaryContactPhone", StringUtils.trimToEmpty(primaryContact.getPhone())));

    // TODO: For a future release - depends on modification to Registry WS
    // data.add(new BasicNameValuePair("primaryContactFirstName",
    // StringUtils.trimToNull(StringUtils.trimToEmpty(primaryContact.getFirstName()))));
    // data.add(new BasicNameValuePair("primaryContactLastName",
    // StringUtils.trimToNull(StringUtils.trimToEmpty(primaryContact.getLastName()))));

    // see if we have a published dwca or if its only metadata
    RegistryServices services = buildServiceTypeParams(resource);
    data.add(new BasicNameValuePair("serviceTypes", services.serviceTypes));
    data.add(new BasicNameValuePair("serviceURLs", services.serviceURLs));

    return data;
  }

  /**
   * Builds service type parameters used in push or post to Registry. There can only be 3 different types of Services
   * that the IPT registers: EML, DWC-ARCHIVE-OCCURRENCE or DWC-ARCHIVE-CHECKLIST - that's it.
   *
   * @param resource published resource
   *
   * @return RegistryServices object, with urls and types strings
   */
  private RegistryServices buildServiceTypeParams(Resource resource) {
    RegistryServices rs = new RegistryServices();

    // the EML service is mandatory, so add the type and URL
    rs.serviceTypes = SERVICE_TYPE_EML;
    rs.serviceURLs = cfg.getResourceEmlUrl(resource.getShortname());

    // now check if there are any other services: either DWC-ARCHIVE-OCCURRENCE or DWC-ARCHIVE-CHECKLIST
    if (resource.hasPublishedData() && resource.getCoreTypeTerm() != null) {
      if (DwcTerm.Occurrence == resource.getCoreTypeTerm()) {
        log.debug("Registering EML & DwC-A Occurrence Service");
        rs.serviceURLs += "|" + cfg.getResourceArchiveUrl(resource.getShortname());
        rs.serviceTypes += "|" + SERVICE_TYPE_OCCURRENCE;
      } else if (DwcTerm.Taxon == resource.getCoreTypeTerm()) {
        log.debug("Registering EML & DwC-A Checklist Service");
        rs.serviceURLs += "|" + cfg.getResourceArchiveUrl(resource.getShortname());
        rs.serviceTypes += "|" + SERVICE_TYPE_CHECKLIST;
      } else {
        log.warn("Unknown core resource type " + resource.getCoreTypeTerm());
        log.debug("Registering EML service only");
      }
    } else {
      log.debug("Resource has no published data, therefore only the EML Service will be registered");
    }
    return rs;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.registry.RegistryManager#deregister(org.gbif.ipt.model.Resource)
   */
  public void deregister(Resource resource) throws RegistryException {
    try {
      if (resource.getOrganisation() != null) {
        Response resp =
          http.delete(getDeleteResourceUri(resource.getKey().toString()), orgCredentials(resource.getOrganisation()));
        if (http.success(resp)) {
          log.info("The resource has been deleted. Resource key: " + resource.getKey().toString());
        } else {
          throw new RegistryException(TYPE.BAD_RESPONSE, "Empty registry response");
        }
      } else {
        throw new RegistryException(TYPE.NOT_AUTHORISED, "Credentials should be specified");
      }
    } catch (IOException e) {
      throw new RegistryException(TYPE.IO_ERROR, e);
    } catch (Exception e) {
      throw new RegistryException(RegistryException.TYPE.BAD_RESPONSE, e);
    }

  }

  /**
   * Returns the delete resource URL
   */
  private String getDeleteResourceUri(String resourceKey) {
    return String.format("%s%s%s", cfg.getRegistryUrl(), "/registry/ipt/resource/", resourceKey);
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.registry.RegistryManager#getExtensions()
   */
  public List<Extension> getExtensions() throws RegistryException {
    try {
      Response resp = http.get(getExtensionsURL(true));
      if (resp.content != null) {
        Map<String, List<Extension>> jSONextensions =
          gson.fromJson(resp.content, new TypeToken<Map<String, List<Extension>>>() {
          }.getType());
        return jSONextensions.get("extensions");
      } else {
        throw new RegistryException(TYPE.BAD_RESPONSE, "Response content is null");
      }
    } catch (ClassCastException e) {
      throw new RegistryException(TYPE.BAD_RESPONSE, e);
    } catch (IOException e) {
      throw new RegistryException(TYPE.IO_ERROR, e);
    } catch (Exception e) {
      throw new RegistryException(TYPE.IO_ERROR, e);
    }
  }

  /**
   * Returns the Extensions url.
   */
  private String getExtensionsURL(boolean json) {
    return String.format("%s%s%s", cfg.getRegistryUrl(), "/registry/extensions", json ? ".json" : "/");
  }

  /**
   * Returns the IPT Resource url.
   */
  private String getIptResourceUri() {
    return String.format("%s%s", cfg.getRegistryUrl(), "/registry/ipt/resource");
  }

  /**
   * Returns the IPT update Resource url.
   */
  private String getIptUpdateResourceUri(String resourceKey) {
    return String.format("%s%s%s", cfg.getRegistryUrl(), "/registry/ipt/resource/", resourceKey);
  }

  /**
   * Returns the IPT update url used in GBIF Registry.
   */
  private String getIptUpdateUri(String iptKey) {
    return String.format("%s%s%s", cfg.getRegistryUrl(), "/registry/ipt/update/", iptKey);
  }

  /**
   * Returns the IPT url.
   */
  private String getIptUri() {
    return String.format("%s%s", cfg.getRegistryUrl(), "/registry/ipt/register");
  }

  /**
   * Returns the login URL.
   */
  private String getLoginURL(String organisationKey) {
    return String.format("%s%s%s%s", cfg.getRegistryUrl(), "/registry/organisation/", organisationKey, "?op=login");
  }

  public List<Organisation> getOrganisations() throws RegistryException {
    try {
      Response resp = http.get(getOrganisationsURL(true));
      if (resp.content != null) {
        List<Map<String, String>> organisationsTemp =
          gson.fromJson(resp.content, new TypeToken<List<Map<String, String>>>() {
          }.getType());
        List<Organisation> organisations = new ArrayList<Organisation>();
        int invalid = 0;
        for (Map<String, String> org : organisationsTemp) {
          if (org.isEmpty() || StringUtils.isBlank(org.get("key")) || StringUtils.isBlank(org.get("name"))) {
            invalid++;
          } else {
            Organisation o = new Organisation();
            o.setName(org.get("name"));
            try {
              o.setKey(org.get("key"));
              organisations.add(o);
            } catch (IllegalArgumentException e) {
              // this is not a uuid...
              invalid++;
            }
          }
          if (invalid > 0) {
            log.debug("Skipped " + invalid + " invalid organisation JSON objects");
          }
        }
        return organisations;
        // return gson.fromJson(resp.content, new TypeToken<List<Organisation>>() {}.getType());
      } else {
        throw new RegistryException(TYPE.BAD_RESPONSE, "Response content is null");
      }
    } catch (ClassCastException e) {
      throw new RegistryException(TYPE.BAD_RESPONSE, e);
    } catch (ConnectException e) {
      // This normally happend when a time out appear. Probably is a problem of firewall or proxy.
      throw new RegistryException(TYPE.PROXY, e);
    } catch (UnknownHostException e) {
      try {
        // if server can not connect to google. Probably the internet connection is not active.
        http.get("http://www.google.com");
      } catch (Exception e1) {
        throw new RegistryException(TYPE.NO_INTERNET, e);
      }
      // if server could connect to google. Probably GBIF Registry page is down.
      throw new RegistryException(TYPE.SITE_DOWN, e);
    } catch (IOException e) {
      throw new RegistryException(TYPE.IO_ERROR, e);
    } catch (Exception e) {
      throw new RegistryException(TYPE.UNKNOWN, e);
    }
  }

  /**
   * Returns the Organisations url
   */
  private String getOrganisationsURL(boolean json) {
    return String.format("%s%s%s", cfg.getRegistryUrl(), "/registry/organisation", json ? ".json" : "/");
  }

  /**
   * Returns the primary contact agent depending on the following rules:
   * 1. Resource Contact.
   * 2. If (1) is incomplete (missing email or last name) use Resource Creator.
   * 3. if (2) is incomplete, use Metadata Provider.
   * 4. if (3) is incomplete return null.
   */
  private Agent getPrimaryContact(Eml eml) {
    Agent[] primaryContacts = new Agent[3];
    primaryContacts[0] = eml.getContact();
    primaryContacts[1] = eml.getResourceCreator();
    primaryContacts[2] = eml.getMetadataProvider();
    int position = 0;
    for (Agent primaryContact : primaryContacts) {
      if (primaryContact != null && AgentValidator.hasCompleteContactInfo(primaryContact)) {
        // Setting the role to use it only in the primaryContact type validation, then it will return to null.
        switch (position) {
          case 0:
            primaryContact.setRole("PointOfContact");
            break;
          case 1:
            primaryContact.setRole("Originator");
            break;
          case 2:
            primaryContact.setRole("MetadataProvider");
            break;
          default:
            // it should never come here.
        }
        return primaryContact;
      }
      position++;
    }
    return null;
  }

  /**
   * Returns the ATOM url
   */
  private String getRssFeedURL() {
    return String.format("%s/rss.do", cfg.getBaseUrl());
  }

  protected InputStream getStream(String source) {
    return new ByteArrayInputStream(source.getBytes());
  }

  public List<Vocabulary> getVocabularies() throws RegistryException {
    try {
      Response resp = http.get(getVocabulariesURL(true));
      if (resp.content != null) {
        Map<String, List<Vocabulary>> map = gson.fromJson(resp.content, new TypeToken<Map<String, List<Vocabulary>>>() {
        }.getType());
        return map.get("thesauri");
      } else {
        throw new RegistryException(TYPE.BAD_RESPONSE, "Response content is null");
      }
    } catch (ClassCastException e) {
      throw new RegistryException(TYPE.BAD_RESPONSE, e);
    } catch (IOException e) {
      throw new RegistryException(TYPE.IO_ERROR, e);
    } catch (Exception e) {
      throw new RegistryException(TYPE.IO_ERROR, e);
    }
  }

  /**
   * Returns the Extensions url.
   */
  private String getVocabulariesURL(boolean json) {
    return String.format("%s%s%s", cfg.getRegistryUrl(), "/registry/thesauri", json ? ".json" : "/");
  }

  private UsernamePasswordCredentials orgCredentials(Organisation org) {
    return new UsernamePasswordCredentials(org.getKey().toString(), org.getPassword());
  }

  public UUID register(Resource resource, Organisation org, Ipt ipt) throws RegistryException {
    if (!resource.isPublished()) {
      log.warn("Cannot register, resource not published yet");
      return null;
    }

    // registering a new IPT resource
    log.debug("Last published: " + resource.getLastPublished());

    List<NameValuePair> data = buildRegistryParameters(resource);
    // add additional ipt and organisation parameters
    data.add(new BasicNameValuePair("organisationKey", StringUtils.trimToEmpty(org.getKey().toString())));
    data.add(new BasicNameValuePair("iptKey", StringUtils.trimToEmpty(ipt.getKey().toString())));

    try {
      UrlEncodedFormEntity uefe = new UrlEncodedFormEntity(data, HTTP.UTF_8);
      Response result = http.post(getIptResourceUri(), null, null, orgCredentials(org), uefe);
      if (result != null) {
        // read new UDDI ID
        saxParser.parse(getStream(result.content), newRegistryEntryHandler);
        String key = newRegistryEntryHandler.key;
        if (StringUtils.trimToNull(key) == null) {
          key = newRegistryEntryHandler.resourceKey;
        }
        try {
          UUID uuidKey = UUID.fromString(key);
          if (uuidKey != null) {
            resource.setKey(uuidKey);
            resource.setOrganisation(org);
            log.info("A new resource has been registered with GBIF. Key = " + key);
            return uuidKey;
          } else {
            throw new RegistryException(RegistryException.TYPE.BAD_RESPONSE, "Missing UUID key in response");
          }
        } catch (IllegalArgumentException e) {
          throw new RegistryException(RegistryException.TYPE.BAD_RESPONSE, "Invalid UUID key in response");
        }
      }
      throw new RegistryException(RegistryException.TYPE.BAD_RESPONSE, "Empty registry response");
    } catch (Exception e) {
      throw new RegistryException(RegistryException.TYPE.BAD_RESPONSE, e);
    }
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.registry.RegistryManager#registerIPT(org.gbif.ipt.model.Ipt)
   */
  public String registerIPT(Ipt ipt, Organisation org) throws RegistryException {
    // registering IPT resource

    List<NameValuePair> data = new ArrayList<NameValuePair>();
    data.add(new BasicNameValuePair("organisationKey", StringUtils.trimToEmpty(org.getKey().toString())));
    data.add(new BasicNameValuePair("name", StringUtils.trimToEmpty(ipt.getName()))); // name
    data.add(new BasicNameValuePair("description", StringUtils.trimToEmpty(ipt.getDescription()))); // description
    // IPT password used for updating the IPT's own metadata & issuing atomic updateURL operations
    data.add(new BasicNameValuePair("wsPassword", StringUtils.trimToEmpty(ipt.getWsPassword()))); // IPT instance
    // password
    data.add(new BasicNameValuePair("primaryContactType", ipt.getPrimaryContactType()));
    // TODO: For release 2.0.4
    // data.add(new BasicNameValuePair("primaryContactFirstName",
    // StringUtils.trimToEmpty(ipt.getPrimaryContactFirstName())));
    // data.add(new BasicNameValuePair("primaryContactLastName",
    // StringUtils.trimToEmpty(ipt.getPrimaryContactLastName())));
    data.add(new BasicNameValuePair("primaryContactName", StringUtils.trimToEmpty(ipt.getPrimaryContactName())));

    data.add(new BasicNameValuePair("primaryContactEmail", StringUtils.trimToEmpty(ipt.getPrimaryContactEmail())));
    data.add(new BasicNameValuePair("serviceTypes", SERVICE_TYPE_RSS));
    data.add(new BasicNameValuePair("serviceURLs", getRssFeedURL()));

    String key = null;
    try {
      UrlEncodedFormEntity uefe = new UrlEncodedFormEntity(data, HTTP.UTF_8);
      Response result = http.post(getIptUri(), null, null, orgCredentials(org), uefe);
      if (result != null) {
        // read new UDDI ID
        saxParser.parse(getStream(result.content), newRegistryEntryHandler);
        key = newRegistryEntryHandler.key;
        if (StringUtils.trimToNull(key) == null) {
          key = newRegistryEntryHandler.resourceKey;
        }
        log.info("A new ipt has been registered with GBIF. Key = " + key);
        ipt.setKey(key);
      } else {
        throw new RegistryException(RegistryException.TYPE.BAD_RESPONSE, "Bad registry response");
      }
    } catch (Exception e) {
      log.error("Bad registry response", e);
      throw new RegistryException(RegistryException.TYPE.BAD_RESPONSE, "Bad registry response");
    }
    return key;
  }

  public void updateIpt(Ipt ipt) {
    log.warn("Updating IPT instance throught GBIF Registry");
    UsernamePasswordCredentials iptCredentials =
      new UsernamePasswordCredentials(ipt.getKey().toString(), ipt.getWsPassword());
    List<NameValuePair> data = new ArrayList<NameValuePair>();
    data.add(new BasicNameValuePair("organisationKey", StringUtils.trimToEmpty(ipt.getKey().toString())));
    data.add(new BasicNameValuePair("name", StringUtils.trimToEmpty(ipt.getName())));
    data.add(new BasicNameValuePair("description", StringUtils.trimToEmpty(ipt.getDescription())));
    data.add(new BasicNameValuePair("language", StringUtils.trimToEmpty(ipt.getLanguage())));
    data.add(new BasicNameValuePair("homepageURL", StringUtils.trimToEmpty(ipt.getHomepageURL())));
    data.add(new BasicNameValuePair("logoURL", StringUtils.trimToEmpty(ipt.getLogoUrl())));
    // TODO: For release 2.0.4
    // data.add(new BasicNameValuePair("primaryContactFirstName",
    // StringUtils.trimToEmpty(ipt.getPrimaryContactFirstName())));
    // data.add(new BasicNameValuePair("primaryContactLastName",
    // StringUtils.trimToEmpty(ipt.getPrimaryContactLastName())));
    data.add(new BasicNameValuePair("primaryContactName", StringUtils.trimToEmpty(ipt.getPrimaryContactName())));

    data.add(new BasicNameValuePair("primaryContactType", StringUtils.trimToEmpty(ipt.getPrimaryContactType())));
    data.add(new BasicNameValuePair("primaryContactAddress", StringUtils.trimToEmpty(ipt.getPrimaryContactAddress())));
    data.add(new BasicNameValuePair("primaryContactEmail", StringUtils.trimToEmpty(ipt.getPrimaryContactEmail())));
    data.add(new BasicNameValuePair("primaryContactPhone", StringUtils.trimToEmpty(ipt.getPrimaryContactPhone())));
    data.add(new BasicNameValuePair("serviceTypes", SERVICE_TYPE_RSS));
    data.add(new BasicNameValuePair("serviceURLs", getRssFeedURL()));

    try {
      Response resp =
        http.post(getIptUpdateUri(ipt.getKey().toString()), null, null, iptCredentials, new UrlEncodedFormEntity(data));
      if (http.success(resp)) {
        log.debug("Ipt's registration info has been updated");
      } else {
        throw new RegistryException(RegistryException.TYPE.FAILED, "Registration update failed");
      }
    } catch (Exception e) {
      throw new RegistryException(RegistryException.TYPE.BAD_RESPONSE, "Bad registry response");

    }
  }

  public void updateResource(Resource resource) throws RegistryException, IllegalArgumentException {
    if (!resource.isRegistered()) {
      throw new IllegalArgumentException("Resource is not registered");
    }

    // registering IPT resource
    if (!resource.isPublished()) {
      log.warn("Updating registered resource although resource is not published yet");
    }

    log.debug("Last published: " + resource.getLastPublished());
    List<NameValuePair> data = buildRegistryParameters(resource);

    try {
      Response resp = http.post(getIptUpdateResourceUri(resource.getKey().toString()), null, null,
        orgCredentials(resource.getOrganisation()), new UrlEncodedFormEntity(data));
      if (http.success(resp)) {
        log.debug("Resource's registration info has been updated");
      } else {
        throw new RegistryException(RegistryException.TYPE.FAILED, "Registration update failed");
      }
    } catch (Exception e) {
      throw new RegistryException(RegistryException.TYPE.BAD_RESPONSE, "Bad registry response");
    }
  }

  public boolean validateOrganisation(String organisationKey, String password) {
    try {
      Response resp =
        http.get(getLoginURL(organisationKey), null, new UsernamePasswordCredentials(organisationKey, password));
      return http.success(resp);
    } catch (Exception e) {
      log.warn(e);
    }
    return false;
  }
}
