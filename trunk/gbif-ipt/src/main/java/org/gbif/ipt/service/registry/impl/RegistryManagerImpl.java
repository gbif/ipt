package org.gbif.ipt.service.registry.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
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
import org.gbif.ipt.utils.HttpUtil;
import org.gbif.ipt.utils.HttpUtil.Response;
import org.gbif.ipt.utils.RegistryEntryHandler;
import org.gbif.metadata.eml.Eml;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;

public class RegistryManagerImpl extends BaseManager implements RegistryManager {
  private RegistryEntryHandler newRegistryEntryHandler = new RegistryEntryHandler();

  private HttpUtil http;
  private SAXParser saxParser;
  private Gson gson;

  @Inject
  public RegistryManagerImpl(AppConfig cfg, DataDir dataDir, HttpUtil http, SAXParserFactory saxFactory)
      throws ParserConfigurationException, SAXException {
    super(cfg, dataDir);
    this.saxParser = saxFactory.newSAXParser();
    this.http = http;
    this.gson = new Gson();
  }

  /*
   * (non-Javadoc)
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
  public List<Extension> getExtensions() throws RegistryException {
    try {    	
    	Response resp = http.get(getExtensionsURL(true));
    	if(resp.content != null) {
    		Map<String, List<Extension>> jSONextensions = gson.fromJson(resp.content, new TypeToken<Map<String, List<Extension>>>() {}.getType());
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
   * @see org.gbif.ipt.service.registry.RegistryManager#getOrganisations()
   */
  public List<Organisation> getOrganisations() throws RegistryException {
    try {
    	Response resp = http.get(getOrganisationsURL(true));    	
    	if(resp.content != null) {
    		return gson.fromJson(resp.content, new TypeToken<List<Organisation>>() {}.getType());
    	} else {
    		throw new RegistryException(TYPE.BAD_RESPONSE, "Response content is null");
    	}    	
    } catch (ClassCastException e) {
    	throw new RegistryException(TYPE.BAD_RESPONSE, e);
    } catch (IOException e) {
    	throw new RegistryException(TYPE.IO_ERROR, e);
    } catch (URISyntaxException e) {
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
   * @see org.gbif.ipt.service.registry.RegistryManager#getVocabularies()
   */
  public List<Vocabulary> getVocabularies() throws RegistryException {
    try {
    	Response resp = http.get(getVocabulariesURL(true));
    	if(resp.content != null) {
    		Map<String, List<Vocabulary>> map = gson.fromJson(resp.content, new TypeToken<Map<String, List<Vocabulary>>>(){}.getType());
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
   * @see org.gbif.ipt.service.registry.RegistryManager#register(org.gbif.ipt.model.Resource,
   * org.gbif.ipt.model.Organisation, org.gbif.ipt.model.Ipt)
   */
  public UUID register(Resource resource, Organisation org, Ipt ipt) throws RegistryException {
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
    } else {
      log.debug("No DWC & EML Service present");
    }

    List<NameValuePair> data = new ArrayList<NameValuePair>();
    data.add(new BasicNameValuePair("organisationKey", StringUtils.trimToEmpty(org.getKey().toString())));
    data.add(new BasicNameValuePair("iptKey", StringUtils.trimToEmpty(ipt.getKey().toString())));
    data.add(new BasicNameValuePair("name", ((resource.getTitle() != null)
        ? StringUtils.trimToEmpty(resource.getTitle()) : StringUtils.trimToEmpty(resource.getShortname()))));
    data.add(new BasicNameValuePair("description", StringUtils.trimToEmpty(resource.getDescription())));
    data.add(new BasicNameValuePair("primaryContactType", "technical"));
    data.add(new BasicNameValuePair("primaryContactName",
        StringUtils.trimToNull(StringUtils.trimToEmpty(resource.getCreator().getName()))));
    data.add(new BasicNameValuePair("primaryContactEmail", StringUtils.trimToEmpty(resource.getCreator().getEmail())));
    data.add(new BasicNameValuePair("serviceTypes", serviceTypes));
    data.add(new BasicNameValuePair("serviceURLs", serviceURLs));

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
        resource.setKey(UUID.fromString(key));
        if (key != null) {
          log.info("A new resource has been registered with GBIF. Key = " + key);
          return UUID.fromString(key);
        }
      } else {
        throw new RegistryException(RegistryException.TYPE.BAD_RESPONSE, "Empty registry response");
      }
    } catch (Exception e) {
      throw new RegistryException(RegistryException.TYPE.BAD_RESPONSE, e);
    }
    return null;
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
    // TODO: what is this wsPassword for when registering a new IPT ?
    // data.add(new BasicNameValuePair("wsPassword", StringUtils.trimToEmpty(ipt.getWsPassword()))); // description
    data.add(new BasicNameValuePair("primaryContactType", ipt.getPrimaryContactType()));
    data.add(new BasicNameValuePair("primaryContactName", StringUtils.trimToEmpty(ipt.getPrimaryContactName())));
    data.add(new BasicNameValuePair("primaryContactEmail", StringUtils.trimToEmpty(ipt.getPrimaryContactEmail())));
    data.add(new BasicNameValuePair("serviceTypes", "RSS"));
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

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.registry.RegistryManager#updateResource(org.gbif.ipt.model.Resource,
   * org.gbif.ipt.model.Organisation, org.gbif.ipt.model.Ipt)
   */
  public void updateResource(Resource resource, Organisation organisation, Ipt ipt) throws RegistryException,
      IllegalArgumentException {
    if (resource.getKey() == null) {
      throw new IllegalArgumentException("Resource is not registered");
    }

    Eml eml = resource.getEml();
    // registering IPT resource

    // services should be registered?
    String serviceTypes = null;
    String serviceURLs = null;
    log.debug("Last published: " + resource.getLastPublished());
    if (resource.getLastPublished() != null) {
      log.debug("Registering EML Service");
      serviceTypes = "EML";
      serviceURLs = getEmlURL(resource.getShortname());
      // dwca exists?
      if (resource.getRecordsPublished() > 0) {
        log.debug("Registering DWC-A Service");
        serviceTypes += "|DWC-ARCHIVE";
        serviceURLs += "|" + getDwcArchiveURL(resource.getShortname());
      }
    } else {
      log.debug("No DWC or EML Service present");
    }

    List<NameValuePair> data = new ArrayList<NameValuePair>();
    // data.put("organisationKey", StringUtils.trimToEmpty(organisation.getKey().toString())),
    // data.put("iptKey", StringUtils.trimToEmpty(ipt.getKey().toString())),
    data.add(new BasicNameValuePair("name", StringUtils.trimToEmpty(resource.getTitleOrShortname())));
    data.add(new BasicNameValuePair("description", StringUtils.trimToEmpty(resource.getDescription())));

    // TODO: should this not be the eml contact agent instead?
    data.add(new BasicNameValuePair("primaryContactType", "technical"));
    data.add(new BasicNameValuePair("primaryContactName",
        StringUtils.trimToNull(StringUtils.trimToEmpty(resource.getCreator().getName()))));
    data.add(new BasicNameValuePair("primaryContactEmail", StringUtils.trimToEmpty(resource.getCreator().getEmail())));
    data.add(new BasicNameValuePair("serviceTypes", serviceTypes));
    data.add(new BasicNameValuePair("serviceURLs", serviceURLs));

    try {
      Response resp = http.post(getIptUpdateResourceUri(resource.getKey().toString()), null, null,
          orgCredentials(organisation), new UrlEncodedFormEntity(data));
      if (http.success(resp)) {
        // read new UDDI ID
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
