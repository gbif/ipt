/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import java.io.Serial;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import jakarta.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.interceptor.parameter.StrutsParameter;
import org.apache.struts2.ServletActionContext;

/**
 * The Action responsible for all user input relating to the organisations allowed in the IPT.
 */
public class OrganisationsAction extends POSTAction {

  @Serial
  private static final long serialVersionUID = 7297470324204084809L;

  private static final Logger LOG = LogManager.getLogger(OrganisationsAction.class);

  private static final String SESSION_ORGANISATIONS_KEY = "organisations";
  private static final String SESSION_ORGANISATIONS_LAST_UPDATED_KEY = "organisations.lastUpdated";

  private RegistryManager registryManager;
  private ResourceManager resourceManager;
  private final OrganisationSupport organisationValidation;

  private Organisation organisation;
  private List<Organisation> organisations = new ArrayList<>();
  private List<Organisation> linkedOrganisations;
  private Boolean synchronise = false;
  private boolean organisationWithDoiRegistrationAgencyPresent = false;

  private static final Map<String, DOIRegistrationAgency> DOI_REGISTRATION_AGENCIES = new HashMap<>();

  static {
    DOI_REGISTRATION_AGENCIES.put(DOIRegistrationAgency.DATACITE.name(), DOIRegistrationAgency.DATACITE);
  }

  @Inject
  public OrganisationsAction(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      RegistrationManager registrationManager,
      OrganisationSupport organisationValidation,
      ResourceManager resourceManager,
      RegistryManager registryManager) {
    super(textProvider, cfg, registrationManager);
    this.organisationValidation = organisationValidation;
    this.resourceManager = resourceManager;
    this.registryManager = registryManager;
  }

  private void loadOrganisations() {
    HttpSession session = ServletActionContext.getRequest().getSession();
    List<Organisation> sessionOrganisations = (List<Organisation>) session.getAttribute(SESSION_ORGANISATIONS_KEY);

    if (sessionOrganisations == null) {
      LOG.debug("Fetching list of organisations from registry");
      try {
        organisations = registryManager.getOrganisations();
        LOG.debug("Organisations returned from the Registry: {}", organisations.size());

        // empty <option></option> needed by Select2 jquery library, to be able to display placeholder "Select an org.."
        Organisation o = new Organisation();
        o.setName("");
        organisations.add(0, o);
        session.setAttribute(SESSION_ORGANISATIONS_KEY, organisations);
        session.setAttribute(SESSION_ORGANISATIONS_LAST_UPDATED_KEY, new Date());
      } catch (RegistryException e) {
        LOG.error("Failed to load organisations", e);
        addActionError("Failed to load organisations");
        organisations = new ArrayList<>();
      }
    } else {
      organisations = sessionOrganisations;
    }
  }

  private void clearCache() {
    organisations = new ArrayList<>();
  }

  /**
   * @return a list of DOI registration agencies that the IPT supports
   */
  public Map<String, DOIRegistrationAgency> getDoiRegistrationAgencies() {
    return DOI_REGISTRATION_AGENCIES;
  }

  @Override
  public String delete() {
    try {
      List<Resource> resources = resourceManager.list();
      Organisation removedOrganisation = registrationManager.delete(id, resources);
      if (removedOrganisation == null) {
        return NOT_FOUND;
      }
      // force a reload of the cached organisation
      clearCache();
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

  public List<Organisation> getLinkedOrganisations() {
    return linkedOrganisations;
  }

  @StrutsParameter(depth = 2)
  public Organisation getOrganisation() {
    return organisation;
  }

  /**
   * @return all non-deleted registered organisations in GBIF Registry, excluding those already associated to the IPT.
   */
  public List<Organisation> getOrganisations() {
    loadOrganisations();
    List<Organisation> filteredOrganisations = new ArrayList<>(organisations);
    filteredOrganisations.removeAll(linkedOrganisations);

    return filteredOrganisations;
  }

  public String getRegistryURL() {
    return cfg.getRegistryUrl() + "/registry/";
  }

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
    try {
      loadOrganisations();
    } catch (RegistryException e) {
      String msg = getText("admin.registration.error.registry");
      LOG.error(msg, e);
      addActionError(msg);
    }
    linkedOrganisations = registrationManager.listAll();

    // remove default organisation named "no organisation" from list of editable organisations
    linkedOrganisations.removeIf(entry -> entry.getKey().equals(Constants.DEFAULT_ORG_KEY));

    for (Organisation org : linkedOrganisations) {
      if (org.isAgencyAccountPrimary()) {
        organisationWithDoiRegistrationAgencyPresent = true;
        break;
      }
    }

    if (id == null) {
      //  if no id was submitted we wanted to create a new organisation
      organisation = new Organisation();
    } else {
      if (!isHttpPost()) {
        // load existing organisation from disk
        Organisation fromDisk = registrationManager.getFromDisk(id);
        if (fromDisk != null) {
          organisation = new Organisation(fromDisk);
        } else {
          notFound = true;
        }
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
        resourceManager.updateOrganisationNameForResources(organisation);
        addActionMessage(getText("admin.organisation.associated.ipt"));
      } else {
        // update associated organisations
        registrationManager.addAssociatedOrganisation(organisation);
        resourceManager.updateOrganisationNameForResources(organisation);
        addActionMessage(getText("admin.organisation.updated.ipt"));
      }
      registrationManager.save();
      return SUCCESS;
    } catch (IOException e) {
      LOG.error("The organisation association couldn't be saved: {}", e.getMessage(), e);
      addActionError(getText("admin.organisation.error.save"));
      addActionError(e.getMessage());
      return INPUT;
    } catch (AlreadyExistingException e) {
      addActionError(getText("admin.organisation.exists", new String[]{id}));
      return INPUT;
    }
  }

  public String synchronize() {
    try {
      registrationManager.updateAssociatedOrganisationsMetadata();
      addActionMessage(getText("admin.organisations.synchronized"));
    } catch (IOException e) {
      LOG.error("Failed to synchronize organizations with the registry", e);
      addActionError(getText("admin.organisation.error.save"));
    }

    return SUCCESS;
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
    if (isHttpPost() && !cancel && !delete & !synchronise) {
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
   * <p>
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
                new String[]{fromDisk.getDoiRegistrationAgency().toString().toLowerCase(), doi.toString()});
            LOG.error(msg);
            addActionError(msg);
            return true;
          }
        }
      }
    }
    return false;
  }

  public String getPortalUrl() {
    return cfg.getPortalUrl();
  }

  public void setSynchronise(String synchronise) {
    this.synchronise = StringUtils.trimToNull(synchronise) != null;
  }

  public boolean isOrganisationWithDoiRegistrationAgencyPresent() {
    return organisationWithDoiRegistrationAgencyPresent;
  }
}
