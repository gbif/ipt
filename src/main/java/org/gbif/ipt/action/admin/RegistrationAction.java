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
import org.gbif.ipt.struts2.SimpleTextProvider;
import org.gbif.ipt.validation.IptValidator;
import org.gbif.ipt.validation.OrganisationSupport;

import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;

import jakarta.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.ServletActionContext;

import lombok.Getter;

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

  private List<Organisation> organisations = new ArrayList<>();
  @Getter
  private Map<String, String> networks = new LinkedHashMap<>();
  @Getter
  private Organisation organisation;
  @Getter
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
    } else if (sessionNetworksRaw instanceof List) {
      // Safely cast to Map<?,?> and check if it contains String objects
      Map<?, ?> networksGenericList = (Map<?, ?>) sessionNetworksRaw;
      if (!networksGenericList.isEmpty() && networksGenericList.get(0) instanceof String) {
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
        networks = registryManager.getNetworksBrief().stream()
            .collect(Collectors.toMap(KeyNamePair::getKey, KeyNamePair::getName));
        LOG.debug("Networks returned from the Registry: {}", networks.size());

        networks.put("", getText("admin.ipt.network.selection"));

        session.setAttribute(SESSION_NETWORKS_KEY, networks);
        session.setAttribute(SESSION_NETWORKS_LAST_UPDATED_KEY, new Date());
      } catch (RegistryException e) {
        LOG.error("Failed to load networks", e);
        String msg = RegistryException.logRegistryException(e, this);
        addActionWarning(getText("admin.networks.couldnt.load", new String[]{cfg.getRegistryUrl()}) + msg);
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
      msg = getText("admin.registration.failed.update", new String[]{e.getMessage()});
      addActionError(msg);
      LOG.error(msg);
      return INPUT;
    } catch (Exception e) {
      addActionError(e.getMessage());
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

  public String associateWithNetwork() {
    try {
      if (cancel) {
        return cancel();
      }
      if (StringUtils.isNotEmpty(networkKey)) {
        String networkName = networks.get(networkKey);
        registrationManager.associateWithNetwork(networkKey, networkName);

        if (applyToExistingResources) {
          List<Resource> resources = resourceManager.list();
          for (Resource resource : resources) {
            if (resource.isRegistered()) {
              registryManager.addResourceToNetwork(resource, networkKey);
            }
          }
        }

        addActionMessage(getText("admin.ipt.success.associateWithNetwork", new String[]{networkName}));
      } else {
        Network network = getNetwork();

        if (network != null) {
          String networkName = Optional.ofNullable(network.getName()).orElse("");
          String networkKey = Optional.ofNullable(network.getKey()).map(UUID::toString).orElse("");
          registrationManager.removeAssociationWithNetwork();

          if (applyToExistingResources) {
            List<Resource> resources = resourceManager.list();
            for (Resource resource : resources) {
              if (resource.isRegistered()) {
                registryManager.removeResourceFromNetwork(resource, networkKey);
              }
            }
          }

          addActionMessage(getText("admin.ipt.success.associationWithNetworkRemoved", new String[]{networkName}));
        }
      }
    } catch (Exception e) {
      addActionError(getText("admin.ipt.update.failed"));
      LOG.error("Exception caught", e);
      return INPUT;
    }
    return SUCCESS;
  }

  @Override
  public void validate() {
    if (!isHttpPost() || cancel) {
      return;
    }

    if (tokenChange) {
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

  public void setRegisteredIptPassword(String registeredIptPassword) {
    this.registeredIptPassword = registeredIptPassword;
  }

  public void setHostingOrganisationToken(String hostingOrganisationToken) {
    this.hostingOrganisationToken = hostingOrganisationToken;
  }

  public void setTokenChange(boolean tokenChange) {
    this.tokenChange = tokenChange;
  }

  public void setNetworkKey(String networkKey) {
    this.networkKey = networkKey;
  }

  public void setApplyToExistingResources(boolean applyToExistingResources) {
    this.applyToExistingResources = applyToExistingResources;
  }
}
