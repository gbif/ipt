package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.RegistryException.Type;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.validation.IptValidator;
import org.gbif.ipt.validation.OrganisationSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.servlet.SessionScoped;
import org.apache.log4j.Logger;

/**
 * The Action responsible for all user input relating to the registration options.
 */
public class RegistrationAction extends POSTAction {

  // logging
  private static final Logger LOG = Logger.getLogger(RegistrationAction.class);

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
      LOG.debug("getting list of organisations from registry");
      List<Organisation> tempOrganisations;
      tempOrganisations = registryManager.getOrganisations();
      organisations.clear();

      // empty <option></option> needed by Select2 jquery library, to be able to display placeholder "Select an org.."
      Organisation o = new Organisation();
      o.setName("");
      organisations.add(o);

      organisations.addAll(tempOrganisations);
      LOG.debug("organisations returned: " + organisations.size());
    }

  }

  private static final long serialVersionUID = -6522969037528106704L;
  private final RegistryManager registryManager;
  private final OrganisationSupport organisationValidation;
  private final IptValidator iptValidation;

  private boolean validatedBaseURL = false;

  private List<Organisation> organisations = new ArrayList<Organisation>();
  private Organisation organisation;
  private Ipt ipt;
  private RegisteredOrganisations orgSession;

  @Inject
  public RegistrationAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
    RegistryManager registryManager, OrganisationSupport organisationValidation, IptValidator iptValidation,
    RegisteredOrganisations orgSession) {
    super(textProvider, cfg, registrationManager);
    this.registryManager = registryManager;
    this.organisationValidation = organisationValidation;
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
  public void prepare() {
    super.prepare();
    // will not be session scoping the list of organisations from the registry as this is basically a 1 time step
    if (getRegisteredIpt() == null && !orgSession.isLoaded()) {
      try {
        orgSession.load();
      } catch (RegistryException e) {
        String msg = getText("admin.registration.error.registry");
        if (e.getType() == Type.PROXY) {
          msg = getText("admin.registration.error.proxy");
        } else if (e.getType() == Type.SITE_DOWN) {
          msg = getText("admin.registration.error.siteDown");
        } else if (e.getType() == Type.NO_INTERNET) {
          msg = getText("admin.registration.error.internetConnection");
        }
        LOG.error(msg, e);
        addActionError(msg);
      }
    }
  }

  @Override
  public String save() {
    if (getRegisteredIpt() == null) {
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
        // add error message explaining why the Registry error occurred
        String msg = RegistryException.logRegistryException(re, this);
        addActionError(msg);
        LOG.error(msg);

        // add error message that explains the consequence of the Registry error
        msg = getText("admin.registration.failed");
        addActionError(msg);
        LOG.error(msg);
        return INPUT;
      } catch (AlreadyExistingException e) {
        LOG.error(e);
      } catch (IOException e) {
        LOG.error("The organisation association couldnt be saved: " + e.getMessage(), e);
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
      addActionMessage(getText("admin.registration.success.update"));
    } catch (RegistryException e) {
      // add error message explaining why the Registry error occurred
      String msg = RegistryException.logRegistryException(e, this);
      addActionError(msg);
      LOG.error(msg);

      // add error message that explains the root cause of the Registry error
      msg = getText("admin.registration.failed.update", new String[]{e.getMessage()});
      addActionError(msg);
      LOG.error(msg);
      return INPUT;
    } catch (IOException e) {
      addActionError(e.getMessage());
      LOG.error("Exception caught", e);
      return INPUT;
    }
    return SUCCESS;
  }

  @Override
  public void validate() {
    if (isHttpPost()) {
      if (getRegisteredIpt() != null) {
        iptValidation.validateUpdate(this, getRegisteredIpt());
      } else {
        iptValidation.validate(this, ipt);
        validatedBaseURL = true;
        organisationValidation.validate(this, organisation);
      }
    }
  }
}
