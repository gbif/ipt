package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.admin.ConfigManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;

import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.List;

public class UpdateResourceMetadataAction extends POSTAction {
  private static final long serialVersionUID = -2717994514136947049L;

  @Inject
  protected ConfigManager configManager;
  @Inject
  protected ResourceManager resourceManager;
  @Inject
  protected RegistryManager registryManager;
  @Inject
  protected RegistrationManager registrationManager;

  @Override
  public String execute() throws Exception {
    if (log.isDebugEnabled()) log.debug("Loading published resources");
    // many iterations are a bit wasteful, but keep the logic and error handling cleaner
    List<Resource> allResources = resourceManager.list();
    List<Resource> publishedResources = new ArrayList<Resource>();
    for (Resource res : allResources) {
      if (res.isPublished()) publishedResources.add(res);
    }
    if (log.isDebugEnabled())
      log.debug("Got [" + publishedResources.size() + "] published resources of [" + allResources.size()
          + "] total resources");

    log.info("Updating resource metadata - eml.xml");
    for (Resource res : publishedResources) {
      /** TODO: log progress to client */
      resourceManager.publishEml(res, this);
    }

    log.info("Updating resource metadata - GBIF registry");
    for (Resource res : publishedResources) {
      /** TODO: log progress to client */
      if (res.isRegistered()) registryManager.updateResource(res, res.getOrganisation(), registrationManager.getIpt());
    }

// log.info("Updating resource metadata - published DwC archives");

    return SUCCESS;
  }
}
