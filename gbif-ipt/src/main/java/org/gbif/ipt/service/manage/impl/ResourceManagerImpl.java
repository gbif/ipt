package org.gbif.ipt.service.manage.impl;

import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.converter.UserEmailConverter;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.model.voc.ResourceType;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.InvalidConfigException.TYPE;
import org.gbif.ipt.service.manage.ResourceManager;

import com.google.inject.Singleton;
import com.thoughtworks.xstream.XStream;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class ResourceManagerImpl extends BaseManager implements ResourceManager {
  // key=shortname in lower case, value=resource
  private Map<String, Resource> resources = new HashMap<String, Resource>();
  public static final String PERSISTENCE_FILE = "resource.xml";
  private final XStream xstream = new XStream();

  public ResourceManagerImpl() {
    super();
    defineXstreamMapping();
  }

  private void addResource(Resource res) {
    resources.put(res.getShortname().toLowerCase(), res);
    log.debug("Added resource " + res.getShortname());
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.ResourceManager#create(java.lang.String)
   */
  public Resource create(String shortname, User creator) throws AlreadyExistingException {
    Resource res = new Resource();
    res.setShortname(shortname);
    res.setCreated(new Date());
    res.setCreator(creator);
    // create dir
    try {
      save(res);
      log.debug("Created resource " + res.getShortname());
    } catch (IOException e) {
      log.error("Error creating resource", e);
    }
    return res;
  }

  /**
   * 
   */
  private void defineXstreamMapping() {
    xstream.alias("resource", Resource.class);
    xstream.addImplicitCollection(Resource.class, "managers");
    xstream.omitField(Resource.class, "eml");
    xstream.omitField(Resource.class, "config");
    // persist only emails for users
    xstream.registerConverter(new UserEmailConverter());
    xstream.alias("user", User.class);
    xstream.useAttributeFor(User.class, "email");
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
   * @see org.gbif.ipt.service.manage.ResourceManager#list(org.gbif.ipt.model.voc.PublicationStatus)
   */
  public List<Resource> list(PublicationStatus status) {
    // TODO: select basedon user rights - for testing return all resources for now
    return new ArrayList<Resource>(resources.values());
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
      log.debug("Loaded resource " + resource.getShortname());
    } catch (FileNotFoundException e) {
      log.error("Cannot load main resource configuration", e);
      throw new InvalidConfigException(TYPE.RESOURCE_CONFIG, "Cannot load main resource configuration: "
          + e.getMessage());
    }
    return resource;
  }

  public void save(Resource resource) throws IOException {
    File resDir = dataDir.resourceFile(resource, "");
    FileUtils.forceMkdir(resDir);
    // persist data
    Writer writer = org.gbif.ipt.utils.FileUtils.startNewUtf8File(dataDir.resourceFile(resource, PERSISTENCE_FILE));
    xstream.toXML(resource, writer);
    // add to internal map
    addResource(resource);
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.ResourceManager#search(java.lang.String, org.gbif.ipt.model.voc.ResourceType)
   */
  public List<Resource> search(String q, ResourceType type) {
    // TODO: do real search - for testing return all resources for now
    return new ArrayList<Resource>(resources.values());
  }

}
