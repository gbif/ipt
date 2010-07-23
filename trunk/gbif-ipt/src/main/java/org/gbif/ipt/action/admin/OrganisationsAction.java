/**
 * 
 */
package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.model.registration.Organisation;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.admin.GBIFRegistryManager;
import org.gbif.ipt.service.admin.OrganisationManager;

import com.google.inject.Inject;

import java.io.IOException;
import java.util.List;

/**
 * The Action responsible for all user input relating to the organisations allowed in the IPT
 * 
 * @author tim
 */
public class OrganisationsAction extends POSTAction {
  private static final long serialVersionUID = 7297470324204084809L;

  @Inject
  private GBIFRegistryManager registryManager;
  @Inject
  private OrganisationManager organisationManager;

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
    System.out.println("PRINTING SAVE() KEY- " + organisation.getKey());
    System.out.println("PRINTING SAVE() PWD- " + organisation.getPassword());

    try {
      if (organisation.getKey() != null && organisation.getPassword() != null) {
        boolean validateStatus = registryManager.validateOrganisation(organisation.getKey(), organisation.getPassword());
        if (validateStatus) {
          addActionMessage("The organisation has been associated to this IPT");
          organisationManager.add(organisation);
          organisationManager.save();
          return INPUT;
        } else {
          organisations = registryManager.listAllOrganisations();
          addActionError("The password provided does not match the organisation's password. Association has not been made.");
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
    } catch (IOException e) {
      log.error("The organisation association couldnt be saved: " + e.getMessage(), e);
      addActionError(getText("admin.organisation.saveError"));
      addActionError(e.getMessage());
      return INPUT;
    } catch (AlreadyExistingException e) {
      addActionError(getText("admin.organisation.exists", new String[]{id,}));
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
