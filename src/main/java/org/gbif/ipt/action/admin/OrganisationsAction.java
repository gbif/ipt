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
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.RequirePost;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.validation.OrganisationSupport;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.interceptor.parameter.StrutsParameter;
import org.apache.struts2.ActionContext;
import org.apache.struts2.ActionInvocation;
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
  // the update from the Registry was asked from the list, so it returns there
  private boolean fromList;
  // the organisation whose DOI account is the active one, if any
  private Organisation activeDoiOrganisation;
  // per linked organisation key: its resources, and why it can't be removed (absent when it can)
  private Map<String, List<Resource>> resourcesByOrganisation;
  private Map<String, String> deleteBlocked;
  private Map<String, Integer> resourceCounts;
  // token card: a new shared token for an existing organisation, saved on its own (changeToken)
  private String newToken;
  // existing organisation saved without a new token: its stored token is used and not checked again
  private boolean storedToken;

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
      } else if (e.getReason().equals(DeletionNotAllowedException.Reason.RESOURCE_DOI_REGISTERED_WITH_ORGANISATION)) {
        addActionError(getText("admin.organisation.deleted.cant.doi"));
      }
      addActionExceptionWarning(e);
    } catch (IOException e) {
      addActionError(getText("admin.organisation.cantSave") + ": " + escapeHtml(e.getMessage()));
    }
    // the organisation page is shown again: a delete post carries only the id
    Organisation fromDisk = registrationManager.getFromDisk(id);
    if (fromDisk != null) {
      organisation = new Organisation(fromDisk);
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
        activeDoiOrganisation = org;
        break;
      }
    }

    if (id == null) {
      //  if no id was submitted we wanted to create a new organisation
      organisation = new Organisation();
    } else {
      // the token change posts only the token: the page shown on a refusal needs the organisation
      if (!isHttpPost() || isTokenChange()) {
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
      addActionError(escapeHtml(e.getMessage()));
      return INPUT;
    } catch (AlreadyExistingException e) {
      addActionError(getAlertText("admin.organisation.exists", new String[]{id}));
      return INPUT;
    }
  }

  /**
   * Updates one organization's details from the Registry, then returns to the page it was asked from.
   */
  @RequirePost
  public String updateFromRegistry() {
    if (id == null || registrationManager.get(id) == null) {
      return NOT_FOUND;
    }
    try {
      if (registrationManager.updateOrganisationFromRegistry(id)) {
        Organisation updated = registrationManager.get(id);
        resourceManager.updateOrganisationNameForResources(updated);
        if (registryManager.isOrganisationDeleted(id)) {
          addActionWarning(getAlertText("admin.organisation.update.deleted", label(updated)));
        } else {
          addActionMessage(getAlertText("admin.organisation.update.done", label(updated)));
        }
      } else {
        addActionWarning(getAlertText("admin.organisation.update.notFound", label(registrationManager.get(id))));
      }
    } catch (RegistryException e) {
      LOG.error("Failed to update organisation {} from the Registry", id, e);
      addActionError(getAlertText("admin.organisation.update.unreachable", label(registrationManager.get(id))));
    } catch (IOException e) {
      LOG.error("Failed to save organisation {} after updating it from the Registry", id, e);
      addActionError(getText("admin.organisation.error.save"));
    }
    return SUCCESS;
  }

  /**
   * @return the organization's name as the pages show it: its alias, else its name, else its key
   */
  private static String label(Organisation organisation) {
    return StringUtils.firstNonBlank(organisation.getAlias(), organisation.getName(),
      String.valueOf(organisation.getKey()));
  }

  /**
   * @return where the update from the Registry returns to: the list, or the organization's page
   */
  public String getUpdateReturnUrl() {
    return fromList ? "organisations.do" : "organisation.do?id=" + id;
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
    if (isHttpPost() && isTokenChange()) {
      String token = StringUtils.trimToNull(newToken);
      if (token == null) {
        addFieldError("newToken", getText("admin.organisation.token.empty"));
      } else if (id == null) {
        addActionError(getText("admin.organisation.error.save"));
      } else if (!registryManager.validateOrganisation(id, token)) {
        addFieldError("newToken", getText("validation.organisation.password.invalid"));
      }
      return;
    }
    if (isHttpPost() && !cancel && !delete && !isMethod("updateFromRegistry")) {
      useStoredSecrets();
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
      if (!organisationValidation.validate(this, organisation, !storedToken)) {
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
   * The page never shows the stored shared token or DataCite password: for an existing organisation an empty one
   * means "keep the stored one". The DataCite password is kept only while the account fields are sent (the DOI
   * switch is on), so turning the switch off still clears the account.
   */
  private void useStoredSecrets() {
    if (id == null || organisation == null) {
      return;
    }
    Organisation fromDisk = registrationManager.getFromDisk(id);
    if (fromDisk == null) {
      return;
    }
    if (StringUtils.isBlank(organisation.getPassword())) {
      organisation.setPassword(fromDisk.getPassword());
      storedToken = true;
    }
    boolean accountSent = organisation.getDoiRegistrationAgency() != null
        || StringUtils.isNotBlank(organisation.getAgencyAccountUsername());
    if (accountSent && StringUtils.isBlank(organisation.getAgencyAccountPassword())) {
      organisation.setAgencyAccountPassword(fromDisk.getAgencyAccountPassword());
    }
  }

  /**
   * Saves a new shared token for an existing organisation, checked with the Registry in {@link #validate()}.
   */
  @RequirePost
  public String changeToken() {
    Organisation stored = registrationManager.get(id);
    if (stored == null) {
      return NOT_FOUND;
    }
    try {
      stored.setPassword(StringUtils.trimToNull(newToken));
      registrationManager.save();
      addActionMessage(getText("admin.organisation.token.changed"));
      return SUCCESS;
    } catch (IOException e) {
      LOG.error("The organisation token couldn't be saved: {}", e.getMessage(), e);
      addActionError(getText("admin.organisation.error.save"));
      return INPUT;
    }
  }

  /**
   * @return true when this request is the token change (organisationToken.do), which validates only the token
   */
  private boolean isTokenChange() {
    return isMethod("changeToken");
  }

  /**
   * @return true if this request runs the given action method
   */
  private boolean isMethod(String method) {
    ActionInvocation invocation = ActionContext.getContext() == null ? null : ActionContext.getContext().getActionInvocation();
    return invocation != null && invocation.getProxy() != null && method.equals(invocation.getProxy().getMethod());
  }

  @StrutsParameter
  public void setNewToken(String newToken) {
    this.newToken = newToken;
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

  /**
   * Fills the resources per organisation and the reasons an organisation can't be removed, with the same rules as
   * {@link RegistrationManager#delete(String, List)}: it hosts this IPT, resources belong to it, or resources have
   * DOIs registered with its account. All resources count, deleted ones too, since they can be restored.
   */
  private void loadUsage() {
    if (resourcesByOrganisation != null) {
      return;
    }
    resourcesByOrganisation = new HashMap<>();
    deleteBlocked = new HashMap<>();
    Map<String, List<String>> doiResources = new HashMap<>();
    for (Resource r : resourceManager.list()) {
      if (r.getOrganisation() != null && r.getOrganisation().getKey() != null) {
        resourcesByOrganisation.computeIfAbsent(r.getOrganisation().getKey().toString(), k -> new ArrayList<>()).add(r);
      }
      if (r.getDoi() != null && r.getDoiOrganisationKey() != null) {
        doiResources.computeIfAbsent(r.getDoiOrganisationKey().toString(), k -> new ArrayList<>()).add(r.getShortname());
      }
    }
    resourcesByOrganisation.values()
        .forEach(list -> list.sort(Comparator.comparing(r -> StringUtils.defaultIfBlank(r.getTitle(), r.getShortname()).toLowerCase())));

    Organisation host = registrationManager.getHostingOrganisation();
    String hostKey = host != null && host.getKey() != null ? host.getKey().toString() : null;
    for (Organisation org : linkedOrganisations) {
      String key = org.getKey().toString();
      if (key.equals(hostKey)) {
        deleteBlocked.put(key, getText("admin.organisation.delete.blocked.host"));
      } else if (resourcesByOrganisation.containsKey(key)) {
        deleteBlocked.put(key, getText("admin.organisation.delete.blocked.resources",
            new String[] {namesOrCount(resourcesByOrganisation.get(key).stream().map(Resource::getShortname).toList())}));
      } else if (doiResources.containsKey(key)) {
        deleteBlocked.put(key, getText("admin.organisation.delete.blocked.doi",
            new String[] {namesOrCount(doiResources.get(key))}));
      }
    }
  }

  /**
   * A few resource names, or only their number when there are many (it goes in a menu).
   */
  private static String namesOrCount(List<String> names) {
    return names.size() <= 5 ? String.join(", ", names) : String.valueOf(names.size());
  }

  /**
   * @return number of resources per linked organisation key (absent: none)
   */
  public Map<String, Integer> getResourceCounts() {
    loadUsage();
    if (resourceCounts == null) {
      resourceCounts = new HashMap<>();
      resourcesByOrganisation.forEach((k, v) -> resourceCounts.put(k, v.size()));
    }
    return resourceCounts;
  }

  /**
   * @return why each linked organisation can't be removed, by key (absent: it can)
   */
  public Map<String, String> getDeleteBlocked() {
    loadUsage();
    return deleteBlocked;
  }

  /**
   * Organisation page: the resources under this organisation, by title.
   */
  public List<Resource> getOrganisationResources() {
    if (organisation == null || organisation.getKey() == null) {
      return new ArrayList<>();
    }
    loadUsage();
    return resourcesByOrganisation.getOrDefault(organisation.getKey().toString(), new ArrayList<>());
  }

  /**
   * Organisation page: why it can't be removed, null when it can.
   */
  public String getDeleteBlockedReason() {
    if (organisation == null || organisation.getKey() == null) {
      return null;
    }
    return getDeleteBlocked().get(organisation.getKey().toString());
  }

  public String getHostingOrganisationKey() {
    Organisation host = registrationManager.getHostingOrganisation();
    return host != null && host.getKey() != null ? host.getKey().toString() : null;
  }

  public Organisation getActiveDoiOrganisation() {
    return activeDoiOrganisation;
  }

  public String getPortalUrl() {
    return cfg.getPortalUrl();
  }

  @StrutsParameter
  public void setFromList(boolean fromList) {
    this.fromList = fromList;
  }
}
