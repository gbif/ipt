package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.voc.DOIRegistrationAgency;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.validation.OrganisationSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.servlet.SessionScoped;
import org.apache.log4j.Logger;

/**
 * The Action responsible for all user input relating to the organisations allowed in the IPT.
 */
public class OrganisationsAction extends POSTAction {

  // logging
  private static final Logger LOG = Logger.getLogger(OrganisationsAction.class);

  /**
   * A session scoped cache of the organisations from the GBIF registry.
   */
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

    /**
     * Invalidates the session scoped cache of organisations.
     */
    public void clearCache() {
      organisations = new ArrayList<Organisation>();
    }

    public void load() throws RuntimeException {
      LOG.debug("getting list of organisations from registry");

      List<Organisation> tempOrganisations;
      tempOrganisations = registryManager.getOrganisations();

      // empty <option></option> needed by Select2 jquery library, to be able to display placeholder "Select an org.."
      Organisation o = new Organisation();
      o.setName("");
      organisations.add(o);

      organisations.addAll(tempOrganisations);
      LOG.debug("organisations returned: " + organisations.size());
    }

  }

  private static final long serialVersionUID = 7297470324204084809L;

  private ResourceManager resourceManager;
  private final OrganisationSupport organisationValidation;

  private Organisation organisation;
  private List<Organisation> linkedOrganisations;
  private final RegisteredOrganisations orgSession;

  private static final List<String> DOI_REGISTRATION_AGENCIES = ImmutableList.of(DOIRegistrationAgency.DATACITE.name(), DOIRegistrationAgency.EZID.name());

  @Inject
  public OrganisationsAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
    OrganisationSupport organisationValidation, RegisteredOrganisations orgSession, ResourceManager resourceManager) {
    super(textProvider, cfg, registrationManager);
    this.organisationValidation = organisationValidation;
    this.orgSession = orgSession;
    this.resourceManager = resourceManager;
  }

  /**
   * @return a list of DOI registration agencies that the IPT supports
   */
  public List<String> getDoiRegistrationAgencies() {
    return DOI_REGISTRATION_AGENCIES;
  }

  @Override
  public String delete() {
    try {
      Organisation removedOrganisation = registrationManager.delete(id);
      if (removedOrganisation == null) {
        return NOT_FOUND;
      }
      // force a reload of the cached organisation
      orgSession.clearCache();
      registrationManager.save();
      addActionMessage(getText("admin.organisation.deleted"));
      return SUCCESS;
    } catch (DeletionNotAllowedException e) {
      if (e.getReason().equals(DeletionNotAllowedException.Reason.RESOURCE_REGISTERED_WITH_ORGANISATION)) {
        addActionError(getText("admin.organisation.deleted.cant.resources"));
      } else if (e.getReason().equals(DeletionNotAllowedException.Reason.IPT_REGISTERED_WITH_ORGANISATION)) {
        addActionError(getText("admin.organisation.deleted.cant.ipt"));
      }
      addActionExceptionWarning(e);
    } catch (IOException e) {
      addActionError(getText("admin.organisation.cantSave") + ": " + e.getMessage());
    }
    return INPUT;
  }

  /**
   * @return the linkedOrganisations
   */
  public List<Organisation> getLinkedOrganisations() {
    return linkedOrganisations;
  }

  /**
   * @return the organisation
   */
  public Organisation getOrganisation() {
    return organisation;
  }

  /**
   * @return all non-deleted registered organisations in GBIF Registry, excluding those already associated to the IPT.
   */
  public List<Organisation> getOrganisations() {
    List<Organisation> allOrganisations = orgSession.organisations;
    for (Organisation linkedOrganisation : getLinkedOrganisations()) {
      if (allOrganisations.contains(linkedOrganisation)) {
        allOrganisations.remove(linkedOrganisation);
      }
    }
    return allOrganisations;
  }

  public String getRegistryURL() {
    return cfg.getRegistryUrl() + "/registry/";
  }

  /**
   * @return the resourceManager
   */
  public ResourceManager getResourceManager() {
    return resourceManager;
  }

  public String list() {
    return SUCCESS;
  }

  @Override
  public void prepare() {
    super.prepare();
    // load orgs from registry if not done yet
    if (!orgSession.isLoaded()) {
      try {
        orgSession.load();
      } catch (RegistryException e) {
        String msg = getText("admin.registration.error.registry");
        LOG.error(msg, e);
        addActionError(msg);
      }
    }
    linkedOrganisations = registrationManager.listAll();
    if (id != null) {
      // modify existing organisation
      organisation = registrationManager.get(id);
    }
    // if no id was submitted we wanted to create a new organisation
    if (organisation == null) {
      // reset id
      id = null;
      // create new organisation
      organisation = new Organisation();
    }
  }

  @Override
  public String save() {

    try {
      if (id == null) {
        if (registrationManager.get(organisation.getKey()) != null) {
          LOG.error("The organisation association already exists");
          addActionError(getText("admin.organisation.error.existing"));
          return INPUT;
        }
        registrationManager.addAssociatedOrganisation(organisation);
        addActionMessage(getText("admin.organisation.associated.ipt"));
      } else {
        addActionMessage(getText("admin.organisation.updated.ipt"));
      }
      registrationManager.save();
      return SUCCESS;
    } catch (IOException e) {
      LOG.error("The organisation association couldn't be saved: " + e.getMessage(), e);
      addActionError(getText("admin.organisation.error.save"));
      addActionError(e.getMessage());
      return INPUT;
    } catch (AlreadyExistingException e) {
      addActionError(getText("admin.organisation.exists", new String[] {id}));
      return INPUT;
    }

  }

  /**
   * @param linkedOrganisations the linkedOrganisations to set
   */
  public void setLinkedOrganisations(List<Organisation> linkedOrganisations) {
    this.linkedOrganisations = linkedOrganisations;
  }

  /**
   * @param organisation the organisation to set
   */
  public void setOrganisation(Organisation organisation) {
    this.organisation = organisation;
  }

  /**
   * @param resourceManager the resourceManager to set
   */
  public void setResourceManager(ResourceManager resourceManager) {
    this.resourceManager = resourceManager;
  }

  @Override
  public void validate() {
    if (isHttpPost()) {
      if (organisation.isAgencyAccountPrimary()) {
        // ensure only one DOI account is selected as primary!
        for (Organisation org: linkedOrganisations) {
          if (!organisation.getKey().equals(org.getKey()) && org.isAgencyAccountPrimary()) {
            organisation.setAgencyAccountPrimary(false);
            addFieldError("organisation.agencyAccountPrimary",
              getText("admin.organisation.doiAccount.activated.exists"));
            break;
          }
        }
        // ensure archival mode is turned ON, otherwise ensure activation of agency account fails
        if (!cfg.isArchivalMode()) {
          organisation.setAgencyAccountPrimary(false);
          addActionError(getText("admin.organisation.doiAccount.activated.failed"));
        }
      }
      // if organisation doesn't validate prevent DOI account from being selected as primary!
      if (!organisationValidation.validate(this, organisation)) {
        organisation.setAgencyAccountPrimary(false);
      }
    }
  }

}
