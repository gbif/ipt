package org.gbif.ipt.service.registry.impl;

import org.gbif.api.model.common.DOI;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.ConfigWarnings;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.VersionHistory;
import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.RegistryException.Type;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.utils.RegistryEntryHandler;
import org.gbif.ipt.validation.AgentValidator;
import org.gbif.metadata.eml.Agent;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.EmlFactory;
import org.gbif.utils.HttpClient;
import org.gbif.utils.HttpUtil;
import org.gbif.utils.ExtendedResponse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
  private static final String SERVICE_TYPE_SAMPLING_EVENT = "DWC-ARCHIVE-SAMPLING-EVENT";
  private static final String SERVICE_TYPE_RSS = "RSS";
  private static final String CONTACT_TYPE_TECHNICAL = "technical";
  private static final String CONTACT_TYPE_ADMINISTRATIVE = "administrative";

  private final HttpClient http;
  private SAXParser saxParser;
  private Gson gson;
  private ConfigWarnings warnings;
  private ResourceManager resourceManager;
  // create instance of BaseAction - allows class to retrieve i18n terms via getText()
  private BaseAction baseAction;

  @Inject
  public RegistryManagerImpl(AppConfig cfg, DataDir dataDir, HttpClient client, SAXParserFactory saxFactory,
                             ConfigWarnings warnings, SimpleTextProvider textProvider, RegistrationManager registrationManager,
                             ResourceManager resourceManager)
    throws ParserConfigurationException, SAXException {
    super(cfg, dataDir);
    this.saxParser = saxFactory.newSAXParser();
    this.http = client;
    this.gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    this.warnings = warnings;
    this.resourceManager = resourceManager;
    baseAction = new BaseAction(textProvider, cfg, registrationManager);
  }

  private List<NameValuePair> buildRegistryParameters(Resource resource) {
    List<NameValuePair> data = new ArrayList<>();

    Eml eml = resource.getEml();

    // the DOI assigned/registered to the last published public version (not the DOI reserved)
    DOI doi = resource.getAssignedDoi();
    if (doi != null) {
      data.add(new BasicNameValuePair("doi", doi.toString()));
      LOG.debug("Including registry param doi=" + doi);
    }
    // otherwise try using the DOI citation identifier of the last published public version, see issue #1276
    else {
      DOI existingDoi = getLastPublishedVersionExistingDoi(resource);
      if (existingDoi != null) {
        data.add(new BasicNameValuePair("doi", existingDoi.toString()));
        LOG.debug("Including registry param doi=" + existingDoi);
      }
    }

    data.add(new BasicNameValuePair("name", resource.getTitle() != null ? StringUtils.trimToEmpty(resource.getTitle())
      : StringUtils.trimToEmpty(resource.getShortname())));

    data.add(new BasicNameValuePair("description", String.join(System.lineSeparator(), eml.getDescription())));
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
   * Builds service type parameters used in push or post to Registry. There can only be 4 different types of Services
   * that the IPT registers: EML, DWC-ARCHIVE-OCCURRENCE, DWC-ARCHIVE-CHECKLIST, DWC-ARCHIVE-SAMPLING-EVENT - that's it.
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

    // check are there any other services: DWC-ARCHIVE-OCCURRENCE, DWC-ARCHIVE-CHECKLIST, or DWC-ARCHIVE-SAMPLING-EVENT
    if (resource.hasPublishedData() && resource.getCoreTypeTerm() != null) {
      if (DwcTerm.Occurrence == resource.getCoreTypeTerm()) {
        LOG.debug("Registering EML & DwC-A Occurrence Service");
        rs.serviceURLs += "|" + cfg.getResourceArchiveUrl(resource.getShortname());
        rs.serviceTypes += "|" + SERVICE_TYPE_OCCURRENCE;
      } else if (DwcTerm.Taxon == resource.getCoreTypeTerm()) {
        LOG.debug("Registering EML & DwC-A Checklist Service");
        rs.serviceURLs += "|" + cfg.getResourceArchiveUrl(resource.getShortname());
        rs.serviceTypes += "|" + SERVICE_TYPE_CHECKLIST;
      } else if (DwcTerm.Event == resource.getCoreTypeTerm()) {
        LOG.debug("Registering EML & DwC-A Sampling Event Service");
        rs.serviceURLs += "|" + cfg.getResourceArchiveUrl(resource.getShortname());
        rs.serviceTypes += "|" + SERVICE_TYPE_SAMPLING_EVENT;
      } else {
        LOG.warn("Unknown core resource type " + resource.getCoreTypeTerm());
        LOG.debug("Registering EML service only");
      }
    } else {
      LOG.debug("Resource has no published data, therefore only the EML Service will be registered");
    }
    return rs;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.registry.RegistryManager#deregister(org.gbif.ipt.model.Resource)
   */
  @Override
  public void deregister(Resource resource) throws RegistryException {
    String url = getDeleteResourceUri(resource.getKey().toString());
    try {
      if (resource.getOrganisation() != null) {
        ExtendedResponse resp = http.delete(url, orgCredentials(resource.getOrganisation()));
        if (HttpUtil.success(resp)) {
          LOG.info("The resource has been deleted. Resource key: " + resource.getKey().toString());
        } else {
          LOG.error("Deregister resource response received=" + resp.getStatusCode() + ": " + resp.getContent());
          throw new RegistryException(Type.BAD_RESPONSE, url, "Empty registry response");
        }
      } else {
        throw new RegistryException(Type.NOT_AUTHORISED, null, "Credentials should be specified");
      }
    } catch (IOException e) {
      throw new RegistryException(Type.IO_ERROR, url, e);
    } catch (Exception e) {
      String msg = "Bad registry response: " + e.getMessage();
      LOG.error(msg, e);
      throw new RegistryException(RegistryException.Type.BAD_RESPONSE, url, msg);
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
  @Override
  public List<Extension> getExtensions() throws RegistryException {
    Map<String, List<Extension>> jSONExtensions = gson
      .fromJson(requestHttpGetFromRegistry(getExtensionsURL(true)).getContent(),
        new TypeToken<Map<String, List<Extension>>>() {
        }.getType());
    return (jSONExtensions.get("extensions") == null) ? new ArrayList<>() : jSONExtensions.get("extensions");
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
  @Override
  public List<Organisation> getOrganisations() {
    List<Map<String, String>> organisationsTemp = new ArrayList<>();
    try {
      organisationsTemp = gson
        .fromJson(requestHttpGetFromRegistry(getOrganisationsURL(true)).getContent(),
          new TypeToken<List<Map<String, String>>>() {
          }.getType());
    } catch (RegistryException e) {
      // log as specific error message as possible about why the Registry error occurred
      String msg = RegistryException.logRegistryException(e, baseAction);
      // add startup error message about Registry error
      warnings.addStartupError(msg);
      LOG.error(msg);

      // add startup error message that explains the consequence of the Registry error
      msg = baseAction.getText("admin.organisations.couldnt.load", new String[] {cfg.getRegistryUrl()});
      warnings.addStartupError(msg);
      LOG.error(msg);
    }
    // populate Organisation list
    List<Organisation> organisations = new ArrayList<>();
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
        LOG.debug("Skipped " + invalid + " invalid organisation JSON objects");
      }
    }
    return organisations;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.registry.RegistryManager#getOrganisation()
   */
  @Override
  public Organisation getRegisteredOrganisation(String key) {
    Organisation organisation = null;
    if (StringUtils.isNotBlank(key)) {
      try {
        organisation =
          gson.fromJson(requestHttpGetFromRegistry(getOrganisationUri(key)).getContent(), new TypeToken<Organisation>() {
          }.getType());
      } catch (RegistryException e) {
        // log as specific error message as possible about why the Registry error occurred
        String msg = RegistryException.logRegistryException(e, baseAction);
        // add startup error message about Registry error
        warnings.addStartupError(msg);
        LOG.error(msg);

        // add startup error message that explains the consequence of the Registry error
        msg = baseAction.getText("admin.organisation.couldnt.load", new String[] {key, cfg.getRegistryUrl()});
        warnings.addStartupError(msg);
        LOG.error(msg);
      } catch (JsonSyntaxException e) {
        // add startup error message that explains the consequence of the error
        String msg = baseAction.getText("admin.organisation.couldnt.load", new String[] {key, cfg.getRegistryUrl()});
        warnings.addStartupError(msg);
        LOG.error(msg);
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
    List<Agent> agents = new ArrayList<>();
    for (Agent contact: eml.getContacts()) {
      contact.setRole("PointOfContact");
      agents.add(contact);
    }
    for (Agent creator: eml.getCreators()) {
      creator.setRole("Originator");
      agents.add(creator);
    }
    for (Agent metadataProvider: eml.getMetadataProviders()) {
      metadataProvider.setRole("MetadataProvider");
      agents.add(metadataProvider);
    }
    for (Agent agent: agents) {
      if (AgentValidator.hasCompleteContactInfo(agent)) {
        return agent;
      }
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
  @Override
  public List<Vocabulary> getVocabularies() throws RegistryException {
    Map<String, List<Vocabulary>> jSONVocabularies = gson
      .fromJson(requestHttpGetFromRegistry(getVocabulariesURL(true)).getContent(),
      new TypeToken<Map<String, List<Vocabulary>>>() {
      }.getType());
    return (jSONVocabularies.get("thesauri") == null) ? new ArrayList<>() : jSONVocabularies.get("thesauri");
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.registry.RegistryManager#getOrganisationsResources
   */
  @Override
  public List<Resource> getOrganisationsResources(String organisationKey) throws RegistryException {
    List<Map<String, String>> resourcesTemp;
    String url = getOrganisationsResourcesUri(organisationKey);
    try {
      resourcesTemp = gson.fromJson(requestHttpGetFromRegistry(url).getContent(),
          new TypeToken<List<Map<String, String>>>() {}.getType());
    } catch (JsonSyntaxException e) {
      // throw new RegistryException if a non-parsable response was encountered
      throw new RegistryException(Type.BAD_RESPONSE, url, "Unexpected, non-parsable response format encountered.");
    }
    // populate Resources list
    List<Resource> resources = new ArrayList<>();
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
        LOG.debug("Skipped " + invalid + " invalid dataset JSON objects");
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
  private ExtendedResponse requestHttpGetFromRegistry(String url) throws RegistryException {
    try {
      ExtendedResponse resp = http.get(url);
      if (resp.getContent() != null) {
        return resp;
      } else {
        throw new RegistryException(Type.BAD_RESPONSE, url, "Response content is null");
      }
    } catch (RegistryException e) { // The one thrown 2 lines above
      throw e;
    } catch (ClassCastException e) {
      throw new RegistryException(Type.BAD_RESPONSE, url, e);
    } catch (ConnectException | SocketTimeoutException e) {
      // normally happens when a timeout appears - probably a firewall or proxy problem.
      throw new RegistryException(Type.PROXY, url, e);
    } catch (UnknownHostException e) {
      try {
        // if server cannot connect to Google - probably the Internet connection is not active.
        http.get("https://www.google.com");
      } catch (Exception e1) {
        throw new RegistryException(Type.NO_INTERNET, url, e1);
      }
      // if server can connect to Google - probably the GBIF Registry page is down.
      throw new RegistryException(Type.SITE_DOWN, url, e);
    } catch (IOException e) {
      throw new RegistryException(Type.IO_ERROR, url, e);
    } catch (URISyntaxException e) {
      throw new RegistryException(Type.BAD_REQUEST, url,
        "Please check the request URL: " + ((url != null) ? url : "empty URL used!"));
    } catch (Exception e) {
      throw new RegistryException(Type.UNKNOWN, url, e);
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

  @Override
  public UUID register(Resource resource, Organisation org, Ipt ipt) throws RegistryException {
    LOG.debug("Registering resource...");

    if (!resource.isPublished()) {
      LOG.warn("Cannot register, resource not published yet");
      return null;
    }

    // populate params for ws call to register resource
    List<NameValuePair> data = buildRegistryParameters(resource);
    // add additional ipt and organisation parameters
    data.add(new BasicNameValuePair("organisationKey", StringUtils.trimToEmpty(org.getKey().toString())));
    data.add(new BasicNameValuePair("iptKey", StringUtils.trimToEmpty(ipt.getKey().toString())));

    ExtendedResponse resp;
    String url = getIptResourceUri();
    try {
      resp = http.post(url, null, orgCredentials(org),
        new UrlEncodedFormEntity(data, StandardCharsets.UTF_8));
    } catch (URISyntaxException e) {
      throw new RegistryException(Type.BAD_REQUEST, url, "Register resource failed: request URI invalid", e);
    } catch (IOException e) {
      throw new RegistryException(Type.IO_ERROR, url, "Register resource failed: I/O exception occurred", e);
    }

    if (HttpUtil.success(resp)) {
      LOG.info("Register resource was successful!");
    } else {
      Type type = getRegistryExceptionType(resp.getStatusCode());
      throw new RegistryException(type, url, "Register resource failed: " + resp.getStatusLine());
    }

    // parse GBIF UDDI key
    String key;
    try {
      saxParser.parse(getStream(resp.getContent()), newRegistryEntryHandler);
      key = newRegistryEntryHandler.key;
      if (StringUtils.trimToNull(key) == null) {
        key = newRegistryEntryHandler.resourceKey;
      }
    } catch (SAXException e) {
      throw new RegistryException(Type.BAD_RESPONSE, url, "Response received from resource registration couldn't be parsed", e);
    } catch (IOException e) {
      throw new RegistryException(Type.IO_ERROR, url, "Response received from resource registration couldn't be parsed", e);
    }

    // ensure non-empty key was found
    if (StringUtils.trimToNull(key) == null) {
      throw new RegistryException(Type.BAD_RESPONSE, url, "Response received from resource registration missing key!");
    }

    // ensure valid UUID was found
    UUID uuidKey;
    try {
      uuidKey = UUID.fromString(key);
    } catch (IllegalArgumentException e) {
      throw new RegistryException(Type.BAD_RESPONSE, url, "Response received from resource registration has invalid key");
    }

    LOG.info("A new resource has been registered with GBIF. [Key=" + key + "]");
    resource.setKey(uuidKey);
    resource.setOrganisation(org);
    return uuidKey;
  }

  @Override
  public String registerIPT(Ipt ipt, Organisation org) throws RegistryException {
    LOG.info("Registering IPT instance...");

    // populate params for ws call to register IPT
    String orgKey = org.getKey().toString();
    List<NameValuePair> data = buildIPTParameters(ipt, orgKey);
    // add IPT password used for updating the IPT
    data.add(new BasicNameValuePair("wsPassword", StringUtils.trimToEmpty(ipt.getWsPassword()))); // IPT instance

    ExtendedResponse resp;
    String url = getIptUri();
    try {
      resp = http.post(url, null, orgCredentials(org), new UrlEncodedFormEntity(data, StandardCharsets.UTF_8));
    } catch (URISyntaxException e) {
      throw new RegistryException(Type.BAD_REQUEST, url, "Register IPT failed: request URI invalid", e);
    } catch (IOException e) {
      throw new RegistryException(Type.IO_ERROR, url, "Register IPT failed: I/O exception occurred", e);
    }

    if (HttpUtil.success(resp)) {
      LOG.info("Register IPT was successful!");
    } else {
      Type type = getRegistryExceptionType(resp.getStatusCode());
      throw new RegistryException(type, url, "Register IPT failed: " + resp.getStatusLine());
    }

    // parse GBIF UUID key from response
    String key;
    try {
      saxParser.parse(getStream(resp.getContent()), newRegistryEntryHandler);
      key = newRegistryEntryHandler.key;
    } catch (SAXException e) {
      throw new RegistryException(Type.BAD_RESPONSE, url, "Response received from IPT registration couldn't be parsed", e);
    } catch (IOException e) {
      throw new RegistryException(Type.IO_ERROR, url, "Response received from IPT registration couldn't be parsed", e);
    }

    // ensure non-empty key was found
    if (StringUtils.trimToNull(key) == null) {
      throw new RegistryException(Type.BAD_RESPONSE, url, "Response received from IPT registration missing key!");
    }

    // ensure valid UUID was found
    UUID uuidKey;
    try {
      uuidKey = UUID.fromString(key);
    } catch (IllegalArgumentException e) {
      throw new RegistryException(Type.BAD_RESPONSE, url, "Response received from IPT registration has invalid key");
    }

    LOG.info("A new ipt has been registered with GBIF. [Key=" + uuidKey + "]");
    ipt.setKey(uuidKey.toString());
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
    List<NameValuePair> data = new ArrayList<>();
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
      LOG.debug("One or both of IPT and Organisation key were null. Params needed for ws will be empty");
    }
    return data;
  }

  @Override
  public void updateIpt(Ipt ipt) throws RegistryException {
    LOG.info("Update IPT registration...");

    // populate params for ws call to update IPT
    String orgKey = (ipt != null && ipt.getOrganisationKey() != null) ? ipt.getOrganisationKey().toString() : null;
    List<NameValuePair> data = buildIPTParameters(ipt, orgKey);

    ExtendedResponse resp;
    String url = getIptUpdateUri(ipt.getKey().toString());
    try {
      resp = http.post(url, null, iptCredentials(ipt),
        new UrlEncodedFormEntity(data, StandardCharsets.UTF_8));
    } catch (URISyntaxException e) {
      throw new RegistryException(Type.BAD_REQUEST, url, "Update IPT registration failed: request URI invalid", e);
    } catch (IOException e) {
      throw new RegistryException(Type.IO_ERROR, url, "Update IPT registration failed: I/O exception occurred", e);
    }

    if (HttpUtil.success(resp)) {
      LOG.info("Update IPT registration was successful!");
    } else {
      // to continue updating registered resources, IPT update must have been successful
      Type type = getRegistryExceptionType(resp.getStatusCode());
      throw new RegistryException(type, url, "Update IPT registration failed: " + resp.getStatusLine());
    }

    List<Resource> resources = resourceManager.list(PublicationStatus.REGISTERED);
    if (!resources.isEmpty()) {
      LOG.info("Next, update " + resources.size() + " resource registrations...");
      for (Resource resource : resources) {
        try {
          updateResource(resource, ipt.getKey().toString());
        } catch (IllegalArgumentException e) {
          LOG.error(e.getMessage());
        }
      }
      LOG.info("Resource registrations updated successfully!");
    }
  }

  @Override
  public void updateResource(Resource resource, String iptKey) throws RegistryException, IllegalArgumentException {
    if (!resource.isRegistered() || resource.getKey() == null) {
      throw new IllegalArgumentException(
        "Update resource registration failed: resource [shortname=" + resource.getShortname() + "] is not registered");
    }

    LOG.info("Update resource registration... [key=" + resource.getKey().toString() + "]");
    // populate params for ws call to update registered resource
    List<NameValuePair> data = buildRegistryParameters(resource);
    // ensure IPT serves relationship always gets created/updated
    data.add(new BasicNameValuePair("iptKey", StringUtils.trimToEmpty(iptKey)));

    ExtendedResponse resp;
    String url = getIptUpdateResourceUri(resource.getKey().toString());
    try {
      resp = http.post(url, null,
        orgCredentials(resource.getOrganisation()), new UrlEncodedFormEntity(data, StandardCharsets.UTF_8));
    } catch (URISyntaxException e) {
      throw new RegistryException(Type.BAD_REQUEST, url, "Update resource registration failed: request URI invalid", e);
    } catch (IOException e) {
      throw new RegistryException(Type.IO_ERROR, url, "Update resource registration failed: I/O exception occurred", e);
    }

    if (HttpUtil.success(resp)) {
      LOG.info("Update resource registration was successful! [key=" + resource.getKey().toString() + "]");
        // to avoid repetition, alert user here that update was successful
        baseAction.addActionMessage(
          baseAction.getText("manage.overview.resource.update.registration", new String[] {resource.getTitle()}));
    } else {
      Type type = getRegistryExceptionType(resp.getStatusCode());
      throw new RegistryException(type, url, "Update resource registration failed [shortname=" + resource.getShortname() + ", key=" + resource.getKey().toString() + "]: " + resp.getStatusLine());
    }
  }

  /**
   * Determine the type of RegistryException based on the error response code. In this context, error response codes
   * include all response codes higher than 300.
   *
   * @param code response code from GBIF Registry web service response
   *
   * @return RegistryException type based on response code
   */
  protected Type getRegistryExceptionType(int code) {
    // never called on successful codes include OK (200) and CREATED (201)
    if (code <= 300) {
      throw new IllegalArgumentException();
    }
    Type type;
    switch (code) {
      case 400:
        type = Type.BAD_REQUEST;
        break;
      case 401:
        type = Type.NOT_AUTHORISED;
        break;
      default:
        type = Type.BAD_RESPONSE;
    }
    return type;
  }

  @Override
  public boolean validateOrganisation(String organisationKey, String password) {
    try {
      ExtendedResponse resp =
          http.get(getLoginURL(organisationKey), new UsernamePasswordCredentials(organisationKey, password));
      return HttpUtil.success(resp);
    } catch (Exception e) {
      LOG.warn(
        "The organisation could not be validated using key (" + organisationKey + ") and password (" + password + ")",
        e);
    }
    return false;
  }

  /**
   * @return DOI citation identifier of last published version or null if no DOI citation identifier was assigned
   */
  protected DOI getLastPublishedVersionExistingDoi(Resource resource) {
    VersionHistory lastPublishedVersion = resource.getLastPublishedVersion();
    if (lastPublishedVersion != null) {
      BigDecimal version = new BigDecimal(lastPublishedVersion.getVersion());
      File emlFile = cfg.getDataDir().resourceEmlFile(resource.getShortname(), version);
      if (emlFile.exists()) {
        try {
          LOG.debug("Loading EML from file: " + emlFile.getAbsolutePath());
          InputStream in = new FileInputStream(emlFile);
          Eml eml = EmlFactory.build(in);
          if (eml.getCitation() != null && StringUtils.isNotBlank(eml.getCitation().getIdentifier())) {
            String identifier = StringUtils.trimToNull(eml.getCitation().getIdentifier());
            if (DOI.isParsable(identifier)) {
              return new DOI(identifier);
            }
          }
        } catch (Exception e) {
          LOG.error("Failed to check last published version citation identifier: " + e.getMessage(), e);
        }
      }
    }
    return null;
  }
}
