package org.gbif.ipt.service.registry.impl;

import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.utils.NewRegistryEntryHandler;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

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

  public String getAtomFeedURL() {
    return String.format("%s/atom.xml", cfg.getBaseURL());
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

  private String getIptUri() {
    return String.format("%s/%s", cfg.getRegistryUrl(), "registry/ipt/register");
  }

  private PostMethod newHttpPost(String url, boolean authenticate) {
    PostMethod method = new PostMethod(url);
    method.setDoAuthentication(authenticate);
    return method;
  }

}
