/**
 * 
 */
package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.service.admin.GBIFRegistryManager;

import com.google.inject.Inject;

import java.util.List;

/**
 * The Action responsible for all user input relating to the registration options
 * 
 * @author tim
 * @author josecuadra
 */
public class RegistrationAction extends POSTAction {
  private static final long serialVersionUID = -6522969037528106704L;

  @Inject
  private GBIFRegistryManager registryManager;

  private List<Organisation> organisations;
  private Organisation organisation;

  /**
   * @return the organisation
   */
  public Organisation getOrganisation() {
    return organisation;
  }

  /**
   * @return the organisations
   */
  public List<Organisation> getOrganisations() {
    return organisations;
  }

  @Override
  public void prepare() throws Exception {
    super.prepare();
    log.debug("getting list of organisations");
    organisations = registryManager.listAllOrganisations();
    log.debug("organisations returned: " + organisations.size());
  }

  @Override
  public String save() {
    if (organisation.getKey() != null && organisation.getPassword() != null) {
      boolean validateStatus = registryManager.validateOrganisation(organisation.getKey().toString(),
          organisation.getPassword());
      if (validateStatus) {
        addActionMessage("This IPT instance has been associated to the organisation");
        return INPUT;
      } else {
        organisations = registryManager.listAllOrganisations();
        addActionError("The password provided does not match the organisation's password");
        return INPUT;
      }
    } else {
      organisations = registryManager.listAllOrganisations();
      if (organisation.getKey() == null) {
        addFieldError("organisationKey", "Please select an organisation from the list");
      }
      if (organisation.getPassword() == null) {
        addFieldError("organisationPassword", "Please fill in the password field");
      }
      addActionError("One or more fields are missing values");
      return INPUT;
    }
  }

  /**
   * @param organisation the organisation to set
   */
  public void setOrganisation(Organisation organisation) {
    this.organisation = organisation;
  }

  /**
   * @param organisations the organisations to set
   */
  public void setOrganisations(List<Organisation> organisations) {
    this.organisations = organisations;
  }

}
