/**
 * 
 */
package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.model.registry.BriefOrganisation;
import org.gbif.ipt.service.admin.GBIFRegistryManager;

import com.google.inject.Inject;

import java.util.List;

/**
 * The Action responsible for all user input relating to the GBIF registry options
 * 
 * @author tim
 * @author josecuadra
 */
public class GBIFRegistryAction extends POSTAction {
  private static final long serialVersionUID = -6522969037528106704L;

  @Inject
  private GBIFRegistryManager registryManager;

  private List<BriefOrganisation> organisations;
  private String organisationKey;
  private String organisationPassword;

  public String associateOrgToIPT() {
    if (organisationKey != null && organisationPassword != null) {
      boolean validateStatus = registryManager.validateOrganisation(organisationKey, organisationPassword);
      if (validateStatus) {
        addActionError("Organisation  validated");
        return SUCCESS;
      } else {
        organisations = registryManager.listAllOrganisations();
        addActionError("The password provided does not match the organisation's password");
        return INPUT;
      }
    } else {
      organisations = registryManager.listAllOrganisations();
      if (organisationKey == null) {
        addFieldError("organisationKey", "Please select an organisation from the list");
      }
      if (organisationPassword == null) {
        addFieldError("organisationPassword", "Please fill in the password field");
      }
      addActionError("One or more fields are missing values");
      return INPUT;
    }
  }

  /**
   * @return the organisationKey
   */
  public String getOrganisationKey() {
    if (organisationKey == null || organisationKey.equals("")) {
      return null;
    } else {
      return organisationKey;
    }
  }

  /**
   * @return the organisationPassword
   */
  public String getOrganisationPassword() {
    if (organisationPassword == null || organisationPassword.equals("")) {
      return null;
    } else {
      return organisationPassword;
    }
  }

  // Getters / Setters follow
  /**
   * @return the organisations
   */
  public List<BriefOrganisation> getOrganisations() {
    return organisations;
  }

  // Proper action methods
  public String initRegistration() {
    log.debug("getting list of organisations");
    organisations = registryManager.listAllOrganisations();
    log.debug("organisations returned: " + organisations.size());
    return SUCCESS;
  }

  /**
   * @param organisationKey the organisationKey to set
   */
  public void setOrganisationKey(String organisationKey) {
    if (organisationKey == null || organisationKey.equals("")) {
      this.organisationKey = null;
    } else {
      this.organisationKey = organisationKey;
    }
  }

  /**
   * @param organisationPassword the organisationPassword to set
   */
  public void setOrganisationPassword(String organisationPassword) {
    if (organisationPassword == null || organisationPassword.equals("")) {
      this.organisationPassword = null;
    } else {
      this.organisationPassword = organisationPassword;
    }
  }

}
