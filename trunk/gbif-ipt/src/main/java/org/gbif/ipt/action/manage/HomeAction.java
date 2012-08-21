package org.gbif.ipt.action.manage;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;

public class HomeAction extends BaseAction {

  private List<Resource> resources = new ArrayList<Resource>();

  private ResourceManager resourceManager;
  private boolean registrationAllowed = false;

  @Inject
  public HomeAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
    ResourceManager resourceManager) {
    super(textProvider, cfg, registrationManager);
    this.resourceManager = resourceManager;
  }

  @Override
  public String execute() {
    resources = resourceManager.list(getCurrentUser());
    return SUCCESS;
  }

  public List<Resource> getResources() {
    return resources;
  }

  public boolean isRegistrationAllowed() {
    return registrationAllowed;
  }

  /**
   * method for dealing with the action for a locked resource.
   * Does nothing but the regular home plus an error message
   */
  public String locked() {
    addActionError(getText("manage.home.resource.locked"));
    return execute();
  }

}
