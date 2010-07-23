package org.gbif.ipt.service.manage.impl;

import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.model.voc.ResourceType;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.manage.ResourceManager;

import com.google.inject.Singleton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class ResourceManagerImpl extends BaseManager implements ResourceManager {
  private Map<String, Resource> resources = new HashMap<String, Resource>();

  private void addResource(Resource res) {
    resources.put(res.getShortname(), res);
    log.debug("Added resource " + res.getShortname());
  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.ResourceManager#create(java.lang.String)
   */
  public Resource create(String shortname) {
    return new Resource();
  }

  public void delete(Resource resource) throws IOException {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * @see org.gbif.ipt.service.manage.ResourceManager#get(java.lang.String)
   */
  public Resource get(String shortname) {
    return new Resource();
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
    Resource res = new Resource();
    res.setShortname(resourceDir.getName());
    log.debug("Loaded resource " + res.getShortname());
    return res;
  }

  public void save(Resource resource) throws IOException {
    // TODO Auto-generated method stub

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
