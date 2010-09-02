package org.gbif.ipt.service.registry.impl;

import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
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

  /*
   * (non-Javadoc)
   * 
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

  /*
   * (non-Javadoc)
   * 
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

  /*
   * (non-Javadoc)
   * 
   * @see org.gbif.ipt.service.registry.RegistryManager#register(org.gbif.ipt.model.Resource,
   * org.gbif.ipt.model.Organisation, org.gbif.ipt.model.Ipt)
   */
  public UUID register(Resource resource, Organisation organisation, Ipt ipt) throws RegistryException {
    Eml eml = resource.getEml();
    // registering IPT resource

    // services should be registered?
    String serviceTypes = null;
    String serviceURLs = null;
    if (resource.getType() != null) {
      serviceTypes = "EML|DWC-ARCHIVE";
      serviceURLs = getEmlURL(resource.getShortname()) + "|" + getDwcArchiveURL(resource.getShortname());
    }

    NameValuePair[] data = {
        new NameValuePair("organisationKey", StringUtils.trimToEmpty(organisation.getKey().toString())),
        new NameValuePair("iptKey", StringUtils.trimToEmpty(ipt.getKey().toString())),
        new NameValuePair("name", ((resource.getTitle() != null) ? StringUtils.trimToEmpty(resource.getTitle())
            : StringUtils.trimToEmpty(resource.getShortname()))), // name
        new NameValuePair("description", StringUtils.trimToEmpty(resource.getDescription())), // description
        new NameValuePair("primaryContactType", "technical"),
        new NameValuePair("primaryContactName",
            StringUtils.trimToNull(StringUtils.trimToEmpty(eml.getContact().getFirstName()) + " "
                + StringUtils.trimToEmpty(eml.getContact().getLastName()))),
        new NameValuePair("primaryContactEmail", StringUtils.trimToEmpty(eml.getContact().getEmail())),
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
        ipt.setKey(key);
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
   * 
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
   * 
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

  protected InputStream getStream(String source) {
    return new ByteArrayInputStream(source.getBytes());
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

  /**
   * Returns the Extensions url
   * 
   * @param json
   * @return
   */
  private String getExtensionsURL(boolean json) {
    return String.format("%s%s%s", cfg.getRegistryUrl(), "registry/extensions", json ? ".json" : "/");
  }

  /**
   * Returns the IPT Resource url
   * 
   * @return
   */
  private String getIptResourceUri() {
    return String.format("%s/%s", cfg.getRegistryUrl(), "registry/ipt/resource");
  }

  /**
   * Returns the IPT url
   * 
   * @return
   */
  private String getIptUri() {
    return String.format("%s/%s", cfg.getRegistryUrl(), "registry/ipt/register");
  }

  /**
   * Returns the Organisations url
   * 
   * @param json
   * @return
   */
  private String getOrganisationsURL(boolean json) {
    return String.format("%s%s%s", cfg.getRegistryUrl(), "registry/organisation", json ? ".json" : "/");
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

public boolean validateOrganisation(String organisationKey, String password) {
	return false;
}

}
