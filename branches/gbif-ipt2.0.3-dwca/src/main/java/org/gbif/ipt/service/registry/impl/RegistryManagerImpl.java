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
import org.gbif.metadata.eml.Eml;
import org.gbif.utils.HttpUtil;
import org.gbif.utils.HttpUtil.Response;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.xml.sax.SAXException;

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

public class RegistryManagerImpl extends BaseManager implements RegistryManager {
  class RegistryServices {
    public String serviceURLs = null;
    public String serviceTypes = null;
  }

  private RegistryEntryHandler newRegistryEntryHandler = new RegistryEntryHandler();
  private static final String SERVICE_TYPE_EML = "EML";
  private static final String SERVICE_TYPE_DWCA = "DWC-ARCHIVE";
  private static final String SERVICE_TYPE_OCCURRENCE = "DWC-ARCHIVE-OCCURRENCE";
  private static final String SERVICE_TYPE_CHECKLIST = "DWC-ARCHIVE-CHECKLIST";
  private static final String SERVICE_TYPE_RSS = "RSS";
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
    data.add(new BasicNameValuePair("name", ((resource.getTitle() != null)
        ? StringUtils.trimToEmpty(resource.getTitle()) : StringUtils.trimToEmpty(resource.getShortname()))));
    data.add(new BasicNameValuePair("description", StringUtils.trimToEmpty(resource.getDescription())));
    data.add(new BasicNameValuePair("homepageURL", StringUtils.trimToEmpty(eml.getDistributionUrl())));
    data.add(new BasicNameValuePair("logoURL", StringUtils.trimToEmpty(eml.getLogoUrl())));

    // TODO: should this not be the eml contact agent instead?
    data.add(new BasicNameValuePair("primaryContactType", "technical"));
    data.add(new BasicNameValuePair("primaryContactName",
        StringUtils.trimToNull(StringUtils.trimToEmpty(resource.getCreator().getName()))));
    data.add(new BasicNameValuePair("primaryContactEmail", StringUtils.trimToEmpty(resource.getCreator().getEmail())));

    // the following are not yet supported by the registry at the time of writing, but a request has been logged:
    // http://code.google.com/p/gbif-registry/issues/detail?id=88
    data.add(new BasicNameValuePair("primaryContactFirstName",
        StringUtils.trimToNull(StringUtils.trimToEmpty(resource.getCreator().getFirstname()))));
    data.add(new BasicNameValuePair("primaryContactLastName",
        StringUtils.trimToNull(StringUtils.trimToEmpty(resource.getCreator().getLastname()))));

    // see if we have a published dwca or if its only metadata
    RegistryServices services = buildServiceTypeParams(resource);
    data.add(new BasicNameValuePair("serviceTypes", services.serviceTypes));
    data.add(new BasicNameValuePair("serviceURLs", services.serviceURLs));

    return data;
  }

  private RegistryServices buildServiceTypeParams(Resource resource) {
    RegistryServices rs = new RegistryServices();
    rs.serviceTypes = SERVICE_TYPE_EML;
    rs.serviceURLs = cfg.getResourceEmlUrl(resource.getShortname());
    if (resource.hasPublishedData()) {
      rs.serviceURLs += "|" + cfg.getResourceArchiveUrl(resource.getShortname());
      if (DwcTerm.Occurrence.equals(resource.getCoreType())) {
        log.debug("Registering EML & DwC-A Occurrence Service");
        rs.serviceTypes += "|" + SERVICE_TYPE_OCCURRENCE;
      } else if (DwcTerm.Taxon.equals(resource.getCoreType())) {
        log.debug("Registering EML & DwC-A Checklist Service");
        rs.serviceTypes += "|" + SERVICE_TYPE_CHECKLIST;
      } else {
        log.warn("Unknown core resource type " + resource.getCoreType());
        log.debug("Registering EML & general DwC-A Service");
        rs.serviceTypes += "|" + SERVICE_TYPE_DWCA;
      }
    } else {
      log.debug("Registering EML Service only");
    }
    return rs;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.ipt.service.registry.RegistryManager#deregister(org.gbif.ipt.model.Resource)
   */
  public void deregister(Resource resource) throws RegistryException {
    try {
      if (resource.getOrganisation() != null) {
        Response resp = http.delete(getDeleteResourceUri(resource.getKey().toString()),
            orgCredentials(resource.getOrganisation()));
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
   * 
   * @return
   */
  private String getDeleteResourceUri(String resourceKey) {
    return String.format("%s%s%s", cfg.getRegistryUrl(), "/registry/ipt/resource/", resourceKey);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.ipt.service.registry.RegistryManager#getExtensions()
   */
  public List<Extension> getExtensions() throws RegistryException {
    try {
      Response resp = http.get(getExtensionsURL(true));
      if (resp.content != null) {
        Map<String, List<Extension>> jSONextensions = gson.fromJson(resp.content,
            new TypeToken<Map<String, List<Extension>>>() {
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
   * Returns the Extensions url
   * 
   * @param json
   * @return
   */
  private String getExtensionsURL(boolean json) {
    return String.format("%s%s%s", cfg.getRegistryUrl(), "/registry/extensions", json ? ".json" : "/");
  }

  /**
   * Returns the IPT Resource url
   * 
   * @return
   */
  private String getIptResourceUri() {
    return String.format("%s%s", cfg.getRegistryUrl(), "/registry/ipt/resource");
  }

  /**
   * Returns the IPT update Resource url
   * 
   * @return
   */
  private String getIptUpdateResourceUri(String resourceKey) {
    return String.format("%s%s%s", cfg.getRegistryUrl(), "/registry/ipt/resource/", resourceKey);
  }

  /**
   * Returns the IPT update url used in GBIF Registry
   * 
   * @return
   */
  private String getIptUpdateUri(String iptKey) {
    return String.format("%s%s%s", cfg.getRegistryUrl(), "/registry/ipt/update/", iptKey);
  }

  /**
   * Returns the IPT url
   * 
   * @return
   */
  private String getIptUri() {
    return String.format("%s%s", cfg.getRegistryUrl(), "/registry/ipt/register");
  }

  /**
   * Returns the login URL
   * 
   * @param organisationKey
   * @return
   */
  private String getLoginURL(String organisationKey) {
    return String.format("%s%s%s%s", cfg.getRegistryUrl(), "/registry/organisation/", organisationKey, "?op=login");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.ipt.service.registry.RegistryManager#getOrganisations()
   */
  public List<Organisation> getOrganisations() throws RegistryException {
    try {
      Response resp = http.get(getOrganisationsURL(true));
      if (resp.content != null) {
        List<Map<String, String>> organisationsTemp = gson.fromJson(resp.content,
            new TypeToken<List<Map<String, String>>>() {
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
   * 
   * @param json
   * @return
   */
  private String getOrganisationsURL(boolean json) {
    return String.format("%s%s%s", cfg.getRegistryUrl(), "/registry/organisation", json ? ".json" : "/");
  }

  /**
   * Returns the ATOM url
   * 
   * @return
   */
  private String getRssFeedURL() {
    return String.format("%s/rss.do", cfg.getBaseURL());
  }

  protected InputStream getStream(String source) {
    return new ByteArrayInputStream(source.getBytes());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.ipt.service.registry.RegistryManager#getVocabularies()
   */
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
   * Returns the Extensions url
   * 
   * @param json
   * @return
   */
  private String getVocabulariesURL(boolean json) {
    return String.format("%s%s%s", cfg.getRegistryUrl(), "/registry/thesauri", json ? ".json" : "/");
  }

  private UsernamePasswordCredentials orgCredentials(Organisation org) {
    return new UsernamePasswordCredentials(org.getKey().toString(), org.getPassword());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.ipt.service.registry.RegistryManager#register(org.gbif.ipt.model.Resource,
   * org.gbif.ipt.model.Organisation, org.gbif.ipt.model.Ipt)
   */
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
        UUID uuidKey;
        try {
          uuidKey = UUID.fromString(key);
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
   * 
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
    UsernamePasswordCredentials iptCredentials = new UsernamePasswordCredentials(ipt.getKey().toString(),
        ipt.getWsPassword());
    List<NameValuePair> data = new ArrayList<NameValuePair>();
    data.add(new BasicNameValuePair("organisationKey", StringUtils.trimToEmpty(ipt.getKey().toString())));
    data.add(new BasicNameValuePair("name", StringUtils.trimToEmpty(ipt.getName())));
    data.add(new BasicNameValuePair("description", StringUtils.trimToEmpty(ipt.getDescription())));
    data.add(new BasicNameValuePair("language", StringUtils.trimToEmpty(ipt.getLanguage())));
    data.add(new BasicNameValuePair("homepageURL", StringUtils.trimToEmpty(ipt.getHomepageURL())));
    data.add(new BasicNameValuePair("logoURL", StringUtils.trimToEmpty(ipt.getLogoUrl())));
    data.add(new BasicNameValuePair("primaryContactName", StringUtils.trimToEmpty(ipt.getPrimaryContactName())));
    data.add(new BasicNameValuePair("primaryContactType", StringUtils.trimToEmpty(ipt.getPrimaryContactType())));
    data.add(new BasicNameValuePair("primaryContactAddress", StringUtils.trimToEmpty(ipt.getPrimaryContactAddress())));
    data.add(new BasicNameValuePair("primaryContactEmail", StringUtils.trimToEmpty(ipt.getPrimaryContactEmail())));
    data.add(new BasicNameValuePair("primaryContactPhone", StringUtils.trimToEmpty(ipt.getPrimaryContactPhone())));
    data.add(new BasicNameValuePair("serviceTypes", SERVICE_TYPE_RSS));
    data.add(new BasicNameValuePair("serviceURLs", getRssFeedURL()));

    // data.add(new BasicNameValuePair("primaryContactFirstName", "")); TODO
    // data.add(new BasicNameValuePair("primaryContactLastName", "")); TODO

    try {
      Response resp = http.post(getIptUpdateUri(ipt.getKey().toString()), null, null, iptCredentials,
          new UrlEncodedFormEntity(data));
      if (http.success(resp)) {
        log.debug("Ipt's registration info has been updated");
      } else {
        throw new RegistryException(RegistryException.TYPE.FAILED, "Registration update failed");
      }
    } catch (Exception e) {
      throw new RegistryException(RegistryException.TYPE.BAD_RESPONSE, "Bad registry response");

    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.ipt.service.registry.RegistryManager#updateResource(org.gbif.ipt.model.Resource,
   * org.gbif.ipt.model.Organisation, org.gbif.ipt.model.Ipt)
   */
  public void updateResource(Resource resource, Ipt ipt) throws RegistryException, IllegalArgumentException {
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
      Response resp = http.get(getLoginURL(organisationKey), null, new UsernamePasswordCredentials(organisationKey,
          password));
      if (http.success(resp)) {
        return true;
      }
      return false;
    } catch (Exception e) {
      log.warn(e);
    }
    return false;
  }
}
