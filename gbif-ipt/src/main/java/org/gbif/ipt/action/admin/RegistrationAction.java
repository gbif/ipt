/**
 * 
 */
package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.admin.GBIFRegistryManager;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.validation.OrganisationSupport;

import com.google.inject.Inject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The Action responsible for all user input relating to the registration options
 * 
 * @author tim
 * @author josecuadra
 */
public class RegistrationAction extends POSTAction {
  private static final long serialVersionUID = -6522969037528106704L;

  private GBIFRegistryManager registryManager;
  private RegistrationManager registrationManager;
  private OrganisationSupport organisationValidation;

  private static boolean validatedBaseURL = false;

  private List<Organisation> organisations = new ArrayList<Organisation>();
  private Organisation organisation;

  /**
   * @param registryManager
   * @param organisationValidation
   * @param registrationManager
   */
  @Inject
  public RegistrationAction(GBIFRegistryManager registryManager, OrganisationSupport organisationValidation,
      RegistrationManager registrationManager) {
    this.registryManager = registryManager;
    this.organisationValidation = organisationValidation;
    this.registrationManager = registrationManager;
  }

  public Organisation getHostingOrganisation() {
    return registrationManager.getHostingOrganisation();
  }

  /**
   * @return the registered
   */
  public boolean getIsRegistered() {
    if (registrationManager.getHostingOrganisation() != null) {
      return true;
    } else {
      return false;
    }
  }

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

  /**
   * @return the validatedBaseURL
   */
  public boolean getValidatedBaseURL() {
    return validatedBaseURL;
  }

  @Override
  public void prepare() throws Exception {
    // will not be session scoping the list of organisations from the registry as this is basically a 1 time step
    super.prepare();
    log.debug("getting list of organisations");
    List<Organisation> tempOrganisations = registryManager.listAllOrganisations();
    organisations.addAll(tempOrganisations);
    Collections.sort(organisations, new Comparator<Organisation>() {
      public int compare(Organisation org1, Organisation org2) {
        if (org1 == null || org1.getName() == null) {
          return 1;
        }
        if (org2 == null || org2.getName() == null) {
          return -1;
        }
        return org1.getName().compareToIgnoreCase(org2.getName());
      }
    });
    log.debug("organisations returned: " + organisations.size());
  }

  @Override
  public String save() {
    if (registrationManager.getHostingOrganisation() == null) {
      addActionMessage("The IPT has been registered under the organisation");
      try {
        registrationManager.addHostingOrganisation(organisation);
        // add the hosting organisation to the associated list of organisations as well
        registrationManager.addAssociatedOrganisation(organisation);
        // save everything
        registrationManager.save();
        return SUCCESS;
      } catch (AlreadyExistingException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        log.error("The organisation association couldnt be saved: " + e.getMessage(), e);
        addActionError(getText("admin.organisation.saveError"));
        addActionError(e.getMessage());
        return INPUT;
      }
    }
    addActionError("This IPT is already registered against an existing organisation.");
    addActionError("To change the association, please contact GBIF's help desk");
    return SUCCESS;
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

  @Override
  public void validate() {
    if (isHttpPost()) {
      validatedBaseURL = true;
      organisationValidation.validate(this, organisation);
    }
  }

}
