package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.PublicationException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.validation.EmlValidator;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.google.inject.Inject;
import org.apache.log4j.Logger;

public class PublishAllResourcesAction extends BaseAction {

  // logging
  private static final Logger log = Logger.getLogger(PublishAllResourcesAction.class);

  private static final long serialVersionUID = -2717994514136947049L;

  protected ResourceManager resourceManager;
  protected RegistryManager registryManager;
  private final EmlValidator emlValidator;

  @Inject
  public PublishAllResourcesAction(SimpleTextProvider textProvider, AppConfig cfg,
    RegistrationManager registrationManager, ResourceManager resourceManager, RegistryManager registryManager) {
    super(textProvider, cfg, registrationManager);
    this.resourceManager = resourceManager;
    this.registryManager = registryManager;
    this.emlValidator = new EmlValidator(cfg, registrationManager, textProvider);
  }

  @Override
  public String execute() throws Exception {
    // start with IPT registration update, provided the IPT has been registered already
    try {
      if (registrationManager.getIpt() != null) {
        registryManager.updateIpt(registrationManager.getIpt());
      }
    } catch (RegistryException e) {
      // log as specific error message as possible about why the Registry error occurred
      String msg = RegistryException.logRegistryException(e, this);
      addActionError(msg);
      log.error(msg);
    }

    List<Resource> resources = resourceManager.list();
    if (resources.isEmpty()) {
      this.addActionWarning(getText("admin.config.updateMetadata.none"));
      return SUCCESS;
    }

    // kick off publishing for all resources, unless a) the resource has exceeded the maximum number of failed
    // publications, b) the mandatory metadata has not been provided for the resource
    for (Resource resource : resources) {
      // next version number - the version of newly published eml/rtf/archive
      BigDecimal nextVersion = new BigDecimal(resource.getNextVersion().toPlainString());
      try {
        if (!resourceManager.hasMaxProcessFailures(resource)) {
          boolean isValidMetadata = emlValidator.isValid(resource, null);
          if (isValidMetadata) {
            // publish a new version of the resource - dwca gets published asynchronously
            resourceManager.publish(resource, nextVersion, this);
          } else {
            // alert user publication failed
            addActionError(getText("publishing.failed",
              new String[] {nextVersion.toPlainString(), resource.getShortname(),
                getText("manage.overview.published.missing.metadata")}));
          }
        } else {
          addActionError(getText("publishing.skipping",
            new String[] {String.valueOf(resource.getNextVersion()), resource.getTitleAndShortname()}));
        }
      } catch (PublicationException e) {
        if (PublicationException.TYPE.LOCKED == e.getType()) {
          addActionError(
            getText("manage.overview.resource.being.published", new String[] {resource.getTitleAndShortname()}));
        } else {
          // alert user publication failed
          addActionError(
            getText("publishing.failed", new String[] {nextVersion.toPlainString(), resource.getShortname(), e.getMessage()}));
          // restore the previous version since publication was unsuccessful
          resourceManager.restoreVersion(resource, nextVersion, this);
          // keep track of how many failures on auto publication have happened
          resourceManager.getProcessFailures().put(resource.getShortname(), new Date());
        }
      } catch (InvalidConfigException e) {
        // with this type of error, the version cannot be rolled back - just alert user publication failed
        String msg =
          getText("publishing.failed", new String[] {nextVersion.toPlainString(), resource.getShortname(), e.getMessage()});
        log.error(msg, e);
        addActionError(msg);
      }
    }

    // wait around for all resources to finish publishing
    // PublishingMonitor thread is running in the background completing asynchronous publishing tasks
    while (resourceManager.getProcessFutures().size() > 0) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        log.error("Thread waiting during publish all resources was interrupted", e);
      }
    }
    // only display sinlge message: that publish all finished
    clearMessages();
    addActionMessage(getText("admin.config.updateMetadata.summary"));
    return SUCCESS;
  }
}
