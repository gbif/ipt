package org.gbif.ipt.action.manage;

import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.User.Role;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.PublicationException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.task.StatusReport;
import org.gbif.ipt.utils.FileUtils;
import org.gbif.ipt.validation.EmlValidator;

import com.google.inject.Inject;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OverviewAction extends ManagerBaseAction {
  private static final String PUBLISHING = "publishing";
  @Inject
  private ResourceManager resourceManager;
  @Inject
  private UserAccountManager userManager;
  @Inject
  private RegistrationManager registrationManager;
  @Inject
  private ExtensionManager extensionManager;
  private List<User> potentialManagers;
  private List<Extension> potentialExtensions;
  private List<Organisation> organisations;
  private final EmlValidator emlValidator = new EmlValidator();
  private boolean missingMetadata = false;
  private boolean missingRegistrationMetadata = false;
  private StatusReport report;
  private Date now;
  private boolean unpublish = false;

  public String addmanager() throws Exception {
    if (resource == null) {
      return NOT_FOUND;
    }
    User u = userManager.get(id);
    if (u != null && !potentialManagers.contains(u)) {
      addActionError(getText("manage.overview.manager.not.available", new String[]{id}));
    } else if (u != null) {
      resource.addManager(u);
      addActionMessage(getText("manage.overview.user.added", new String[]{u.getName()}));
      saveResource();
      potentialManagers.remove(u);
    }
    return execute();
  }

  public String cancel() throws Exception {
    if (resource != null) {
      try {
        resourceManager.cancelPublishing(resource.getShortname(), this);
        addActionMessage(getText("manage.overview.stopped.publishing", new String[]{resource.toString()}));
      } catch (Exception e) {
        String reason = "";
        if (e.getMessage() != null) {
          reason = e.getMessage();
        }
        addActionError(getText("manage.overview.failed.stop.publishing", new String[]{reason}));
        return ERROR;
      }
    } else {
      return NOT_FOUND;
    }
    return execute();
  }

  @Override
  public String delete() {
    if (resource == null) {
      return NOT_FOUND;
    }
    try {
      Resource res = resource;
      resourceManager.delete(res);
      addActionMessage(getText("manage.overview.resource.deleted", new String[]{res.toString()}));
      return HOME;
    } catch (IOException e) {
      String msg = getText("manage.resource.delete.failed");
      log.error(msg, e);
      addActionError(msg);
      addActionExceptionWarning(e);
    } catch (DeletionNotAllowedException e) {
      String msg = getText("manage.resource.delete.failed");
      log.error(msg, e);
      addActionError(msg);
      addActionExceptionWarning(e);
    }

    return SUCCESS;
  }

  public String delmanager() throws Exception {
    if (resource == null) {
      return NOT_FOUND;
    }
    User u = userManager.get(id);
    if (u == null || !resource.getManagers().contains(u)) {
      addActionError(getText("manage.overview.manager.not.available", new String[]{id}));
    } else {
      resource.getManagers().remove(u);
      addActionMessage(getText("manage.overview.user.removed", new String[]{u.getName()}));
      saveResource();
      potentialManagers.add(u);
    }
    return execute();
  }

  @Override
  public String execute() throws Exception {
    if (resource == null) {
      return NOT_FOUND;
    }
    return SUCCESS;
  }

  /**
   * Return the size of the DwC-A file.
   * 
   * @return
   */
  public String getDwcaFormattedSize() {
    return FileUtils.formatSize(resourceManager.getDwcaSize(resource), 2);
  }

  /**
   * Return the EML file size
   * 
   * @return
   */
  public String getEmlFormattedSize() {
    return FileUtils.formatSize(resourceManager.getEmlSize(resource), 2);
  }

  public boolean getMissingBasicMetadata() {
    return !emlValidator.isValid(resource.getEml(), "basic");
  }

  /**
   * @return the missingRegistrationMetadata
   */
  public boolean getMissingRegistrationMetadata() {
    return missingRegistrationMetadata;
  }

  public Date getNow() {
    return now;
  }

  public List<Organisation> getOrganisations() {
    return organisations;
  }

  public List<Extension> getPotentialExtensions() {
    return potentialExtensions;
  }

  public List<User> getPotentialManagers() {
    return potentialManagers;
  }

  public StatusReport getReport() {
    return report;
  }

  /**
   * Return the RTF file size
   * 
   * @return
   */
  public String getRtfFormattedSize() {
    return FileUtils.formatSize(resourceManager.getRtfSize(resource), 2);
  }

  public boolean isMissingMetadata() {
    return missingMetadata;
  }

  public boolean isRtfFileExisting() {
    return resourceManager.isRtfExisting(resource.getShortname());
  }

  public String locked() throws Exception {
    now = new Date();
    if (report != null && report.isCompleted()) {
      addActionMessage(getText("manage.overview.resource.published"));
      return "cancel";
    }
    return SUCCESS;
  }

  public String makePrivate() throws Exception {
    if (resource == null) {
      return NOT_FOUND;
    }
    if (PublicationStatus.PUBLIC == resource.getStatus()) {
      if (unpublish) {
        // makePrivate
        try {
          resourceManager.visibilityToPrivate(resource);
          addActionMessage(getText("manage.overview.changed.publication.status",
              new String[]{resource.getStatus().toString()}));
        } catch (InvalidConfigException e) {
          log.error("Cant unpublish resource " + resource, e);
        }
      } else {
        addActionWarning(getText("manage.overview.resource.invalid.operation", new String[]{
            resource.getShortname(), resource.getStatus().toString()}));
      }
    } else {
      addActionWarning(getText("manage.overview.resource.invalid.operation", new String[]{
          resource.getShortname(), resource.getStatus().toString()}));
    }
    return execute();
  }

  public String makePublic() throws Exception {
    if (resource == null) {
      return NOT_FOUND;
    }
    if (PublicationStatus.PRIVATE == resource.getStatus()) {
      try {
        resourceManager.visibilityToPublic(resource);
        addActionMessage(getText("manage.overview.changed.publication.status",
            new String[]{resource.getStatus().toString()}));
      } catch (InvalidConfigException e) {
        log.error("Cant publish resource " + resource, e);
      }

    } else {
      addActionWarning(getText("manage.overview.resource.invalid.operation", new String[]{
          resource.getShortname(), resource.getStatus().toString()}));
    }
    return execute();
  }

  private boolean minimumRegistryInfo(Resource resource) {
    if (resource == null) {
      return false;
    }
    if (resource.getEml() == null) {
      return false;
    }
    if (resource.getCreator() == null) {
      return false;
    }
    if (resource.getCreator().getEmail() == null) {
      return false;
    }
    if (!resource.isPublished()) {
      return false;
    }
    return true;
  }

  @Override
  public void prepare() throws Exception {
    super.prepare();
    if (resource != null) {
      // get last archive report
      report = resourceManager.status(resource.getShortname());
      // get potential new managers
      potentialManagers = userManager.list(Role.Publisher);
      potentialManagers.addAll(userManager.list(Role.Manager));
      // remove already associated ones
      for (User u : resource.getManagers()) {
        potentialManagers.remove(u);
      }
      // enabled registry organisations
      organisations = registrationManager.list();
      if (resource.hasCore()) {
        // show extensions suited for this core
        potentialExtensions = extensionManager.list(resource.getCoreRowType());
        // add core itself
        potentialExtensions.add(0, extensionManager.get(resource.getCoreRowType()));
      } else if (!resource.getSources().isEmpty()) {
        // show core extensions for mapping
        potentialExtensions = extensionManager.listCore();
      } else {
        potentialExtensions = new ArrayList<Extension>();
      }
      // check EML
      missingMetadata = !emlValidator.isValid(resource.getEml(), null);
      missingRegistrationMetadata = !minimumRegistryInfo(resource);

      // remove all DwC mappings with 0 terms mapped
      for (ExtensionMapping em : resource.getCoreMappings()) {
        if (em.getFields().size() == 0) {
          resource.deleteMapping(em);
        }
      }

    }
  }

  public String publish() throws Exception {
    if (resource == null) {
      return NOT_FOUND;
    }
    try {
      if (resourceManager.publish(resource, this)) {
        addActionMessage(getText("manage.overview.publishing.resource.version", new String[]{resource.getEmlVersion()
            + ""}));
        return PUBLISHING;
      } else {
        if (!resource.hasMappedData()) {
          addActionWarning(getText("manage.overview.data.missing"));
        } else {
          addActionWarning(getText("manage.overview.no.data.archive.generated"));
        }
        addActionMessage(getText("manage.overview.published.eml", new String[]{resource.getEmlVersion() + ""}));
        return SUCCESS;
      }
    } catch (PublicationException e) {
      if (PublicationException.TYPE.LOCKED == e.getType()) {
        addActionWarning(getText("manage.overview.resource.being.published"));
      } else {
        addActionWarning(getText("manage.overview.publishing.error"), e);
      }
    } catch (Exception e) {
      log.error("Error publishing resource", e);
      addActionWarning(getText("manage.overview.publishing.error"), e);
    }
    return ERROR;
  }

  public String registerResource() throws Exception {
    if (resource == null) {
      return NOT_FOUND;
    }
    if (PublicationStatus.PUBLIC == resource.getStatus()) {
      if (unpublish) {
        addActionWarning(getText("manage.overview.resource.invalid.operation", new String[]{
            resource.getShortname(), resource.getStatus().toString()}));

      } else {
        // plain managers are not allowed to register a resource
        if (!getCurrentUser().hasRegistrationRights()) {
          addActionError(getText("manage.resource.status.registration.forbidden"));
        } else {
          Organisation org = null;
          try {
            org = registrationManager.get(id);

            // http://code.google.com/p/gbif-providertoolkit/issues/detail?id=594
            // It is safe to test the Organisation here. A resource
            // cannot be registered
            // without an organisation being provided, and the issue
            // 594 is an example
            // how one can produce this sequence of events. A more
            // robust improvement
            // would might be to submit a state transition from the
            // form "makePublic",
            // "makePrivate" which would be more atomic.
            if (org == null) {
              return execute();
            }

            resourceManager.register(resource, org, registrationManager.getIpt());
            if (org != null) {
              addActionMessage(getText("manage.overview.resource.registered", new String[]{org.getName()}));
            }
          } catch (RegistryException e) {
            log.error("Cant register resource " + resource + " with organisation " + org, e);
            addActionError(getText("manage.overview.failed.resource.registration"));
          }
        }
      }
    } else {
      addActionWarning(getText("manage.overview.resource.invalid.operation", new String[]{
          resource.getShortname(), resource.getStatus().toString()}));
    }
    return execute();
  }

  public void setUnpublish(String unpublish) {
    this.unpublish = StringUtils.trimToNull(unpublish) != null;
  }

  public String updateRegistered() throws Exception {
    if (resource == null) {
      return NOT_FOUND;
    }
    if (PublicationStatus.REGISTERED == resource.getStatus()) {
      Organisation org = null;
      try {
        // org = registrationManager.get(resource.getOrganisation());
        resourceManager.updateRegistration(resource, registrationManager.getIpt());
        addActionMessage(getText("manage.overview.resource.update.registration", new String[]{resource.getShortname()}));
      } catch (RegistryException e) {
        log.error("Cant update registration of resource " + resource + " with organisation " + org, e);
        addActionError(getText("manage.overview.failed.resource.update"));
      }
    } else {
      addActionWarning(getText("manage.overview.resource.invalid.operation", new String[]{
          resource.getShortname(), resource.getStatus().toString()}));
    }
    return execute();
  }

}
