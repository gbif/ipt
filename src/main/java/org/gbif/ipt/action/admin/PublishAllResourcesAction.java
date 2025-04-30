/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.PublicationException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.validation.DataPackageMetadataValidator;
import org.gbif.ipt.validation.EmlValidator;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Getter;
import lombok.Setter;

public class PublishAllResourcesAction extends BaseAction {

  // logging
  private static final Logger LOG = LogManager.getLogger(PublishAllResourcesAction.class);

  private static final long serialVersionUID = -2717994514136947049L;

  protected ResourceManager resourceManager;
  protected RegistryManager registryManager;
  private final EmlValidator emlValidator;
  private final DataPackageMetadataValidator dpMetadataValidator;
  @Getter
  private List<Resource> resources;
  @Setter
  private List<String> selectedResources;
  @Setter
  private List<String> excludedResources;
  @Setter
  private BulkPublicationType publishMode;

  @Inject
  public PublishAllResourcesAction(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      RegistrationManager registrationManager,
      ResourceManager resourceManager,
      RegistryManager registryManager,
      DataPackageMetadataValidator dpMetadataValidator) {
    super(textProvider, cfg, registrationManager);
    this.resourceManager = resourceManager;
    this.registryManager = registryManager;
    this.dpMetadataValidator = dpMetadataValidator;
    this.emlValidator = new EmlValidator(cfg, registrationManager, textProvider);
  }

  @Override
  public String execute() throws Exception {
    // if cancel was set to true - call cancel method
    if (cancel) {
      return cancel();
    }

    resources = resourceManager.list();

    // start with IPT registration update, provided the IPT has been registered already
    try {
      Ipt ipt = registrationManager.getIpt();
      if (ipt != null) {
        registryManager.updateIpt(ipt);
        updateResources(ipt);
      }
    } catch (RegistryException e) {
      // log as specific error message as possible about why the Registry error occurred
      String msg = RegistryException.logRegistryException(e, this);
      addActionError(msg);
      LOG.error(msg);
    }

    resourceManager.clearProcessReports();
    List<Resource> allResources = resourceManager.list();
    List<Resource> resources;
    boolean skipIfNotChanged = false;

    if (publishMode == BulkPublicationType.SELECTED) {
      resources = allResources.stream()
          .filter(res -> selectedResources.contains(res.getShortname()))
          .collect(Collectors.toList());
    } else if (publishMode == BulkPublicationType.EXCLUDED) {
      resources = allResources.stream()
          .filter(res -> !excludedResources.contains(res.getShortname()))
          .collect(Collectors.toList());
    } else if (publishMode == BulkPublicationType.CHANGED){
      resources = allResources;
      skipIfNotChanged = true;
    } else {
      resources = allResources;
    }

    if (resources.isEmpty()) {
      this.addActionWarning(getText("admin.config.updateMetadata.none"));
      return SUCCESS;
    }

    // kick off publishing for all resources, unless
    // a) the resource has exceeded the maximum number of failed publications
    // b) the mandatory metadata has not been provided for the resource
    for (Resource resource : resources) {
      // next version number - the version of newly published eml/rtf/archive
      BigDecimal nextVersion = new BigDecimal(resource.getNextVersion().toPlainString());
      try {
        if (!resourceManager.hasMaxProcessFailures(resource)) {
          boolean isValidMetadata;

          if (resource.isDataPackage()) {
            isValidMetadata = dpMetadataValidator.isValid(resource);
          } else {
            isValidMetadata = emlValidator.isValid(resource, null);
          }

          if (isValidMetadata) {
            // publish a new version of the resource - dwca gets published asynchronously
            resourceManager.publish(resource, nextVersion, this, skipIfNotChanged);
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
        LOG.error(msg, e);
        addActionError(msg);
      }
    }

    // wait around for all resources to finish publishing
    // PublishingMonitor thread is running in the background completing asynchronous publishing tasks
    while (!resourceManager.getProcessFutures().isEmpty()) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        LOG.error("Thread waiting during publish all resources was interrupted", e);
      }
    }
    // only display sinlge message: that publish all finished
    clearMessages();
    addActionMessage(getText("admin.config.updateMetadata.summary"));
    return SUCCESS;
  }

  private void updateResources(Ipt ipt) {
    List<Resource> resources = resourceManager.list(PublicationStatus.REGISTERED);
    if (!resources.isEmpty()) {
      LOG.info("Next, update {} resource registrations...", resources.size());
      for (Resource resource : resources) {
        try {
          registryManager.updateResource(resource, ipt.getKey().toString());
        } catch (IllegalArgumentException e) {
          LOG.error(e.getMessage());
        }
      }
      LOG.info("Resource registrations updated successfully!");
    }
  }

  public enum BulkPublicationType {
    ALL, SELECTED, EXCLUDED, CHANGED
  }
}
