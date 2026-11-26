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

import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.KeyNamePair;
import org.gbif.ipt.model.Network;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.voc.PublicationStatus;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.RegistryException;
import org.gbif.ipt.service.RegistryException.Type;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.struts2.RequirePost;
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.validation.IptValidator;
import org.gbif.ipt.validation.OrganisationSupport;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.interceptor.parameter.StrutsParameter;
import org.apache.struts2.ServletActionContext;

import lombok.Getter;
import lombok.Setter;

/**
 * The Action responsible for all user input relating to the registration options.
 */
public class RegistrationAction extends POSTAction {

  @Serial
  private static final long serialVersionUID = -6522969037528106704L;

  private static final Logger LOG = LogManager.getLogger(RegistrationAction.class);

  private static final String SESSION_ORGANISATIONS_KEY = "organisations";
  private static final String SESSION_ORGANISATIONS_LAST_UPDATED_KEY = "organisations.lastUpdated";
  private static final String SESSION_NETWORKS_KEY = "networks";
  private static final String SESSION_NETWORKS_LAST_UPDATED_KEY = "networks.lastUpdated";
  private static final int MAX_FAILED_NAMES = 10;

  private final RegistryManager registryManager;
  private final ResourceManager resourceManager;
  private final OrganisationSupport organisationValidation;
  private final IptValidator iptValidation;

  @Getter
  private String registeredIptPassword;
  @Getter
  private String hostingOrganisationToken;
  @Getter
  protected boolean tokenChange = false;
  @Getter
  private String networkKey;
  @Getter
  private boolean applyToExistingResources = false;

  private boolean validatedBaseURL = false;

  @Setter
  private List<Organisation> organisations = new ArrayList<>();
  @Getter
  private Map<String, String> networks = new LinkedHashMap<>();
  @Setter
  private Organisation organisation;
  @Setter
  private Ipt ipt;

  @Inject
  public RegistrationAction(
      SimpleTextProvider textProvider,
      AppConfig cfg,
      RegistrationManager registrationManager,
      RegistryManager registryManager,
      ResourceManager resourceManager,
      OrganisationSupport organisationValidation,
      IptValidator iptValidation
  ) {
    super(textProvider, cfg, registrationManager);
    this.registryManager = registryManager;
    this.resourceManager = resourceManager;
    this.organisationValidation = organisationValidation;
    this.iptValidation = iptValidation;
  }

  private void loadOrganisations() {
    HttpSession session = ServletActionContext.getRequest().getSession();
    Object sessionOrganisationsRaw = session.getAttribute(SESSION_ORGANISATIONS_KEY);
    boolean requestRegistry = false;

    if (sessionOrganisationsRaw == null) {
      // null session organisation cache - request registry
      requestRegistry = true;
    } else if (sessionOrganisationsRaw instanceof List<?> organisationsGenericList) {
      // Safely cast to List<?> and check if it contains Organisation objects
      if (!organisationsGenericList.isEmpty() && organisationsGenericList.get(0) instanceof Organisation) {
        // The list is already in the session and is of the correct type
        //noinspection unchecked
        organisations = (List<Organisation>) sessionOrganisationsRaw;
      } else {
        // organisation cache is empty or of wrong type - request registry
        requestRegistry = true;
      }
    } else {
      // organisation cache is of wrong type - request registry
      requestRegistry = true;
    }

    if (requestRegistry) {
      LOG.debug("Fetching organisations from registry");
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

  private void loadNetworks() {
    HttpSession session = ServletActionContext.getRequest().getSession();
    Object sessionNetworksRaw = session.getAttribute(SESSION_NETWORKS_KEY);
    boolean requestRegistry = false;

    if (sessionNetworksRaw == null) {
      // null session networks cache - request registry
      requestRegistry = true;
    } else if (sessionNetworksRaw instanceof Map<?, ?> networksGenericMap) {
      // check the cached map holds network names by key
      if (!networksGenericMap.isEmpty() && networksGenericMap.values().iterator().next() instanceof String) {
        // The map is already in the session and is of the correct type
        //noinspection unchecked
        networks = (Map<String, String>) sessionNetworksRaw;
      } else {
        // network cache is empty or of wrong type - request registry
        requestRegistry = true;
      }
    } else {
      // network cache is of wrong type - request registry
      requestRegistry = true;
    }

    if (requestRegistry) {
      LOG.debug("Fetching networks from registry");
      try {
        // sorted by name for the picker
        networks = registryManager.getNetworksBrief().stream()
            .sorted(Comparator.comparing(KeyNamePair::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
            .collect(Collectors.toMap(KeyNamePair::getKey, p -> Objects.toString(p.getName(), p.getKey()),
                (a, b) -> a, LinkedHashMap::new));
        LOG.debug("Networks returned from the Registry: {}", networks.size());

        session.setAttribute(SESSION_NETWORKS_KEY, networks);
        session.setAttribute(SESSION_NETWORKS_LAST_UPDATED_KEY, new Date());
      } catch (RegistryException e) {
        LOG.error("Failed to load networks", e);
        String msg = RegistryException.logRegistryException(e, this);
        addActionWarning(getAlertText("admin.networks.couldnt.load", cfg.getRegistryUrl()) + msg);
        networks = new HashMap<>();
      }
    }
  }

  public Organisation getHostingOrganisation() {
    return registrationManager.getHostingOrganisation();
  }

  public Network getNetwork() {
    return registrationManager.getNetwork();
  }

  /**
   * @return the organisations
   */
  public List<Organisation> getOrganisations() {
    loadOrganisations();
    return organisations;
  }

  /**
   * @return the number of resources registered with GBIF: saving the registration details updates each of them
   */
  public int getRegisteredResourcesCount() {
    return resourceManager.list(PublicationStatus.REGISTERED).size();
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
    if (getRegisteredIpt() == null) {
      loadOrganisations();
    }

    loadNetworks();
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
        LOG.error("The organisation association couldnt be saved: {}", e.getMessage(), e);
        addActionError(getText("admin.organisation.saveError"));
        addActionError(escapeHtml(e.getMessage()));
        return INPUT;
      }
    }
    addActionError(getText("admin.registration.error.alreadyRegistered1"));
    addActionError(getText("admin.registration.error.alreadyRegistered2"));
    return SUCCESS;
  }

  @RequirePost
  public String update() {
    try {
      if (cancel) {
        return cancel();
      }
      registryManager.updateIpt(getRegisteredIpt());
      updateResources(getRegisteredIpt());
      registrationManager.save();
      addActionMessage(getText("admin.registration.success.update"));
    } catch (RegistryException e) {
      // add error message explaining why the Registry error occurred
      String msg = RegistryException.logRegistryException(e, this);
      addActionError(msg);
      LOG.error(msg);

      // add error message that explains the root cause of the Registry error
      msg = getAlertText("admin.registration.failed.update", e.getMessage());
      addActionError(msg);
      LOG.error(msg);
      return INPUT;
    } catch (Exception e) {
      addActionError(escapeHtml(e.getMessage()));
      LOG.error("Exception caught", e);
      return INPUT;
    }
    return SUCCESS;
  }

  private void updateResources(Ipt ipt) {
    List<Resource> resources = resourceManager.list(PublicationStatus.REGISTERED);
    if (!resources.isEmpty()) {
      LOG.info("Next, update {} resource registrations...", resources.size());
      for (Resource resource : resources) {
        try {
          registryManager.updateResource(resource, ipt.getKey().toString());
        } catch (IllegalArgumentException e) {
          LOG.error(e.getMessage());
        }
      }
      LOG.info("Resource registrations updated successfully!");
    }
  }

  @RequirePost
  public String changeTokens() {
    try {
      if (cancel) {
        return cancel();
      }
      if (StringUtils.isNotEmpty(hostingOrganisationToken)) {
        getHostingOrganisation().setPassword(hostingOrganisationToken);
      }
      if (StringUtils.isNotEmpty(registeredIptPassword)) {
        getRegisteredIpt().setWsPassword(registeredIptPassword);
      }
      registrationManager.save();
      addActionMessage(getText("admin.ipt.success.update"));
    } catch (Exception e) {
      addActionError(getText("admin.ipt.update.failed"));
      LOG.error("Exception caught", e);
      return INPUT;
    }
    return SUCCESS;
  }

  @RequirePost
  public String associateWithNetwork() {
    if (cancel) {
      return Constants.CANCEL;
    }
    Network current = getNetwork();
    String oldKey = current == null ? null : Objects.toString(current.getKey(), null);
    String oldName = current == null ? "" : StringUtils.defaultIfBlank(current.getName(), Objects.toString(oldKey, ""));
    String newKey = StringUtils.trimToNull(networkKey);

    if (newKey == null && oldKey == null) {
      // no network before or after: nothing to change
      return SUCCESS;
    }
    if (newKey != null && !networks.containsKey(newKey)) {
      LOG.error("Network {} is not in the list of networks from the Registry", newKey);
      addActionError(getText("admin.ipt.update.failed"));
      return INPUT;
    }

    // the IPT setting first: if it can't be saved, no resource is touched
    try {
      if (newKey != null) {
        registrationManager.associateWithNetwork(newKey, networks.get(newKey));
      } else {
        registrationManager.removeAssociationWithNetwork();
      }
    } catch (IOException e) {
      LOG.error("Failed to save the default network", e);
      addActionError(getText("admin.ipt.update.failed"));
      return INPUT;
    }

    if (applyToExistingResources) {
      List<String> failed = moveRegisteredResources(oldKey, newKey);
      if (!failed.isEmpty()) {
        // a long list would swamp the page: name the first few, the log has all of them
        String names = failed.stream().limit(MAX_FAILED_NAMES).collect(Collectors.joining(", "))
            + (failed.size() > MAX_FAILED_NAMES ? ", …" : "");
        addActionWarning(getAlertText("admin.ipt.network.resourcesFailed", String.valueOf(failed.size()), names));
      }
    }

    if (newKey != null) {
      addActionMessage(getAlertText("admin.ipt.success.associateWithNetwork", networks.get(newKey)));
    } else {
      addActionMessage(getAlertText("admin.ipt.success.associationWithNetworkRemoved", oldName));
    }
    return SUCCESS;
  }

  /**
   * Moves every registered resource from the old default network to the new one. Each resource is checked against its
   * current networks first, because the Registry refuses to add a resource twice or remove one that isn't there. It is
   * added to the new network before it leaves the old one, so a failure doesn't leave it in neither. A failing resource
   * doesn't stop the others.
   *
   * @param oldKey previous default network, null if there was none
   * @param newKey new default network, null if the association was removed
   * @return short names of the resources the Registry couldn't update
   */
  private List<String> moveRegisteredResources(String oldKey, String newKey) {
    List<String> failed = new ArrayList<>();
    for (Resource resource : resourceManager.list(PublicationStatus.REGISTERED)) {
      // the Registry calls need the resource key
      if (!resource.isRegistered()) {
        continue;
      }
      try {
        Set<String> inNetworks = registryManager.getResourceNetworks(resource).stream()
            .map(n -> Objects.toString(n.getKey(), null))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        if (newKey != null && !inNetworks.contains(newKey)) {
          registryManager.addResourceToNetwork(resource, newKey);
        }
        if (oldKey != null && !oldKey.equals(newKey) && inNetworks.contains(oldKey)) {
          registryManager.removeResourceFromNetwork(resource, oldKey);
        }
      } catch (Exception e) {
        LOG.error("Failed to move resource {} from network {} to network {}", resource.getShortname(), oldKey, newKey, e);
        failed.add(resource.getShortname());
      }
    }
    return failed;
  }

  @Override
  public void validate() {
    if (!isHttpPost() || cancel) {
      return;
    }

    if (tokenChange) {
      if (StringUtils.isAllEmpty(hostingOrganisationToken, registeredIptPassword)) {
        addActionError(getText("admin.registration.tokens.empty"));
      }
      if (StringUtils.isNotEmpty(hostingOrganisationToken)) {
        organisationValidation.validateOrganisationToken(this, getHostingOrganisation().getKey(), hostingOrganisationToken);
      }
      if (StringUtils.isNotEmpty(registeredIptPassword)) {
        iptValidation.validateIptPassword(this, registeredIptPassword);
      }
    } else if (getRegisteredIpt() != null) {
      iptValidation.validateUpdate(this, getRegisteredIpt());
    } else {
      iptValidation.validate(this, ipt);
      validatedBaseURL = true;
      organisationValidation.validate(this, organisation);
    }
  }

  @StrutsParameter
  public void setRegisteredIptPassword(String registeredIptPassword) {
    this.registeredIptPassword = registeredIptPassword;
  }

  @StrutsParameter
  public void setHostingOrganisationToken(String hostingOrganisationToken) {
    this.hostingOrganisationToken = hostingOrganisationToken;
  }

  @StrutsParameter
  public void setTokenChange(boolean tokenChange) {
    this.tokenChange = tokenChange;
  }

  @StrutsParameter
  public void setNetworkKey(String networkKey) {
    this.networkKey = networkKey;
  }

  @StrutsParameter
  public void setApplyToExistingResources(boolean applyToExistingResources) {
    this.applyToExistingResources = applyToExistingResources;
  }

  @StrutsParameter(depth = 3)
  public Organisation getOrganisation() {
    return organisation;
  }

  @StrutsParameter(depth = 3)
  public Ipt getIpt() {
    return ipt;
  }

  /**
   * The registration form edits the registered IPT (registeredIpt.*). Only this admin action binds it, not every action.
   */
  @StrutsParameter(depth = 2)
  @Override
  public Ipt getRegisteredIpt() {
    return super.getRegisteredIpt();
  }
}
