package org.gbif.ipt.action.manage;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.model.Extension;
import org.gbif.ipt.model.ExtensionMapping;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.User;
import org.gbif.ipt.model.User.Role;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.ExtensionManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.UserAccountManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.validation.EmlValidator;

import com.google.inject.Inject;

import java.io.IOException;
import java.util.List;

public class OverviewAction extends ManagerBaseAction {
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
  private boolean missingBasicMetadata = false;

  public String addmanager() throws Exception {
	  if (resource==null){
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

  public String delete() {
	  if (resource==null){
		  return NOT_FOUND;
	  }
    try {
      Resource res = resource;
      System.out.println("DELETING " + res);
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
	  if (resource==null){
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
	  if (resource==null){
		  return NOT_FOUND;
	  }
    // check EML
    missingMetadata = !emlValidator.isValid(resource.getEml(), null);

    return SUCCESS;
  }

  public boolean getMissingBasicMetadata() {
    return !emlValidator.isValid(resource.getEml(), "basic");
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

  public boolean isMissingMetadata() {
    return missingMetadata;
  }

  @Override
  public void prepare() throws Exception {
    super.prepare();
    if (resource!=null){
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
	}
  }

  public String publish() throws Exception {
	  if (resource==null){
		  return NOT_FOUND;
	  }
	  addActionMessage("Resource published as version "+resource.getEml().getEmlVersion()+".");
	  addActionMessage("Darwin Core Archive (re)generated.");
	  resourceManager.publish(resource);
	  return SUCCESS;
  }
  
  public String visibility() throws Exception {
	  if (resource==null){
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
      Organisation org = null;
      try {
        org = registrationManager.get(id);
        resourceManager.register(resource, org, registrationManager.getIpt());
        if (org != null) {
          addActionMessage("Registered resource with " + org.getName() + " in GBIF");
        }
      } catch (RegistryException e) {
        log.error("Cant register resource " + resource + " with organisation " + org, e);
      }

    }
    return execute();
  }

  public String unpublish() throws Exception {
	  if (resource==null){
		  return NOT_FOUND;
	  }
    try {
      resourceManager.visibilityToPrivate(resource);
      addActionMessage("Changed Publication Status to " + resource.getStatus());
    } catch (InvalidConfigException e) {
      log.error("Cant unpublish resource " + resource, e);
    }
    return execute();
  }

}
