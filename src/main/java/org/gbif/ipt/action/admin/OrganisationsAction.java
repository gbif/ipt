package org.gbif.ipt.action.admin;

import org.gbif.api.model.common.DOI;
import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
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
import java.util.Collections;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.servlet.SessionScoped;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Action responsible for all user input relating to the organisations allowed in the IPT.
 */
public class OrganisationsAction extends POSTAction {

  // logging
  private static final Logger LOG = LogManager.getLogger(OrganisationsAction.class);

  /**
   * A session scoped cache of the organisations from the GBIF registry.
   */
  @SessionScoped
  public static class RegisteredOrganisations {

    private List<Organisation> organisations = new ArrayList<>();
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
      organisations = new ArrayList<>();
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

  private static final List<String> DOI_REGISTRATION_AGENCIES = Collections.singletonList(DOIRegistrationAgency.DATACITE.name());

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
      allOrganisations.remove(linkedOrganisation);
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

    // remove default organisation named "no organisation" from list of editable organisations
    linkedOrganisations.removeIf(entry -> entry.getKey().equals(Constants.DEFAULT_ORG_KEY));

    if (id == null) {
      //  if no id was submitted we wanted to create a new organisation
      organisation = new Organisation();
    } else {
      if (!isHttpPost()) {
        // load existing organisation from disk
        Organisation fromDisk = registrationManager.getFromDisk(id);
        organisation = new Organisation(fromDisk);
      }
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
        // update associated organisations
        registrationManager.addAssociatedOrganisation(organisation);
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
      boolean validated = true;
      if (organisation.isAgencyAccountPrimary()) {
        // ensure only one DOI account is selected as primary!
        for (Organisation org : linkedOrganisations) {
          if (!organisation.getKey().equals(org.getKey()) && org.isAgencyAccountPrimary()) {
            validated = false;
            addFieldError("organisation.agencyAccountPrimary",
              getText("admin.organisation.doiAccount.activated.exists"));
            break;
          }
        }
      }
      // if organisation doesn't validate prevent DOI account from being selected as primary!
      if (!organisationValidation.validate(this, organisation)) {
        validated = false;
      } else {
        if (organisation.isAgencyAccountPrimary()) {
          // if organisation validated, and this account has been selected as the primary DOI account,
          // make sure all existing DOIs comply with this account as its being saved
          if (isAnotherAccountInUseAlready(organisation)) {
            validated = false;
          }
        }
      }

      // be sure to deactivate account as primary DOI account in case validation failed
      if (!validated) {
        organisation.setAgencyAccountPrimary(false);
      }
    }
  }

  /**
   * Make sure all DOIs in this IPT correspond to the account being saved. Otherwise, the user could switch the
   * account type from EZID to DataCite, and render all DataCite DOIs unable to be updated.
   *
   * (Support for EZID was removed in version 2.4.0.)
   *
   * @return true if DOIs assigned using another account are found in the IPT, false otherwise
   */
  protected boolean isAnotherAccountInUseAlready(Organisation organisation) {
    // iterate through all resources, including deleted ones since they can be undeleted
    for (Resource resource : resourceManager.list()) {
      DOI doi = resource.getDoi();
      // clone organisation being saved to new organisation to compare against organisation on disk
      Organisation fromDisk = registrationManager.getFromDisk(organisation.getKey().toString());
      if (doi != null && fromDisk != null) {
        // the doi agency account must be the same
        if (organisation.getDoiRegistrationAgency() != null && fromDisk.getDoiRegistrationAgency() != null) {
          if (!organisation.getDoiRegistrationAgency().equals(fromDisk.getDoiRegistrationAgency())) {
            String msg = getText("admin.organisation.doiAccount.differentTypeInUse",
              new String[] {fromDisk.getDoiRegistrationAgency().toString().toLowerCase(), doi.toString()});
            LOG.error(msg);
            addActionError(msg);
            return true;
          }
        }
      }
    }
    return false;
  }
}
