package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.PublicationException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.inject.Inject;
import org.apache.log4j.Logger;

public class PublishAllResourcesAction extends BaseAction {

  // logging
  private static final Logger log = Logger.getLogger(PublishAllResourcesAction.class);

  private static final long serialVersionUID = -2717994514136947049L;

  protected ResourceManager resourceManager;
  protected RegistryManager registryManager;

  @Inject
  public PublishAllResourcesAction(SimpleTextProvider textProvider, AppConfig cfg,
    RegistrationManager registrationManager, ResourceManager resourceManager, RegistryManager registryManager) {
    super(textProvider, cfg, registrationManager);
    this.resourceManager = resourceManager;
    this.registryManager = registryManager;
  }

  @Override
  public String execute() throws Exception {
    // start with IPT registration update, provided the IPT has been registered already
    try {
      if (registrationManager.getIpt() != null) {
        registryManager.updateIpt(registrationManager.getIpt());
        addActionMessage(getText("admin.registration.success.update"));
      }
    } catch (RegistryException e) {
      // log as specific error message as possible about why the Registry error occurred
      String msg = RegistryException.logRegistryException(e.getType(), this);
      addActionError(msg);
      log.error(msg);
    }

    List<Resource> resources = resourceManager.list();
    if (resources.isEmpty()) {
      this.addActionWarning(getText("admin.config.updateMetadata.none"));
      return SUCCESS;
    }

    // capture resources waiting to finish publishing
    List<String> resourcesWaiting = new ArrayList<String>();

    // kick off publishing for all resources
    for (Resource resource : resources) {
      int v = 0;
      try {
        // increment version number - this will be the version of newly published eml/rtf/archive
        v = resource.getEmlVersion() + 1;
        // publish a new version of the resource
        if (resourceManager.publish(resource, v, this)) {
          // if dwca still needs to get published (asynchronously), add resource to waiting list
          resourcesWaiting.add(resource.getShortname());
        }
      } catch (PublicationException e) {
        if (PublicationException.TYPE.LOCKED == e.getType()) {
          addActionError(getText("manage.overview.resource.being.published"));
        } else {
          // alert user publication failed
          addActionError(
            getText("publishing.failed", new String[] {String.valueOf(v), resource.getShortname(), e.getMessage()}));
          // restore the previous version since publication was unsuccessful
          resourceManager.restoreVersion(resource, v - 1, this);
        }
      }
    }

    // wait around for all resources to finish publishing
    Iterator<String> iterator = resourcesWaiting.iterator();
    while (iterator.hasNext()) {
      String shortname = iterator.next();

      // wait a second before polling to give tasks a chance to finish
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        log.error("Thread waiting during publish all resources was interrupted", e);
      }

      // method isLocked takes care of all logging and rolling version back in case of failure
      if (resourceManager.isLocked(shortname, this)) {
        log.debug("Resource " + shortname + " is still locked - will check again so to wait");
      } else {
        iterator.remove();
        log.debug("Resource " + shortname + " finished");
      }
    }
    addActionMessage(getText("admin.config.updateMetadata.summary"));
    return SUCCESS;
  }
}
