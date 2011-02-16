/**
 * 
 */
package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.service.admin.RegistrationManager;

/**
 * The base of all admin actions.
 * 
 * @author
 */
public class AdminBaseAction extends BaseAction {

  RegistrationManager registrationManager;

  public AdminBaseAction(RegistrationManager registrationManager) {
    this.registrationManager = registrationManager;
  }

  /**
   * @return the registered
   */
  public boolean getRegistered() {
    if (registrationManager.getHostingOrganisation() != null) {
      return true;
    } else {
      return false;
    }
  }

}
