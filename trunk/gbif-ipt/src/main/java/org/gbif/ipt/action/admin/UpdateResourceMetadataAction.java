package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.PublicationException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.ConfigManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;

import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

  private final String eml = "EML";
  private final String registry = "REGISTRY";
  private final String dwca = "DWCA";
  private final String success = "SUCCESS";

  @Override
  public String execute() throws Exception {
    // key representing resource and update step (eml, registry, dwca), value "success" or error message
    Map<String, String> resUpdateStatus = new HashMap<String, String>();

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
      try {
        resourceManager.publishEml(res, this);
        resUpdateStatus.put(res.getShortname() + eml, success);
      } catch (PublicationException e) {
        resUpdateStatus.put(res.getShortname() + eml, e.getMessage());
      }
    }

    log.info("Updating resource metadata - GBIF registry");
    for (Resource res : publishedResources) {
      if (res.isRegistered()) {
        try {
          registryManager.updateResource(res, res.getOrganisation(), registrationManager.getIpt());
          resUpdateStatus.put(res.getShortname() + registry, success);
        } catch (RegistryException e) {
          resUpdateStatus.put(res.getShortname() + registry, e.getMessage());
        }
      }
    }

    /** TODO: unzip published dwca, replace eml.xml with newly generated file, rezip */
// log.info("Updating resource metadata - published DwC archives");

    logFeedback(publishedResources, resUpdateStatus);

    return SUCCESS;
  }

  private void logFeedback(List<Resource> publishedResources, Map<String, String> resUpdateStatus) {
    for (Resource res : publishedResources) {
      String logMsg = "Resource " + res.getShortname() + " : ";
      String emlMsg = resUpdateStatus.get(res.getShortname() + eml);

      /** TODO: make this msg creation better */
      // an extremely painful way of creating a nice looking message
      boolean allGood = false;
      if (res.isRegistered()) {
        String registryMsg = resUpdateStatus.get(res.getShortname() + registry);

        allGood = emlMsg.equals(success) && registryMsg.equals(success);
        if (allGood) {
          logMsg = logMsg + "successfully updated eml and GBIF registry";
        } else if (!emlMsg.equals(success) && registryMsg.equals(success)) {
          logMsg = logMsg + "successfully updated GBIF registry, but error on eml update - " + emlMsg;
        } else if (emlMsg.equals(success) && !registryMsg.equals(success)) {
          logMsg = logMsg + "successfully updated eml, but error on GBIF registry update - " + registryMsg;
        } else {
          logMsg = logMsg + "error on eml update - " + emlMsg + ", and error on GBIF registry update - " + registryMsg;
        }
      } else {
        allGood = emlMsg.equals(success);
        if (allGood) {
          logMsg = logMsg + "successfully updated eml, and resource is not registered with GBIF registry";
        } else {
          logMsg = logMsg + "error on eml update - " + emlMsg + ", and resource is not registered with GBIF registry";
        }
      }

      if (allGood) {
        this.addActionMessage(logMsg);
      } else {
        this.addActionWarning(logMsg);
      }
    }
  }
}
