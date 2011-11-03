package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.RegistryException.TYPE;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.validation.IptValidator;
import org.gbif.ipt.validation.OrganisationSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.servlet.SessionScoped;

/**
 * The Action responsible for all user input relating to the registration options.
 */
public class RegistrationAction extends POSTAction {

  @SessionScoped
  public static class RegisteredOrganisations {

    private List<Organisation> organisations = new ArrayList<Organisation>();
    private final RegistryManager registryManager;

    @Inject
    public RegisteredOrganisations(RegistryManager registryManager) {
      this.registryManager = registryManager;
    }

    public boolean isLoaded() {
      return !organisations.isEmpty();
    }

    public void load() throws RuntimeException {
      log.debug("getting list of organisations from registry");

      List<Organisation> tempOrganisations;
      tempOrganisations = registryManager.getOrganisations();
      organisations.clear();
      organisations.addAll(tempOrganisations);
      // sort by name
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

  }

  private static final long serialVersionUID = -6522969037528106704L;
  private final RegistryManager registryManager;
  private final RegistrationManager registrationManager;
  private final OrganisationSupport organisationValidation;
  private final IptValidator iptValidation;

  private boolean validatedBaseURL = false;

  private List<Organisation> organisations = new ArrayList<Organisation>();
  private Organisation organisation;
  private String iptPassword;
  private Ipt ipt;
  private RegisteredOrganisations orgSession;

  /**
   * @param registryManager
   * @param organisationValidation
   * @param registrationManager
   */
  @Inject
  public RegistrationAction(OrganisationSupport organisationValidation, RegistrationManager registrationManager,
    RegistryManager registryManager, IptValidator iptValidation, RegisteredOrganisations orgSession) {
    this.registryManager = registryManager;
    this.organisationValidation = organisationValidation;
    this.registrationManager = registrationManager;
    this.iptValidation = iptValidation;
    this.orgSession = orgSession;
  }

  public Organisation getHostingOrganisation() {
    return registrationManager.getHostingOrganisation();
  }

  /**
   * @return the ipt
   */
  public Ipt getIpt() {
    return ipt;
  }

  /**
   * @return the registered
   */
  public boolean getIsRegistered() {
    return registrationManager.getHostingOrganisation() != null;
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
    organisations.addAll(orgSession.organisations);
    return organisations;
  }

  public Ipt getRegisteredIpt() {
    return registrationManager.getIpt();
  }

  public String getRegistryURL() {
    return cfg.getRegistryUrl() + "/registry/";
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
    if (!getIsRegistered() && !orgSession.isLoaded()) {
      try {
        orgSession.load();
      } catch (RegistryException e) {
        String msg = getText("admin.registration.error.registry");
        if (e.getType() == TYPE.PROXY) {
          msg = getText("admin.registration.error.proxy");
        } else if (e.getType() == TYPE.SITE_DOWN) {
          msg = getText("admin.registration.error.siteDown");
        } else if (e.getType() == TYPE.NO_INTERNET) {
          msg = getText("admin.registration.error.internetConnection");
        }
        log.error(msg, e);
        addActionError(msg);
      }
    }
  }

  @Override
  public String save() {
    if (registrationManager.getHostingOrganisation() == null) {
      try {
        // register against the Registry
        registryManager.registerIPT(ipt, organisation);
        registrationManager.addHostingOrganisation(organisation);
        // add the hosting organisation to the associated list of organisations as well
        registrationManager.addAssociatedOrganisation(organisation);
        // add the IPT proper info
        registrationManager.addIptInstance(ipt);
        registrationManager.save();
        addActionMessage(getText("admin.registration.success"));
        return SUCCESS;
      } catch (RegistryException re) {
        addActionError(getText("admin.registration.error.registry"));
        return INPUT;
      } catch (AlreadyExistingException e) {
        log.error(e);
      } catch (IOException e) {
        log.error("The organisation association couldnt be saved: " + e.getMessage(), e);
        addActionError(getText("admin.organisation.saveError"));
        addActionError(e.getMessage());
        return INPUT;
      }
    }
    addActionError(getText("admin.registration.error.alreadyRegistered1"));
    addActionError(getText("admin.registration.error.alreadyRegistered2"));
    return SUCCESS;
  }

  /**
   * @param ipt the ipt to set
   */
  public void setIpt(Ipt ipt) {
    this.ipt = ipt;
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

  public String update() {
    try {
      registryManager.updateIpt(getRegisteredIpt());
      registrationManager.save();
      addActionMessage("success IPT update!");
    } catch (RegistryException e) {
      addActionError(e.getMessage());
      return INPUT;
    } catch (IOException e) {
      addActionError(e.getMessage());
      log.error("Exception caught", e);
      return INPUT;
    }
    return SUCCESS;
  }

  @Override
  public void validate() {
    if (isHttpPost()) {
      if (getIsRegistered()) {
        iptValidation.validateUpdate(this, getRegisteredIpt());
      } else {
        validatedBaseURL = true;
        organisationValidation.validate(this, organisation);
      }
    }
  }
}
