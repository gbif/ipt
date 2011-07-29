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
import java.util.Collections;
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
  private final String rtf = "RTF";

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
    Collections.sort(publishedResources);
    if (log.isDebugEnabled()) {
      log.debug("Got [" + publishedResources.size() + "] published resources of [" + allResources.size()
          + "] total resources");
    }

    log.info("Updating ipt instance");
    try {
      if ((registrationManager.getIpt()) != null) {
        registryManager.updateIpt(registrationManager.getIpt());
        resUpdateStatus.put(registrationManager.getIpt().getName() + registry, success);
      }
    } catch (RegistryException e) {
      log.warn("Registry exception updating ipt instance", e);
      resUpdateStatus.put(registrationManager.getIpt().getName() + registry, e.getMessage());
    }

    log.info("Updating resource metadata - eml.xml");
    for (Resource res : publishedResources) {
      try {
        resourceManager.publishMetadata(res, this);
        resUpdateStatus.put(res.getShortname() + rtf, success);
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
      } catch (PublicationException e) {
        resUpdateStatus.put(res.getShortname() + dwca, e.getMessage());
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
      String rtfMsg = resUpdateStatus.get(res.getShortname() + rtf);

      // 0 is fail, 1 is success, 2 is only for reg, means not registered
      // emlVal can be 0 or 100, reg 0, 10, or 20, dwca 0 or 1 - their sum gives unique state
      int emlVal = 100 * (emlMsg.equals(success) ? 1 : 0);
      int registryVal = 20;
      if (res.isRegistered()) {
        registryMsg = resUpdateStatus.get(res.getShortname() + registry);
        registryVal = 10 * (registryMsg.equals(success) ? 1 : 0);
      }
      int dwcaVal = (dwcaMsg.equals(success) ? 1 : 0);

      int rtfVal = 1000 * (rtfMsg.equals(success) ? 1 : 0);

      int state = emlVal + rtfVal + dwcaVal + registryVal;
      if (log.isDebugEnabled()) {
        log.debug("Logging feedback for state [" + state + "]");
      }

      String logMsg = null;
      switch (state) {
        case 0000:
          logMsg = getTextWithDynamicArgs("admin.config.updateMetadata.resource.failed.all", emlMsg, rtfMsg,
              registryMsg);
          break;
        case 0001:
          logMsg = getTextWithDynamicArgs("admin.config.updateMetadata.resource.failed.eml_rtf_and_registry", emlMsg,
              rtfMsg, registryMsg);
          break;
        case 0010:
          logMsg = getTextWithDynamicArgs("admin.config.updateMetadata.resource.failed.eml_rtf_and_dwca", emlMsg,
              rtfMsg, dwcaMsg);
          break;
        case 0011:
          logMsg = getTextWithDynamicArgs("admin.config.updateMetadata.resource.failed.eml_and_rtf", emlMsg, rtfMsg);
          break;
        case 0020:
          logMsg = getTextWithDynamicArgs("admin.config.updateMetadata.resource.failed.eml_rtf_and_dwca.notRegistered",
              emlMsg, rtfMsg, dwcaMsg);
          break;
        case 0021:
          logMsg = getTextWithDynamicArgs("admin.config.updateMetadata.resource.failed.eml_and_rtf.notRegistered",
              emlMsg, rtfMsg);
          break;
        case 0100:
          logMsg = getTextWithDynamicArgs("admin.config.updateMetadata.resource.failed.rtf_dwca_and_registry", rtfMsg,
              registryMsg);
          break;
        case 0101:
          logMsg = getTextWithDynamicArgs("admin.config.updateMetadata.resource.failed.rtf_and_registry", rtfMsg,
              registryMsg);
          break;
        case 0110:
          logMsg = getTextWithDynamicArgs("admin.config.updateMetadata.resource.failed.rtf_and_dwca", rtfMsg);
          break;
        case 0111:
          logMsg = getTextWithDynamicArgs("admin.config.updateMetadata.resource.failed.rtf", rtfMsg);
          break;
        case 1111:
          logMsg = getText("admin.config.updateMetadata.resource.success");
          break;
        case 0120:
          logMsg = getTextWithDynamicArgs("admin.config.updateMetadata.resource.failed.rtf_and_dwca.notRegistered",
              rtfMsg, dwcaMsg);
          break;
        case 0121:
          logMsg = getText("admin.config.updateMetadata.resource.failed.rtf.notRegistered", rtfMsg);
          break;
        case 1121:
          logMsg = getText("admin.config.updateMetadata.resource.success.notRegistered");
          break;
        case 1000:
          logMsg = getTextWithDynamicArgs("admin.config.updateMetadata.resource.failed.eml_dwca_and_registry", emlMsg,
              dwcaMsg, registryMsg);
          break;
        case 1001:
          logMsg = getTextWithDynamicArgs("admin.config.updateMetadata.resource.failed.eml_and_registry", emlMsg,
              registryMsg);
          break;
        case 1010:
          logMsg = getTextWithDynamicArgs("admin.config.updateMetadata.resource.failed.eml_and_dwca", emlMsg);
          break;
        case 1011:
          logMsg = getTextWithDynamicArgs("admin.config.updateMetadata.resource.failed.eml", emlMsg);
          break;
        case 1020:
          logMsg = getTextWithDynamicArgs("admin.config.updateMetadata.resource.failed.eml_and_dwca.notRegistered",
              emlMsg);
          break;
        case 1120:
          logMsg = getTextWithDynamicArgs("admin.config.updateMetadata.resource.failed.dwca.notRegistered");
          break;
        case 1021:
          logMsg = getTextWithDynamicArgs("admin.config.updateMetadata.resource.failed.eml.notRegistered", emlMsg);
          break;
        case 1100:
          logMsg = getTextWithDynamicArgs("admin.config.updateMetadata.resource.failed.dwca_and_registry", registryMsg);
          break;
        case 1101:
          logMsg = getTextWithDynamicArgs("admin.config.updateMetadata.resource.failed.registry", registryMsg);
          break;
        case 1110:
          logMsg = getTextWithDynamicArgs("admin.config.updateMetadata.resource.failed.dwca");
          break;
      }

      logMsg = "Resource " + res.getShortname() + " : " + logMsg;
      if (log.isDebugEnabled()) {
        log.debug("User feedback: " + logMsg);
      }

      if (state == 1111 | state == 1121 | state == 1110 | state == 1120) {
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
      if (successCounter > 0) {
        this.addActionMessage(getTextWithDynamicArgs("admin.config.updateMetadata.summary",
            String.valueOf(successCounter), String.valueOf(publishedResources.size())));
      }
    }
  }
}
