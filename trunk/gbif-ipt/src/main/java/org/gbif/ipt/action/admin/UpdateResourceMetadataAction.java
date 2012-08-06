package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.Resource.CoreRowType;
import org.gbif.ipt.service.PublicationException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.ConfigManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;

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

  private static final String EML = "EML";
  private static final String REGISTRY = "REGISTRY";
  private static final String DWCA = "DWCA";
  private static final String SUCCESS_TYPE = "SUCCESS";
  private static final String RTF = "RTF";

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
      // Saving the coreType for all resources
      if (res.getCoreType() == null && res.getCoreTypeTerm() != null) {
        String core = res.getCoreTypeTerm().simpleName().toLowerCase();
        if (Constants.DWC_ROWTYPE_TAXON.toLowerCase().contains(core)) {
          res.setCoreType(StringUtils.capitalize(CoreRowType.OCCURRENCE.toString().toLowerCase()));
        } else if (Constants.DWC_ROWTYPE_OCCURRENCE.toLowerCase().contains(core)) {
          res.setCoreType(StringUtils.capitalize(CoreRowType.CHECKLIST.toString().toLowerCase()));
        }
        // Save resource information (resource.xml)
        resourceManager.save(res);
        // Set resource modified date
        res.setModified(new Date());
      }
    }
    Collections.sort(publishedResources);
    if (log.isDebugEnabled()) {
      log.debug(
        "Got [" + publishedResources.size() + "] published resources of [" + allResources.size() + "] total resources");
    }

    log.info("Updating ipt instance");
    try {
      if (registrationManager.getIpt() != null) {
        registryManager.updateIpt(registrationManager.getIpt());
        resUpdateStatus.put(registrationManager.getIpt().getName() + REGISTRY, SUCCESS_TYPE);
      }
    } catch (RegistryException e) {
      log.warn("Registry exception updating ipt instance", e);
      resUpdateStatus.put(registrationManager.getIpt().getName() + REGISTRY, e.getMessage());
    }

    log.info("Updating resource metadata - eml.xml");
    for (Resource res : publishedResources) {
      try {

        resourceManager.publishMetadata(res, this);

        // save resource
        res.setLastPublished(new Date());
        resourceManager.save(res);

        resUpdateStatus.put(res.getShortname() + RTF, SUCCESS_TYPE);
        resUpdateStatus.put(res.getShortname() + EML, SUCCESS_TYPE);
      } catch (PublicationException e) {
        resUpdateStatus.put(res.getShortname() + EML, e.getMessage());
      }
    }

    log.info("Updating resource metadata - GBIF registry");
    for (Resource res : publishedResources) {
      if (res.isRegistered()) {
        try {
          registryManager.updateResource(res);
          resUpdateStatus.put(res.getShortname() + REGISTRY, SUCCESS_TYPE);
        } catch (RegistryException e) {
          log.warn("Registry exception updating resource", e);
          resUpdateStatus.put(res.getShortname() + REGISTRY, e.getMessage());
        }
      }
    }

    log.info("Updating resource metadata - published DwC archives");
    for (Resource res : publishedResources) {
      try {
        resourceManager.updateDwcaEml(res, this);
        resUpdateStatus.put(res.getShortname() + DWCA, SUCCESS_TYPE);
      } catch (PublicationException e) {
        resUpdateStatus.put(res.getShortname() + DWCA, e.getMessage());
      } catch (RegistryException e) {
        resUpdateStatus.put(res.getShortname() + DWCA, e.getMessage());
      }
    }

    logFeedback(publishedResources, resUpdateStatus);

    log.info("Updating resource metadata complete");

    return SUCCESS;
  }

  private void logFeedback(List<Resource> publishedResources, Map<String, String> resUpdateStatus) {
    int successCounter = 0;
    for (Resource res : publishedResources) {
      String emlMsg = resUpdateStatus.get(res.getShortname() + EML);
      String registryMsg = "";
      String dwcaMsg = resUpdateStatus.get(res.getShortname() + DWCA);
      String rtfMsg = resUpdateStatus.get(res.getShortname() + RTF);
      // Messages states
      // rtfVal 0/1000: 0 is fail, 1000 is success.
      // emlVal 0/100 : 0 is fail, 100 is success.
      // registryVal 0/10/20: 0 is fail, 10 is success and 20 means the resource is not registered.
      // dwcaVal 0/1 : 0 is fail, 1 is success
      // Each state is calculated as follows: rtfVal + emlVal + registryVal + dwcaVal
      // e.g if everything is satisfactory state= 1000 + 100 + 10 + 1 = 1111
      int emlVal = 100 * (emlMsg.equals(SUCCESS_TYPE) ? 1 : 0);
      int registryVal = 20;
      if (res.isRegistered()) {
        registryMsg = resUpdateStatus.get(res.getShortname() + REGISTRY);
        registryVal = 10 * (registryMsg.equals(SUCCESS_TYPE) ? 1 : 0);
      }
      int dwcaVal = dwcaMsg.equals(SUCCESS_TYPE) ? 1 : 0;

      int rtfVal = 1000 * (rtfMsg.equals(SUCCESS_TYPE) ? 1 : 0);

      int state = emlVal + rtfVal + dwcaVal + registryVal;
      if (log.isDebugEnabled()) {
        log.debug("Logging feedback for state [" + state + "]");
      }

      StringBuilder logMsg = new StringBuilder();
      switch (state) {
        case 0000:
          // All failed. (DwC-A)
          logMsg.append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.eml", emlMsg)).append(' ')
            .append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.rtf", rtfMsg)).append(' ')
            .append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.registry", registryMsg));
          break;
        case 0001:
          // Updated: DwC-A. Failed: EML, RTF, registry.
          logMsg.append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.eml", emlMsg)).append(' ')
            .append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.rtf", rtfMsg)).append(' ')
            .append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.registry", registryMsg));
          break;
        case 0010:
          // Updated: registry. Failed: EML, RTF, (DwC-A).
          logMsg.append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.eml", emlMsg)).append(' ')
            .append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.rtf", rtfMsg));
          break;
        case 0011:
          // Updated: registry and DwC-A. Failed: EML, RTF.
          logMsg.append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.eml", emlMsg)).append(' ')
            .append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.rtf", rtfMsg));
          break;
        case 0020:
          // Failed: eml, rtf. Not registered.
          logMsg.append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.eml", emlMsg)).append(' ')
            .append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.rtf", rtfMsg)).append(' ')
            .append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.notRegistered"));
          break;
        case 0021:
          // Updated DwC-A. Failed: EML, RTF. Not registered.
          logMsg.append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.eml", emlMsg)).append(' ')
            .append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.rtf", rtfMsg)).append(' ')
            .append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.notRegistered"));
          break;
        case 0100:
          // Updated: EML. Failed: RTF, registry. (DwC-A)
          logMsg.append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.rtf", rtfMsg)).append(' ')
            .append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.registry", registryMsg));
          break;
        // Updated: EML, DwC-A. Failed: RTF, registry.
        case 0101:
          logMsg.append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.rtf", rtfMsg)).append(' ')
            .append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.registry", registryMsg));
          break;
        case 0110:
          // Updated: EML, registry. Failed: RTF, (DwC-A).
          logMsg.append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.rtf", rtfMsg));
          break;
        case 0111:
          // Updated: EML, DwC-A, registry. Failed: RTF.
          logMsg.append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.rtf", rtfMsg));
          break;
        case 1111:
          // All updated.
          logMsg.append(getText("admin.config.updateMetadata.resource.success"));
          break;
        case 0120:
          // Updated: EML. Failed: RTF, (DwC-A). Not registered.
          logMsg.append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.rtf", rtfMsg)).append(' ')
            .append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.notRegistered"));
          break;
        case 0121:
          // Updated: EML, DwC-A. Failed: RTF. Not registered.
          logMsg.append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.rtf", rtfMsg)).append(' ')
            .append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.notRegistered"));
          break;
        case 1121:
          // Updated: EML, RTF, DwC-A. Not registered.
          logMsg.append(getText("admin.config.updateMetadata.resource.notRegistered"));
          break;
        case 1000:
          // Updated: RTF. Failed: EML, registry, (DwC-A).
          logMsg.append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.eml", emlMsg)).append(' ')
            .append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.registry", registryMsg));
          break;
        case 1001:
          // Updated: RTF, DwC-A. Failed: EML, registry.
          logMsg.append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.eml", emlMsg)).append(' ')
            .append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.registry", registryMsg));
          break;
        case 1010:
          // Updated: RTF, registry. Failed: EML, (DwC-A).
          logMsg.append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.eml", emlMsg));
          break;
        case 1011:
          // Updated: RTF, DwC-A, registry. Failed: EML.
          logMsg.append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.eml", emlMsg));
          break;
        case 1020:
          // Updated RTF. Failed: EML, (DwC-A). Not registered.
          logMsg.append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.eml", emlMsg)).append(' ')
            .append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.notRegistered"));
          break;
        case 1120:
          // Updated: EML, RTF. Failed: (DwC-A). not registered
          logMsg.append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.notRegistered"));
          break;
        case 1021:
          // Updated: RTF, DwC-A. Failed: EML. Not registered.
          logMsg.append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.eml", emlMsg)).append(' ')
            .append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.notRegistered"));
          break;
        case 1100:
          // Updated: EML, RTF. Failed: (DwC-A), registry
          logMsg.append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.registry", registryMsg));
          break;
        case 1101:
          // Updated: EML, RTF, DwC-A. Failed: registry.
          logMsg.append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.registry", registryMsg));
          break;
        case 1110:
          // Updated: EML, RTF, registry. Failed (DwC-A)
          logMsg.append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.success"));
          break;
      }

      logMsg.append("Resource ").append(res.getShortname()).append(" : ").append(logMsg.toString());
      if (log.isDebugEnabled()) {
        log.debug("User feedback: " + logMsg);
      }

      if (state == 1111 || state == 1121 || state == 1110 || state == 1120) {
        successCounter++;
        this.addActionMessage(logMsg.toString());
      } else {
        this.addActionWarning(logMsg.toString());
      }
    }

    // final summary message
    if (publishedResources.isEmpty()) {
      this.addActionWarning(getText("admin.config.updateMetadata.nonePublished"));
    } else {
      if (successCounter > 0) {
        this.addActionMessage(
          getTextWithDynamicArgs("admin.config.updateMetadata.summary", String.valueOf(successCounter),
            String.valueOf(publishedResources.size())));
      }
    }
  }
}
