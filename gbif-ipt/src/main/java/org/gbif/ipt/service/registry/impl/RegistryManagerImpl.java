package org.gbif.ipt.service.registry.impl;

import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.utils.NewRegistryEntryHandler;
import org.gbif.metadata.eml.Eml;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class RegistryManagerImpl extends BaseManager implements RegistryManager {
  // private ResourceMetadataHandler metaHandler = new ResourceMetadataHandler();
  private NewRegistryEntryHandler newRegistryEntryHandler = new NewRegistryEntryHandler();

  public static final String FORM_URL_ENCODED_CONTENT_TYPE = "application/x-www-form-urlencoded";

  protected static HttpClient client = new HttpClient(new MultiThreadedHttpConnectionManager());

  private SAXParser saxParser;

  public RegistryManagerImpl() throws ParserConfigurationException, SAXException {
    super();
    SAXParserFactory factory = SAXParserFactory.newInstance();
    saxParser = factory.newSAXParser();
  }

  /**
   * Takes an JSON object and returns an Extension object
   * 
   * @param ext
   * @return
   */
  private Extension buildExtension(JSONObject ext) {
    Extension extension = new Extension();
    try {
      extension.setDescription(ext.getString("description"));
      extension.setUrl(new URL(ext.getString("url")));
      extension.setTitle(ext.getString("title"));
      extension.setSubject(ext.getString("subject"));
      extension.setRowType(ext.getString("identifier"));
    } catch (JSONException e) {
      e.printStackTrace();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return extension;
  }

  /**
   * Takes an JSON object and returns an Organisation object
   * 
   * @param ext
   * @return
   */
  private Organisation buildOrganisation(JSONObject org) {
    Organisation organisation = new Organisation();
    try {
      organisation.setName(org.getString("name"));
      organisation.setKey(org.getString("key"));
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return organisation;
  }

  /**
   * @param jsonObject
   * @return
   */
  private Vocabulary buildVocabulary(JSONObject ext) {
    Vocabulary extension = new Vocabulary();
    try {
      extension.setUri(ext.getString("identifier"));
      extension.setUrl(new URL(ext.getString("url")));
      extension.setDescription(ext.getString("description"));
      extension.setTitle(ext.getString("title"));
      extension.setSubject(ext.getString("subject"));
    } catch (JSONException e) {
      e.printStackTrace();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return extension;
  }

  /**
   * Executes a generic POST request
   * 
   * @param uri
   * @param params
   * @param authenticate
   * @return
   */
  protected String executePost(String uri, NameValuePair[] params, boolean authenticate) {
    String result = null;
    log.info("Posting to " + uri);
    PostMethod method = newHttpPost(uri, authenticate);
    method.setRequestBody(params);
    try {
      client.executeMethod(method);
      if (succeeded(method)) {
        result = method.getResponseBodyAsString();
      }
    } catch (Exception e) {
      log.warn(uri + ": " + e.toString());
    } finally {
      if (method != null) {
        method.releaseConnection();
      }
    }
    return result;
  }
  
  /**
   * Executes a generic POST request
   * 
   * @param uri
   * @param params
   * @param authenticate
   * @return
   */
  protected boolean executeUpdate(String uri, NameValuePair[] params, boolean authenticate) {
    boolean result = false;
    log.info("Posting to " + uri);
    PostMethod method = newHttpPost(uri, authenticate);
    method.setRequestBody(params);
    try {
      client.executeMethod(method);
      if (succeeded(method)) {
        result = true;
      }
    } catch (Exception e) {
      log.warn(uri + ": " + e.toString());
    } finally {
      if (method != null) {
        method.releaseConnection();
      }
    }
    return result;
  }  

  /**
   * Returns the ATOM url
   * 
   * @return
   */
  private String getAtomFeedURL() {
    return String.format("%s/atom.xml", cfg.getBaseURL());
  }

  /**
   * Returns the DwCArchive url
   * 
   * @param resName
   * @return
   */
  private String getDwcArchiveURL(String resName) {
    return String.format("%s/archive.do?resource=%s", cfg.getBaseURL(), resName);
  }

  /**
   * Returns the EML url
   * 
   * @param resName
   * @return
   */
  private String getEmlURL(String resName) {
    return String.format("%s/eml.do?resource=%s", cfg.getBaseURL(), resName);
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.registry.RegistryManager#getExtensions()
   */
  public List<Extension> getExtensions() {
    List<Extension> extensions = new ArrayList<Extension>();
    JSONObject jSONextensions = null;
    String result = null;
    GetMethod method = new GetMethod(getExtensionsURL(true));
    try {
      client.executeMethod(method);
      if (succeeded(method)) {
        result = method.getResponseBodyAsString();
        if (result != null) {
          jSONextensions = new JSONObject(result);

          JSONArray jSONArray = (JSONArray) jSONextensions.get("extensions");

          for (int i = 0; i < jSONArray.length(); i++) {
            extensions.add(buildExtension((JSONObject) jSONArray.get(i)));
          }
        }
      }
    } catch (JSONException e) {
      e.printStackTrace();
    } catch (HttpException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return extensions;
  }

  /**
   * Returns the Extensions url
   * 
   * @param json
   * @return
   */
  private String getExtensionsURL(boolean json) {
    return String.format("%s%s%s", cfg.getRegistryUrl(), "extensions", json ? ".json" : "/");
  }

  /**
   * Returns the IPT Resource url
   * 
   * @return
   */
  private String getIptResourceUri() {
    return String.format("%s%s", cfg.getRegistryUrl(), "ipt/resource");
  }
  
  /**
   * Returns the IPT update Resource url
   * 
   * @return
   */
  private String getIptUpdateResourceUri(String resourceKey) {
    return String.format("%s%s%s", cfg.getRegistryUrl(), "ipt/resource/", resourceKey);
  }  

  /**
   * Returns the IPT url
   * 
   * @return
   */
  private String getIptUri() {
    return String.format("%s%s", cfg.getRegistryUrl(), "ipt/register");
  }

  /**
   * Returns the login URL
   * 
   * @param organisationKey
   * @return
   */
  private String getLoginURL(String organisationKey) {
    return String.format("%s%s%s%s", cfg.getRegistryUrl(), "organisation/", organisationKey, "?op=login");
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.registry.RegistryManager#getOrganisations()
   */
  public List<Organisation> getOrganisations() {
    List<Organisation> organisations = new ArrayList<Organisation>();
    JSONArray jSONorganisations = null;
    String result = null;
    System.out.println("url: " + getOrganisationsURL(true));
    GetMethod method = new GetMethod(getOrganisationsURL(true));
    try {
      client.executeMethod(method);
      if (succeeded(method)) {
        result = method.getResponseBodyAsString();
        if (result != null) {
          jSONorganisations = new JSONArray(result);

          for (int i = 0; i < jSONorganisations.length(); i++) {
            organisations.add(buildOrganisation((JSONObject) jSONorganisations.get(i)));
          }
        }
      }
    } catch (JSONException e) {
      e.printStackTrace();
    } catch (HttpException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return organisations;
  }

  /**
   * Returns the Organisations url
   * 
   * @param json
   * @return
   */
  private String getOrganisationsURL(boolean json) {
    return String.format("%s%s%s", cfg.getRegistryUrl(), "organisation", json ? ".json" : "/");
  }

  protected InputStream getStream(String source) {
    return new ByteArrayInputStream(source.getBytes());
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.registry.RegistryManager#getVocabularies()
   */
  public List<Vocabulary> getVocabularies() {
    List<Vocabulary> extensions = new ArrayList<Vocabulary>();
    JSONObject jSONextensions = null;
    String result = null;
    GetMethod method = new GetMethod(getVocabulariesURL(true));
    try {
      client.executeMethod(method);
      if (succeeded(method)) {
        result = method.getResponseBodyAsString();
        if (result != null) {
          jSONextensions = new JSONObject(result);

          JSONArray jSONArray = (JSONArray) jSONextensions.get("thesauri");

          for (int i = 0; i < jSONArray.length(); i++) {
            extensions.add(buildVocabulary((JSONObject) jSONArray.get(i)));
          }
        }
      }
    } catch (JSONException e) {
      e.printStackTrace();
    } catch (HttpException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return extensions;
  }

  /**
   * Returns the Extensions url
   * 
   * @param json
   * @return
   */
  private String getVocabulariesURL(boolean json) {
    return String.format("%s%s%s", cfg.getRegistryUrl(), "thesauri", json ? ".json" : "/");
  }

  /**
   * @param url
   * @param authenticate
   * @return
   */
  private PostMethod newHttpPost(String url, boolean authenticate) {
    PostMethod method = new PostMethod(url);
    method.setDoAuthentication(authenticate);
    return method;
  }
  
  

  /* (non-Javadoc)
   * @see org.gbif.ipt.service.registry.RegistryManager#updateResource(org.gbif.ipt.model.Resource, org.gbif.ipt.model.Organisation, org.gbif.ipt.model.Ipt)
   */
  public UUID updateResource(Resource resource, Organisation organisation, Ipt ipt) throws RegistryException {
    Eml eml = resource.getEml();
    // registering IPT resource

    // services should be registered?
    String serviceTypes = null;
    String serviceURLs = null;
    log.debug("Last published: " + resource.getLastPublished());
    if (resource.getLastPublished() != null) {
      log.debug("Registering DWC & EML Service");
      serviceTypes = "EML|DWC-ARCHIVE";
      serviceURLs = getEmlURL(resource.getShortname()) + "|" + getDwcArchiveURL(resource.getShortname());
    }
    else {
      log.debug("No DWC & EML Service present");
    }

    NameValuePair[] data = {
        //new NameValuePair("organisationKey", StringUtils.trimToEmpty(organisation.getKey().toString())),
        //new NameValuePair("iptKey", StringUtils.trimToEmpty(ipt.getKey().toString())),
        new NameValuePair("name", ((resource.getTitle() != null) ? StringUtils.trimToEmpty(resource.getTitle())
            : StringUtils.trimToEmpty(resource.getShortname()))), // name
        new NameValuePair("description", StringUtils.trimToEmpty(resource.getDescription())), // description
        new NameValuePair("primaryContactType", "technical"),
        new NameValuePair("primaryContactName",
            StringUtils.trimToNull(StringUtils.trimToEmpty(resource.getCreator().getName()))),
        new NameValuePair("primaryContactEmail", StringUtils.trimToEmpty(resource.getCreator().getEmail())),
        new NameValuePair("serviceTypes", serviceTypes), new NameValuePair("serviceURLs", serviceURLs)};
    boolean result = executeUpdate(getIptUpdateResourceUri(resource.getKey().toString()), data, true);
    if (result) {
      // read new UDDI ID
      log.debug("Resource's registration info has been updated");
    } else {
      throw new RegistryException(RegistryException.TYPE.BAD_RESPONSE, "Bad registry response");
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.registry.RegistryManager#register(org.gbif.ipt.model.Resource,
   * org.gbif.ipt.model.Organisation, org.gbif.ipt.model.Ipt)
   */
  public UUID register(Resource resource, Organisation organisation, Ipt ipt) throws RegistryException {
    Eml eml = resource.getEml();
    // registering IPT resource

    // services should be registered?
    String serviceTypes = null;
    String serviceURLs = null;
    log.debug("Last published: " + resource.getLastPublished());
    if (resource.getLastPublished() != null) {
      log.debug("Registering DWC & EML Service");
      serviceTypes = "EML|DWC-ARCHIVE";
      serviceURLs = getEmlURL(resource.getShortname()) + "|" + getDwcArchiveURL(resource.getShortname());
    }
    else {
      log.debug("No DWC & EML Service present");
    }

    NameValuePair[] data = {
        new NameValuePair("organisationKey", StringUtils.trimToEmpty(organisation.getKey().toString())),
        new NameValuePair("iptKey", StringUtils.trimToEmpty(ipt.getKey().toString())),
        new NameValuePair("name", ((resource.getTitle() != null) ? StringUtils.trimToEmpty(resource.getTitle())
            : StringUtils.trimToEmpty(resource.getShortname()))), // name
        new NameValuePair("description", StringUtils.trimToEmpty(resource.getDescription())), // description
        new NameValuePair("primaryContactType", "technical"),
        new NameValuePair("primaryContactName",
            StringUtils.trimToNull(StringUtils.trimToEmpty(resource.getCreator().getName()))),
        new NameValuePair("primaryContactEmail", StringUtils.trimToEmpty(resource.getCreator().getEmail())),
        new NameValuePair("serviceTypes", serviceTypes), new NameValuePair("serviceURLs", serviceURLs)};
    String result = executePost(getIptResourceUri(), data, true);
    if (result != null) {
      // read new UDDI ID
      try {
        saxParser.parse(getStream(result), newRegistryEntryHandler);
        String key = newRegistryEntryHandler.key;
        if (StringUtils.trimToNull(key) == null) {
          key = newRegistryEntryHandler.resourceKey;
        }
        resource.setKey(UUID.fromString(key));
        if (key != null) {
          log.info("A new resource has been registered with GBIF. Key = " + key);
          return UUID.fromString(key);
        }
      } catch (Exception e) {
        throw new RegistryException(RegistryException.TYPE.BAD_RESPONSE, "Bad registry response");
      }
    } else {
      throw new RegistryException(RegistryException.TYPE.BAD_RESPONSE, "Bad registry response");
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.registry.RegistryManager#registerIPT(org.gbif.ipt.model.Ipt)
   */
  public String registerIPT(Ipt ipt) throws RegistryException {
    // registering IPT resource
    NameValuePair[] data = {
        new NameValuePair("organisationKey", StringUtils.trimToEmpty(ipt.getOrganisationKey().toString())),
        new NameValuePair("name", StringUtils.trimToEmpty(ipt.getName())), // name
        new NameValuePair("description", StringUtils.trimToEmpty(ipt.getDescription())), // description
        new NameValuePair("wsPassword", StringUtils.trimToEmpty(ipt.getWsPassword())), // description
        new NameValuePair("primaryContactType", ipt.getPrimaryContactType()),
        new NameValuePair("primaryContactName", StringUtils.trimToEmpty(ipt.getPrimaryContactName())),
        new NameValuePair("primaryContactEmail", StringUtils.trimToEmpty(ipt.getPrimaryContactEmail())),
        new NameValuePair("serviceTypes", "RSS"), new NameValuePair("serviceURLs", getAtomFeedURL())};
    String result = executePost(getIptUri(), data, true);
    if (result != null) {
      // read new UDDI ID
      try {
        saxParser.parse(getStream(result), newRegistryEntryHandler);
        String key = newRegistryEntryHandler.key;
        if (StringUtils.trimToNull(key) == null) {
          key = newRegistryEntryHandler.resourceKey;
        }
        ipt.setKey(key);
        if (key != null) {
          log.info("A new ipt has been registered with GBIF. Key = " + key);
          return key;
        }
      } catch (Exception e) {
        throw new RegistryException(RegistryException.TYPE.BAD_RESPONSE, "Bad registry response");
      }
    } else {
      throw new RegistryException(RegistryException.TYPE.BAD_RESPONSE, "Bad registry response");
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.registry.RegistryManager#setRegistryCredentials(java.lang.String, java.lang.String)
   */
  public void setRegistryCredentials(String username, String password) {
    try {
      URI geoURI = new URI(cfg.getRegistryUrl());
      AuthScope scope = new AuthScope(geoURI.getHost(), -1, AuthScope.ANY_REALM); // AuthScope.ANY;
      client.getState().setCredentials(scope,
          new UsernamePasswordCredentials(StringUtils.trimToEmpty(username), StringUtils.trimToEmpty(password)));
      client.getParams().setAuthenticationPreemptive(true);
    } catch (URISyntaxException e) {
      log.error("Exception setting the registry credentials", e);
    }
  }

  /**
   * Whether a request has succedded, i.e.: 200 response code
   * 
   * @param method
   * @return
   */
  protected boolean succeeded(HttpMethodBase method) {
    if (method.getStatusCode() >= 200 && method.getStatusCode() < 300) {
      return true;
    }
    try {
      log.warn("Http request to " + method.getURI() + " failed: " + method.getStatusLine());
    } catch (URIException e) {
      log.warn("Http request to ??? failed: " + method.getStatusLine());
    }
    return false;
  }

  public boolean validateOrganisation(String organisationKey, String password) {
    setRegistryCredentials(organisationKey, password);
    GetMethod method = new GetMethod(getLoginURL(organisationKey));
    // GetMethod method = newHttpPost(getLoginURL(organisationKey), true);
    try {
      client.executeMethod(method);
      if (succeeded(method)) {
        return true;
      }
      return false;
    } catch (Exception e) {
      log.warn(e.toString());
    }
    return false;
  }
}
