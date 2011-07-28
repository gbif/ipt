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

    if (log.isDebugEnabled()) {
      log.debug("Loading published resources");
    }
    // many iterations are a bit wasteful, but keep the logic and error handling cleaner
    List<Resource> allResources = resourceManager.list();
    List<Resource> publishedResources = new ArrayList<Resource>();
    for (Resource res : allResources) {
      if (res.isPublished()) {
        publishedResources.add(res);
      }
    }
    if (log.isDebugEnabled()) {
      log.debug("Got [" + publishedResources.size() + "] published resources of [" + allResources.size()
          + "] total resources");
    }

    log.info("Updating ipt instance");
    try {
      registryManager.updateIpt(registrationManager.getIpt());
      resUpdateStatus.put(registrationManager.getIpt().getName() + registry, success);
    } catch (RegistryException e) {
      log.warn("Registry exception updating ipt instance", e);
      resUpdateStatus.put(registrationManager.getIpt().getName() + registry, e.getMessage());
    }

    log.info("Updating resource metadata - eml.xml");
    for (Resource res : publishedResources) {
      try {
        resourceManager.publishMetadata(res, this);
        resUpdateStatus.put(res.getShortname() + eml, success);
      } catch (PublicationException e) {
        resUpdateStatus.put(res.getShortname() + eml, e.getMessage());
      }
    }

    log.info("Updating resource metadata - GBIF registry");
    for (Resource res : publishedResources) {
      if (res.isRegistered()) {
        try {
          registryManager.updateResource(res, registrationManager.getIpt());
          resUpdateStatus.put(res.getShortname() + registry, success);
        } catch (RegistryException e) {
          log.warn("Registry exception updating resource", e);
          resUpdateStatus.put(res.getShortname() + registry, e.getMessage());
        }
      }
    }

    log.info("Updating resource metadata - published DwC archives");
    for (Resource res : publishedResources) {
      try {
        resourceManager.updateDwcaEml(res, this);
        resUpdateStatus.put(res.getShortname() + dwca, success);
      } catch (RegistryException e) {
        resUpdateStatus.put(res.getShortname() + dwca, e.getMessage());
      }
    }

    logFeedback(publishedResources, resUpdateStatus);

    log.info("Updating resource metadata complete");

    return SUCCESS;
  }

  private void logFeedback(List<Resource> publishedResources, Map<String, String> resUpdateStatus) {
    int successCounter = 0;
    for (Resource res : publishedResources) {
      String emlMsg = resUpdateStatus.get(res.getShortname() + eml);
      String registryMsg = "";
      String dwcaMsg = resUpdateStatus.get(res.getShortname() + dwca);

      // 0 is fail, 1 is success, 2 is only for reg, means not registered
      // emlVal can be 0 or 100, reg 0, 10, or 20, dwca 0 or 1 - their sum gives unique state
      int emlVal = 100 * (emlMsg.equals(success) ? 1 : 0);
      int registryVal = 20;
      if (res.isRegistered()) {
        registryMsg = resUpdateStatus.get(res.getShortname() + registry);
        registryVal = 10 * (registryMsg.equals(success) ? 1 : 0);
      }
      int dwcaVal = (dwcaMsg.equals(success) ? 1 : 0);

      int state = emlVal + registryVal + dwcaVal;
      if (log.isDebugEnabled()) {
        log.debug("Logging feedback for state [" + state + "]");
      }

      String logMsg = null;
      switch (state) {
        case 000:
          logMsg = getTextWithDynamicArgs("admin.config.updateMetadata.resource.failed.all", emlMsg, registryMsg,
              dwcaMsg);
          break;
        case 001:
          logMsg = getTextWithDynamicArgs("admin.config.updateMetadata.resource.failed.eml_and_registry", emlMsg,
              registryMsg);
          break;
        case 010:
          logMsg = getTextWithDynamicArgs("admin.config.updateMetadata.resource.failed.eml_and_dwca", emlMsg, dwcaMsg);
          break;
        case 011:
          logMsg = getTextWithDynamicArgs("admin.config.updateMetadata.resource.failed.eml", emlMsg);
          break;
        case 020:
          logMsg = getTextWithDynamicArgs("admin.config.updateMetadata.resource.failed.eml_and_dwca.notRegistered",
              emlMsg, dwcaMsg);
          break;
        case 021:
          logMsg = getTextWithDynamicArgs("admin.config.updateMetadata.resource.failed.eml.notRegistered", emlMsg);
          break;
        case 100:
          logMsg = getTextWithDynamicArgs("admin.config.updateMetadata.resource.failed.registry_and_dwca", registryMsg,
              dwcaMsg);
          break;
        case 101:
          logMsg = getTextWithDynamicArgs("admin.config.updateMetadata.resource.failed.registry", registryMsg);
          break;
        case 110:
          logMsg = getTextWithDynamicArgs("admin.config.updateMetadata.resource.failed.dwca", dwcaMsg);
          break;
        case 111:
          logMsg = getText("admin.config.updateMetadata.resource.success");
          break;
        case 120:
          logMsg = getTextWithDynamicArgs("admin.config.updateMetadata.resource.failed.dwca.notRegistered", dwcaMsg);
          break;
        case 121:
          logMsg = getText("admin.config.updateMetadata.resource.success.notRegistered");
          break;
      }

      logMsg = "Resource " + res.getShortname() + " : " + logMsg;
      if (log.isDebugEnabled()) {
        log.debug("User feedback: " + logMsg);
      }

      if (state == 111 | state == 121) {
        successCounter++;
        this.addActionMessage(logMsg);
      } else {
        this.addActionWarning(logMsg);
      }
    }

    // final summary message
    if (publishedResources.isEmpty()) {
      this.addActionWarning(getText("admin.config.updateMetadata.nonePublished"));
    } else {
      this.addActionMessage(getTextWithDynamicArgs("admin.config.updateMetadata.summary",
          String.valueOf(successCounter), String.valueOf(publishedResources.size())));
    }

  }
}
