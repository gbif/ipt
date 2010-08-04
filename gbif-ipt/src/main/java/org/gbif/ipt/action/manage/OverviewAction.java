package org.gbif.ipt.action.manage;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.User.Role;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.validation.EmlSupport;

import com.google.inject.Inject;

import java.util.List;

public class OverviewAction extends BaseAction {
  @Inject
  // the resource manager session is populated by the resource interceptor and kept alive for an entire manager session
  private ResourceManagerSession ms;
  @Inject
  private ResourceManager resourceManager;
  @Inject
  private UserAccountManager userManager;
  @Inject
  private RegistrationManager registrationManager;
  private List<User> potentialManagers;
  private List<Organisation> organisations;
  private EmlSupport emlValidator = new EmlSupport();
  private boolean missingMetadata = false;

  public String addmanager() throws Exception {
    User u = userManager.get(id);
    if (u != null && !potentialManagers.contains(u)) {
      addActionError("Manager " + id + " not available");
    } else if (u != null) {
      ms.getResource().addManager(u);
      addActionMessage("Added " + u.getName() + " to resource managers");
      ms.saveResource();
      potentialManagers.remove(u);
    }
    return execute();
  }

  public String delmanager() throws Exception {
    User u = userManager.get(id);
    if (u == null || !ms.getResource().getManagers().contains(u)) {
      addActionError("Manager " + id + " not available");
    } else {
      ms.getResource().getManagers().remove(u);
      addActionMessage("Removed " + u.getName() + " from resource managers");
      ms.saveResource();
      potentialManagers.add(u);
    }
    return execute();
  }

  @Override
  public String execute() throws Exception {
    // check EML
    missingMetadata = !emlValidator.isValid(ms.getEml());

    return SUCCESS;
  }

  public ResourceManagerSession getMs() {
    return ms;
  }

  public List<Organisation> getOrganisations() {
    return organisations;
  }

  public List<User> getPotentialManagers() {
    return potentialManagers;
  }

  public boolean isMissingMetadata() {
    return missingMetadata;
  }

  @Override
  public void prepare() throws Exception {
    super.prepare();
    // get potential new managers
    potentialManagers = userManager.list(Role.Manager);
    // remove already associated ones
    for (User u : ms.getResource().getManagers()) {
      potentialManagers.remove(u);
    }

    // enabled registry organisations
    organisations = registrationManager.list();
  }

  public String publish() throws Exception {
    if (PublicationStatus.PRIVATE == ms.getResource().getStatus()) {
      try {
        resourceManager.publish(ms.getResource());
        addActionMessage("Changed Publication Status to " + ms.getResource().getStatus());
      } catch (InvalidConfigException e) {
        log.error("Cant publish resource " + ms.getResource(), e);
      }

    } else if (PublicationStatus.PUBLIC == ms.getResource().getStatus()) {
      Organisation org = null;
      try {
        org = registrationManager.get(id);
        resourceManager.register(ms.getResource(), org);
        if (org != null) {
          addActionMessage("Registered resource with " + org.getName() + " in GBIF");
        }
      } catch (RegistryException e) {
        log.error("Cant register resource " + ms.getResource() + " with organisation " + org, e);
      }

    }
    return execute();
  }

  public String unpublish() throws Exception {
    try {
      resourceManager.unpublish(ms.getResource());
      addActionMessage("Changed Publication Status to " + ms.getResource().getStatus());
    } catch (InvalidConfigException e) {
      log.error("Cant unpublish resource " + ms.getResource(), e);
    }
    return execute();
  }

}
