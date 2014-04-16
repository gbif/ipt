package org.gbif.ipt.service.registry.impl;

import org.gbif.dwc.terms.DwcTerm;
import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.RegistryException.TYPE;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
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
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.xml.sax.SAXException;

public class RegistryManagerImpl extends BaseManager implements RegistryManager {

  private static class RegistryServices {

    public String serviceURLs = null;
    public String serviceTypes = null;
  }

  private final RegistryEntryHandler newRegistryEntryHandler = new RegistryEntryHandler();
  private static final String SERVICE_TYPE_EML = "EML";
  private static final String SERVICE_TYPE_OCCURRENCE = "DWC-ARCHIVE-OCCURRENCE";
  private static final String SERVICE_TYPE_CHECKLIST = "DWC-ARCHIVE-CHECKLIST";
  private static final String SERVICE_TYPE_RSS = "RSS";
  private static final String CONTACT_TYPE_TECHNICAL = "technical";
  private static final String CONTACT_TYPE_ADMINISTRATIVE = "administrative";

  private HttpUtil http;
  private SAXParser saxParser;

  private Gson gson;

  private ConfigWarnings warnings;

  // create instance of BaseAction - allows class to retrieve i18n terms via getText()
  private BaseAction baseAction;

  @Inject
  public RegistryManagerImpl(AppConfig cfg, DataDir dataDir, HttpUtil httpUtil, SAXParserFactory saxFactory,
    ConfigWarnings warnings, SimpleTextProvider textProvider, RegistrationManager registrationManager)
    throws ParserConfigurationException, SAXException {
    super(cfg, dataDir);
    this.saxParser = saxFactory.newSAXParser();
    this.http = httpUtil;
    this.gson = new Gson();
    this.warnings = warnings;
    baseAction = new BaseAction(textProvider, cfg, registrationManager);
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
        if (HttpUtil.success(resp)) {
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
      String msg = "Bad registry response: " + e.getMessage();
      log.error(msg, e);
      throw new RegistryException(RegistryException.TYPE.BAD_RESPONSE, msg);
    }

  }

  /**
   * Returns the delete resource URL.
   */
  private String getDeleteResourceUri(String resourceKey) {
    return String.format("%s%s%s", cfg.getRegistryUrl(), "/registry/ipt/resource/", resourceKey);
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.registry.RegistryManager#getExtensions()
   */
  public List<Extension> getExtensions() throws RegistryException {
    Map<String, List<Extension>> jSONextensions = gson
      .fromJson(requestHttpGetFromRegistry(getExtensionsURL(true)).content,
        new TypeToken<Map<String, List<Extension>>>() {
        }.getType());
    return (jSONextensions.get("extensions") == null) ? new ArrayList<Extension>() : jSONextensions.get("extensions");
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

  /**
   * Returns the URI that will return a list of Resources associated to an Organization in JSON.
   */
  private String getOrganisationsResourcesUri(final String organisationKey) {
    return String.format("%s%s%s", cfg.getRegistryUrl(), "/registry/resource.json?organisationKey=", organisationKey);
  }

  /**
   * Returns the URI that will return a single Organization in JSON.
   */
  private String getOrganisationUri(final String organisationKey) {
    return String.format("%s%s%s", cfg.getRegistryUrl(), "/registry/organisation/", organisationKey + ".json");
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.registry.RegistryManager#getOrganisations()
   */
  public List<Organisation> getOrganisations() {
    List<Map<String, String>> organisationsTemp = new ArrayList<Map<String, String>>();
    try {
      organisationsTemp = gson
        .fromJson(requestHttpGetFromRegistry(getOrganisationsURL(true)).content,
          new TypeToken<List<Map<String, String>>>() {
          }.getType());
    } catch (RegistryException e) {
      // log as specific error message as possible about why the Registry error occurred
      String msg = RegistryException.logRegistryException(e.getType(), baseAction);
      // add startup error message about Registry error
      warnings.addStartupError(msg);
      log.error(msg);

      // add startup error message that explains the consequence of the Registry error
      msg = baseAction.getText("admin.organisations.couldnt.load", new String[] {cfg.getRegistryUrl()});
      warnings.addStartupError(msg);
      log.error(msg);
    }
    // populate Organisation list
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
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.registry.RegistryManager#getOrganisation()
   */
  public Organisation getRegisteredOrganisation(String key) {
    Organisation organisation = null;
    if (!Strings.isNullOrEmpty(key)) {
      try {
        organisation =
          gson.fromJson(requestHttpGetFromRegistry(getOrganisationUri(key)).content, new TypeToken<Organisation>() {
          }.getType());
      } catch (RegistryException e) {
        // log as specific error message as possible about why the Registry error occurred
        String msg = RegistryException.logRegistryException(e.getType(), baseAction);
        // add startup error message about Registry error
        warnings.addStartupError(msg);
        log.error(msg);

        // add startup error message that explains the consequence of the Registry error
        msg = baseAction.getText("admin.organisation.couldnt.load", new String[] {key, cfg.getRegistryUrl()});
        warnings.addStartupError(msg);
        log.error(msg);
      } catch (JsonSyntaxException e) {
        // add startup error message that explains the consequence of the error
        String msg = baseAction.getText("admin.organisation.couldnt.load", new String[] {key, cfg.getRegistryUrl()});
        warnings.addStartupError(msg);
        log.error(msg);
      }
    }

    return organisation;
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

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.registry.RegistryManager#getVocabularies()
   */
  public List<Vocabulary> getVocabularies() throws RegistryException {
    Map<String, List<Vocabulary>> map = gson.fromJson(requestHttpGetFromRegistry(getVocabulariesURL(true)).content,
      new TypeToken<Map<String, List<Vocabulary>>>() {
      }.getType());
    return (map.get("thesauri") == null) ? new ArrayList<Vocabulary>() : map.get("thesauri");
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.registry.RegistryManager#getOrganisationsResources
   */
  public List<Resource> getOrganisationsResources(String organisationKey) throws RegistryException {
    List<Map<String, String>> resourcesTemp;
    try {
      resourcesTemp = gson.fromJson(requestHttpGetFromRegistry(getOrganisationsResourcesUri(organisationKey)).content,
        new TypeToken<List<Map<String, String>>>() {
        }.getType());
    } catch (JsonSyntaxException e) {
      // throw new RegistryException if a non-parsable response was encountered
      throw new RegistryException(TYPE.BAD_RESPONSE, "Unexpected, non-parsable response format encountered.");
    } catch (RegistryException e) {
      // just rethrow if a RegistryException was encountered
      throw e;
    }
    // populate Resources list
    List<Resource> resources = new ArrayList<Resource>();
    int invalid = 0;
    for (Map<String, String> res : resourcesTemp) {
      if (res.isEmpty() || StringUtils.isBlank(res.get("key")) || StringUtils.isBlank(res.get("name"))) {
        invalid++;
      } else {
        Resource r = new Resource();
        r.setShortname(res.get("name"));
        r.setTitle(res.get("name"));

        String key = res.get("key");
        // key must be UUID - convert from String to UUID
        try {
          UUID uuid = UUID.fromString(key);
          r.setKey(uuid);
        } catch (IllegalArgumentException e) {
          invalid++;
        }
        resources.add(r);

      }
      if (invalid > 0) {
        log.debug("Skipped " + invalid + " invalid dataset JSON objects");
      }
    }
    return resources;
  }

  /**
   * Executes an HTTP Get Request against the GBIF Registry. If the content is not null, the Response is returned.
   * Otherwise, if the content was null, or an exception occurred, it throws the appropriate type of RegistryException.
   * 
   * @param url Get request URL
   * @return Response if the content was not null, or a RegistryException
   * @throws RegistryException (with RegistryException.type) if the content was null or an exception occurred
   */
  private Response requestHttpGetFromRegistry(String url) throws RegistryException {
    try {
      Response resp = http.get(url);
      if (resp != null && resp.content != null) {
        return resp;
      } else {
        throw new RegistryException(TYPE.BAD_RESPONSE, "Response content is null");
      }
    } catch (ClassCastException e) {
      throw new RegistryException(TYPE.BAD_RESPONSE, e);
    } catch (ConnectException e) {
      // normally happens when a timeout appears - probably a firewall or proxy problem.
      throw new RegistryException(TYPE.PROXY, e);
    } catch (UnknownHostException e) {
      try {
        // if server cannot connect to Google - probably the Internet connection is not active.
        http.get("http://www.google.com");
      } catch (Exception e1) {
        throw new RegistryException(TYPE.NO_INTERNET, e1);
      }
      // if server can connect to Google - probably the GBIF Registry page is down.
      throw new RegistryException(TYPE.SITE_DOWN, e);
    } catch (IOException e) {
      throw new RegistryException(TYPE.IO_ERROR, e);
    } catch (URISyntaxException e) {
      throw new RegistryException(TYPE.BAD_REQUEST,
        "Please check the request URL: " + ((url != null) ? url : "empty URL used!"));
    }
  }

  /**
   * Returns the Extensions url.
   */
  private String getVocabulariesURL(boolean json) {
    return String.format("%s%s%s", cfg.getRegistryUrl(), "/registry/thesauri", json ? ".json" : "/");
  }

  /**
   * Populate credentials for Organisation ws request.
   * 
   * @param org Organisation
   * @return credentials
   */
  private UsernamePasswordCredentials orgCredentials(Organisation org) {
    return new UsernamePasswordCredentials(org.getKey().toString(), org.getPassword());
  }

  public UUID register(Resource resource, Organisation org, Ipt ipt) throws RegistryException {
    if (!resource.isPublished()) {
      log.warn("Cannot register, resource not published yet");
      return null;
    }

    // registering a new resource
    log.debug("Last published: " + resource.getLastPublished());

    List<NameValuePair> data = buildRegistryParameters(resource);
    // add additional ipt and organisation parameters
    data.add(new BasicNameValuePair("organisationKey", StringUtils.trimToEmpty(org.getKey().toString())));
    data.add(new BasicNameValuePair("iptKey", StringUtils.trimToEmpty(ipt.getKey().toString())));

    try {
      UrlEncodedFormEntity uefe = new UrlEncodedFormEntity(data, Charset.forName("UTF-8"));
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
            throw new RegistryException(TYPE.BAD_RESPONSE, "Missing UUID key in response");
          }
        } catch (IllegalArgumentException e) {
          throw new RegistryException(TYPE.BAD_RESPONSE, "Invalid UUID key in response");
        }
      }
      throw new RegistryException(TYPE.BAD_RESPONSE, "Empty registry response");
    } catch (Exception e) {
      String msg = "Bad registry response: " + e.getMessage();
      log.error(msg, e);
      throw new RegistryException(TYPE.BAD_RESPONSE, msg);
    }
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.registry.RegistryManager#registerIPT(org.gbif.ipt.model.Ipt)
   */
  public String registerIPT(Ipt ipt, Organisation org) throws RegistryException {
    // registering IPT resource
    log.info("Registering IPT instance...");

    // populate params for ws
    String orgKey = (org != null && org.getKey() != null) ? org.getKey().toString() : null;
    List<NameValuePair> data = buildIPTParameters(ipt, orgKey);

    // add IPT password used for updating the IPT's own metadata & issuing atomic updateURL operations
    data.add(new BasicNameValuePair("wsPassword", StringUtils.trimToEmpty(ipt.getWsPassword()))); // IPT instance

    String key;
    try {
      UrlEncodedFormEntity uefe = new UrlEncodedFormEntity(data, Charset.forName("UTF-8"));
      Response result = http.post(getIptUri(), null, null, orgCredentials(org), uefe);
      if (result != null) {
        // read new UDDI ID
        saxParser.parse(getStream(result.content), newRegistryEntryHandler);
        key = newRegistryEntryHandler.key;
        // ensure key was found, otherwise throw Exception
        if (StringUtils.trimToNull(key) == null) {
          String msg = "Newly registered IPT Key not found";
          log.error(msg);
          throw new RegistryException(TYPE.BAD_RESPONSE, msg);
        }
        log.info("A new ipt has been registered with GBIF. Key = " + key);
        ipt.setKey(key);
      } else {
        String msg = "Bad registry response, response was null";
        log.error(msg);
        throw new RegistryException(TYPE.BAD_RESPONSE, msg);
      }
    } catch (Exception e) {
      String msg = "Bad registry response: " + e.getMessage();
      log.error(msg, e);
      throw new RegistryException(TYPE.BAD_RESPONSE, msg);
    }
    return key;
  }

  /**
   * Populate credentials for IPT ws request.
   * 
   * @param ipt IPT
   * @return credentials
   */
  private UsernamePasswordCredentials iptCredentials(Ipt ipt) {
    return new UsernamePasswordCredentials(ipt.getKey().toString(), ipt.getWsPassword());
  }

  /**
   * Populate a list of name value pairs used in the common ws requests for IPT registrations and updates.
   * 
   * @param ipt IPT
   * @param organisationKey Organisation key string
   * @return list of name value pairs, or an empty list if the IPT or organisation key were null
   */
  private List<NameValuePair> buildIPTParameters(Ipt ipt, String organisationKey) {
    List<NameValuePair> data = new ArrayList<NameValuePair>();
    if (ipt != null && organisationKey != null) {
      // main
      data.add(new BasicNameValuePair("organisationKey", StringUtils.trimToEmpty(organisationKey)));
      data.add(new BasicNameValuePair("name", StringUtils.trimToEmpty(ipt.getName())));
      data.add(new BasicNameValuePair("description", StringUtils.trimToEmpty(ipt.getDescription())));

      // primary contact
      data.add(new BasicNameValuePair("primaryContactType", StringUtils.trimToEmpty(ipt.getPrimaryContactType())));
      data.add(new BasicNameValuePair("primaryContactName", StringUtils.trimToEmpty(ipt.getPrimaryContactName())));
      data.add(new BasicNameValuePair("primaryContactEmail", StringUtils.trimToEmpty(ipt.getPrimaryContactEmail())));

      // service/endpoint
      data.add(new BasicNameValuePair("serviceTypes", SERVICE_TYPE_RSS));
      data.add(new BasicNameValuePair("serviceURLs", getRssFeedURL()));
    } else {
      log.debug("One or both of IPT and Organisation key were null. Params needed for ws will be empty");
    }
    return data;
  }

  public void updateIpt(Ipt ipt) throws RegistryException {
    log.info("Updating IPT registration...");

    // populate params for ws
    String orgKey = (ipt != null && ipt.getOrganisationKey() != null) ? ipt.getOrganisationKey().toString() : null;
    List<NameValuePair> data = buildIPTParameters(ipt, orgKey);

    try {
      Response resp = http.post(getIptUpdateUri(ipt.getKey().toString()), null, null, iptCredentials(ipt),
        new UrlEncodedFormEntity(data, Charset.forName("UTF-8")));
      if (HttpUtil.success(resp)) {
        log.info("IPT registration update was successful");
      } else {
        String msg = "Bad registry response";
        log.error(msg);
        throw new RegistryException(TYPE.BAD_RESPONSE, msg);
      }
    } catch (Exception e) {
      String msg = "Bad registry response: " + e.getMessage();
      log.error(msg, e);
      throw new RegistryException(TYPE.BAD_RESPONSE, "Bad registry response");
    }
  }

  public void updateResource(Resource resource, String iptKey) throws RegistryException, IllegalArgumentException {
    if (!resource.isRegistered()) {
      throw new IllegalArgumentException("Resource is not registered");
    }

    // registering IPT resource
    if (!resource.isPublished()) {
      log.warn("Updating registered resource although resource is not published yet");
    }

    log.debug("Last published: " + resource.getLastPublished());
    List<NameValuePair> data = buildRegistryParameters(resource);

    // ensure IPT serves relationship always gets created/updated
    data.add(new BasicNameValuePair("iptKey", StringUtils.trimToEmpty(iptKey)));

    try {
      Response resp = http.post(getIptUpdateResourceUri(resource.getKey().toString()), null, null,
        orgCredentials(resource.getOrganisation()), new UrlEncodedFormEntity(data, Charset.forName("UTF-8")));
      if (HttpUtil.success(resp)) {
        log.debug("Resource's registration info has been updated");
      } else {
        throw new RegistryException(TYPE.BAD_RESPONSE, "Registration update failed");
      }
    } catch (Exception e) {
      String msg = "Bad registry response: " + e.getMessage();
      log.error(msg, e);
      throw new RegistryException(TYPE.BAD_RESPONSE, msg);
    }
  }

  public boolean validateOrganisation(String organisationKey, String password) {
    try {
      Response resp =
        http.get(getLoginURL(organisationKey), null, new UsernamePasswordCredentials(organisationKey, password));
      return HttpUtil.success(resp);
    } catch (Exception e) {
      log.warn(
        "The organisation could not be validated using key (" + organisationKey + ") and password (" + password + ")",
        e);
    }
    return false;
  }
}
