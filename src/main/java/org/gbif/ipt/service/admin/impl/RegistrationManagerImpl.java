/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
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
package org.gbif.ipt.service.admin.impl;

import com.thoughtworks.xstream.security.AnyTypePermission;
import org.gbif.doi.service.DoiService;
import org.gbif.doi.service.datacite.RestJsonApiDataCiteService;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.DataDir;
import org.gbif.ipt.model.Ipt;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Registration;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.converter.PasswordEncrypter;
import org.gbif.ipt.model.legacy.LegacyIpt;
import org.gbif.ipt.model.legacy.LegacyOrganisation;
import org.gbif.ipt.model.legacy.LegacyRegistration;
import org.gbif.ipt.model.voc.DOIRegistrationAgency;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.BaseManager;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.DeletionNotAllowedException.Reason;
import org.gbif.ipt.service.InvalidConfigException;
import org.gbif.ipt.service.InvalidConfigException.TYPE;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.service.registry.RegistryManager;
import org.gbif.ipt.utils.FileUtils;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.thoughtworks.xstream.XStream;

@Singleton
public class RegistrationManagerImpl extends BaseManager implements RegistrationManager {

  private static final Logger LOG = LogManager.getLogger(RegistrationManagerImpl.class);

  private static final Comparator<Organisation> ORG_BY_NAME_ORD = (left, right) -> StringUtils.compare(left.getName(), right.getName());

  public static final String PERSISTENCE_FILE_V1 = "registration.xml";
  public static final String PERSISTENCE_FILE_V2 = "registration2.xml";
  private Registration registration = new Registration();
  private final XStream xstreamV1 = new XStream();
  private final XStream xstreamV2 = new XStream();
  private ResourceManager resourceManager;
  private RegistryManager registryManager;

  @Inject
  public RegistrationManagerImpl(AppConfig cfg, DataDir dataDir, ResourceManager resourceManager,
    RegistryManager registryManager, PasswordEncrypter passwordEncrypter) {
    super(cfg, dataDir);
    this.resourceManager = resourceManager;
    defineXstreamMappingV1();
    defineXstreamMappingV2(passwordEncrypter);
    this.registryManager = registryManager;
  }

  @Override
  public Organisation addAssociatedOrganisation(Organisation organisation)
    throws AlreadyExistingException, InvalidConfigException {
    if (organisation != null) {

      // ensure max 1 DOI account is activated in the IPT
      if (organisation.isAgencyAccountPrimary() && findPrimaryDoiAgencyAccount() != null && !organisation.getKey()
        .equals(findPrimaryDoiAgencyAccount().getKey())) {
        throw new InvalidConfigException(TYPE.REGISTRATION_BAD_CONFIG,
          "Multiple DOI accounts activated in registration information - only one is allowed.");
      }

      LOG.debug("Adding/updating associated organisation " + organisation.getKey() + " - " + organisation.getName());
      registration.getAssociatedOrganisations().put(organisation.getKey().toString(), organisation);
    }
    return organisation;
  }

  /**
   * Find the organisation associated to the IPT that has a DOI agency account that has been activated. This
   * organisation's DOI agency account has been chosen as the only account used to register DOIs for
   * datasets.
   *
   * @return organisation with activated DOI agency account if found, null otherwise
   */
  @Override
  public Organisation findPrimaryDoiAgencyAccount() {
    for (Organisation organisation : registration.getAssociatedOrganisations().values()) {
      if (organisation.isAgencyAccountPrimary()) {
        return organisation;
      }
    }
    return null;
  }

  @Override
  public DoiService getDoiService() throws InvalidConfigException {
    Organisation organisation = findPrimaryDoiAgencyAccount();
    if (organisation != null) {
      String username = organisation.getAgencyAccountUsername();
      String password = organisation.getAgencyAccountPassword();
      DOIRegistrationAgency agency = organisation.getDoiRegistrationAgency();

      if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password) && agency != null) {
        return new RestJsonApiDataCiteService(cfg.getDataCiteUrl(), username, password);
      } else {
        throw new InvalidConfigException(TYPE.REGISTRATION_BAD_CONFIG,
          "DOI agency account for " + organisation.getName() + " is missing information!");
      }
    } else {
      LOG.debug("No DOI agency account has been added and activated yet to this IPT.");
    }
    return null;
  }

  @Override
  public Organisation addHostingOrganisation(Organisation organisation) {
    if (organisation != null) {
      LOG.debug("Adding hosting organisation " + organisation.getKey() + " - " + organisation.getName());
      registration.setHostingOrganisation(organisation);
    }
    return organisation;
  }

  @Override
  public void addIptInstance(Ipt ipt) {
    if (ipt != null) {
      if (ipt.getCreated() == null) {
        ipt.setCreated(new Date());
      }
      registration.setIpt(ipt);
    }
  }

  /**
   * Populate ipt instance from LegacyIpt, for all fields.
   *
   * @param ipt LegacyOrganisation
   *
   * @return Ipt populated from LegacyIpt
   */
  private Ipt createIptFromLegacyIpt(LegacyIpt ipt) {
    Ipt i = null;
    if (ipt != null) {
      i = new Ipt();
      String key = (ipt.getKey() == null) ? null : ipt.getKey().toString();
      if (key != null) {
        i.setKey(key);
      }
      i.setDescription(StringUtils.trimToNull(ipt.getDescription()));
      i.setWsPassword(StringUtils.trimToNull(ipt.getWsPassword()));
      i.setName(StringUtils.trimToNull(ipt.getName()));
      i.setCreated(ipt.getCreated());
      i.setLanguage(StringUtils.trimToNull(ipt.getLanguage()));
      i.setLogoUrl(StringUtils.trimToNull(ipt.getLogoUrl()));
      i.setHomepageURL(StringUtils.trimToNull(ipt.getHomepageURL()));
      i.setOrganisationKey(StringUtils.trimToNull(ipt.getOrganisationKey().toString()));
      i.setPrimaryContactType(StringUtils.trimToNull(ipt.getPrimaryContactType()));
      i.setPrimaryContactPhone(StringUtils.trimToNull(ipt.getPrimaryContactPhone()));
      i.setPrimaryContactLastName(StringUtils.trimToNull(ipt.getPrimaryContactLastName()));
      i.setPrimaryContactFirstName(StringUtils.trimToNull(ipt.getPrimaryContactFirstName()));
      i.setPrimaryContactAddress(StringUtils.trimToNull(ipt.getPrimaryContactAddress()));
      i.setPrimaryContactEmail(StringUtils.trimToNull(ipt.getPrimaryContactEmail()));
      i.setPrimaryContactDescription(StringUtils.trimToNull(ipt.getPrimaryContactDescription()));
      i.setPrimaryContactName(StringUtils.trimToNull(ipt.getPrimaryContactName()));
    }
    return i;
  }

  /**
   * Populate Organisation instance from LegacyOrganisation for only the key, plus fields not coming from registry.
   *
   * @param organisation LegacyOrganisation
   *
   * @return Organisation populated from LegacyOrganisation
   */
  private Organisation createOrganisationFromLegacyOrganisation(LegacyOrganisation organisation) {
    Organisation o = null;
    if (organisation != null) {
      o = new Organisation();
      String key = (organisation.getKey() == null) ? null : organisation.getKey().toString();
      if (key != null) {
        o.setKey(key);
      }
      o.setName(organisation.getName());
      o.setAlias(organisation.getAlias());
      o.setCanHost(organisation.isCanHost());
      o.setPassword(organisation.getPassword());
    }
    return o;
  }

  /**
   * Define XStream used to parse former registration (registration.xml).
   */
  private void defineXstreamMappingV1() {
    xstreamV1.addPermission(AnyTypePermission.ANY);
    xstreamV1.omitField(LegacyRegistration.class, "associatedOrganisations");
    xstreamV1.alias("organisation", LegacyOrganisation.class);
    xstreamV1.alias("registry", LegacyRegistration.class);
  }

  /**
   * Define XStream used to parse encrypted registration (registration2.xml) with passwords encrypted.
   *
   * @param passwordEncrypter PasswordConverter
   */
  private void defineXstreamMappingV2(PasswordEncrypter passwordEncrypter) {
    xstreamV2.addPermission(AnyTypePermission.ANY);
    xstreamV2.omitField(Registration.class, "associatedOrganisations");
    xstreamV2.alias("organisation", Organisation.class);
    xstreamV2.alias("registry", Registration.class);
    // encrypt passwords
    xstreamV2.registerConverter(passwordEncrypter);
  }

  @Override
  public Organisation delete(String key) throws DeletionNotAllowedException {
    Organisation org = get(key);
    if (org != null) {
      for (Resource resource : resourceManager.list()) {
        // Ensure the organisation is not associated to any registered resources
        if (resource.getOrganisation() != null && resource.getOrganisation().equals(org)) {
          throw new DeletionNotAllowedException(Reason.RESOURCE_REGISTERED_WITH_ORGANISATION,
            "Resource " + resource.getShortname() + " associated with organisation");
        }
        // Ensure the organisation is not associated to any resources with registered DOIs
        else if (resource.getDoiOrganisationKey() != null && resource.getDoiOrganisationKey().equals(org.getKey())) {
          throw new DeletionNotAllowedException(Reason.RESOURCE_DOI_REGISTERED_WITH_ORGANISATION,
            "Resource " + resource.getShortname() + " has DOI associated with organisation");
        }
      }
      // Check that the organization is not the hosting organization (IPT is not registered against the organization)
      Organisation host = registration.getHostingOrganisation();
      if (host != null && host.getKey() != null && host.getKey().toString().equals(key)) {
        throw new DeletionNotAllowedException(Reason.IPT_REGISTERED_WITH_ORGANISATION,
          "The IPT instance is associated with this organisation");
      }
      registration.getAssociatedOrganisations().remove(key);
    }
    return org;
  }

  @Override
  public Organisation get(String key) {
    if (key == null) {
      return null;
    }
    return registration.getAssociatedOrganisations().get(key);
  }

  @Override
  public Organisation get(UUID key) {
    if (key == null) {
      return null;
    }
    return registration.getAssociatedOrganisations().get(key.toString());
  }

  @Override
  public Organisation getHostingOrganisation() {
    return registration.getHostingOrganisation();
  }

  @Override
  public Ipt getIpt() {
    return registration.getIpt();
  }

  @Override
  public List<Organisation> list() {
    return registration.getAssociatedOrganisations().values().stream()
        .filter(Organisation::isCanHost)
        .sorted(ORG_BY_NAME_ORD)
        .collect(Collectors.toList());
  }

  @Override
  public List<Organisation> listAll() {
    return registration.getAssociatedOrganisations().values().stream()
        .sorted(ORG_BY_NAME_ORD)
        .collect(Collectors.toList());
  }

  @Override
  public void load() throws InvalidConfigException {
    try (Reader registrationReader = FileUtils.getUtf8Reader(dataDir.configFile(PERSISTENCE_FILE_V2));
         ObjectInputStream in = xstreamV2.createObjectInputStream(registrationReader)) {
      registration.getAssociatedOrganisations().clear();

      try {
        Registration reg = (Registration) in.readObject();
        // load the organisation this IPT is registered against
        addHostingOrganisation(reg.getHostingOrganisation());
        addIptInstance(reg.getIpt());

        // load the associated organisations
        while (true) {
          try {
            Organisation org = (Organisation) in.readObject();
            addAssociatedOrganisation(org);
          } catch (EOFException e) {
            // end of file, expected exception!
            break;
          } catch (ClassNotFoundException e) {
            LOG.error(e.getMessage(), e);
          }
        }
      } catch (EOFException e) {
        // end of file, expected exception!
      } catch (AlreadyExistingException e) {
        LOG.error(e);
      }

    } catch (FileNotFoundException e) {
      LOG.warn("Registration information not existing, " + PERSISTENCE_FILE_V2
               + " file missing  (This is normal when IPT is not registered yet)");
    } catch (ClassNotFoundException e) {
      LOG.error(e.getMessage(), e);
    } catch (IOException e) {
      LOG.error(e.getMessage(), e);
      throw new InvalidConfigException(TYPE.REGISTRATION_CONFIG,
        "Couldnt read the registration information: " + e.getMessage());
    }

    // it could be organisations have changed their name or node in the Registry, so update all organisation metadata
    updateAssociatedOrganisationsMetadata();
  }

  @Override
  public Organisation getFromDisk(String key) {
    SortedMap<String, Organisation> associatedOrganisations = new TreeMap<>();
    try (Reader registrationReader = FileUtils.getUtf8Reader(dataDir.configFile(PERSISTENCE_FILE_V2));
         ObjectInputStream in = xstreamV2.createObjectInputStream(registrationReader)) {

      in.readObject(); // skip over Registration block
      // now parse the associated organisations
      while (true) {
        try {
          Organisation org = (Organisation) in.readObject();
          associatedOrganisations.put(org.getKey().toString(), org);
        } catch (EOFException e) {
          // end of file, expected exception!
          break;
        }
      }
    } catch (ClassNotFoundException e) {
      LOG.error(e.getMessage(), e);
    } catch (IOException e) {
      LOG.error(e.getMessage(), e);
      throw new InvalidConfigException(TYPE.REGISTRATION_CONFIG, "Couldnt read registration info: " + e.getMessage());
    }
    // return the organisation requested
    return associatedOrganisations.get(key);
  }

  @Override
  public void encryptRegistration() throws InvalidConfigException {
    File registrationV1 = dataDir.configFile(PERSISTENCE_FILE_V1);
    if (registrationV1.exists()) {
      try (Reader registrationReader = FileUtils.getUtf8Reader(registrationV1);
        ObjectInputStream in = xstreamV1.createObjectInputStream(registrationReader)) {
        registration.getAssociatedOrganisations().clear();

        try {
          LegacyRegistration reg = (LegacyRegistration) in.readObject();
          // load the organisation this IPT is registered against
          LegacyOrganisation legacyHostingOrganisation = reg.getHostingOrganisation();
          if (legacyHostingOrganisation != null) {
            Organisation hostingOrganisation = createOrganisationFromLegacyOrganisation(legacyHostingOrganisation);
            addHostingOrganisation(hostingOrganisation);
          }

          // load the IPT installation
          LegacyIpt legacyIpt = reg.getIpt();
          if (legacyIpt != null) {
            Ipt ipt = createIptFromLegacyIpt(legacyIpt);
            addIptInstance(ipt);
          }

          // load the associated organisations
          while (true) {
            try {
              LegacyOrganisation legacyOrganisation = (LegacyOrganisation) in.readObject();
              if (legacyOrganisation != null) {
                Organisation organisation = createOrganisationFromLegacyOrganisation(legacyOrganisation);
                addAssociatedOrganisation(organisation);
              }
            } catch (EOFException e) {
              // end of file, expected exception!
              break;
            } catch (ClassNotFoundException e) {
              LOG.error(e.getMessage(), e);
            }
          }
        } catch (EOFException e) {
          // end of file, expected exception!
        } catch (AlreadyExistingException e) {
          LOG.error(e);
        }

        // ensure changes are persisted to registration2.xml
        save();

      } catch (ClassNotFoundException e) {
        LOG.error(e.getMessage(), e);
        throw new InvalidConfigException(TYPE.REGISTRATION_CONFIG,
          "Problem reading the registration information: " + e.getMessage());
      } catch (IOException e) {
        LOG.error(e.getMessage(), e);
        throw new InvalidConfigException(TYPE.REGISTRATION_CONFIG,
          "Couldnt read the registration information: " + e.getMessage());
      } finally {
        // delete former registration configuration (registration.xml)
        org.apache.commons.io.FileUtils.deleteQuietly(registrationV1);
      }
    }
  }

  /**
   * Update the metadata of each organization that has been added to the IPT with the latest version coming from the
   * Registry.
   */
  private void updateAssociatedOrganisationsMetadata() {
    try {
      // 1. update associated organisations' metadata
      for (Map.Entry<String, Organisation> entry : registration.getAssociatedOrganisations().entrySet()) {
        Organisation o = entry.getValue();
        updateOrganisationMetadata(o);
        // replace organisation in list of associated organisations now
        registration.getAssociatedOrganisations().put(entry.getKey(), o);
      }

      // 2. update hosting organisation's metadata
      Organisation hostingOrganisation = registration.getHostingOrganisation();
      if (hostingOrganisation != null) {
        updateOrganisationMetadata(hostingOrganisation);
      }

      // ensure changes are persisted to registration2.xml
      save();
    } catch (IOException e) {
      LOG.error("A problem occurred saving ");
    }
  }

  /**
   * For a single organization, update its metadata. Only updates the metadata for an organisation coming from the
   * registry, not the metadata set by the IPT administrator like can host data, DOI configuration, etc.
   *
   * @param organisation Organisation
   */
  private void updateOrganisationMetadata(Organisation organisation) {
    if (organisation != null) {
      // the organization key
      String key = (organisation.getKey() == null) ? null : organisation.getKey().toString();

      // retrieve the latest copy of the organisation from the Registry
      Organisation o = registryManager.getRegisteredOrganisation(key);

      if (o != null) {

        String oKey = (o.getKey() == null) ? null : o.getKey().toString();
        String oName = StringUtils.trimToNull(o.getName());

        // sanity check - only the key must be exactly the same, and at least the name must not be null
        if (oKey != null && oKey.equalsIgnoreCase(key) && oName != null) {
          // organisation
          organisation.setName(oName);
          organisation.setDescription(StringUtils.trimToNull(o.getDescription()));
          organisation.setHomepageURL(StringUtils.trimToNull(o.getHomepageURL()));
          // organisation node
          organisation.setNodeKey(StringUtils.trimToNull(o.getNodeKey()));
          organisation.setNodeName(StringUtils.trimToNull(o.getNodeName()));
          organisation.setNodeContactEmail(StringUtils.trimToNull(o.getNodeContactEmail()));
          // organisation primary contact
          organisation.setPrimaryContactName(StringUtils.trimToNull(o.getPrimaryContactName()));
          organisation.setPrimaryContactFirstName(StringUtils.trimToNull(o.getPrimaryContactFirstName()));
          organisation.setPrimaryContactLastName(StringUtils.trimToNull(o.getPrimaryContactLastName()));
          organisation.setPrimaryContactAddress(StringUtils.trimToNull(o.getPrimaryContactAddress()));
          organisation.setPrimaryContactDescription(StringUtils.trimToNull(o.getPrimaryContactDescription()));
          organisation.setPrimaryContactEmail(StringUtils.trimToNull(o.getPrimaryContactEmail()));
          organisation.setPrimaryContactPhone(StringUtils.trimToNull(o.getPrimaryContactPhone()));
          organisation.setPrimaryContactType(StringUtils.trimToNull(o.getPrimaryContactType()));
          LOG.debug("Organisation (" + key + ") updated with latest metadata from Registry");
        } else {
          LOG.debug("Update of organisation failed: organisation retrieved from Registry was missing name");
        }
      } else {
        LOG.debug("Update of organisation failed: organisation retrieved from Registry was null");
      }
    } else {
      LOG.debug("Update of organisation failed: organisation was null");
    }
  }

  @Override
  public synchronized void save() throws IOException {
    LOG.debug("Saving all user organisations associated to this IPT...");
    Writer organisationWriter = FileUtils.startNewUtf8File(dataDir.configFile(PERSISTENCE_FILE_V2));
    ObjectOutputStream out = xstreamV2.createObjectOutputStream(organisationWriter, "registration");
    out.writeObject(registration);
    for (Organisation organisation : registration.getAssociatedOrganisations().values()) {
      out.writeObject(organisation);
    }
    out.close();
  }
}
