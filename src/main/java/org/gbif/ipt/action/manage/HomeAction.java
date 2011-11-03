package org.gbif.ipt.action.manage;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.manage.ResourceManager;

import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.List;

public class HomeAction extends BaseAction {
  private List<Resource> resources = new ArrayList<Resource>();
  @Inject
  private ResourceManager resourceManager;
  private boolean registrationAllowed = false;

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
   * 
   * @return
   */
  public String locked() {
    addActionError(getText("manage.home.resource.locked"));
    return execute();
  }

}
