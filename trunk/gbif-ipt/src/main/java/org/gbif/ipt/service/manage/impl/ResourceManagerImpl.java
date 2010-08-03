package org.gbif.ipt.service.manage.impl;

import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.converter.OrganisationKeyConverter;
import org.gbif.ipt.model.converter.UserEmailConverter;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.model.voc.ResourceType;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.InvalidConfigException.TYPE;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.GBIFRegistryManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.EmlFactory;
import org.gbif.metadata.eml.EmlWriter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.thoughtworks.xstream.XStream;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import freemarker.template.TemplateException;

@Singleton
public class ResourceManagerImpl extends BaseManager implements ResourceManager {
  // key=shortname in lower case, value=resource
  private Map<String, Resource> resources = new HashMap<String, Resource>();
  public static final String PERSISTENCE_FILE = "resource.xml";
  public static final String EML_FILE = "eml.xml";
  private final XStream xstream = new XStream();
  private UserEmailConverter userConverter;
  private OrganisationKeyConverter orgConverter;
  private GBIFRegistryManager registryManager;

  @Inject
  public ResourceManagerImpl(UserEmailConverter userConverter, OrganisationKeyConverter orgConverter,
      GBIFRegistryManager registryManager) {
    super();
    this.userConverter = userConverter;
    this.registryManager = registryManager;
    this.orgConverter = orgConverter;
    defineXstreamMapping();
  }

  private void addResource(Resource res) {
    resources.put(res.getShortname().toLowerCase(), res);
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.ResourceManager#create(java.lang.String)
   */
  public Resource create(String shortname, User creator) throws AlreadyExistingException {
    Resource res = null;
    if (shortname != null) {
      // check if existing already
      shortname = shortname.toLowerCase();
      if (resources.containsKey(shortname)) {
        throw new AlreadyExistingException();
      }
      res = new Resource();
      res.setShortname(shortname);
      res.setCreated(new Date());
      res.setCreator(creator);
      // create dir
      try {
        save(res);
        log.debug("Created resource " + res.getShortname());
      } catch (InvalidConfigException e) {
        log.error("Error creating resource", e);
        return null;
      }
    }
    return res;
  }

  /**
   * 
   */
  private void defineXstreamMapping() {
    xstream.alias("resource", Resource.class);
//    xstream.omitField(Resource.class, "eml");
//    xstream.omitField(Resource.class, "config");
    // persist only emails for users
    xstream.registerConverter(userConverter);
    xstream.registerConverter(orgConverter);
    xstream.alias("user", User.class);
//    xstream.useAttributeFor(User.class, "email");
  }

  public void delete(Resource resource) throws IOException {
    // TODO Auto-generated method stub
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.ResourceManager#get(java.lang.String)
   */
  public Resource get(String shortname) {
    if (shortname == null) {
      return null;
    }
    return resources.get(shortname.toLowerCase());
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.ResourceManager#getEml(java.lang.String)
   */
  public Eml getEml(Resource resource) {
    File emlFile = dataDir.resourceFile(resource, EML_FILE);
    Eml eml = null;
    try {
      InputStream in = new FileInputStream(emlFile);
      eml = EmlFactory.build(in);
    } catch (FileNotFoundException e) {
      eml = new Eml();
    } catch (IOException e) {
      log.error(e);
    } catch (SAXException e) {
      log.error("Invalid EML document", e);
    }

    updateEmlWithResourceBasics(resource, eml);
    return eml;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.ResourceManager#getResourceLink(java.lang.String)
   */
  public URL getResourceLink(String shortname) {
    URL url = null;
    try {
      url = new URL(cfg.getBaseURL() + "/resource.do?id=" + shortname);
    } catch (MalformedURLException e) {
      log.error(e);
    }
    return url;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.ResourceManager#list(org.gbif.ipt.model.voc.PublicationStatus)
   */
  public List<Resource> list(PublicationStatus status) {
    List<Resource> result = new ArrayList<Resource>();
    for (Resource r : resources.values()) {
      if (r.getStatus() == status) {
        result.add(r);
      }
    }
    return result;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.ResourceManager#list(org.gbif.ipt.model.User)
   */
  public List<Resource> list(User user) {
    // TODO: select basedon user rights - for testing return all resources for now
    List<Resource> result = new ArrayList<Resource>(resources.values());
    return result;
  }

  public int load() {
    File extensionDir = dataDir.dataFile(DataDir.RESOURCES_DIR);
    int counter = 0;
    resources.clear();
    for (File resourceDir : extensionDir.listFiles()) {
      if (resourceDir.isDirectory()) {
        try {
          addResource(loadFromDir(resourceDir));
          counter++;
        } catch (InvalidConfigException e) {
          log.error("Cant load resource " + resourceDir.getName(), e);
        }
      }
    }
    log.info("Loaded " + counter + " resources alltogether.");
    return counter;
  }

  /**
   * Reads a local extension file into manager cache
   * 
   * @param resourceDir
   * @return
   */
  private Resource loadFromDir(File resourceDir) throws InvalidConfigException {
    File resCfg = dataDir.resourceFile(resourceDir.getName(), PERSISTENCE_FILE);
    InputStream input;
    Resource resource = null;
    try {
      input = new FileInputStream(resCfg);
      resource = (Resource) xstream.fromXML(input);
      resource.setShortname(resourceDir.getName());
      log.debug("Loaded " + resource);
    } catch (FileNotFoundException e) {
      log.error("Cannot load main resource configuration", e);
      throw new InvalidConfigException(TYPE.RESOURCE_CONFIG, "Cannot load main resource configuration: "
          + e.getMessage());
    }
    return resource;
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.ResourceManager#publish(org.gbif.ipt.model.Resource,
   * org.gbif.ipt.model.voc.PublicationStatus)
   */
  public void publish(Resource resource) throws InvalidConfigException {
    if (PublicationStatus.REGISTERED == resource.getStatus()) {
      throw new InvalidConfigException(TYPE.RESOURCE_ALREADY_REGISTERED, "The resource is already registered with GBIF");
    } else if (PublicationStatus.PRIVATE == resource.getStatus()) {
      resource.setStatus(PublicationStatus.PUBLIC);
      save(resource);
    }
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.ResourceManager#register(org.gbif.ipt.model.Resource,
   * org.gbif.ipt.model.Organisation)
   */
  public void register(Resource resource, Organisation organisation) throws RegistryException {
    if (PublicationStatus.REGISTERED != resource.getStatus()) {
      UUID key = registryManager.register(resource, organisation);
      if (key == null) {
        throw new RegistryException(RegistryException.TYPE.MISSING_METADATA, "No key returned for registered resoruce.");
      }
      resource.setKey(key);
      resource.setOrganisation(organisation);
      resource.setStatus(PublicationStatus.REGISTERED);
      save(resource);
    }
  }

  public void save(Resource resource) throws InvalidConfigException {
    File resDir = dataDir.resourceFile(resource, "");
    try {
      FileUtils.forceMkdir(resDir);
      // persist data
      Writer writer = org.gbif.ipt.utils.FileUtils.startNewUtf8File(dataDir.resourceFile(resource, PERSISTENCE_FILE));
      xstream.toXML(resource, writer);
      // add to internal map
      addResource(resource);
      log.debug("Saved " + resource);
    } catch (IOException e) {
      log.error(e);
      throw new InvalidConfigException(TYPE.CONFIG_WRITE, "Cant write resource configuration");
    }
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.ResourceManager#save(java.lang.String, org.gbif.metadata.eml.Eml)
   */
  public void saveEml(Resource resource, Eml eml) throws InvalidConfigException {
    // udpate EML with latest resource basics
    updateEmlWithResourceBasics(resource, eml);
    // save into data dir
    File emlFile = dataDir.resourceFile(resource, EML_FILE);
    try {
      EmlWriter.writeEmlXmlFile(emlFile, eml);
      log.debug("Updated EML file for " + resource);
    } catch (IOException e) {
      log.error(e);
      throw new InvalidConfigException(TYPE.CONFIG_WRITE, "IO exception when writing eml for " + resource);
    } catch (TemplateException e) {
      log.error("EML template exception", e);
      throw new InvalidConfigException(TYPE.CONFIG_WRITE, "EML template exception when writing eml for " + resource);
    }
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.ResourceManager#search(java.lang.String, org.gbif.ipt.model.voc.ResourceType)
   */
  public List<Resource> search(String q, ResourceType type) {
    // TODO: do real search - for testing return all resources for now
    return new ArrayList<Resource>(resources.values());
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.ResourceManager#unpublish(org.gbif.ipt.model.Resource)
   */
  public void unpublish(Resource resource) throws InvalidConfigException {
    if (PublicationStatus.REGISTERED == resource.getStatus()) {
      throw new InvalidConfigException(TYPE.RESOURCE_ALREADY_REGISTERED, "The resource is already registered with GBIF");
    } else if (PublicationStatus.PUBLIC == resource.getStatus()) {
      resource.setStatus(PublicationStatus.PRIVATE);
      save(resource);
    }
  }

  private void updateEmlWithResourceBasics(Resource resource, Eml eml) {
    eml.setTitle(resource.getTitle());
    eml.setDescription(resource.getDescription());
    // we need some GUID. If we have use the registry key, if not use the resource URL
    if (resource.getKey() != null) {
      eml.setGuid(resource.getKey().toString());
    } else {
      eml.setGuid(getResourceLink(resource.getShortname()).toString());
    }
  }
}
