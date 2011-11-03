package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.service.admin.RegistrationManager;

import com.google.inject.Inject;

public class HomeAction extends BaseAction {

  private final RegistrationManager registrationManager;

  /**
   * @param registrationManager
   */
  @Inject
  public HomeAction(RegistrationManager registrationManager) {
    this.registrationManager = registrationManager;
  }

  @Override
  public String execute() {
    return SUCCESS;
  }

  /**
   * @return the isRegistered
   */
  public boolean getIsRegistered() {
    return registrationManager.getHostingOrganisation() != null;
  }

}
