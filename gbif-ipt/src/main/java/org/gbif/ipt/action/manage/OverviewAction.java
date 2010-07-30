package org.gbif.ipt.action.manage;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.User.Role;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.service.manage.ResourceManager;

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
  private List<User> potentialManagers;
  private String action;

  @Override
  public String execute() throws Exception {
    // get potential new managers
    potentialManagers = userManager.list(Role.Manager);
    // do something?
    if (action != null) {
      if (id != null) {
        if (action.equals("delmanager") || action.equals("addmanager")) {
          User u = userManager.get(id);
          // check if user can be added or if the URL was hijacked
          if (potentialManagers.contains(u)) {
            if (action.equals("delmanager")) {
              ms.getResource().getManagers().remove(u);
              addActionMessage("Removed " + u.getName() + " from resource managers");
            } else {
              ms.getResource().addManager(u);
              addActionMessage("Added " + u.getName() + " to resource managers");
            }
            ms.saveResource();
          } else {
            addActionError("Manager " + id + " not available");
          }
        }
      }
    }
    return SUCCESS;
  }

  public ResourceManagerSession getMs() {
    return ms;
  }

  public List<User> getPotentialManagers() {
    return potentialManagers;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public void setMs(ResourceManagerSession ms) {
    this.ms = ms;
  }

}
