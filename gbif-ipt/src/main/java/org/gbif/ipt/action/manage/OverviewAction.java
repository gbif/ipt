package org.gbif.ipt.action.manage;

import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.User.Role;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.PublicationException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.task.StatusReport;
import org.gbif.ipt.validation.EmlValidator;

import com.google.inject.Inject;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class OverviewAction extends ManagerBaseAction {
  private static final int interval = 20000;
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
  private EmlValidator emlValidator = new EmlValidator();
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
      addActionError("Manager " + id + " not available");
    } else if (u != null) {
      resource.addManager(u);
      addActionMessage("Added " + u.getName() + " to resource managers");
      saveResource();
      potentialManagers.remove(u);
    }
    return execute();
  }

  public String cancel() throws Exception {
    if (resource != null) {
      try {
        resourceManager.cancelPublishing(resource.getShortname(), this);
        addActionMessage("Stopped publishing " + resource);
      } catch (Exception e) {
        String reason = "";
        if (e.getMessage() != null) {
          reason = e.getMessage();
        }
        addActionError("Failed to stop publishing resource. " + reason);
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
      addActionMessage("Deleted " + res);
      return HOME;
    } catch (IOException e) {
      log.error("Cannot delete resource", e);
      addActionError("Cannot delete resource: " + e.getMessage());
    }
    return SUCCESS;
  }

  public String delmanager() throws Exception {
    if (resource == null) {
      return NOT_FOUND;
    }
    User u = userManager.get(id);
    if (u == null || !resource.getManagers().contains(u)) {
      addActionError("Manager " + id + " not available");
    } else {
      resource.getManagers().remove(u);
      addActionMessage("Removed " + u.getName() + " from resource managers");
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

  public boolean isMissingMetadata() {
    return missingMetadata;
  }

  public String locked() throws Exception {
    report = resourceManager.status(resource.getShortname());
    now = new Date();
    if (report.isCompleted()) {
      addActionMessage("Resource published!");
      return "cancel";
    }
    return SUCCESS;
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
    // if (!resource.isPublished()) {
    // return false;
    // }
    return true;
  }

  @Override
  public void prepare() throws Exception {
    super.prepare();
    if (resource != null) {
      // get potential new managers
      potentialManagers = userManager.list(Role.Manager);
      // remove already associated ones
      for (User u : resource.getManagers()) {
        potentialManagers.remove(u);
      }
      // enabled registry organisations
      organisations = registrationManager.list();
      if (resource.getCore() == null) {
        // show core extensions for mapping
        potentialExtensions = extensionManager.listCore();
      } else {
        // show unmapped extensions
        potentialExtensions = extensionManager.list(resource.getCore().getExtension());
        // remove already associated ones
        for (ExtensionMapping e : resource.getExtensions()) {
          potentialExtensions.remove(e.getExtension());
        }
      }
      // check EML
      missingMetadata = !emlValidator.isValid(resource.getEml(), null);
      missingRegistrationMetadata = !minimumRegistryInfo(resource);
    }
  }

  public String publish() throws Exception {
    if (resource == null) {
      return NOT_FOUND;
    }
    try {
      if (resourceManager.publish(resource, this)) {
        addActionMessage("Publishing resource version " + resource.getEmlVersion() + ".");
        return PUBLISHING;
      } else {
        addActionMessage("Published resource version " + resource.getEmlVersion() + ".");
        return SUCCESS;
      }
    } catch (PublicationException e) {
      if (PublicationException.TYPE.LOCKED == e.getType()) {
        addActionError("Resource is being published already. Please be patient");
      } else {
        addActionError("Error publishing resource: " + e.getMessage());
      }
    } catch (Exception e) {
      log.error("Error publishing resource", e);
      addActionError("Error publishing resource: " + e.getMessage());
    }
    return ERROR;
  }

  public void setUnpublish(String unpublish) {
    this.unpublish = StringUtils.trimToNull(unpublish) != null;
  }

  public String visibility() throws Exception {
    if (resource == null) {
      return NOT_FOUND;
    }
    if (PublicationStatus.PRIVATE == resource.getStatus()) {
      try {
        resourceManager.visibilityToPublic(resource);
        addActionMessage("Changed Publication Status to " + resource.getStatus());
      } catch (InvalidConfigException e) {
        log.error("Cant publish resource " + resource, e);
      }

    } else if (PublicationStatus.PUBLIC == resource.getStatus()) {
      if (unpublish) {
        try {
          resourceManager.visibilityToPrivate(resource);
          addActionMessage("Changed Publication Status to " + resource.getStatus());
        } catch (InvalidConfigException e) {
          log.error("Cant unpublish resource " + resource, e);
        }
      } else {
        Organisation org = null;
        try {
          org = registrationManager.get(id);
          resourceManager.register(resource, org, registrationManager.getIpt());
          if (org != null) {
            addActionMessage("Registered resource with " + org.getName() + " in GBIF");
          }
        } catch (RegistryException e) {
          log.error("Cant register resource " + resource + " with organisation " + org, e);
          addActionError("Registration of resource failed!");
        }
      }
    } else if (PublicationStatus.REGISTERED == resource.getStatus()) {
      Organisation org = null;
      try {
        // org = registrationManager.get(resource.getOrganisation());
        resourceManager.updateRegistration(resource, resource.getOrganisation(), registrationManager.getIpt());
        addActionMessage("Updated registration of resource " + resource.getShortname() + " in GBIF");
      } catch (RegistryException e) {
        log.error("Cant update registration of resource " + resource + " with organisation " + org, e);
        addActionError("Registration update failed!");
      }

    }
    return execute();
  }

}
