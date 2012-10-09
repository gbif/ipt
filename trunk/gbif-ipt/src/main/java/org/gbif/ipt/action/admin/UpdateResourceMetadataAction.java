package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.PublicationException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.ConfigManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;
import org.apache.log4j.Logger;

public class UpdateResourceMetadataAction extends POSTAction {

  // logging
  private static final Logger log = Logger.getLogger(UpdateResourceMetadataAction.class);

  private static final long serialVersionUID = -2717994514136947049L;

  protected ConfigManager configManager;
  protected ResourceManager resourceManager;
  protected RegistryManager registryManager;

  private static final String EML = "EML";
  private static final String REGISTRY = "REGISTRY";
  private static final String DWCA = "DWCA";
  private static final String SUCCESS_TYPE = "SUCCESS";

  @Inject
  public UpdateResourceMetadataAction(SimpleTextProvider textProvider, AppConfig cfg,
    RegistrationManager registrationManager, ConfigManager configManager, ResourceManager resourceManager,
    RegistryManager registryManager) {
    super(textProvider, cfg, registrationManager);
    this.configManager = configManager;
    this.resourceManager = resourceManager;
    this.registryManager = registryManager;
  }

  @Override
  public String execute() throws Exception {
    if (log.isDebugEnabled()) {
      log.debug("Loading published resources");
    }
    // retrieve a list of all resources that have been published
    List<Resource> publishedResources = resourceManager.listPublished();

    // key representing resource and update step (eml, registry, dwca), value "success" or error message
    Map<String, String> resUpdateStatus = new HashMap<String, String>();

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

        // update eml pubDate (represents date when the resource was last published)
        res.getEml().setPubDate(new Date());

        // publishes both the EML and the RTF
        resourceManager.publishMetadata(res, this);

        // save resource
        res.setLastPublished(new Date());
        resourceManager.save(res);

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

  /**
   * Logs all feedback collected during update all resources execution. Each resource can have its metadata, DwC-A,
   * and registry updates be successful or not. A message state for each event is calculated as follows:
   * </p>
   * Messages states:
   * </p>
   * emlVal 0/100 : 0 is fail, 100 is success.
   * registryVal 0/10/20: 0 is fail, 10 is success and 20 means the resource is not registered.
   * dwcaVal 0/1 : 0 is fail, 1 is success
   * </p>
   * The combination of the 3 states is then summed together as follows: emlVal + registryVal + dwcaVal
   * e.g if everything is satisfactory state= 100 + 10 + 1 = 1111
   * </p>
   * This sum is then used to identify a unique state that determines what combination of log messages are outputted.
   */
  private void logFeedback(List<Resource> publishedResources, Map<String, String> resUpdateStatus) {
    int successCounter = 0;
    for (Resource res : publishedResources) {
      String emlMsg = resUpdateStatus.get(res.getShortname() + EML);
      String registryMsg = "";
      String dwcaMsg = resUpdateStatus.get(res.getShortname() + DWCA);

      // Calculate individual messages states - refer to method Javadoc for more info
      int emlVal = 100 * (emlMsg.equals(SUCCESS_TYPE) ? 1 : 0);
      int registryVal = 20;
      if (res.isRegistered()) {
        registryMsg = resUpdateStatus.get(res.getShortname() + REGISTRY);
        registryVal = 10 * (registryMsg.equals(SUCCESS_TYPE) ? 1 : 0);
      }
      int dwcaVal = dwcaMsg.equals(SUCCESS_TYPE) ? 1 : 0;

      // Calculate combined state
      int state = emlVal + dwcaVal + registryVal;
      if (log.isDebugEnabled()) {
        log.debug("Logging feedback for state [" + state + "]");
      }

      // to store log message in its entirety for a single resource
      StringBuilder logMsg = new StringBuilder();

      // construct log msg prefix for each message, including resource title + shortName if they are different
      String title = res.getTitleOrShortname();
      if (title.equalsIgnoreCase(res.getShortname())) {
        logMsg.append(title).append(": ");
      } else {
        logMsg.append(title).append(" (").append(res.getShortname()).append("): ");
      }

      // append log message depending on combined state
      switch (state) {
        case 000:
          // All failed: metadata, registry, DwC-A
          logMsg.append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.eml", emlMsg)).append(' ')
            .append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.registry", registryMsg))
            .append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.dwca", dwcaMsg));
          break;
        case 001:
          // Updated: DwC-A. Failed: metadata, registry.
          logMsg.append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.eml", emlMsg)).append(' ')
            .append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.registry", registryMsg));
          break;
        case 010:
          // Updated: registry. Failed: metadata, DwC-A.
          logMsg.append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.eml", emlMsg)).append(' ')
            .append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.dwca", dwcaMsg));
          break;
        case 011:
          // Updated: registry and DwC-A. Failed: metadata.
          logMsg.append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.eml", emlMsg)).append(' ');
          break;
        case 020:
          // Failed: metadata, DwC-A. Not registered.
          logMsg.append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.eml", emlMsg)).append(' ')
            .append(getText("admin.config.updateMetadata.resource.notRegistered"));
          break;
        case 021:
          // Updated DwC-A. Failed: metadata. Not registered.
          logMsg.append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.eml", emlMsg)).append(' ')
            .append(getText("admin.config.updateMetadata.resource.notRegistered"));
          break;
        case 100:
          // Updated: metadata. Failed: registry, DwC-A
          logMsg.append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.registry", registryMsg))
            .append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.dwca", dwcaMsg));
          break;
        // Updated: metadata, DwC-A. Failed: registry.
        case 101:
          logMsg.append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.registry", registryMsg));
          break;
        case 110:
          // Updated: metadata, registry. Failed: DwC-A
          logMsg.append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.dwca", dwcaMsg));
          break;
        case 111:
          // All updated.
          logMsg.append(getText("admin.config.updateMetadata.resource.success"));
          break;
        case 120:
          // Updated: metadata. Failed: DwC-A. Not registered.
          logMsg.append(getTextWithDynamicArgs("admin.config.updateMetadata.resource.fail.dwca", dwcaMsg))
            .append(getText("admin.config.updateMetadata.resource.notRegistered"));
          break;
        case 121:
          // Updated: metadata, DwC-A. Not registered.
          logMsg.append(getText("admin.config.updateMetadata.resource.notRegistered"));
          break;
      }

      if (log.isDebugEnabled()) {
        log.debug("User feedback: " + logMsg);
      }

      if (state == 111 || state == 121 || state == 110 || state == 120) {
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
